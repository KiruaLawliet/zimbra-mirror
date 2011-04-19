/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2006, 2007, 2008, 2009, 2010, 2011 Zimbra, Inc.
 *
 * The contents of this file are subject to the Zimbra Public License
 * Version 1.3 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.cs.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.db.DbSearch;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Mountpoint;
import com.zimbra.cs.mailbox.Tag;
import com.zimbra.cs.service.util.ItemId;

/**
 * An interface to a tree of ANDed and ORed search constraints for the DB-backed data in a search request.
 *
 * Instead of a single SearchConstraints, DB searches can be done on a tree of SearchConstraints. Each node of the tree
 * is either an AND, OR, or a leaf node.
 *
 * @author tim
 * @author ysasaki
 */
public interface DbSearchConstraints extends Cloneable {

    /**
     * Used during query optimization: the optimizer is saying "make sure this query subtree is excluding spam and or
     * trash".
     *
     * @param excludeFolders List of spam or trash folders to be excluded
     */
    void ensureSpamTrashSetting(Mailbox mbox, List<Folder> excludeFolders) throws ServiceException;

    /**
     * Used during query optimization: returns TRUE if we have folder settings (such as "in:foo") that mean we don't
     * need to have constraints added for the implicit spam/trash setting.
     */
    boolean hasSpamTrashSetting();

    /**
     * A bit of a hack -- when we combine a "all items including trash/spam" term with another query, this lets us force
     * the "include trash/spam" part in the other query and thereby drop the 1st one.
     */
    void forceHasSpamTrashSetting();

    /**
     * AND another {@link DbSearchConstraints} into our tree: this returns the new root node (might be this, might be a
     * new top level).
     */
    DbSearchConstraints and(DbSearchConstraints other);

    /**
     * OR another {@link DbSearchConstraints} into our tree: this returns the new root node (might be this, might be a
     * new top level).
     */
    DbSearchConstraints or(DbSearchConstraints other);

    /**
     * Helper for query optimization: returns TRUE if the current set of constraints returns ZERO results.
     */
    boolean hasNoResults();

    /**
     * Used during query optimization: returns TRUE if the constraints are such that we think a DB-first plan will be
     * faster.
     */
    boolean tryDbFirst(Mailbox mbox) throws ServiceException;


    /**
     * The allowable query types are added to the constraints tree after it is generated.
     */
    void setTypes(Set<MailItem.Type> types);

    /**
     * Clone is critical for things to work correctly (exploding constraints into multiple trees if the query goes to
     * many target servers).
     */
    public Object clone();

    /**
     * Outputs the constraints tree in a format that is parsable via our QueryParser. This is used when we have to send
     * a query to a remote server.
     */
    StringBuilder toQueryString(StringBuilder out);

    /**
     * Returns the list of ANDed or ORed sub nodes, or NULL if this is a LEAF node.
     */
    List<DbSearchConstraints> getChildren();

    /**
     * Casts to {@link Leaf} if it is a {@link Leaf}, or NULL if it is not.
     */
    Leaf toLeaf();

    public static final class Leaf implements DbSearchConstraints, Cloneable {

        @Override
        public List<DbSearchConstraints> getChildren() {
            return null;
        }

        @Override
        public Leaf toLeaf() {
            return this;
        }

        /**
         * When we COMBINE the operations during query optimization, we'll need to track some state values for example
         * "no results".
         */
        public boolean noResults = false;

        // These are the main constraints

        /** optional - SPECIAL CASE -- ALL listed tags must be present. */
        public Set<Tag> tags = new HashSet<Tag>();

        /** optional - ALL listed tags must be NOT present. */
        public Set<Tag> excludeTags = new HashSet<Tag>();

        /** optional - ANY of these folders are OK. */
        public Set<Folder> folders = new HashSet<Folder>();

        /** optional - ALL listed folders not allowed. */
        public Set<Folder> excludeFolders = new HashSet<Folder>();

        /** optional - ANY of these folders are OK. */
        public Set<RemoteFolderDescriptor> remoteFolders = new HashSet<RemoteFolderDescriptor>();

        /** optional - ALL listed folders are not OK. */
        public Set<RemoteFolderDescriptor> excludeRemoteFolders = new HashSet<RemoteFolderDescriptor>();

        /** optional - must match this convId. */
        public int convId = 0;

        /** optional - ALL listed convs not allowed. */
        public Set<Integer> prohibitedConvIds = new HashSet<Integer>();

        /** optional. */
        public ItemId remoteConvId = null;

        /** optional - ALL listed convs not allowed. */
        public Set<ItemId> prohibitedRemoteConvIds = new HashSet<ItemId>();

        /** optional - ANY of these itemIDs are OK. */
        public Set<Integer> itemIds = new HashSet<Integer>();

        /** optional - ALL of these itemIDs are excluded. */
        public Set<Integer> prohibitedItemIds = new HashSet<Integer>();

        /** optional - ANY of these itemIDs are OK. */
        public Set<ItemId> remoteItemIds = new HashSet<ItemId>();

        /** optional - ALL of these itemIDs are excluded. */
        public Set<ItemId> prohibitedRemoteItemIds = new HashSet<ItemId>();

        /** optional - ANY of these indexIDs are OK. */
        public Set<Integer> indexIds = new HashSet<Integer>();

        /** optional - index_id must be present. */
        public Boolean hasIndexId = null;

