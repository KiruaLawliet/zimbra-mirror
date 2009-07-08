/*
 * ***** BEGIN LICENSE BLOCK *****
 * 
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2007, 2008, 2009 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Yahoo! Public License
 * Version 1.0 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * 
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.cs.mailbox;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.ZcsMailbox.OfflineContext;
import com.zimbra.cs.redolog.op.CreateFolder;

public class LocalMailbox extends DesktopMailbox {
    public LocalMailbox(MailboxData data) throws ServiceException {
        super(data);
    }
    
    @Override synchronized boolean finishInitialization() throws ServiceException {
        if (super.finishInitialization()) {
            try {
                getFolderByPath(null, GLOBAL_SEARCHES_PATH);
            } catch (MailServiceException.NoSuchItemException x) {
                CreateFolder redo = new CreateFolder(getId(), GLOBAL_SEARCHES_PATH,
                    ID_FOLDER_USER_ROOT, Folder.FOLDER_IS_IMMUTABLE,
                    MailItem.TYPE_SEARCHFOLDER, 0, MailItem.DEFAULT_COLOR_RGB, null);
                redo.setFolderId(ID_FOLDER_GLOBAL_SEARCHES);
                redo.start(System.currentTimeMillis());
                createFolder(new OfflineContext(redo), GLOBAL_SEARCHES_PATH,
                    ID_FOLDER_USER_ROOT, Folder.FOLDER_IS_IMMUTABLE,
                    MailItem.TYPE_SEARCHFOLDER, 0, MailItem.DEFAULT_COLOR, null);
            }
            return true;
        }
        return false;
    }
    
    @Override protected synchronized void initialize() throws ServiceException {
        super.initialize();
        Folder.create(ID_FOLDER_GLOBAL_SEARCHES, this,
            getFolderById(ID_FOLDER_USER_ROOT), GLOBAL_SEARCHES_PATH,
            Folder.FOLDER_IS_IMMUTABLE, MailItem.TYPE_SEARCHFOLDER, 0,
            MailItem.DEFAULT_COLOR_RGB, null, null);
    }
}
