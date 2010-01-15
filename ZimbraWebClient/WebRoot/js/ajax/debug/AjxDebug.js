/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Web Client
 * Copyright (C) 2005, 2006, 2007, 2008, 2009 Zimbra, Inc.
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


/**
* Creates a new debug window. The document inside is not kept open.  All the 
  output goes into a single &lt;div&gt; element.
* @constructor
* @class
* This class pops up a debug window and provides functions to send output there 
* in various ways. The output is continuously appended to the bottom of the 
* window. The document is left unopened so that the browser doesn't think it's 
* continuously loading and keep its little icon flailing forever. Also, the DOM 
* tree can't be manipulated on an open document. All the output is added to the 
* window by appending it the DOM tree. Another method of appending output is to 
* open the document and use document.write(), but then the document is left open.
* <p>
* Any client that uses this class can turn off debugging by changing the first 
* argument to the constructor to AjxDebug.NONE.
*
* @author Conrad Damon
* @author Ross Dargahi
* @param level	 	[constant]		debug level for the current debugger (no window will be displayed for a level of NONE)
* @param name 		[string]*		the name of the window. Defaults to "debug_" prepended to the calling window's URL.
* @param showTime	[boolean]*		if true, display timestamps before debug messages
*/
AjxDebug = function(level, name, showTime) {
	this._dbgName = "AjxDebugWin_" + location.hostname.replace(/\./g,'_');
	this._level = Number(level);
	this._showTime = showTime;
	this._showTiming = false;
	this._startTimePt = this._lastTimePt = 0;
	this._dbgWindowInited = false;

	this._msgQueue = [];
	this._isPrevWinOpen = false;
	this._enable(this._level != AjxDebug.NONE);
};

AjxDebug.NONE = 0; // no debugging (window will not come up)
AjxDebug.DBG1 = 1; // minimal debugging
AjxDebug.DBG2 = 2; // moderate debugging
AjxDebug.DBG3 = 3; // anything goes

AjxDebug.MAX_OUT = 25000; // max length capable of outputting

AjxDebug._CONTENT_FRAME_ID	= "AjxDebug_CF";
AjxDebug._LINK_FRAME_ID		= "AjxDebug_LF";
AjxDebug._BOTTOM_FRAME_ID	= "AjxDebug_BFI";
AjxDebug._BOTTOM_FRAME_NAME	= "AjxDebug_BFN";

AjxDebug.prototype.toString =
function() {
	return "AjxDebug";
};

AjxDebug.prototype.setTitle =
function(title) {
	if (this._document && !AjxEnv.isIE) {
		this._document.title = title;
	}
};

/**
* Set debug level. May open or close the debug window if moving to or from level NONE.
*
* @param level	 	debug level for the current debugger
*/
AjxDebug.prototype.setDebugLevel =
function(level, disable) {
	this._level = /^[\d]+$/.test(level) ? Number(level) : level;
	if (!disable) {
		this._enable(level != AjxDebug.NONE);
	}
};

/**
* Returns the current debug level.
*/
AjxDebug.prototype.getDebugLevel =
function() {
	return this._level;
};

/**
* Prints a debug message. Any HTML will be rendered, and a line break is added.
*
* @param level	 	debug level for the current debugger
* @param msg		the text to display
*/
AjxDebug.prototype.println =
function(level, msg, linkName) {
	if (!this._isWriteable()) { return; }

	try {
		var args = this._handleArgs(arguments, linkName);
		if (!args) { return; }

		msg = args.join("");
		this._add(this._timestamp() + msg + "<br>", null, null, null, linkName);
	} catch (ex) {
		// do nothing
	}
};

AjxDebug.prototype.isDisabled =
function () {
	return !this._enabled;
};

/**
* Prints an object into a table, with a column for properties and a column for values. Above the table is a header with the object
* class and the CSS class (if any). The properties are sorted (numerically if they're all numbers). Creating and appending table
* elements worked in Mozilla but not IE. Using the insert* methods works for both. Properties that are function
* definitions are skipped.
*
* @param level	 	debug level for the current debugger
* @param obj		the object to be printed
* @param showFuncs	whether to show props that are functions
*/
AjxDebug.prototype.dumpObj =
function(level, obj, showFuncs, linkName) {
	if (!this._isWriteable()) { return; }

	var args = this._handleArgs(arguments, linkName);
	if (!args) { return; }

	obj = args[0];
	if (!obj) { return; }

	showFuncs = args[1];
	this._add(null, obj, false, false, null, showFuncs);
};