        /** optional - ANY of these types are OK. */
        public Set<MailItem.Type> types = EnumSet.noneOf(MailItem.Type.class);

        /** optional - ALL of these types are excluded. */
        public Set<MailItem.Type> excludeTypes = EnumSet.noneOf(MailItem.Type.class);

        /** optional - join with mail_address on sender_id = id, and mail_address.contact_count > 0. */
        public Boolean fromContact;

        // A possible result must match *ALL* the range constraints below.  It might seem strange that we don't
        // just combine the ranges -- yes this would be easy for positive ranges (foo>5 AND foo >7 and foo<10)
        // but it quickly gets very complicated with negative ranges such as (3<foo<100 AND NOT 7<foo<20)
        public List<NumericRange> dateRanges = new ArrayList<NumericRange>(); // optional
        public List<NumericRange> calStartDateRanges = new ArrayList<NumericRange>(); // optional
        public List<NumericRange> calEndDateRanges = new ArrayList<NumericRange>(); // optional
        public List<NumericRange> modSeqRanges = new ArrayList<NumericRange>(); // optional
        public List<NumericRange> sizeRanges = new ArrayList<NumericRange>(); // optional
        public List<NumericRange> convCountRanges = new ArrayList<NumericRange>(); // optional
        public List<StringRange> subjectRanges = new ArrayList<StringRange>(); // optional
        public List<StringRange> senderRanges = new ArrayList<StringRange>(); // optional

        private Set<MailItem.Type> calcTypes() {
            if (excludeTypes.isEmpty()) {
                return types;
            }
            if (types.isEmpty()) {
                types.addAll(EnumSet.allOf(MailItem.Type.class));
                types.remove(MailItem.Type.UNKNOWN);
            }
            for (MailItem.Type type : excludeTypes) {
                types.remove(type);
            }
            excludeTypes.clear();
            return types;
        }

        /**
         * Returns TRUE if these constraints have a non-appointment type in them.
         */
        public boolean hasNonAppointmentTypes() {
            Set<MailItem.Type> types = EnumSet.copyOf(calcTypes());
            types.remove(MailItem.Type.APPOINTMENT);
            types.remove(MailItem.Type.TASK);
            return !types.isEmpty();
        }

        /**
         * Returns TRUE if these constraints have a constraint that requires a join with the Appointment table.
         */
        public boolean hasAppointmentTableConstraints() {
            Set<MailItem.Type> fullTypes = calcTypes();
            return ((!calStartDateRanges.isEmpty() || !calEndDateRanges.isEmpty()) &&
                    (fullTypes.contains(MailItem.Type.APPOINTMENT) || fullTypes.contains(MailItem.Type.TASK)));
        }

        /**
         * Returns the only folder if query is for a single folder, item type list includes
         * {@link MailItem.Type#MESSAGE}, and no other conditions are specified. Otherwise returns null.
         */
        public Folder getOnlyFolder() {
            if (folders.size() == 1 && excludeFolders.isEmpty() &&
                    types.contains(MailItem.Type.MESSAGE) && !excludeTypes.contains(MailItem.Type.MESSAGE) &&
                    tags.isEmpty() && excludeTags.isEmpty() && convId == 0 &&
                    prohibitedConvIds.isEmpty() && itemIds.isEmpty() && prohibitedItemIds.isEmpty() &&
                    indexIds.isEmpty() && dateRanges.isEmpty() && calStartDateRanges.isEmpty() &&
                    calEndDateRanges.isEmpty() && modSeqRanges.isEmpty() && sizeRanges.isEmpty() &&
                    convCountRanges.isEmpty() && subjectRanges.isEmpty() && senderRanges.isEmpty()) {
                return Iterables.getOnlyElement(folders);
            } else {
                return null;
            }
        }

        /**
         * Returns TRUE/FALSE if the conv-count constraint is set, and if the constraint is exactly 1 (ie >=1 && <=1).
         * This is a useful special case b/c if this is true then we can generate much simpler SQL than for the more
         * complicated conv-count cases. Returns NULL otherwise.
         *
         *    TRUE   "is:solo"
         *    FLASE  "-is:solo"
         *    null   everything else
         */
        public Boolean getIsSoloPart() {
            if (convCountRanges.size() != 1) {
                return null;
            }
            NumericRange range = convCountRanges.get(0);
            if (range.max == 1 && range.maxInclusive && range.min == 1 && range.minInclusive) {
                return range.bool;
            } else {
                return null;
            }
        }

