/*
 * ***** BEGIN LICENSE BLOCK *****
 * Version: ZPL 1.1
 * 
 * The contents of this file are subject to the Zimbra Public License
 * Version 1.1 ("License"); you may not use this file except in
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
 * Portions created by Zimbra are Copyright (C) 2005 Zimbra, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s):
 * 
 * ***** END LICENSE BLOCK *****
 */

/**
*
* @param appCtxt	[ZmAppCtxt]			the app context
*/
function ZmAssistantDialog(appCtxt) {

	var helpButton = new DwtDialog_ButtonDescriptor(ZmAssistantDialog.HELP_BUTTON, 
														   "help", DwtDialog.ALIGN_LEFT);
														   
	var extraButton = new DwtDialog_ButtonDescriptor(ZmAssistantDialog.EXTRA_BUTTON, 
														   ZmMsg.moreDetails, DwtDialog.ALIGN_LEFT);														   
														   
	DwtDialog.call(this, appCtxt.getShell(), "ZmAssistantDialog", ZmMsg.zimbraAssistant, null, [extraButton]);
//	ZmQuickAddDialog.call(this, appCtxt.getShell(), null, null, []);

	this._appCtxt = appCtxt;

	this.setContent(this._contentHtml());
	this._initContent();
	this._msgDialog = this._appCtxt.getMsgDialog();
	this.setButtonListener(DwtDialog.OK_BUTTON, new AjxListener(this, this._okButtonListener));
	this.setButtonListener(ZmAssistantDialog.EXTRA_BUTTON, new AjxListener(this, this._extraButtonListener));	

	// only trigger matching after a sufficient pause
	this._parseInterval = 75; //this._appCtxt.get(ZmSetting.AC_TIMER_INTERVAL);
	this._parseTimedAction = new AjxTimedAction(this, this._parseAction);
	this._parseActionId = -1;
	
	
	// TODO: need to init these based on COS features (calendars, contacts, etc)
	if (!ZmAssistantDialog._handlerInit) {
		ZmAssistantDialog._handlerInit = true;
		if (this._appCtxt.get(ZmSetting.CONTACTS_ENABLED)) {
			ZmAssistant.register(new ZmContactAssistant(appCtxt));
		}
		if (this._appCtxt.get(ZmSetting.CALENDAR_ENABLED)) {
			ZmAssistant.register(new ZmCalendarAssistant(appCtxt));	
			ZmAssistant.register(new ZmAppointmentAssistant(appCtxt));
		}
//		ZmAssistant.register(new ZmCallAssistant(appCtxt));
		ZmAssistant.register(new ZmMailAssistant(appCtxt));
		ZmAssistant.register(new ZmVersionAssistant(appCtxt));
		ZmAssistant.register(new ZmDebugAssistant(appCtxt));
	}	
	//var ok = this.getButton(DwtDialog.OK_BUTTON);
	//ok.setAlign(DwtLabel.IMAGE_RIGHT);
};

//ZmAssistantDialog.prototype = new ZmQuickAddDialog;
ZmAssistantDialog.prototype = new DwtDialog;
ZmAssistantDialog.prototype.constructor = ZmAssistantDialog;

ZmAssistantDialog.HELP_BUTTON = ++DwtDialog.LAST_BUTTON;
ZmAssistantDialog.EXTRA_BUTTON = ++DwtDialog.LAST_BUTTON;

ZmAssistantDialog._handlerInit = false;

/**
*/
ZmAssistantDialog.prototype.popup =
function() {
	this._commandEl.value = "";
	var commands = ZmAssistant.getHandlerCommands().join(", ");
	this._availableCommands = ZmMsg.ASST_availableCommands+ " " + commands;
	this._setDefault();

	DwtDialog.prototype.popup.call(this);
	this._commandEl.focus();
};

/*
* Returns HTML that forms the basic framework of the dialog.
*/
ZmAssistantDialog.prototype._contentHtml =
function() {
	var html = new AjxBuffer();
	this._contentId = Dwt.getNextId();	
	this._commandId = Dwt.getNextId();
	this._commandTitleId = Dwt.getNextId();
	html.append("<table cellspacing=3 border=0 width=400>");
	html.append("<tr><td colspan=3>", ZmMsg.enterCommand, "&nbsp;<span class='ZmAsstField' id='", this._commandTitleId, "'></span></td></tr>");	
	//html.append("<tr><td colspan=3 id='", this._commandTitleId, "'>", ZmMsg.enterCommand, "</td></tr>");		
	html.append("<tr><td colspan=3><div>");
	html.append("<textarea rows=2 style='width:100%' id='",this._commandId,"'>");
	html.append("</textarea>");
	html.append("</div></td></tr>");
	html.append("<tr><td colspan=3><div class=horizSep></div></td></tr>");
	html.append("<tr><td colspan=3><div id='", this._contentId, "'></div></td></tr>");
	html.append("</table>");	
	return html.toString();
};