/**
* Dumps a bunch of text into a &lt;textarea&gt;, so that it is wrapped and scrollable. HTML will not be rendered.
*
* @param level	 	debug level for the current debugger
* @param text		the text to output as is
*/
AjxDebug.prototype.printRaw =
function(level, text, linkName) {
	if (!this._isWriteable()) { return; }

	var args = this._handleArgs(arguments, linkName);
	if (!args) { return; }

	this._add(null, args[0], false, true);
};

/**
* Pretty-prints a chunk of XML, doing color highlighting for different types of nodes.

* @param level	 	debug level for the current debugger
* @param text		some XML
*/
AjxDebug.prototype.printXML =
function(level, text, linkName) {
	if (!this._isWriteable()) { return; }

	var args = this._handleArgs(arguments, linkName);
	if (!args) { return; }

	text = args[0];
	if (!text) { return; }

	// skip generating pretty xml if theres too much data
	if (text.length > AjxDebug.MAX_OUT) {
		this.printRaw(text);
		return;
	}
	this._add(null, text, true, false);
};

/**
* Reveals white space in text by replacing it with tags.
*
* @param level	 	debug level for the current debugger
* @param text		the text to be displayed
*/
AjxDebug.prototype.display =
function(level, text) {
	if (!this._isWriteable()) { return; }

	var args = this._handleArgs(arguments);
	if (!args) { return; }

	text = args[0];
	text = text.replace(/\r?\n/g, '[crlf]');
	text = text.replace(/ /g, '[space]');
	text = text.replace(/\t/g, '[tab]');
	this.printRaw(level, text);
};

/**
* Turn the display of timing statements on/off.
*
* @param on			[boolean]		if true, display timing statements
* @param msg		[string]*		message to show when timing is turned on
*/
AjxDebug.prototype.showTiming =
function(on, msg) {
	this._showTiming = on;
	if (on) {
		this._enable(true);
	}
	var state = on ? "on" : "off";
	var text = "Turning timing info " + state;
	if (msg) {
		text = text + ": " + msg;
	}

	var debugMsg = new DebugMessage(text);
	this._addMessage(debugMsg);
	this._startTimePt = this._lastTimePt = new Date().getTime();
};

/**
* Displays time elapsed since last time point.
*
* @param msg		[string]*		text to display with timing info
* @param restart	[boolean]*		if true, set timer back to zero
*/
AjxDebug.prototype.timePt =
function(msg, restart) {
	if (!this._showTiming || !this._isWriteable()) { return; }

	if (restart) {
		this._startTimePt = this._lastTimePt = new Date().getTime();
	}
	var now = new Date().getTime();
	var elapsed = now - this._startTimePt;
	var interval = now - this._lastTimePt;
	this._lastTimePt = now;

	var spacer = restart ? "<br/>" : "";
	msg = msg ? " " + msg : "";
	var text = [spacer, "[", elapsed, " / ", interval, "]", msg].join("");
	var html = "<div>" + text + "</div>";

	// Add the message to our stack
	this._addMessage(new DebugMessage(html));
	return interval;
};

AjxDebug.prototype.getContentFrame =
function() {
	if (this._contentFrame) {
		return this._contentFrame;
	}
	if (this._debugWindow && this._debugWindow.document) {
		return this._debugWindow.document.getElementById(AjxDebug._CONTENT_FRAME_ID);
	}
	return null;
};

AjxDebug.prototype.getLinkFrame =
function(noOpen) {
	if (this._linkFrame) {
		return this._linkFrame;
	}
	if (this._debugWindow && this._debugWindow.document) {
		return this._debugWindow.document.getElementById(AjxDebug._LINK_FRAME_ID);
	}
	if (!noOpen) {
		this._openDebugWindow();
		return this.getLinkFrame(true);
	}
	return null;
};

// Private methods

AjxDebug.prototype._enable =
function(enabled) {
	this._enabled = enabled;
	if (enabled) {
		if (this._debugWindow == null || this._debugWindow.closed)
			this._openDebugWindow();
	} else {
		if (this._debugWindow) {
			this._debugWindow.close();
			this._debugWindow = null;
		}
	}
};

AjxDebug.prototype._isWriteable =
function() {
	return (!this._isPaused && !this.isDisabled() && this._debugWindow && !this._debugWindow.closed);
};

