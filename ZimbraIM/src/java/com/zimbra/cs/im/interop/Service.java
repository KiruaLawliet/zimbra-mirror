/*
 * ***** BEGIN LICENSE BLOCK ***** Version: MPL 1.1
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 ("License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.zimbra.com/license
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * The Original Code is: Zimbra Collaboration Suite Server.
 * 
 * The Initial Developer of the Original Code is Zimbra, Inc. Portions created
 * by Zimbra are Copyright (C) 2005 Zimbra, Inc. All Rights Reserved.
 * 
 * Contributor(s):
 * 
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.cs.im.interop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.util.Log;
import org.jivesoftware.wildfire.SharedGroupException;
import org.jivesoftware.wildfire.roster.Roster;
import org.jivesoftware.wildfire.roster.RosterEventDispatcher;
import org.jivesoftware.wildfire.roster.RosterEventListener;
import org.jivesoftware.wildfire.roster.RosterItem;
import org.jivesoftware.wildfire.roster.RosterManager;
import org.jivesoftware.wildfire.user.UserAlreadyExistsException;
import org.jivesoftware.wildfire.user.UserNotFoundException;
import org.xmpp.component.Component;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import com.zimbra.common.util.ClassLogger;
import com.zimbra.common.util.ListUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.im.interop.Interop.UserStatus;

/**
 * This class represents one type of IM service that we interoperate with. There
 * is one instance of this class for every service type defined in
 * {@link Interop.ServiceName}.
 */
final class Service extends ClassLogger implements Component, RosterEventListener {

    Service(Interop.ServiceName name, SessionFactory fact) {
        super(ZimbraLog.im);
        mName = name;
        mRosterManager = new RosterManager();
        mFact = fact;
    }
    
    /* (non-Javadoc)
     * @see org.jivesoftware.wildfire.roster.RosterEventListener#addingContact(
     * org.jivesoftware.wildfire.roster.Roster, org.jivesoftware.wildfire.roster.RosterItem, boolean) */
    public boolean addingContact(Roster roster, RosterItem item, boolean persistent) {
        if (getTransportDomains().contains(item.getJid().getDomain()))
            return false;
        else
            return true;
    }
    
    /* (non-Javadoc)
     * @see org.jivesoftware.wildfire.roster.RosterEventListener#contactAdded(
     * org.jivesoftware.wildfire.roster.Roster, org.jivesoftware.wildfire.roster.RosterItem) */
    public void contactAdded(Roster roster, RosterItem item) {
        if (!getTransportDomains().contains(item.getJid().getDomain()))
            return;
        
        if (item == null)
            return;
        
        info("contactAdded: %s jid=%s",item.toString(), item.getJid());
        
        InteropSession s = mSessions.get(roster.getUsername());
        if (s != null && item.getJid() != null) {
            s.updateExternalSubscription(item.getJid(), item.getGroups());
        } else {
            warn("in contactAdded for Interop Service but could not find session (%s) - %s %s", 
                roster.getUsername(), item, item.getJid());
        }
    }
    /* (non-Javadoc)
     * @see org.jivesoftware.wildfire.roster.RosterEventListener#contactDeleted(
     * org.jivesoftware.wildfire.roster.Roster, org.jivesoftware.wildfire.roster.RosterItem) */
    public void contactDeleted(Roster roster, RosterItem item) {
        if (!getTransportDomains().contains(item.getJid().getDomain()))
            return;
        info("contactDeleted: "+item.toString());
//        InteropSession s = mSessions.get(roster.getUsername());
//        if (s != null) {
//            s.removeExternalSubscription(item.getJid());
//        } else {
//            warn("in contactAdded for Interop Service but could not find session (%s) - %s", 
//                roster.getUsername(), item.toString());
//        }
        
    }
    /* (non-Javadoc)
     * @see org.jivesoftware.wildfire.roster.RosterEventListener#contactUpdated(
     * org.jivesoftware.wildfire.roster.Roster, org.jivesoftware.wildfire.roster.RosterItem) */
    public void contactUpdated(Roster roster, RosterItem item) {
        if (!getTransportDomains().contains(item.getJid().getDomain()))
            return;
        info("contactUpdated: "+item.toString());
        InteropSession s = mSessions.get(roster.getUsername());
        if (s != null) {
            if (item.getAskStatus() == RosterItem.ASK_UNSUBSCRIBE) {
                s.removeExternalSubscription(item.getJid());                
            } else {
                s.updateExternalSubscription(item.getJid(), item.getGroups());
            }
        } else {
            warn("in contactAdded for Interop Service but could not find session (%s) - %s", 
                roster.getUsername(), item.toString());
        }
    }
    
