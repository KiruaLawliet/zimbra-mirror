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

function ZmApptAssistant(appCtxt) {
	if (arguments.length == 0) return;
	ZmAssistant.call(this, appCtxt);	
};

ZmApptAssistant.prototype = new ZmAssistant();
ZmApptAssistant.prototype.constructor = ZmAssistant;

ZmApptAssistant.prototype.okHandler =
function(dialog) {
	//override
};

/**
 * 
 * (...)                 matched as notes, stripped out
 * [...]                 matched as location, stripped out
 * {date-spec}           first matched pattern is "start date", second is "end date"
 * {time-spec}           first matched pattern is "start time", second is "end time"
 * repat {repeat-spec}   recurrence rule
 * calendar {cal-name}   calendar to add appt to
 * invite {e1,e2,e3}     email addresses to invite (ideally would auto-complete)
 * subject "..."         explicit subject
 * 
 * everything renaming is the subject, unless subject was explicit
 * 
 * example:
 * 
 * lunch 12:30 PM next friday with satish (to discuss future release) [CPK, Palo Alto]
 * 
 * "12:30 PM" matched as a time, saved as "start time" * 
 * "next friday" matches a date, so is stripped out and saved as "start date"
 * (...) matched as notes, stripped out and saved as "notes"
 * [...] matched as location
 * 
 * everything left "lunch with satish" is taken as subject
 * 
 */
ZmApptAssistant.prototype.parse =
function(dialog, verb, args) {
	dialog._setActionField(ZmMsg.newAppt, "NewAppointment");
	dialog._setOkButton(ZmMsg.createNewAppt, true, true, true, "NewAppointment");
	DBG.println("args = "+args);
	var startDate = new Date();
	var endDate = null;
	var match;

//	DBG.println("args = "+args);

	var loc = null;
	match = args.match(/\s*\[([^\]]*)\]?\s*/);	
	if (match) {
		loc = match[1];
		args = args.replace(match[0], " ");
	}

	var notes = null;
	match = args.match(/\s*\(([^)]*)\)?\s*/);
	if (match) {
		notes = match[1];
		args = args.replace(match[0], " ");
	}

	startDate.setMinutes(0);
	var startTime = this._matchTime(args);
	if (startTime) {
		startDate.setHours(startTime.hour, startTime.minute);
		args = startTime.args;
	}

	// look for an end time
	var endTime = this._matchTime(args);
	if (endTime) {
		args = endTime.args;
	}

	// look for start date
	match = this._objectManager.findMatch(args, ZmObjectManager.DATE);
	if (match) {
		args = args.replace(match[0], " ");
		startDate = match.context.date;
		if (startTime) startDate.setHours(startTime.hour, startTime.minute);
	}
	
	// look for end date
	match = this._objectManager.findMatch(args, ZmObjectManager.DATE);
	if (match) {
		args = args.replace(match[0], " ");
		endDate = match.context.date;
		if (endTime != null) endDate.setHours(endTime.hour, endTime.minute);
		else if (startTime != null) endDate.setHours(startTime.hour, startTime.minute);
	} else {
		if (endTime) {
			endDate = new Date(startDate.getTime());
			if (endTime != null) endDate.setHours(endTime.hour, endTime.minute);			
		} else if (startTime) {
			endDate = new Date(startDate.getTime() + 1000 * 60 * 60);
		}
	}
	
	var subject = null;
	match = args.match(/\s*\"([^\"]*)\"?\s*/);
	if (match) {
		subject = match[1];
		args = args.replace(match[0], " ");
	}

	var repeat = null;
	match = args.match(/\s*repeats?\s+(\S+)\s*/);	
	if (match) {
		repeat = match[1];
		args = args.replace(match[0], " ");
	}

	match = args.match(/\s*invite\s+(\S+)\s*/);
	if (match) {
		args = args.replace(match[0], " ");
	}

	if (subject == null) {
		subject = args.replace(/^\s+/, "").replace(/\s+$/, "").replace(/\s+/g, ' ');
	}

	dialog._setOkButton(null, true, subject != null && subject != "");
	
	var subStr = AjxStringUtil.convertToHtml(subject == "" ? "\"enclose subject in quotes or just type\"" : subject);
	var locStr = AjxStringUtil.convertToHtml(loc == null ? "[enclose location in brackets]" : loc);
	var notesStr = AjxStringUtil.convertToHtml(notes == null ? "(enclose notes in parens)" : notes);
	dialog._setField(ZmMsg.subject, subStr, subject == "", false);
	this._setDateFields(dialog, startDate, startTime, endDate, endTime);
	dialog._setField(ZmMsg.location, locStr, loc == null, false);	
	dialog._setField(ZmMsg.notes, notesStr, notes == null, false);
	dialog._setOptField(ZmMsg.repeat, repeat, false, true);
	return;
};

ZmApptAssistant.prototype._setDateFields = 
function(dialog, startDate, startTime, endDate, endTime) {
	var startDateValue = DwtCalendar.getDateFullFormatter().format(startDate);
	var sameDay = false;
	var html = new AjxBuffer();
	html.append("<table border=0 cellpadding=0 cellspacing=0>");
	html.append("<tr>");
	html.append("<td>", AjxStringUtil.htmlEncode(startDateValue), "</td>");
	if (startTime) {
		var startTimeValue = AjxDateUtil.computeTimeString(startDate);
		html.append("<td></td><td>&nbsp;</td><td>@</td><td>&nbsp;</td>");
		html.append("<td>", AjxStringUtil.htmlEncode(startTimeValue), "</td>");
		sameDay = endDate && endDate.getFullYear() == startDate.getFullYear() && 
			endDate.getMonth() == startDate.getMonth() && endDate.getDate() == startDate.getDate();
		if (sameDay) {
			var endTimeValue = AjxDateUtil.computeTimeString(endDate);
			html.append("<td>&nbsp;-&nbsp;</td>");
			html.append("<td>", AjxStringUtil.htmlEncode(endTimeValue), "</td>");
		}
	}
	html.append("</tr></table>");	
	var doEnd = (endDate && !sameDay);
	
	if (doEnd) {
		dialog._clearField(ZmMsg.time);
		dialog._setField(ZmMsg.startTime, html.toString(), false, false, ZmMsg.subject);
		
		html.clear();
		var endDateValue = DwtCalendar.getDateFullFormatter().format(endDate);
			html.append("<table border=0 cellpadding=0 cellspacing=0>");		
		html.append("<tr>");
		html.append("<td>", AjxStringUtil.htmlEncode(endDateValue), "</td>");
		if (startTime) { // display end time if a startTime was specified
			var endTimeValue = AjxDateUtil.computeTimeString(endDate);
			html.append("<td></td><td>&nbsp;</td><td>@</td><td>&nbsp;</td>");
			html.append("<td>", AjxStringUtil.htmlEncode(endTimeValue), "</td>");
		}
		html.append("</tr></table>");
		dialog._setField(ZmMsg.endTime, html.toString(), false, false, ZmMsg.startTime);
		
	} else {
		dialog._setField(ZmMsg.time, html.toString(), false, false, ZmMsg.subject);
		dialog._clearField(ZmMsg.startTime);
		dialog._clearField(ZmMsg.endTime);		
	}
};