AjxDebug.prototype._getHtmlForObject =
function(anObj, isXml, isRaw, timestamp, showFuncs) {
	var html = [];
	var idx = 0;

	if (anObj === undefined) {
		html[idx++] = "<span>Undefined</span>";
	} else if (anObj === null) {
		html[idx++] = "<span>NULL</span>";
	} else if (AjxUtil.isBoolean(anObj)) {
		html[idx++] = "<span>" + anObj + "</span>";
	} else if (AjxUtil.isNumber(anObj)) {
		html[idx++] = "<span>" + anObj +"</span>";
	} else {
		if (isRaw) {
			html[idx++] = this._timestamp();
			html[idx++] = "<textarea rows='25' style='width:100%' readonly='true'>";
			html[idx++] = anObj;
			html[idx++] = "</textarea><p></p>";
		} else if (isXml) {
			var xmldoc = new AjxDebugXmlDocument;
			var doc = xmldoc.create();
			// IE bizarrely throws error if we use doc.loadXML here (bug 40451)
			if (doc && ("loadXML" in doc)) {
				doc.loadXML(anObj);
	//			if (timestamp) {
	//				html[idx++] = [doc.documentElement.nodeName, this._getTimeStamp(timestamp)].join(" - ");
	//			}
				html[idx++] = "<div style='border-width:2px; border-style:inset; width:100%; height:300px; overflow:auto'>";
				html[idx++] = this._createXmlTree(doc, 0);
				html[idx++] = "</div>";
			} else {
				html[idx++] = "<span>Unable to create XmlDocument to show XML</span>";
			}
		} else {
			html[idx++] = "<div style='border-width:2px; border-style:inset; width:100%; height:300px; overflow:auto'><pre>";
			html[idx++] = this._dump(anObj, true, showFuncs);
			html[idx++] = "</div></pre>";
		}
	}
	return html.join("");
};

// Pretty-prints a Javascript object
AjxDebug.prototype._dump =
function(obj, recurse, showFuncs) {

	return AjxStringUtil.prettyPrint(obj, recurse, showFuncs, {ZmAppCtxt:true});
};

/*
* Marshals args to public debug functions. In general, the debug level is an optional
* first arg. If the first arg is a debug level, check it and then strip it from the args.
* Returns a normalized list of args.
*
* @param args				[array]			arguments list
* @param linkNameSpecified	[boolean]*		if true, link text for left frame was provided
*/
AjxDebug.prototype._handleArgs =
function(args, linkNameSpecified) {
	// don't output anything if debugging is off, or timing is on
	if (this._level == AjxDebug.NONE || this._showTiming) { return; }

	var msgLevel = AjxDebug.DBG1;
	if (args.length > 1) {
		if (typeof args[0] == "number" && typeof this._level == "number") {
			msgLevel = args[0];
			if (msgLevel > this._level) return;
		} else {
			// check for custom debug level
			if (args[0] && args[0] != this._level) return;
		}
	}

	// NOTE: Can't just slice the items we want because args is not a true Array
	var array = new Array(args.length);
	var len = (linkNameSpecified) ? args.length - 1 : args.length;
	for (var i = 0; i < len; i++) {
		array[i] = args[i];
	}
	if (len > 1) {
		array.shift();	// remove level
	}

	return array;
};