    /* @see org.xmpp.component.Component#getDescription() */
    public String getDescription() {
        return mName.getDescription();
    }

    /* @see org.xmpp.component.Component#getName() */
    public String getName() {
        return mName.name();
    }

    /* @see org.xmpp.component.Component#initialize(org.xmpp.packet.JID, org.xmpp.component.ComponentManager) */
    public void initialize(JID jid, ComponentManager componentManager) {
        mJid = jid;
        mComponentManager = componentManager;
    }

    /* @see org.xmpp.component.Component#processPacket(org.xmpp.packet.Packet) */
    public void processPacket(Packet packet) {
        try {
            debug("Service.processPacket: %s", packet.toXML());
            List<Packet> replies = null;
            if (packet instanceof IQ)
                replies = processIQ((IQ) packet);
            else if (packet instanceof Presence)
                replies = processPresence((Presence) packet);
            else if (packet instanceof Message)
                replies = processMessage((Message) packet);
            else {
                debug("ignoring unknown packet: %s", packet);
            }

            if (replies != null)
                for (Packet p : replies)
                    send(p);
        } catch (Exception e) {
            Log.error("Interop:" + getName() + " - exception in processPacket", e);
        }
    }

    /* @see org.jivesoftware.wildfire.roster.RosterEventListener#rosterLoaded(
     * org.jivesoftware.wildfire.roster.Roster) */
    public void rosterLoaded(Roster roster) {}

    /* @see org.xmpp.component.Component#shutdown() */
    public void shutdown() {
        RosterEventDispatcher.removeListener(this);
        for (InteropSession s : mSessions.values()) {
            s.shutdown();
        }
        mSessions.clear();
    }

    /* @see org.xmpp.component.Component#start() */
    public void start() {
        RosterEventDispatcher.addListener(this);
    }

    /**
     * @param bareJid
     * @return
     */
    private InteropSession getSession(String bareJid) {
        synchronized (mSessions) {
            return mSessions.get(bareJid);
        }
    }

    /* @see com.zimbra.common.util.ClassLogger#getInstanceInfo() */
    @Override
    protected String getInstanceInfo() {
        return "Interop[" + this.getName() + "] - ";
    }

    protected List<Packet> processIQ(IQ iq) {
        return null;
    }

    protected List<Packet> processMessage(Message message) {
        JID to = message.getTo();
        JID from = message.getFrom();

        if (to.getNode() == null) {
            debug("Ignoring Message to transport: %s", message.toXML());
        } else {
            InteropSession s = getSession(from.toBareJID());
            if (s != null) {
                return s.processMessage(message);
            } else {
                info("Couldn't find session, ignoring message: %s", message.toXML());
            }
        }
        return null;
    }

    protected List<Packet> processPresence(Presence pres) {
        JID from = pres.getFrom();
        
        InteropSession s = getSession(from.toBareJID());
        if (s != null) 
            return s.processPresence(pres);
        else {
            debug("Unknown session: sending unavailable reply");
            Presence p = new Presence(Presence.Type.unavailable);
            p.setFrom(pres.getTo());
            p.setTo(pres.getFrom());
            List<Packet> toRet = new ArrayList<Packet>(1);
            toRet.add(p);
            return toRet;
        }
    }
    