        @Override
        public Leaf clone() {
            Leaf result;
            try {
                result = (Leaf) super.clone();
            } catch (CloneNotSupportedException e) { // should never happen
                return null;
            }
            result.tags = new HashSet<Tag>(tags);
            result.excludeTags = new HashSet<Tag>(excludeTags);
            result.folders = new HashSet<Folder>(folders);
            result.excludeFolders = new HashSet<Folder>(excludeFolders);
            result.remoteFolders = new HashSet<RemoteFolderDescriptor>(remoteFolders);
            result.excludeRemoteFolders = new HashSet<RemoteFolderDescriptor>(excludeRemoteFolders);
            result.convId = convId;
            result.prohibitedConvIds = new HashSet<Integer>(prohibitedConvIds);
            result.remoteConvId = remoteConvId;
            result.prohibitedRemoteConvIds = new HashSet<ItemId>(prohibitedRemoteConvIds);
            result.itemIds = new HashSet<Integer>(itemIds);
            result.prohibitedItemIds = new HashSet<Integer>(prohibitedItemIds);
            result.remoteItemIds = new HashSet<ItemId>(remoteItemIds);
            result.prohibitedRemoteItemIds = new HashSet<ItemId>(prohibitedRemoteItemIds);
            result.indexIds = new HashSet<Integer>(indexIds);
            result.types = EnumSet.copyOf(types);
            result.excludeTypes = EnumSet.copyOf(excludeTypes);
            result.dateRanges = new ArrayList<NumericRange>(dateRanges);
            result.calStartDateRanges = new ArrayList<NumericRange>(calStartDateRanges);
            result.calEndDateRanges = new ArrayList<NumericRange>(calEndDateRanges);
            result.modSeqRanges = new ArrayList<NumericRange>(modSeqRanges);
            result.sizeRanges = new ArrayList<NumericRange>(sizeRanges);
            result.convCountRanges = new ArrayList<NumericRange>(convCountRanges);
            result.subjectRanges = new ArrayList<StringRange>(subjectRanges);
            result.senderRanges = new ArrayList<StringRange>(senderRanges);
            result.fromContact = fromContact;
            return result;
        }

        /**
         * Hackhackhack -- the TagConstraints are generated during the query generating process during
         * DbMailItem.encodeConstraint, and then they are cached in this object.  This is pretty ugly
         * but works for now.  TODO cleanup.
         */
        public DbSearch.TagConstraints tagConstraints;

        @Override
        public StringBuilder toQueryString(StringBuilder out) {
            if (noResults) {
                out.append("-IS:anywhere ");
                return out;
            }
            if (!tags.isEmpty()) {
                out.append("TAG:(");
                List<String> list = new ArrayList<String>(tags.size());
                for (Tag tag : tags) {
                    list.add(tag.getName());
                }
                Joiner.on(' ').appendTo(out, list);
                out.append(") ");
            }
            if (!excludeTags.isEmpty()) {
                out.append("-TAG:(");
                List<String> list = new ArrayList<String>(excludeTags.size());
                for (Tag tag : excludeTags) {
                    list.add(tag.getName());
                }
                Joiner.on(' ').appendTo(out, list);
                out.append(") ");
            }
            for (Folder folder : folders) {
                if (folder instanceof Mountpoint) {
                    out.append("INID:").append(((Mountpoint) folder).getRemoteId()).append(' ');
                } else {
                    out.append("IN:").append(folder.getPath()).append(' ');
                }
            }
            for (Folder folder : excludeFolders) {
                if (folder instanceof Mountpoint) {
                    out.append("-INID:").append(((Mountpoint) folder).getRemoteId()).append(' ');
                } else {
                    out.append("-ID:").append(folder.getPath()).append(' ');
                }
            }
            for (RemoteFolderDescriptor folder : remoteFolders) {
                out.append(folder.includeSubfolders ? "UNDERID:\"" : "INID:\"");
                out.append(folder.getFolderId());
                if (!Strings.isNullOrEmpty(folder.getSubfolderPath())) {
                    out.append('/').append(folder.getSubfolderPath());
                }
                out.append("\" ");
            }
            for (RemoteFolderDescriptor folder : excludeRemoteFolders) {
                out.append(folder.includeSubfolders ? "UNDERID:\"" : "INID:\"");
                out.append(folder.getFolderId());
                if (!Strings.isNullOrEmpty(folder.getSubfolderPath())) {
                    out.append('/').append(folder.getSubfolderPath());
                }
                out.append("\" ");
            }
            if (convId != 0) {
                out.append("CONV:").append(convId).append(' ');
            }
            if (!prohibitedConvIds.isEmpty()) {
                out.append("-CONV:(");
                Joiner.on(' ').appendTo(out, prohibitedConvIds);
                out.append(") ");
            }
            if (remoteConvId != null) {
                out.append("CONV:").append(remoteConvId).append(' ');
            }
            if (!prohibitedRemoteConvIds.isEmpty()) {
                out.append("-CONV:(");
                Joiner.on(' ').appendTo(out, prohibitedRemoteConvIds);
                out.append(") ");
            }
            if (!itemIds.isEmpty()) {
                out.append("ITEM:(");
                Joiner.on(' ').appendTo(out, itemIds);
                out.append(") ");
            }
            if (!prohibitedItemIds.isEmpty()) {
                out.append("-ITEM:(");
                Joiner.on(' ').appendTo(out, prohibitedItemIds);
                out.append(") ");
            }
            for (ItemId id : remoteItemIds) {
                out.append("ITEM:\"").append(id).append("\" ");
            }
            for (ItemId id : prohibitedRemoteItemIds) {
                out.append("-ITEM:\"").append(id).append("\" ");
            }
            if (!indexIds.isEmpty()) { // not in the query language
                out.append("INDEXID:(");
                Joiner.on(' ').appendTo(out, indexIds);
                out.append(") ");
            }
            if (hasIndexId != null) { // not in the query language
                out.append(hasIndexId ? "HAS_INDEXID" : "-HAS_INDEXID");
            }
            if (!types.isEmpty()) { // not in the query language
                out.append("ITEM_TYPE:(");
                Joiner.on(' ').appendTo(out, types);
                out.append(") ");
            }
            if (!excludeTypes.isEmpty()) { // not in the query language
                out.append("-ITEM_TYPE:(");
                Joiner.on(' ').appendTo(out, excludeTypes);
                out.append(") ");
            }
            for (NumericRange range : dateRanges) {
                range.toQueryString("DATE", out).append(' ');
            }
            for (NumericRange range : calStartDateRanges) {
                range.toQueryString("APPT-START", out).append(' ');
            }
            for (NumericRange range : calEndDateRanges) {
                range.toQueryString("APPT-END", out).append(' ');
            }
            for (NumericRange range : modSeqRanges) {
                range.toQueryString("MODSEQ", out).append(' ');
            }
            for (NumericRange range : sizeRanges) {
                range.toQueryString("SIZE", out).append(' ');
            }
            for (NumericRange range : convCountRanges) {
                range.toQueryString("CONV-COUNT", out).append(' ');
            }
            for (StringRange range : subjectRanges) {
                range.toQueryString("SUBJECT", out).append(' ');
            }
            for (StringRange range : senderRanges) {
                range.toQueryString("FROM", out).append(' ');
            }
            if (fromContact != null) {
                out.append(fromContact ? "IS:fromcontact" : "-IS:fromcontact");
            }
            return out;
        }