ZmAssistantDialog.prototype.setAssistantContent =
function(html) {
	var contentDivEl = document.getElementById(this._contentId);
	contentDivEl.innerHTML = html;
};

ZmAssistantDialog.prototype.getAssistantDiv =
function(html) {
	return document.getElementById(this._contentId);
};


ZmAssistantDialog.prototype._initContent =
function() {
	this._commandEl = document.getElementById(this._commandId);
	Dwt.associateElementWithObject(this._commandEl, this);
	this._commandEl.onkeyup = ZmAssistantDialog._keyUpHdlr;
};

ZmAssistantDialog._keyUpHdlr =
function(ev) {
	var keyEv = DwtShell.keyEvent;
	keyEv.setFromDhtmlEvent(ev);
	var obj = keyEv.dwtObj;
	obj._commandUpdated();
//	DBG.println("value = "+obj._commandEl.value);
};

ZmAssistantDialog.prototype._commandUpdated =
function() {
	// reset timer on key activity
	if (this._parseActionId != -1) 	AjxTimedAction.cancelAction(this._parseActionId);
	this._parseActionId = AjxTimedAction.scheduleAction(this._parseTimedAction, this._parseInterval);
}

ZmAssistantDialog.prototype._parseAction =
function() {
	var assistant = null;	
	var cmd = this._commandEl.value.replace(/^\s*/, '');
	var match = cmd.match(/^([\.\w]+)\s*/);
	if (match) {
		var args = cmd.substring(match[0].length);
		var mainCommand = match[1];
		var commands = ZmAssistant.matchWord(mainCommand);
		if (commands.length == 1) assistant = ZmAssistant.getHandler(commands[0]);
		else {
			this._availableCommands = ZmMsg.ASST_availableCommands+ " " + commands.join(", ");
		}
		//if (assistant && mainCommand == cmd && mainCommand != assistant.getCommand() && this._assistant != assistant) {		
		if (assistant && mainCommand == cmd && this._assistant != assistant) {
			this._commandEl.value = assistant.getCommand()+ " ";
		}
	} else {
		this._availableCommands = ZmMsg.ASST_availableCommands+ " " + ZmAssistant.getHandlerCommands().join(", ");
	}

	if (this._assistant != assistant) {
		if (this._assistant != null) this._assistant.finish(this);
		this._assistant = assistant;
		if (this._assistant) {
			this._assistant.initialize(this);
			var title = this._assistant.getTitle();
			if (title) this._setCommandTitle(title);
		}
	}

	if (this._assistant) this._assistant.handle(this, null, args);
	else this._setDefault();
};

ZmAssistantDialog.prototype._setDefault = 
function() {
	this.setAssistantContent(this._availableCommands);
	this._setOkButton(AjxMsg.ok, false, false, true, null);
	this._setExtraButton(ZmMsg.moreDetails, false, false, true, null);	
	this._setCommandTitle("");
};	

ZmAssistantDialog.prototype._setCommandTitle =
function(title, dontHtmlEncode) {
	var titleEl = document.getElementById(this._commandTitleId);
	if (titleEl) titleEl.innerHTML = dontHtmlEncode ? title : AjxStringUtil.htmlEncode(title);
};

ZmAssistantDialog.prototype._setOkButton =
function(title, visible, enabled) {
	var ok = this.getButton(DwtDialog.OK_BUTTON);
	if (title) ok.setText(title);
	ok.setEnabled(enabled);
	ok.setVisible(visible);
	//if (setImage) ok.setImage(image);
};

ZmAssistantDialog.prototype._setExtraButton =
function(title, visible, enabled) {
	var ok = this.getButton(ZmAssistantDialog.EXTRA_BUTTON);
	if (title) ok.setText(title);
	ok.setEnabled(enabled);
	ok.setVisible(visible);
	//if (setImage) ok.setImage(image);
};

/**
* Clears the conditions and actions table before popdown so we don't keep
* adding to them.
*/
ZmAssistantDialog.prototype.popdown =
function() {
	DwtDialog.prototype.popdown.call(this);
	if (this._assistant != null) this._assistant.finish(this);
	this._assistant = null;
};

ZmAssistantDialog.prototype._okButtonListener =
function(ev) {
	if (this._assistant && !this._assistant.okHandler(this)) return;
	this.popdown();
};

ZmAssistantDialog.prototype._extraButtonListener =
function(ev) {
	if (this._assistant && !this._assistant.extraButtonHandler(this)) return;
	this.popdown();
};


ZmAssistantDialog.prototype._handleResponseOkButtonListener =
function() {
	this.popdown();
};

ZmAssistantDialog.prototype.messageDialog =
function(message, style) {
	this._msgDialog.reset();
	this._msgDialog.setMessage(message, style);
	this._msgDialog.popup();
};
