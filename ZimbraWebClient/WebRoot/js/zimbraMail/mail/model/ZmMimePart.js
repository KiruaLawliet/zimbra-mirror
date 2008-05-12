/*
 * ***** BEGIN LICENSE BLOCK *****
 * 
 * Zimbra Collaboration Suite Web Client
 * Copyright (C) 2004, 2005, 2006, 2007 Zimbra, Inc.
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

ZmMimePart = function() {
	
	ZmModel.call(this, ZmEvent.S_ATT);
	
	this.children = new AjxVector();
	this.node = new Object();
};

ZmMimePart.prototype = new ZmModel;
ZmMimePart.prototype.constructor = ZmMimePart;

ZmMimePart.prototype.toString = 
function() {
	return "ZmMimePart";
};

ZmMimePart.createFromDom =
function(node, args) {
	var mimePart = new ZmMimePart();
	mimePart._loadFromDom(node, args.attachments, args.bodyParts, args.parentNode);
	return mimePart;
};

ZmMimePart.prototype.getContent = 
function() {
	return this.node.content;
};

ZmMimePart.prototype.getContentForType = 
function(contentType) {
	var topChildren = this.children.getArray();

	if (topChildren.length) {
		for (var i = 0; i < topChildren.length; i++) {
			if (topChildren[i].getContentType() == contentType)
				return topChildren[i].getContent();
		}
	} else {
		if (this.getContentType() == contentType)
			return this.getContent();
	}
	return null;
};

ZmMimePart.prototype.setContent = 
function(content) {
	this.node.content = content;
};

ZmMimePart.prototype.getContentDisposition =
function() {
	return this.node.cd;
};

ZmMimePart.prototype.getContentType =
function() {
	return this.node.ct;
};

ZmMimePart.prototype.setContentType =
function(ct) {
	this.node.ct = ct;
};

ZmMimePart.prototype.setIsBody = 
function(isBody) {
	this.node.body = isBody;
};

ZmMimePart.prototype.getFilename =
function() {
	return this.node.filename;
};

ZmMimePart.prototype.isIgnoredPart =
function(parentNode) {
	// bug fix #5889 - if parent node was multipart/appledouble,
	// ignore all application/applefile attachments - YUCK
	if (parentNode && parentNode.ct == ZmMimeTable.MULTI_APPLE_DBL &&
		this.node.ct == ZmMimeTable.APP_APPLE_DOUBLE)
	{
		return true;
	}

	// bug fix #7271 - dont show renderable body parts as attachments anymore
	if (this.node.body &&
		(this.node.ct == ZmMimeTable.TEXT_HTML || this.node.ct == ZmMimeTable.TEXT_PLAIN))
	{
		return true;
	}

	if (this.node.ct == ZmMimeTable.MULTI_DIGEST) {
		return true;
	}

	return false;
};

ZmMimePart.prototype._loadFromDom =
function(partNode, attachments, bodyParts, parentNode) {
	for (var i = 0; i < partNode.length; i++) {
		this.node = partNode[i];

		if (this.node.content)
			this._loaded = true;

		if (this.node.cd == "attachment" || 
			this.node.ct == ZmMimeTable.MSG_RFC822 ||
			this.node.filename != null || 
			this.node.ci != null ||
			this.node.cl != null)
		{
			if (!this.isIgnoredPart(parentNode)) {
				attachments.push(this.node);
			}
		}

		if (this.node.body && ZmMimeTable.isRenderable(this.node.ct)) {
			bodyParts.push(this.node);
		}

		// bug fix #4616 - dont add attachments part of a rfc822 msg part
		if (this.node.mp && this.node.ct != ZmMimeTable.MSG_RFC822) {
			var params = {attachments: attachments, bodyParts: bodyParts, parentNode: this.node};
			this.children.add(ZmMimePart.createFromDom(this.node.mp, params));
		}
	}
};