        /**
         * this = this AND other
         */
        public void and(Leaf other) {
            if (noResults || other.noResults) {
                noResults = true;
                return;
            }
            tags.addAll(other.tags);
            excludeTags.addAll(other.excludeTags);

            // bug 2426
            if (!Collections.disjoint(tags, excludeTags)) {
                noResults = true;
                return;
            }

            // these we have to intersect:
            //  Folder{A or B or C} AND Folder{B or C or D} --> Folder{IN-BOTH}
            //  if both sets are empty going in, then an empty set means "no constraint"....on the other hand if either set
            //  is nonempty going in, then an empty set coming out means "no results".
            if (!other.folders.isEmpty()) {
                if (folders.isEmpty()) {
                    folders.addAll(other.folders);
                } else {
                    folders.retainAll(other.folders);
                    if (folders.isEmpty()) {
                        noResults = true;
                    }
                }
            }

            excludeFolders.addAll(other.excludeFolders);

            // include-remote-folders searches cannot be properly intersected,
            // so don't try to intersect the sets right now -- just union (bug
            remoteFolders.addAll(other.remoteFolders);
            excludeRemoteFolders.addAll(other.excludeRemoteFolders);

            if (other.convId != 0) {
                if (convId != 0) {
                    if (convId != other.convId) {
                        ZimbraLog.index.debug("ANDING a constraint with incompatible convIds, this is a NO_RESULTS constraint now");
                        noResults = true;
                    }
                } else {
                    convId = other.convId;
                }
            }
            prohibitedConvIds.addAll(other.prohibitedConvIds);

            if (other.remoteConvId != null) {
                if (remoteConvId != null) {
                    if (!remoteConvId.equals(other.remoteConvId)) {
                        ZimbraLog.index.debug("ANDING a constraint with incompatible remoteConvIds, this is a NO_RESULTS constraint now");
                        noResults = true;
                    }
                } else {
                    remoteConvId = other.remoteConvId;
                }
            }
            prohibitedRemoteConvIds.addAll(other.prohibitedRemoteConvIds);

            // these we have to intersect:
            //  Item{A or B or C} AND Item{B or C or D} --> Item{IN-BOTH}
            if (!other.itemIds.isEmpty()) {
                if (itemIds.isEmpty()) {
                    itemIds.addAll(other.itemIds);
                } else {
                    itemIds.retainAll(other.itemIds);
                    if (itemIds.isEmpty()) {
                        noResults = true;
                    }
                }
            }

            // these we can just union, since:
            // -Item{A or B} AND -Item{C or D} -->
            //   (-Item(A) AND -Item(B)) AND (-C AND -D) -->
            //     (A AND B AND C AND D)
            prohibitedItemIds.addAll(other.prohibitedItemIds);

            // these we have to intersect:
            //   Item{A or B or C} AND Item{B or C or D} --> Item{IN-BOTH}
            if (!other.remoteItemIds.isEmpty()) {
                if (remoteItemIds.isEmpty()) {
                    remoteItemIds.addAll(other.remoteItemIds);
                } else {
                    remoteItemIds.retainAll(other.remoteItemIds);
                    if (remoteItemIds.isEmpty()) {
                        noResults = true;
                    }
                }
            }

            // these we can just union, since:
            // -Item{A or B} AND -Item{C or D} -->
            //   (-Item(A) AND -Item(B)) AND (-C AND -D) -->
            //     (A AND B AND C AND D)
            prohibitedRemoteItemIds.addAll(other.prohibitedRemoteItemIds);

            // IndexId{A or B or C} AND IndexId{B or C or D} --> IndexId{IN-BOTH}
            if (!other.indexIds.isEmpty()) {
                if (indexIds.isEmpty()) {
                    indexIds.addAll(other.indexIds);
                } else {
                    indexIds.retainAll(other.indexIds);
                    if (indexIds.isEmpty()) {
                        noResults = true;
                    }
                }
            }

            if (hasIndexId == null) {
                hasIndexId = other.hasIndexId;
            } else if (other.hasIndexId != null) {
                if (!hasIndexId.equals(other.hasIndexId)) {
                    noResults = true;
                    ZimbraLog.index.debug("Adding a HAS_NO_INDEXIDS constraint to a HAS_INDEXIDS one, this is a NO_RESULTS result");
                    return;
                }
            }

            if (!other.types.isEmpty()) {
                if (types.isEmpty()) {
                    types.addAll(other.types);
                } else {
                    types.retainAll(other.types);
                    if (types.isEmpty()) {
                        noResults = true;
                    }
                }
            }

            excludeTypes.addAll(other.excludeTypes);
            dateRanges.addAll(other.dateRanges);
            calStartDateRanges.addAll(other.calStartDateRanges);
            calEndDateRanges.addAll(other.calEndDateRanges);
            modSeqRanges.addAll(other.modSeqRanges);
            sizeRanges.addAll(other.sizeRanges);
            convCountRanges.addAll(other.convCountRanges);
            subjectRanges.addAll(other.subjectRanges);
            senderRanges.addAll(other.senderRanges);

            if (other.fromContact != null) {
                if (fromContact != null) {
                    if (fromContact != other.fromContact) {
                        noResults = true;
                    }
                } else {
                    fromContact = other.fromContact;
                }
            }
        }

