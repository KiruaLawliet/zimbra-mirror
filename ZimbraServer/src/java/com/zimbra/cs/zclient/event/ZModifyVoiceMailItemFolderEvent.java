/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2007, 2009 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Zimbra Public License
 * Version 1.2 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.cs.zclient.event;

import com.zimbra.common.service.ServiceException;

public class ZModifyVoiceMailItemFolderEvent implements ZModifyItemFolderEvent {
	private String mFolderId;

	public ZModifyVoiceMailItemFolderEvent(String folderId) throws ServiceException {
		mFolderId = folderId;
	}

	public String getFolderId(String defaultValue) throws ServiceException {
		return mFolderId;
	}
}
