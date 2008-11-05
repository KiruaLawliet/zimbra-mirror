/*
 * ***** BEGIN LICENSE BLOCK *****
 *
 * Zimbra Collaboration Suite Web Client
 * Copyright (C) 2008 Zimbra, Inc.
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

ZmYahooImServiceController = function() {
	ZmImServiceController.call(this, true);

	// Create the service model object.
	new ZmYahooImService();
}

ZmYahooImServiceController.prototype = new ZmImServiceController;
ZmYahooImServiceController.prototype.constructor = ZmYahooImServiceController;


// Public methods

ZmYahooImServiceController.prototype.toString =
function() {
	return "ZmYahooImServiceController";
};

ZmYahooImServiceController.prototype.getMyPresenceTooltip =
function(showText) {
	if (ZmImService.INSTANCE.isLoggedIn()) {
		this._presenceTooltipFormat = this._presenceTooltipFormat || new AjxMessageFormat(ZmMsg.presenceTooltipYahoo);
		return this._presenceTooltipFormat.format([ZmImService.INSTANCE.getMyAddress(), showText]);
	} else {
		return ZmMsg.presenceTooltipYahooLoggedOut;
	}
};

ZmYahooImServiceController.prototype.login =
function(callback) {
	AjxDispatcher.require(["IM"]);
	var id = appCtxt.get(ZmSetting.IM_YAHOO_ID);
	if (id) {
		this._loginById(callback, id, true);
	} else {
		this._showLoginDialog(callback);
	}
};

ZmYahooImServiceController.prototype.getPresenceOperations =
function() {
	return [
		ZmOperation.IM_PRESENCE_OFFLINE,
		ZmOperation.IM_PRESENCE_CHAT,
		ZmOperation.IM_PRESENCE_AWAY,
		ZmOperation.IM_PRESENCE_XA,
		ZmOperation.IM_PRESENCE_DND
	];
};

ZmYahooImServiceController.prototype.getSupportsAccounts =
function() {
	return false;
};

ZmYahooImServiceController.prototype._showLoginDialog =
function(callback, id, remember, message) {
	var args = {
		callback: new AjxCallback(this, this._loginDialogCallback, [callback]),
		id: id,
		remember: remember,
		message: message
	};
	ZmYahooLoginDialog.getInstance().popup(args);
};

ZmYahooImServiceController.prototype._loginDialogCallback =
function(callback, data) {
	this._loginByPassword(callback, data.id, data.password, data.remember, data.dialog);
};

ZmYahooImServiceController.prototype._loginById =
function(callback, id, remember, dialog) {
	var soapDoc = AjxSoapDoc.create("GetYahooCookieRequest", "urn:zimbraMail");
	soapDoc.setMethodAttribute("user", id);
	var params = {
		asyncMode: true,
		soapDoc: soapDoc,
		callback: new AjxCallback(this, this._handleResponseGetYahooCookie, [callback, id, remember, dialog])
	};
	appCtxt.getAppController().sendRequest(params);
};

ZmYahooImServiceController.prototype._handleResponseGetYahooCookie =
function(callback, id, remember, dialog, response) {
	var responseData = response.getResponse().GetYahooCookieResponse;
	if (responseData.error) {
		this._showLoginDialog(callback, id, remember, ZmMsg.imPasswordExpired);
	} else {
		if (dialog) {
			dialog.popdown();
		}
		function trim(str) {
			return str.substring(0, str.indexOf(';'));
		}
		var cookie = ["Y=", trim(responseData.Y), "; T=", trim(responseData.T)].join("");
		ZmImService.INSTANCE.login(cookie, callback);
		if (remember) {
			var settings = appCtxt.getSettings(),
				setting = settings.getSetting(ZmSetting.IM_YAHOO_ID);
			if (setting.getValue() != id) {
				setting.setValue(id);
				settings.save([setting]);
			}
		}
	}
};

ZmYahooImServiceController.prototype._loginByPassword =
function(callback, id, password, remember, dialog) {
	var soapDoc = AjxSoapDoc.create("GetYahooAuthTokenRequest", "urn:zimbraMail");
	soapDoc.setMethodAttribute("user", id);
	soapDoc.setMethodAttribute("password", password);
	var params = {
		asyncMode: true,
		soapDoc: soapDoc,
		callback: new AjxCallback(this, this._handleResponseGetYahooAuthToken, [callback, id, remember, dialog])
	};
	appCtxt.getAppController().sendRequest(params);
};

ZmYahooImServiceController.prototype._handleResponseGetYahooAuthToken =
function(callback, id, remember, dialog, response) {
	var responseData = response.getResponse().GetYahooAuthTokenResponse;
	if (responseData.failed) {
		this._showLoginDialog(callback, id, remember, ZmMsg.imPasswordFailed);
	} else {
		this._loginById(callback, id, remember, dialog);
	}
};