    void addOrUpdateRosterSubscription(JID userJid, JID remoteId, String friendlyName, List<String> groups, 
        RosterItem.SubType subType, RosterItem.AskType askType, RosterItem.RecvType recvType) 
    throws UserNotFoundException {
        
        debug("addOrUpdateRosterSubscription: user=%s remoteJID=%s friendly=%s groups=%s Sub:%s Ask:%s Recv:%s",
            userJid.toBareJID(), remoteId.toBareJID(), friendlyName, groups.toString(), 
            subType.getName(), askType != null ? askType.getName() : "(null)", 
                recvType != null ? recvType.getName() : "(null)");
        
        if (userJid.toBareJID().equals(remoteId.toBareJID())) 
            throw new IllegalArgumentException("Can't add yourself to your own roster: %s"+remoteId.toBareJID()); 
        
        Roster roster = mRosterManager.getRoster(userJid.toBareJID());
        try {
            RosterItem existing = roster.getRosterItem(remoteId);

            // compare the list of groups
            boolean groupsEqual = ListUtil.listsEqual(groups, existing.getGroups());
            
            // are they different? If so, update!
            if (
                        (existing.getSubStatus() != subType) ||
                        (askType != null && existing.getAskStatus() != askType) ||
                        (recvType != null && existing.getRecvStatus() != recvType) ||
                        (existing.getNickname() == null && friendlyName != null) ||
                        (existing.getNickname() != null && !existing.getNickname().equals(friendlyName)) ||
                        (!groupsEqual)) 
            {
                existing.setSubStatus(subType);
                existing.setAskStatus(askType);
                existing.setRecvStatus(recvType);
                existing.setNickname(friendlyName);
                try {
                    existing.setGroups(groups);
                } catch (Exception e) {
                    warn("Caught exception adding groups: %s", e);
                }
                roster.updateRosterItem(existing);
            }
        } catch (UserNotFoundException e) {
            if (subType == RosterItem.SUB_BOTH || subType==RosterItem.SUB_TO) {
                // only want to bother creating the sub if we're subscribed TO the remote entity 
                try {
                    RosterItem newItem = roster.createRosterItem(remoteId, friendlyName, groups, true, false);
                    newItem.setSubStatus(subType);
                    newItem.setAskStatus(askType);
                    newItem.setRecvStatus(recvType);
                    try {
                        newItem.setGroups(groups);
                    } catch (Exception ignored) {
                        warn("Caught exception adding groups: %s", ignored);
                    }
                    roster.updateRosterItem(newItem);
                } catch (SharedGroupException sge) {
                    assert (false); 
                } catch (UserAlreadyExistsException uae) {
                    Log.error("UserAlreadyExistsException: %s", uae);
                }
            }
        }
    }
    
    UserStatus getConnectionStatus(JID jid) {
        InteropSession s = mSessions.get(jid.toBareJID());
        if (s != null) {
            UserStatus u = new UserStatus();
            u.username = s.getUsername();
            u.password = s.getPassword();
            u.state = s.getState();
            u.nextConnectAttemptTime = s.getNextConnectTime();
            return u;
        } else {
            return null;
        }
    }

    void addOrUpdateRosterSubscription(JID userJid, JID remoteId, String friendlyName, String group,
        RosterItem.SubType subType, RosterItem.AskType askType, RosterItem.RecvType recvType) 
    throws UserNotFoundException {

        ArrayList<String> groupsAL = new ArrayList<String>(1);
        groupsAL.add(group);
        addOrUpdateRosterSubscription(userJid, remoteId, friendlyName, groupsAL, 
            subType, askType, recvType);
    }
    
    void connectUser(JID jid, String name, String password, String transportName, String transportBuddyGroup)
                throws ComponentException, UserNotFoundException {
        synchronized (mSessions) {
            // add the SERVICE user (two-way sub)
            addOrUpdateRosterSubscription(jid, getServiceJID(jid), transportName, transportBuddyGroup, 
                RosterItem.SUB_BOTH, RosterItem.ASK_NONE, RosterItem.RECV_NONE);
            InteropSession s = mSessions.get(jid.toBareJID());
            if (s != null) {
                throw new AlreadyConnectedComponentException(transportName, s.getUsername());
            } else {
                mSessions.put(jid.toBareJID(), mFact.createSession(this, new JID(jid.toBareJID()), name,
                            password));
            }
        }
        // send a probe to the user's jid -- if the user is online, then we'll
        // get a presence packet which will trigger a logon
        Presence p = new Presence(Presence.Type.probe);
        p.setTo(new JID(jid.toBareJID()));
        p.setFrom(getServiceJID(jid));
        send(p);
    }