AjxDebug.prototype._openDebugWindow =
function(force) {
	var name = AjxEnv.isIE ? "_blank" : this._dbgName;
	this._debugWindow = window.open("", name, "width=600,height=400,resizable=yes,scrollbars=yes");

	if (this._debugWindow == null) {
		this._enabled = false;
		return;
	}

	this._enabled = true;
	this._isPrevWinOpen = this._debugWindow.debug;
	this._debugWindow.debug = true;

	try {
		this._document = this._debugWindow.document;
		this.setTitle("Debug");

		if (!this._isPrevWinOpen) {
			this._document.write(
				"<html>",
					"<head>",
						"<script>",
							"function blank() {return [",
								"'<html><head><style type=\"text/css\">',",
									"'P, TD, DIV, SPAN, SELECT, INPUT, TEXTAREA, BUTTON {',",
											"'font-family: Tahoma, Arial, Helvetica, sans-serif;',",
											"'font-size:11px;}',",
									"'.Content {display:block;margin:0.25em 0em;}',",
									"'.Link {cursor: pointer;color:blue;text-decoration:underline;white-space:nowrap;width:100%;}',",
									"'.Run {color:black; background-color:red;width:100%;font-size:18px;font-weight:bold;}',",
									"'.RunLink {display:block;color:black;background-color:red;font-weight:bold;white-space:nowrap;width:100%;}',",
								"'</style></head><body></body></html>'].join(\"\");}",
						"</script>",
					"</head>",
					"<frameset cols='125, *'>",
						"<frameset rows='*,40'>",
							"<frame name='", AjxDebug._LINK_FRAME_ID, "' id='", AjxDebug._LINK_FRAME_ID, "' src='javascript:parent.parent.blank();'>",
							"<frame name='", AjxDebug._BOTTOM_FRAME_NAME, "' id='", AjxDebug._BOTTOM_FRAME_ID, "' src='javascript:parent.parent.blank();' scrolling=no frameborder=0>",
						"</frameset>",
						"<frame name='", AjxDebug._CONTENT_FRAME_ID, "' id='", AjxDebug._CONTENT_FRAME_ID, "' src='javascript:parent.blank();'>",
					"</frameset>",
				"</html>"
			);
			this._document.close();
			
			var ta = new AjxTimedAction(this, AjxDebug.prototype._finishInitWindow);
			AjxTimedAction.scheduleAction(ta, 1500);
		} else {
			this._finishInitWindow();

			this._contentFrame = this._document.getElementById(AjxDebug._CONTENT_FRAME_ID);
			this._linkFrame = this._document.getElementById(AjxDebug._LINK_FRAME_ID);
			this._createLinkNContent("RunLink", "NEW RUN", "Run", "NEW RUN");

			this._attachHandlers();

			this._dbgWindowInited = true;
			// show any messages that have been queued up, while the window loaded.
			this._showMessages();
		}
	} catch (ex) {
		if (this._debugWindow) {
			this._debugWindow.close();
		}
		this._openDebugWindow(true);
	}
};

AjxDebug.prototype._finishInitWindow =
function() {
	try {
		this._contentFrame = this._debugWindow.document.getElementById(AjxDebug._CONTENT_FRAME_ID);
		this._linkFrame = this._debugWindow.document.getElementById(AjxDebug._LINK_FRAME_ID);

		var frame = this._debugWindow.document.getElementById(AjxDebug._BOTTOM_FRAME_ID);
		var doc = frame.contentWindow.document;
		var html = [];
		var i = 0;
		html[i++] = "<table><tr><td><button id='";
		html[i++] = AjxDebug._BOTTOM_FRAME_ID;
		html[i++] = "_clear'>Clear</button></td><td><button id='";
		html[i++] = AjxDebug._BOTTOM_FRAME_ID;
		html[i++] = "_pause'>Pause</button></td></tr></table>";
		doc.body.innerHTML = html.join("");
	}
	catch (ex) {
		// IE chokes on the popup window on cold start-up (when IE is started
		// for the fisrt time after system reboot). This should not prevent the
		// app from running and should not bother the user
	}

	this._clearBtn = doc.getElementById(AjxDebug._BOTTOM_FRAME_ID + "_clear");
	this._pauseBtn = doc.getElementById(AjxDebug._BOTTOM_FRAME_ID + "_pause");

	this._attachHandlers();
	this._dbgWindowInited = true;
	this._showMessages();
};

AjxDebug.prototype._attachHandlers =
function() {
	// Firefox allows us to attach an event listener, and runs it even though
	// the window with the code is gone ... odd, but nice. IE, though will not
	// run the handler, so we make sure, even if we're  coming back to the
	// window, to attach the onunload handler. In general reattach all handlers
	// for IE
	var unloadHandler = AjxCallback.simpleClosure(this._unloadHandler, this);
	if (AjxEnv.isIE) {
		this._unloadHandler = unloadHandler;
		this._debugWindow.attachEvent('onunload', unloadHandler);
	}
	else {
		this._debugWindow.onunload = unloadHandler;
	}

	if (this._clearBtn) {
		this._clearBtn.onclick = AjxCallback.simpleClosure(this._clear, this);
	}
	if (this._pauseBtn) {
		this._pauseBtn.onclick = AjxCallback.simpleClosure(this._pause, this);
	}
};

