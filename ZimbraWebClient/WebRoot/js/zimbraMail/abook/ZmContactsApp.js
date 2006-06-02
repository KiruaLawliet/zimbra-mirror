/*
 * ***** BEGIN LICENSE BLOCK *****
 * Version: ZPL 1.2
 * 
 * The contents of this file are subject to the Zimbra Public License
 * Version 1.2 ("License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.zimbra.com/license
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 * 
 * The Original Code is: Zimbra Collaboration Suite Web Client
 * 
 * The Initial Developer of the Original Code is Zimbra, Inc.
 * Portions created by Zimbra are Copyright (C) 2005, 2006 Zimbra, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s):
 * 
 * ***** END LICENSE BLOCK *****
 */

/**
* Contact app object
* @constructor
* @class
* Description goes here
*
* @author Conrad Damon
* @param appCtxt			The singleton appCtxt object
* @param container			the element that contains everything but the banner (aka _composite)
* @param parentController	Reference to the parent "uber" controller - populated if this is a child window opened by the parent
*/
function ZmContactsApp(appCtxt, container, parentController) {
	ZmApp.call(this, ZmZimbraMail.CONTACTS_APP, appCtxt, container, parentController);
};

ZmContactsApp.prototype = new ZmApp;
ZmContactsApp.prototype.constructor = ZmContactsApp;

ZmContactsApp.prototype.toString = 
function() {
	return "ZmContactsApp";
};

ZmContactsApp.prototype.launch =
function(callback, errorCallback) {
	var respCallback = new AjxCallback(this, this._handleResponseLaunch, callback);
	this.getContactList(respCallback, errorCallback);
};

ZmContactsApp.prototype._handleResponseLaunch =
function(callback) {
	// get the last selected folder ID
	var folderId = ZmOrganizer.ID_ADDRBOOK;
	var treeView = this._appCtxt.getOverviewController().getTreeView(ZmZimbraMail._OVERVIEW_ID, ZmOrganizer.ADDRBOOK);
	var treeItem = treeView ? treeView.getSelection()[0] : null;
	if (treeItem) {
		folderId = treeItem.getData(Dwt.KEY_ID);
	}

	if (this._appCtxt.get(ZmSetting.SHOW_SEARCH_STRING)) {
		var folder = this._appCtxt.getTree(ZmOrganizer.ADDRBOOK).getById(folderId);
		var query = folder.createQuery();
		this._appCtxt.getSearchController().getSearchToolbar().setSearchFieldValue(query);
	}

	this.getContactListController().show(this._contactList, null, folderId);
	if (callback)
		callback.run();
};

ZmContactsApp.prototype.showFolder = 
function(folder) {
	if (this._appCtxt.get(ZmSetting.SHOW_SEARCH_STRING)) {
		var query = folder.createQuery();
		this._appCtxt.getSearchController().getSearchToolbar().setSearchFieldValue(query);
	}
	this.getContactListController().show(this._contactList, null, folder.id);
};

ZmContactsApp.prototype.setActive =
function(active) {
	if (active)
		this.getContactListController().show();
};

ZmContactsApp.prototype.getContactList =
function(callback, errorCallback) {
	if (!this._contactList) {
		try {
			// check if a parent controller exists and ask it for the contact list
			if (this._parentController) {
				this._contactList = this._parentController.getApp(ZmZimbraMail.CONTACTS_APP).getContactList();
			} else {
				this._contactList = new ZmContactList(this._appCtxt);
				var respCallback = new AjxCallback(this, this._handleResponseGetContactList, callback);
				this._contactList.load(respCallback, errorCallback);
			}
		} catch (ex) {
			this._contactList = null;
			throw ex;
		}
	} else {
		if (callback) callback.run();
	}
	if (!callback) return this._contactList;
};

ZmContactsApp.prototype._handleResponseGetContactList =
function(callback) {
	if (callback) callback.run();
};

// NOTE: calling method should handle exceptions!
ZmContactsApp.prototype.getGalContactList =
function() {
	if (!this._galContactList) {
		try {
			this._galContactList = new ZmContactList(this._appCtxt, null, true);
			this._galContactList.load();
		} catch (ex) {
			this._galContactList = null;
			throw ex;
		}
	}
	return this._galContactList;
};

// returns array of all addrbooks (incl. shared but excl. root and Trash)
ZmContactsApp.prototype.getAddrbookList =
function() {
	var addrbookList = [];
	var folders = this._appCtxt.getTree(ZmOrganizer.ADDRBOOK).asList();
	for (var i = 0; i < folders.length; i++) {
		if (folders[i].id == ZmFolder.ID_ROOT || folders[i].isInTrash())
			continue;
		addrbookList.push(folders[i].createQuery());
	}
	return addrbookList;
};

ZmContactsApp.prototype.getContactListController =
function() {
	if (!this._contactListController)
		this._contactListController = new ZmContactListController(this._appCtxt, this._container, this);
	return this._contactListController;
};

ZmContactsApp.prototype.getContactController =
function() {
	if (this._contactController == null)
		this._contactController = new ZmContactController(this._appCtxt, this._container, this);
	return this._contactController;
};