    /**
     * @param jid
     * @throws ComponentException
     * @throws UserNotFoundException
     */
    void disconnectUser(JID jid) throws ComponentException, UserNotFoundException {
        InteropSession s = mSessions.remove(jid.toBareJID());
        if (s != null) {
            s.shutdown();
        }
        removeAllSubscriptions(jid, getServiceJID(jid).getDomain());
    }

    /**
     * @param userJID
     * @return
     */
    JID getServiceJID(JID userJID) {
        return new JID(null, getName() + "." + userJID.getDomain(), null);
    }

    List<String> getTransportDomains() {
        ArrayList<String> toRet = new ArrayList<String>();
        toRet.add(mJid.getDomain());
        return toRet;
    }

    /**
     * Refresh all presence for this user -- used when a new session has come
     * online and needs to probe all the remote users
     * 
     * @param jid
     */
    void refreshAllPresence(JID jid) {
        InteropSession s = getSession(jid.toBareJID());
        serviceOnline(jid);
        if (s != null)
            s.refreshAllPresence();
    }

    /**
     * Remove all subscriptions for this service from the specified user.  This is
     * done when the user logs out of the remote interop system.
     * 
     * @param userJid
     * @param domain
     * @throws UserNotFoundException
     */
    void removeAllSubscriptions(JID userJid, String domain) throws UserNotFoundException {
        Roster roster = mRosterManager.getRoster(userJid.toBareJID());

        Collection<RosterItem> items = roster.getRosterItems();
        for (RosterItem item : items) {
            if (domain.equals(item.getJid().getDomain())) {
                try {
                    roster.deleteRosterItem(item.getJid(), true);
                } catch (SharedGroupException e) {
                    debug("Ignoring SharedGroupException from removeAllSubscriptions(%s, %s)", userJid, item
                                .getJid());
                }
            }
        }
    }

    void removeRosterSubscription(JID userJid, JID remoteId) throws UserNotFoundException {
        Roster roster = mRosterManager.getRoster(userJid.toBareJID());
        try {
            roster.deleteRosterItem(remoteId, true);
        } catch (SharedGroupException e) {
            debug("Ignoring SharedGroupException from removeRosterSubscription(%s, %s)", userJid, remoteId);
        }
    }

    void send(Packet p) throws ComponentException {
        mComponentManager.sendPacket(this, p);
    }

    /**
     * Used when the transport loses connectivity: we update the presence state for ALL 
     * of our roster items 
     * 
     * @param userId
     * @param p
     */
    void serviceOffline(JID userJid) {
        try {
            Roster roster = mRosterManager.getRoster(userJid.toBareJID());
            String domain = getServiceJID(userJid).getDomain();
            
            Collection<RosterItem> items = roster.getRosterItems();
            for (RosterItem item : items) {
                if (domain.equals(item.getJid().getDomain())) {
                    Presence p = new Presence(Presence.Type.unavailable);
                    p.setTo(userJid);
                    p.setFrom(item.getJid());
                    try {
                        send(p);
                    } catch (ComponentException e) {
                        error("ComponentException: %s", e);
                    }
                }
            }
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        }
    }

    
    /**
     * Only have to update the presence for the *service* here
     * 
     * @param userJid
     */
    void serviceOnline(JID userJid) {
        Presence p = new Presence();
        p.setFrom(getServiceJID(userJid));
        p.setTo(userJid);
        try {
            send(p);
        } catch (ComponentException e) {
            error("ComponentException: %s", e);
        }
    }
    private SessionFactory mFact;
    
    protected ComponentManager mComponentManager;

    protected JID mJid;

    protected Interop.ServiceName mName;

    protected RosterManager mRosterManager;

    protected Map<String /* users barejid */, InteropSession> mSessions = new HashMap<String, InteropSession>();
}
