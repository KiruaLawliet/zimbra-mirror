/*
 * ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 ("License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.zimbra.com/license
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 * 
 * The Original Code is: Zimbra Collaboration Suite Server.
 * 
 * The Initial Developer of the Original Code is Zimbra, Inc.
 * Portions created by Zimbra are Copyright (C) 2006 Zimbra, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): 
 * 
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.cs.taglib.bean;

import com.zimbra.cs.zclient.ZEmailAddress;
import com.zimbra.cs.zclient.ZMessageHit;

import java.util.Date;
import java.util.List;

public class ZMessageHitBean extends ZSearchHitBean {

    private ZMessageHit mHit;
    
    public ZMessageHitBean(ZMessageHit hit) {
        super(hit, HitType.message);
        mHit = hit;
    }

    public String getId() { return mHit.getId(); }

    public String getFlags() { return mHit.getFlags(); } 
    
    public String getFolderId() { return mHit.getFolderId(); }

    public long getSize() { return mHit.getSize(); }

    public Date getDate() { return new Date(mHit.getDate()); }
    
    public String getConversationId() { return mHit.getConversationId(); }
    
    public boolean getIsUnread() { return mHit.isUnread(); }

    public boolean getIsFlagged() { return mHit.isFlagged(); }

    public boolean getHasAttachment() { return mHit.hasAttachment(); }

    public boolean getIsRepliedTo() { return mHit.isRepliedTo(); }

    public boolean getIsSentByMe() { return mHit.isSentByMe(); }

    public boolean getIsForwarded() { return mHit.isForwarded(); } 

    public boolean getIsDraft() { return mHit.isDraft(); }

    public boolean getIsDeleted() { return mHit.isDeleted(); }

    public boolean getIsNotificationSent() { return mHit.isNotificationSent(); }
    
    /**
     * @return comma-separated list of tag ids
     */
    public String getTagIds() { return mHit.getTagIds(); }

    public String getSubject() { return mHit.getSubject(); }
    
    public boolean getHasFlags() { return mHit.hasFlags(); }
    
    public boolean getHasTags() { return mHit.hasTags(); }
    
    public String getFragment() { return mHit.getFragment(); }
    
    public ZEmailAddress getSender() { return mHit.getSender(); }

    public String getDisplaySender() { return BeanUtils.getAddr(mHit.getSender()); }

    public boolean getContentMatched() { return mHit.getContentMatched(); }

    public boolean getMessageMatched() { return mHit.getContentMatched() || mHit.getMimePartHits().size() > 0; }

    public ZMessageBean getMessage() {
        if (mHit.getMessage() != null)
            return new ZMessageBean(mHit.getMessage());
        else
            return null;
    }

    /**
     *  @return names (1.2.3...) of mime part(s) that matched, or empty list.
     */
    public List<String> getMimePartHits() { return mHit.getMimePartHits(); }

    public String getStatusImage() {
        // TODO: handle appointments
        if (getIsUnread())
            return "mail/MsgStatusUnread.gif";
        else if (getIsDraft())
            return "mail/MsgStatusDraft.gif";
        else if (getIsRepliedTo())
            return "mail/MsgStatusReply.gif";
        else if (getIsForwarded())
            return "mail/MsgStatusForward.gif";
        else if (getIsSentByMe())
            return "mail/MsgStatusSent.gif";
        else
            return "mail/MsgStatusRead.gif";
    }

}