        public void validate() {
            validate(dateRanges);
            validate(calStartDateRanges);
            validate(calEndDateRanges);
            validate(modSeqRanges);
            validate(convCountRanges);
        }

        void validate(List<NumericRange> ranges) {
            for (Iterator<NumericRange> itr = ranges.iterator(); itr.hasNext(); ) {
                NumericRange range = itr.next();
                if (!range.isValid()) {
                    itr.remove();
                }
            }
        }

        /**
         * Set by the forceHasSpamTrashSetting() API
         *
         * True if we have a SETTING pertaining to Spam/Trash.  This doesn't
         * necessarily mean we actually have "in trash" or something, it just
         * means that we've got something set which means we shouldn't add
         * the default "not in:trash and not in:junk" thing.
         */
        protected boolean mHasSpamTrashSetting = false;

        @Override
        public void ensureSpamTrashSetting(Mailbox mbox, List<Folder> trashSpamFolders) {
            if (!mHasSpamTrashSetting) {
                for (Folder f : trashSpamFolders) {
                    excludeFolders.add(f);
                }
                mHasSpamTrashSetting = true;
            }
        }

        @Override
        public DbSearchConstraints and(DbSearchConstraints other) {
            if (other instanceof Intersection) {
                return other.and(this);
            } else if (other instanceof Union) {
                return other.and(this);
            } else {
                if (other.hasSpamTrashSetting()) {
                    forceHasSpamTrashSetting();
                }
                if (other.hasNoResults()) {
                    noResults = true;
                }
                and((Leaf) other);
                return this;
            }
        }

        @Override
        public DbSearchConstraints or(DbSearchConstraints other) {
            if (other instanceof Union) {
                return other.or(this);
            } else {
                DbSearchConstraints top = new Union();
                return top.or(this).or(other);
            }
        }

        @Override
        public boolean hasSpamTrashSetting() {
            return mHasSpamTrashSetting;
        }

        @Override
        public void forceHasSpamTrashSetting() {
            mHasSpamTrashSetting = true;
        }

        @Override
        public boolean hasNoResults() {
            return noResults;
        }

        @Override
        public boolean tryDbFirst(Mailbox mbox) throws ServiceException {
            return (convId != 0 || (tags != null && tags.contains(mbox.getFlagById(Flag.ID_UNREAD))));
        }

        @Override
        public void setTypes(Set<MailItem.Type> types) {
            this.types = types;
            if (types.isEmpty()) {
                noResults = true;
            }
        }

        void addItemIdClause(Integer itemId, boolean truth) {
            if (truth) {
                if (!itemIds.contains(itemId)) {
                    //
                    // {1} AND {-1} AND {1} == no-results
                    //  ... NOT {1} (which could happen if you removed from both arrays on combining!)
                    if (prohibitedItemIds== null || !prohibitedItemIds.contains(itemId)) {
                        itemIds.add(itemId);
                    }
                }
            } else {
                if (!prohibitedItemIds.contains(itemId)) {
                    //
                    // {1} AND {-1} AND {1} == no-results
                    //  ... NOT {1} (which could happen if you removed from both arrays on combining!)
                    if (itemIds != null && itemIds.contains(itemId)) {
                        itemIds.remove(itemId);
                    }
                    prohibitedItemIds.add(itemId);
                }
            }
        }

        void addRemoteItemIdClause(ItemId itemId, boolean truth) {
            if (truth) {
                if (!remoteItemIds.contains(itemId)) {
                    //
                    // {1} AND {-1} AND {1} == no-results
                    //  ... NOT {1} (which could happen if you removed from both arrays on combining!)
                    if (prohibitedRemoteItemIds== null || !prohibitedRemoteItemIds.contains(itemId)) {
                        remoteItemIds.add(itemId);
                    }
                }
            } else {
                if (!prohibitedRemoteItemIds.contains(itemId)) {
                    //
                    // {1} AND {-1} AND {1} == no-results
                    //  ... NOT {1} (which could happen if you removed from both arrays on combining!)
                    if (remoteItemIds != null && remoteItemIds.contains(itemId)) {
                        remoteItemIds.remove(itemId);
                    }
                    prohibitedRemoteItemIds.add(itemId);
                }
            }
        }

        void addDateRange(long min, boolean minInclusive, long max, boolean maxInclusive, boolean bool) {
            dateRanges.add(new NumericRange(min, minInclusive, max, maxInclusive, bool));
        }