/**
* Scrolls to the bottom of the window. How it does that depends on the browser.
*
* @private
*/
AjxDebug.prototype._scrollToBottom =
function() {
	var contentFrame = this.getContentFrame();
	var contentBody = contentFrame ? contentFrame.contentWindow.document.body : null;
	var linkFrame = this.getLinkFrame();
	var linkBody = linkFrame ? linkFrame.contentWindow.document.body : null;

	if (contentBody && linkBody) {
		contentBody.scrollTop = contentBody.scrollHeight;
		linkBody.scrollTop = linkBody.scrollHeight;
	}
};

/**
* Returns a timestamp string, if we are showing them.
* @private
*/
AjxDebug.prototype._timestamp =
function() {
	return this._showTime ? this._getTimeStamp() + ": " : "";
};

AjxDebug.prototype.setShowTimestamps =
function(show) {
	this._showTime = show;
};

// this function takes an xml node and returns an html string that displays that node
// the indent argument is used to describe what depth the node is at so that
// the html code can create a nice indention
AjxDebug.prototype._createXmlTree =
function (node, indent) {
	if (node == null) { return ""; }

	var str = "";
	var len;
	switch (node.nodeType) {
		case 1:	// Element
			str += "<div style='color: blue; padding-left: 16px;'>&lt;<span style='color: DarkRed;'>" + node.nodeName + "</span>";

			var attrs = node.attributes;
			len = attrs.length;
			for (var i = 0; i < len; i++) {
				str += this._createXmlAttribute(attrs[i]);
			}

			if (!node.hasChildNodes()) {
				return str + "/&gt;</div>";
			}
			str += "&gt;<br />";

			var cs = node.childNodes;
			len = cs.length;
			for (var i = 0; i < len; i++) {
				str += this._createXmlTree(cs[i], indent + 3);
			}
			str += "&lt;/<span style='color: DarkRed;'>" + node.nodeName + "</span>&gt;</div>";
			break;

		case 9:	// Document
			var cs = node.childNodes;
			len = cs.length;
			for (var i = 0; i < len; i++) {
				str += this._createXmlTree(cs[i], indent);
			}
			break;

		case 3:	// Text
			if (!/^\s*$/.test(node.nodeValue)) {
				var val = node.nodeValue.replace(/</g, "&lt;").replace(/>/g, "&gt;");
				str += "<span style='color: WindowText; padding-left: 16px;'>" + val + "</span><br />";
			}
			break;

		case 7:	// ProcessInstruction
			str += "&lt;?" + node.nodeName;

			var attrs = node.attributes;
			len = attrs.length;
			for (var i = 0; i < len; i++) {
				str += this._createXmlAttribute(attrs[i]);
			}
			str+= "?&gt;<br />"
			break;

		case 4:	// CDATA
			str = "<div style=''>&lt;![CDATA[<span style='color: WindowText; font-family: \"Courier New\"; white-space: pre; display: block; border-left: 1px solid Gray; padding-left: 16px;'>" +
				  node.nodeValue +
				  "</span>]" + "]></div>";
			break;

		case 8:	// Comment
			str = "<div style='color: blue; padding-left: 16px;'>&lt;!--<span style='white-space: pre; font-family: \"Courier New\"; color: Gray; display: block;'>" +
				  node.nodeValue +
				  "</span>--></div>";
			break;

		case 10:
				str = "<div style='color: blue; padding-left: 16px'>&lt;!DOCTYPE " + node.name;
				if (node.publicId) {
					str += " PUBLIC \"" + node.publicId + "\"";
					if (node.systemId)
						str += " \"" + node.systemId + "\"";
				}
				else if (node.systemId) {
					str += " SYSTEM \"" + node.systemId + "\"";
				}
				str += "&gt;</div>";

				// TODO: Handle custom DOCTYPE declarations (ELEMENT, ATTRIBUTE, ENTITY)
				break;

		default:
			this._inspect(node);
	}

	return str;
};

AjxDebug.prototype._createXmlAttribute =
function(a) {
	return [" <span style='color: red'>", a.nodeName, "</span><span style='color: blue'>=\"", a.nodeValue, "\"</span>"].join("");
};

AjxDebug.prototype._inspect =
function(obj) {
	var str = "";
	for (var k in obj) {
		str += "obj." + k + " = " + obj[k] + "\n";
	}
	window.alert(str);
};

AjxDebug.prototype._add =
function(aMsg, extraInfo, isXml, isRaw, linkName, showFuncs) {
	var timestamp = new Date();
	if (extraInfo) {
		extraInfo = this._getHtmlForObject(extraInfo, isXml, isRaw, timestamp, showFuncs);
	}

	// Add the message to our stack
    this._addMessage(new DebugMessage(aMsg, null, null, timestamp, extraInfo, linkName));
};