        void addCalStartDateRange(long min, boolean minInclusive, long max, boolean maxInclusive, boolean bool) {
            calStartDateRanges.add(new NumericRange(min, minInclusive, max, maxInclusive, bool));
        }

        void addCalEndDateRange(long min, boolean minInclusive, long max, boolean maxInclusive, boolean bool) {
            calEndDateRanges.add(new NumericRange(min, minInclusive, max, maxInclusive, bool));
        }

        void addModSeqRange(long min, boolean minInclusive, long max, boolean maxInclusive, boolean bool) {
            modSeqRanges.add(new NumericRange(min, minInclusive, max, maxInclusive, bool));
        }

        void addConvCountRange(long min, boolean minInclusive, long max, boolean maxInclusive, boolean bool) {
            convCountRanges.add(new NumericRange(min, minInclusive, max, maxInclusive, bool));
        }

        void addSizeRange(long min, boolean minInclusive, long max, boolean maxInclusive, boolean bool) {
            sizeRanges.add(new NumericRange(min, minInclusive, max, maxInclusive, bool));
        }

        void addSubjectRange(String min, boolean minInclusive, String max, boolean maxInclusive, boolean bool) {
            subjectRanges.add(new StringRange(min, minInclusive, max, maxInclusive, bool));
        }

        void addSenderRange(String min, boolean minInclusive, String max, boolean maxInclusive, boolean bool) {
            senderRanges.add(new StringRange(min, minInclusive, max, maxInclusive, bool));
        }

        void setFromContact(boolean bool) {
            fromContact = bool;
        }

        void addConvId(int cid, boolean truth) {

            if (truth) {
                if (prohibitedConvIds.contains(cid)) {
                    noResults = true;
                }

                if (convId == 0) {
                    convId = cid;
                } else {
                    ZimbraLog.search.debug("Query requested two conflicting convIDs, this is now a no-results-query");
                    convId = Integer.MAX_VALUE;
                    noResults = true;
                }
            } else {
                if (convId == cid) {
                    noResults = true;
                }
                prohibitedConvIds.add(cid);
            }
        }

        void addRemoteConvId(ItemId cid, boolean truth) {

            if (truth) {
                if (prohibitedRemoteConvIds.contains(cid)) {
                    noResults = true;
                }

                if (remoteConvId == null) {
                    remoteConvId = cid;
                } else {
                    ZimbraLog.search.debug("Query requested two conflicting Remote convIDs, this is now a no-results-query");
                    remoteConvId = new ItemId(cid.getAccountId(), Integer.MAX_VALUE);
                    noResults = true;
                }
            } else {
                if (remoteConvId.equals(cid)) {
                    noResults = true;
                }
                prohibitedRemoteConvIds.add(cid);
            }
        }

        void addInRemoteFolder(ItemId id, String subfolderPath, boolean includeSubfolders, boolean bool) {
            if (bool) {
                if ((!remoteFolders.isEmpty() && !remoteFolders.contains(id)) || excludeRemoteFolders.contains(id)) {
                    ZimbraLog.search.debug("AND of conflicting remote folders, no-results-query");
                    noResults = true;
                }
                remoteFolders.clear();
                remoteFolders.add(new Leaf.RemoteFolderDescriptor(id, subfolderPath, includeSubfolders));
                forceHasSpamTrashSetting();
            } else {
                if (remoteFolders.contains(id)) {
                    remoteFolders.remove(id);
                    if (remoteFolders.isEmpty()) {
                        ZimbraLog.search.debug("AND of conflicting remote folders, no-results-query");
                        noResults = true;
                    }
                }
                excludeRemoteFolders.add(new RemoteFolderDescriptor(id, subfolderPath, includeSubfolders));
            }
        }

        void addInFolder(Folder folder, boolean bool) {
            if (bool) {
                if ((!folders.isEmpty() && !folders.contains(folder)) || excludeFolders.contains(folder)) {
                    ZimbraLog.search.debug("AND of conflicting folders, no-results-query");
                    noResults = true;
                }
                folders.clear();
                folders.add(folder);
                forceHasSpamTrashSetting();
            } else {
                if (folders.contains(folder)) {
                    folders.remove(folder);
                    if (folders.isEmpty()) {
                        ZimbraLog.search.debug("AND of conflicting folders, no-results-query");
                        noResults = true;
                    }
                }
                excludeFolders.add(folder);

                int fid = folder.getId();
                if (fid == Mailbox.ID_FOLDER_TRASH || fid == Mailbox.ID_FOLDER_SPAM) {
                    forceHasSpamTrashSetting();
                }
            }
        }

        void addAnyFolder(boolean bool) {
            // support for "is:anywhere" basically as a way to get around
            // the trash/spam autosetting
            forceHasSpamTrashSetting();

            if (!bool) {
                // if they are weird enough to say "NOT is:anywhere" then we
                // just make it a no-results-query.
                ZimbraLog.search.debug("addAnyFolderClause(FALSE) called -- changing to no-results-query.");
                noResults = true;
            }
        }

        void addTag(Tag tag, boolean bool) {
            if (bool) {
                if (excludeTags != null && excludeTags.contains(tag)) {
                    ZimbraLog.search.debug("TAG and NOT TAG = no results");
                    noResults = true;
                }
                tags.add(tag);
            } else {
                if (tags.contains(tag)) {
                    ZimbraLog.search.debug("TAG and NOT TAG = no results");
                    noResults = true;
                }
                excludeTags.add(tag);
            }
        }
    }