AjxDebug.prototype._addMessage =
function(aMsg) {
	this._msgQueue[this._msgQueue.length] = aMsg;
	this._showMessages();
};

AjxDebug.prototype._showMessages =
function() {
	if (!this._dbgWindowInited) {
		// For now, don't show the messages-- assuming that this case only
		// happens at startup, and many messages will be written
		return;
	}
	try {
		if (this._msgQueue.length > 0) {
			var contentFrame = this.getContentFrame();
			var linkFrame = this.getLinkFrame();
			if (!contentFrame || !linkFrame) { return; }

			var msg;
			var contentFrameDoc = contentFrame.contentWindow.document;
			var linkFrameDoc = linkFrame.contentWindow.document;
			var len = this._msgQueue.length;
			for (var i = 0 ; i < len; ++i ) {
				var now = new Date();
				msg = this._msgQueue[i];
				var linkLabel = msg.linkName;
				var contentLabel = [msg.message, msg.eHtml].join("");
				this._createLinkNContent("Link", linkLabel, "Content", contentLabel);
			}
		}
	
		this._msgQueue.length = 0;
		this._scrollToBottom();
	} catch (ex) {
		//debuggins should not stop execution
	}
};

AjxDebug.prototype._getTimeStamp =
function(date) {
	if (!AjxDebug._timestampFormatter) {
		AjxDebug._timestampFormatter = new AjxDateFormat("HH:mm:ss.SSS");
	}
	date = date || new Date();
	return AjxStringUtil.htmlEncode(AjxDebug._timestampFormatter.format(date), true);
};

AjxDebug.prototype._createLinkNContent =
function(linkClass, linkLabel, contentClass, contentLabel) {
	var linkFrame = this.getLinkFrame();
	if (!linkFrame) { return; }

	var now = new Date();
	var timeStamp = ["[", this._getTimeStamp(now), "]"].join("");
	var id = "Lnk_" + now.getTime();

	// create link
	if (linkLabel) {
		var linkFrameDoc = linkFrame.contentWindow.document;
		var linkEl = linkFrameDoc.createElement("DIV");
		linkEl.className = linkClass;
		linkEl.innerHTML = [linkLabel, timeStamp].join(" - ");
		linkEl._targetId = id;
		linkEl._dbg = this;
		linkEl.onclick = AjxDebug._linkClicked;

		var linkBody = linkFrameDoc.body;
		linkBody.appendChild(linkEl);
	}

	// create content
	var contentFrameDoc = this.getContentFrame().contentWindow.document;
	var contentEl = contentFrameDoc.createElement("DIV");
	contentEl.className = contentClass;
	contentEl.id = id;
	contentEl.innerHTML = contentLabel;

	contentFrameDoc.body.appendChild(contentEl);

	// always show latest
	this._scrollToBottom();
};

AjxDebug._linkClicked =
function() {
	var contentFrame = this._dbg.getContentFrame();
	var el = contentFrame.contentWindow.document.getElementById(this._targetId);
	var y = 0;
	while (el) {
		y += el.offsetTop;
		el = el.offsetParent;
	}

	contentFrame.contentWindow.scrollTo(0, y);
};

AjxDebug.prototype._clear =
function() {
	this.getContentFrame().contentWindow.document.body.innerHTML = "";
	this.getLinkFrame().contentWindow.document.body.innerHTML = "";
};

AjxDebug.prototype._pause =
function() {
	this._isPaused = !this._isPaused;
	this._pauseBtn.innerHTML = this._isPaused ? "Resume" : "Pause";
};

AjxDebug.prototype._unloadHandler =
function() {
	if (!this._debugWindow) { return; } // is there anything to do?

	// detach event handlers
	if (AjxEnv.isIE) {
		this._debugWindow.detachEvent('onunload', this._unloadHandler);
	} else {
		this._debugWindow.onunload = null;
	}
};

/**
 * Simple wrapper for log messages
 */
DebugMessage = function(aMsg, aType, aCategory, aTime, extraHtml, linkName) {
	this.message = aMsg || "";
	this.type = aType || null;
	this.category = aCategory || "";
	this.time = aTime || (new Date().getTime());
	this.eHtml = extraHtml || "";
	this.linkName = linkName;
};