    static final class Intersection implements DbSearchConstraints {
        private List<DbSearchConstraints> children = new ArrayList<DbSearchConstraints>();

        @Override
        public List<DbSearchConstraints> getChildren() {
            return children;
        }

        @Override
        public Leaf toLeaf() {
            return null;
        }

        @Override
        public Object clone() {
            Intersection result;
            try {
                result = (Intersection) super.clone();
            } catch (CloneNotSupportedException e) { // should never happen
                return null;
            }

            result.children = new ArrayList<DbSearchConstraints>();
            for (DbSearchConstraints child : children) {
                result.children.add((DbSearchConstraints) child.clone());
            }
            return result;
        }

        Leaf getLeafChild() {
            for (DbSearchConstraints child : children) {
                if (child instanceof Leaf) {
                    return (Leaf) child;
                }
            }
            Leaf leaf = new Leaf();
            children.add(leaf);
            return leaf;
        }

        @Override
        public DbSearchConstraints and(DbSearchConstraints other) {
            if (other instanceof Leaf) {
                Leaf leaf = (Leaf) other;
                getLeafChild().and(leaf);
            } else if (other instanceof Intersection) {
                for (DbSearchConstraints child : ((Intersection) other).children) {
                    if (child instanceof Leaf) {
                        DbSearchConstraints c = getLeafChild().and(child);
                        assert(c == this);
                    } else {
                        children.add(child);
                    }
                }
            } else if (other instanceof Union) {
                children.add(other);
            }
            return this;
        }

        @Override
        public DbSearchConstraints or(DbSearchConstraints other)  {
            if (other instanceof Union) {
                return other.or(other);
            } else {
                DbSearchConstraints top = new Union();
                return top.or(this).or(other);
            }
        }

        @Override
        public void ensureSpamTrashSetting(Mailbox mbox, List<Folder> excludeFolders) throws ServiceException {
            DbSearchConstraints c = getLeafChild();
            c.ensureSpamTrashSetting(mbox, excludeFolders);
        }

        @Override
        public boolean hasSpamTrashSetting() {
            for (DbSearchConstraints child : children) {
               if (child.hasSpamTrashSetting()) {
                   return true;
               }
            }
            return false;
        }

        @Override
        public void forceHasSpamTrashSetting() {
            for (DbSearchConstraints child : children) {
               if (!child.hasSpamTrashSetting()) {
                   child.forceHasSpamTrashSetting();
               }
            }
        }

        @Override
        public boolean hasNoResults() {
            for (DbSearchConstraints child : children) {
                if (child.hasNoResults()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean tryDbFirst(Mailbox mbox) {
            return false;
        }

        @Override
        public void setTypes(Set<MailItem.Type> types) {
            for (DbSearchConstraints child : children) {
                child.setTypes(types);
            }
        }

        @Override
        public StringBuilder toQueryString(StringBuilder out) {
            boolean first = true;
            for (DbSearchConstraints child : children) {
                if (!first) {
                    out.append(" AND ");
                }
                out.append('(');
                child.toQueryString(out);
                out.append(')');
                first = false;
            }
            return out;
        }

        @Override
        public String toString() {
            return toQueryString(new StringBuilder()).toString();
        }
    }

    static final class Union implements DbSearchConstraints {
        private List<DbSearchConstraints> children = new ArrayList<DbSearchConstraints>();

        @Override
        public List<DbSearchConstraints> getChildren() {
            return children;
        }

        @Override
        public Leaf toLeaf() {
            return null;
        }

        @Override
        public Object clone() {
            Union result;
            try {
                result = (Union) super.clone();
            } catch (CloneNotSupportedException e) { // should never happen
                return null;
            }
            result.children = new ArrayList<DbSearchConstraints>();
            for (DbSearchConstraints child : children) {
               result.children.add((DbSearchConstraints) child.clone());
            }
            return result;
        }

        @Override
        public DbSearchConstraints and(DbSearchConstraints other) {
            if (other instanceof Intersection) {
                return other.and(this);
            } else {
                DbSearchConstraints top = new Intersection();
                return top.and(this).and(other);
            }
        }

        @Override
        public DbSearchConstraints or(DbSearchConstraints other) {
            if (other instanceof Union) {
                children.addAll(((Union) other).children);
            } else {
                children.add(other);
            }
            return this;
        }

        @Override
        public void ensureSpamTrashSetting(Mailbox mbox, List<Folder> excludeFolders) throws ServiceException {
            // push down instead of ANDing this at the toplevel!
            //
            // This is important because we exclude (trash spam) and the query is:
            //
            //    (tag:foo is:anywhere) or (tag:bar)
            //
            // we want the resultant query to be:
            //
            //    (tag foo is:anywhere) or (tag:bar -in:trash -in:spam)
            for (DbSearchConstraints child : children) {
                child.ensureSpamTrashSetting(mbox, excludeFolders);
            }
        }

        @Override
        public boolean hasSpamTrashSetting() {
            for (DbSearchConstraints child : children) {
                if (!child.hasSpamTrashSetting()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void forceHasSpamTrashSetting() {
            for (DbSearchConstraints child : children) {
                if (!child.hasSpamTrashSetting()) {
                    child.forceHasSpamTrashSetting();
                }
            }
        }

        @Override
        public boolean hasNoResults() {
            for (DbSearchConstraints child : children) {
                if (!child.hasNoResults()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean tryDbFirst(Mailbox mbox) {
            return false;
        }

        @Override
        public void setTypes(Set<MailItem.Type> types) {
            for (DbSearchConstraints child : children) {
                child.setTypes(types);
            }
        }

        @Override
        public StringBuilder toQueryString(StringBuilder out) {
            boolean first = true;
            for (DbSearchConstraints child : children) {
                if (!first) {
                    out.append(" OR ");
                }
                out.append('(');
                child.toQueryString(out);
                out.append(')');
                first = false;
            }
            return out;
        }

        @Override
        public String toString() {
            return toQueryString(new StringBuilder()).toString();
        }
    }

    public static final class StringRange {
        public final boolean bool;
        public final String min;
        public final boolean minInclusive;
        public final String max;
        public final boolean maxInclusive;

        public StringRange(String min, boolean minInclusive, String max, boolean maxInclusive, boolean bool) {
            this.min = min;
            this.minInclusive = minInclusive;
            this.max = max;
            this.maxInclusive = maxInclusive;
            this.bool = bool;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof StringRange) {
                StringRange other = (StringRange) o;
                return Objects.equal(other.min, min) && other.minInclusive == minInclusive &&
                    Objects.equal(other.max, max) && other.maxInclusive == maxInclusive && other.bool == bool;
            } else {
                return false;
            }
        }

        private StringBuilder toQueryString(String name, StringBuilder out) {
            if (!bool) {
                out.append('-');
            }
            out.append(name).append(":(");
            if (min != null) {
                out.append('>');
                if (minInclusive) {
                    out.append('=');
                }
                out.append('"').append(min).append('"');
            }
            if (max != null) {
                if (min != null) {
                    out.append(' ');
                }
                out.append('<');
                if (maxInclusive) {
                    out.append('=');
                }
                out.append('"').append(max).append('"');
            }
            out.append(')');
            return out;
        }

        @Override
        public String toString() {
            return toQueryString("RANGE", new StringBuilder()).toString();
        }

        boolean isValid() {
            return min != null || max != null;
        }
    }

    public static final class NumericRange {
        public final boolean bool;
        public final long min;;
        public final boolean minInclusive;
        public final long max;
        public final boolean maxInclusive;

        public NumericRange(long min, boolean minInclusive, long max, boolean maxInclusive, boolean bool) {
            this.min = min;
            this.minInclusive = minInclusive;
            this.max = max;
            this.maxInclusive = maxInclusive;
            this.bool = bool;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof NumericRange) {
                NumericRange other = (NumericRange) o;
                return other.min == min && other.minInclusive == minInclusive &&
                    other.max == max && other.maxInclusive == maxInclusive && other.bool == bool;
            } else {
                return false;
            }
        }

        boolean isValid() {
            return min >= 0 || max >= 0;
        }

        private StringBuilder toQueryString(String name, StringBuilder out) {
            if (!bool) {
                out.append('-');
            }
            out.append(name).append(':');
            // special-case ">=N AND <=N" to be "=N"
            if (min >= 0 && minInclusive && maxInclusive && min == max) {
                out.append(min);
            } else {
                out.append('(');
                if (min >= 0) {
                    out.append('>');
                    if (minInclusive) {
                        out.append('=');
                    }
                    out.append(min);
                }
                if (max >= 0) {
                    if (min >= 0) {
                        out.append(' ');
                    }
                    out.append('<');
                    if (maxInclusive) {
                        out.append('=');
                    }
                    out.append(max);
                }
                out.append(')');
            }
            return out;
        }

        @Override
        public String toString() {
            return toQueryString("RANGE", new StringBuilder()).toString();
        }
    }

    public static final class RemoteFolderDescriptor {
        private ItemId folderId;
        private String subfolderPath;
        private boolean includeSubfolders;

        public RemoteFolderDescriptor(ItemId iidFolder, String subpath, boolean includeSubfolders) {
            this.includeSubfolders = includeSubfolders;
            this.folderId = iidFolder;
            this.subfolderPath = subpath;
            if (this.subfolderPath == null)
                this.subfolderPath = "";
        }

        public ItemId getFolderId() {
            return folderId;
        }
        public String getSubfolderPath() {
            return subfolderPath;
        }

        public boolean getIncludeSubfolders() {
            return includeSubfolders;
        }

        @Override
        public int hashCode() {
            final int PRIME = 31;
            int result = 1;
            result = PRIME * result + ((folderId == null) ? 0 : folderId.hashCode());
            result = PRIME * result + ((subfolderPath == null) ? 0 : subfolderPath.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof RemoteFolderDescriptor) {
                final RemoteFolderDescriptor other = (RemoteFolderDescriptor) obj;
                if (other.includeSubfolders != includeSubfolders) {
                    return false;
                }
                if (folderId == null) {
                    if (other.folderId != null) {
                        return false;
                    }
                } else if (!folderId.equals(other.folderId)) {
                    return false;
                }
                if (subfolderPath == null) {
                    if (other.subfolderPath != null) {
                        return false;
                    }
                } else if (!subfolderPath.equals(other.subfolderPath)) {
                    return false;
                }
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return folderId + "/" + subfolderPath + (includeSubfolders ? "..." : "");
        }
    }

}
