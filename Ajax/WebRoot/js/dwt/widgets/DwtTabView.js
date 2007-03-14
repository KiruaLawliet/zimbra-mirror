/*
 * Copyright (C) 2006, The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
* @class
* @constructor
* DwtTabView  - class for the tabbed view
* DwtTabView manages the z-index of the contained tabs. 
* @author Greg Solovyev
**/
function DwtTabView(parent, className, positionStyle) {
	if (arguments.length == 0) return;
	var clsName = className || "ZTabView";
	
	var posStyle = DwtControl.ABSOLUTE_STYLE;
	if ((positionStyle !== void 0) && (positionStyle !== null)){
	    posStyle = positionStyle;
	}
	DwtComposite.call(this, parent, clsName, posStyle);
	this._stateChangeEv = new DwtEvent(true);
	this._tabs = new Array(); 
	this._tabIx = 1;

    this._createHtml();
}

DwtTabView.prototype = new DwtComposite;
DwtTabView.prototype.constructor = DwtTabView;

DwtTabView.prototype.toString = 
function() {
	return "DwtTabView";
}

//
// Constants
//

//Z-index contants for the tabbed view contents are based on Dwt z-index constants
DwtTabView.Z_ACTIVE_TAB = Dwt.Z_VIEW+10;
DwtTabView.Z_HIDDEN_TAB = Dwt.Z_HIDDEN;
DwtTabView.Z_TAB_PANEL = Dwt.Z_VIEW+20;
DwtTabView.Z_CURTAIN = Dwt.Z_CURTAIN;

//
// Data
//

DwtTabView.prototype.TEMPLATE = "ajax.dwt.templates.Widgets#ZTabView";

//
// Public methods
//

DwtTabView.prototype.addStateChangeListener = function(listener) {
	this._eventMgr.addListener(DwtEvent.STATE_CHANGE, listener);
}

DwtTabView.prototype.removeStateChangeListener = function(listener) {
	this._eventMgr.removeListener(DwtEvent.STATE_CHANGE, listener);
}

/**
* @param title  -  text for the tab button
* @param tabView - instance of DwtTabViewPage or an AjxCallback that
*                  returns an instance of DwtTabViewPage
* @return - the key for the added tab. This key can be used to retreive the tab using @link getTab
* public method addTab. Note that this method does not automatically update the tabs panel.
**/
DwtTabView.prototype.addTab =
function (title, tabViewOrCallback) {
	var tabKey = this._tabIx++;	

	// create tab entry
	this._tabs[tabKey] = {
		title: title,
		button: this._tabBar.addButton(tabKey, title)
	};

	//add the page
	this.setTabView(tabKey, tabViewOrCallback);

	//show the first tab
	if(tabKey==1) {
		if (tabViewOrCallback instanceof AjxCallback) {
			tabViewOrCallback = tabViewOrCallback.run(tabKey);
		}
		if(tabViewOrCallback) {
			tabViewOrCallback.showMe();
		}
		this._currentTabKey = tabKey;		
		this.switchToTab(tabKey);
	}
	//hide all the other tabs
	else if (tabViewOrCallback && !(tabViewOrCallback instanceof AjxCallback)) {
		tabViewOrCallback.hideMe();
		Dwt.setVisible(tabViewOrCallback.getHtmlElement(), false);
	}
	
	this._tabBar.addSelectionListener(tabKey, new AjxListener(this, DwtTabView.prototype._tabButtonListener));	
	
	return tabKey;
}

DwtTabView.prototype.getCurrentTab = function() {
	return this._currentTabKey;
}

DwtTabView.prototype.getNumTabs =
function() {
	return (this._tabs.length - 1);
};

/**
* @param tabKey  -  key for the tab, returned from @link addTab
* @return - the view tab (DwtTabViewpage) 
**/
DwtTabView.prototype.getTab =
function (tabKey) {
	if(this._tabs && this._tabs[tabKey])
		return this._tabs[tabKey];
	else
		return null;
}

DwtTabView.prototype.getTabTitle =
function(tabKey) {
	return this._tabs && this._tabs[tabKey] ? this._tabs[tabKey]["title"] : null;
};
DwtTabView.prototype.getTabButton =
function(tabKey) {
	return this._tabs && this._tabs[tabKey] ? this._tabs[tabKey]["button"] : null;
};

DwtTabView.prototype.setTabView =
function(tabKey, tabView) {
	var tab = this.getTab(tabKey);
	tab.view = tabView;
	if (tabView && !(tabView instanceof AjxCallback)) {
		this._pageEl.appendChild(tabView.getHtmlElement());
		tabView._tabKey = tabKey;
	}
};
DwtTabView.prototype.getTabView =
function(tabKey) {
	var tab = this.getTab(tabKey);
	var tabView = tab && tab.view;
	if (tabView instanceof AjxCallback) {
		var callback = tabView;
		tabView = callback.run(tabKey);
		this.setTabView(tabKey, tabView);
		var size = this._getTabSize();
		tabView.setSize(size.x, size.y);
	}
	return tabView;
};

DwtTabView.prototype.switchToTab = 
function(tabKey) {
	if(this._tabs && this._tabs[tabKey]) {
		this._showTab(tabKey);
		this._tabBar.openTab(tabKey);
	}
	if (this._eventMgr.isListenerRegistered(DwtEvent.STATE_CHANGE)) {
		this._eventMgr.notifyListeners(DwtEvent.STATE_CHANGE, this._stateChangeEv);
	}
}

DwtTabView.prototype.setBounds =
function(x, y, width, height) {
	DwtComposite.prototype.setBounds.call(this, x, y, width, height);
	this._resetTabSizes(width, height);
}

DwtTabView.prototype.getActiveView =
function() {
	return this._tabs[this._currentTabKey].view;
}

DwtTabView.prototype.getKeyMapName =
function() {
	return "DwtTabView";
};

DwtTabView.prototype.resetKeyBindings =
function() {
	var kbm = this.shell.getKeyboardMgr();
	if (kbm.isEnabled()) {
		var kmm = kbm.__keyMapMgr;
		var num = this.getNumTabs();
		var seqs = kmm.getKeySequences("DwtTabView", "GoToTab");
		for (var k = 0; k < seqs.length; k++) {
			var ks = seqs[k];
			for (var i = 1; i <= num; i++) {
				var newKs = ks.replace(/NNN/, i);
				kmm.setMapping("DwtTabView", newKs, "GoToTab" + i);
			}
		}
		kmm.reloadMap("DwtTabView");
	}
};

DwtTabView.prototype.handleKeyAction =
function(actionCode) {
	DBG.println(AjxDebug.DBG3, "DwtTabView.handleKeyAction");

	switch (actionCode) {

		case DwtKeyMap.NEXT_TAB:
			var curTab = this.getCurrentTab();
			if (curTab < this.getNumTabs()) {
				this.switchToTab(curTab + 1);
			}
			break;
			
		case DwtKeyMap.PREV_TAB:
			var curTab = this.getCurrentTab();
			if (curTab > 1) {
				this.switchToTab(curTab - 1);
			}
			break;
		
		default:
			// Handle action code like "GoToTab3"
			var m = actionCode.match(DwtKeyMap.GOTO_TAB_RE);
			if (m && m.length) {
				var idx = m[1];
				if ((idx <= this.getNumTabs()) && (idx != this.getCurrentTab())) {
					this.switchToTab(idx);
				}
			} else {
				return false;
			}
	}
	return true;
};

//
// Protected methods
//

DwtTabView.prototype._resetTabSizes =
function (width, height) {
	if(this._tabs && this._tabs.length) {
		for(var curTabKey in this._tabs) {
			var tabView = this._tabs[curTabKey].view;
			if(tabView && !(tabView instanceof AjxCallback)) {
				tabView.resetSize(width, height);
			}	
		}
	}		
};

DwtTabView.prototype._getTabSize = function() {
	var size = this.getSize();
	var width = size.x || this.getHtmlElement().clientWidth;
	var height = size.y || this.getHtmlElement().clientHeight;

	var tabBarSize = this._tabBar.getSize();
	var tabBarHeight = tabBarSize.y || this._tabBar.getHtmlElement().clientHeight;

	var tabWidth = width;
	var tabHeight = height - tabBarHeight;

	return new DwtPoint(tabWidth, tabHeight);
};

DwtTabView.prototype._createHtml = function(templateId) {
    var data = { id: this._htmlElId };
    this._createHtmlFromTemplate(templateId || this.TEMPLATE, data);
};

DwtTabView.prototype._createHtmlFromTemplate = function(templateId, data) {
    DwtComposite.prototype._createHtmlFromTemplate.call(this, templateId, data);
    this._tabBarEl = document.getElementById(data.id+"_tabbar");
    this._tabBar = new DwtTabBar(this);
    this._tabBar.reparentHtmlElement(this._tabBarEl);
    this._pageEl = document.getElementById(data.id+"_page");
};

DwtTabView.prototype._showTab = 
function(tabKey) {
	if(this._tabs && this._tabs[tabKey]) {
		this._currentTabKey = tabKey;
		//hide all the tabs
		this._hideAllTabs();
		//make this tab visible
		var tabView = this.getTabView(tabKey);
        if (tabView) {
            this.applyCaretHack();
            Dwt.setVisible(tabView.getHtmlElement(), true);
            tabView.showMe();
        }
    }
}

DwtTabView.prototype._hideAllTabs = 
function() {
	if(this._tabs && this._tabs.length) {
		for(var curTabKey in this._tabs) {
			var tabView = this._tabs[curTabKey].view;
			if(tabView && !(tabView instanceof AjxCallback)) {
				tabView.hideMe();
				//this._tabs[curTabKey]["view"].setZIndex(DwtTabView.Z_HIDDEN_TAB);
				Dwt.setVisible(tabView.getHtmlElement(), false);
			}	
		}
	}
}

DwtTabView.prototype._tabButtonListener = 
function (ev) {
    this.switchToTab(ev.item.getData("tabKey"));
};

//
// Class
//

/**
* @class
* @constructor
* DwtTabViewPage abstract class for a page in a tabbed view
* tab pages are responsible for creating there own HTML and populating/collecting 
* data to/from any form fields that they display
**/
function DwtTabViewPage(parent, className, posStyle) {
	if (arguments.length == 0) return;
	var clsName = className || "ZTabPage";
	var ps = posStyle || DwtControl.ABSOLUTE_STYLE;
	this._rendered = true; //by default UI creation is not lazy
	DwtPropertyPage.call(this, parent, clsName, ps);
    this._createHtml();
}

DwtTabViewPage.prototype = new DwtPropertyPage;
DwtTabViewPage.prototype.constructor = DwtTabViewPage;

DwtTabViewPage.prototype.toString = 
function() {
	return "DwtTabViewPage";
}

// Data

DwtTabViewPage.prototype.TEMPLATE = "ajax.dwt.templates.Widgets#ZTabPage";

// Public methods

DwtTabViewPage.prototype.showMe =
function() {
	this.setZIndex(DwtTabView.Z_ACTIVE_TAB);
	if(this.parent.getHtmlElement().offsetHeight > 80) { //if the parent is visible use offsetHeight
		this._contentEl.style.height=this.parent.getHtmlElement().offsetHeight-80;
	} else {
		//if the parent is not visible yet, then resize the page to fit the parent
		var parentHeight = parseInt(this.parent.getHtmlElement().style.height);
		var units = AjxStringUtil.getUnitsFromSizeString(this.parent.getHtmlElement().style.height);
		if(parentHeight > 80) {
			this._contentEl.style.height = (Number(parentHeight-80).toString() + units);
		}
	}
	if(this.parent.getHtmlElement().offsetWidth > 0) //if the parent is visible use offsetWidth
		this._contentEl.style.width=this.parent.getHtmlElement().offsetWidth;
	else {
		//if the parent is not visible yet, then resize the page to fit the parent
		this._contentEl.style.width = this.parent.getHtmlElement().style.width;
	}
}

DwtTabViewPage.prototype.hideMe = 
function() {
	this.setZIndex(DwtTabView.Z_HIDDEN_TAB);
}


DwtTabViewPage.prototype.resetSize =
function(newWidth, newHeight) {
	if(this._rendered) {
		this.setSize(newWidth, newHeight);
	}
}

// Protected methods

DwtTabViewPage.prototype._createHtml = function(templateId) {
    var data = { id: this._htmlElId };
    this._createHtmlFromTemplate(templateId || this.TEMPLATE, data);
};

DwtTabViewPage.prototype._createHtmlFromTemplate = function(templateId, data) {
    DwtPropertyPage.prototype._createHtmlFromTemplate.call(this, templateId, data);
    this._contentEl = document.getElementById(data.id+"_content") || this.getHtmlElement();
};

//
// Class
//

/**
* @class
* @constructor
* @param parent
* DwtTabBar 
**/
function DwtTabBar(parent, tabCssClass, btnCssClass) {
	if (arguments.length == 0) return;
	//var _className = className || "DwtTabBar";
	this._buttons = new Array();
	this._btnStyle = btnCssClass ? btnCssClass : "ZTab"; // REVISIT: not used
	this._btnImage = null;
	this._currentTabKey = 1;
	var myClass = tabCssClass ? tabCssClass : "ZTabBar";

	DwtToolBar.call(this, parent, myClass, DwtControl.STATIC_STYLE);
}

DwtTabBar.prototype = new DwtToolBar;
DwtTabBar.prototype.constructor = DwtTabBar;

DwtTabBar.prototype.toString =
function() {
	return "DwtTabBar";
}

// Data

DwtTabBar.prototype.TEMPLATE = "ajax.dwt.templates.Widgets#ZTabBar";

// Public methods

DwtTabBar.prototype.getCurrentTab =
function() {
	return this._currentTabKey;
}

DwtTabBar.prototype.addStateChangeListener =
function(listener) {
	this._eventMgr.addListener(DwtEvent.STATE_CHANGE, listener);
}

DwtTabBar.prototype.removeStateChangeListener = 
function(listener) {
	this._eventMgr.removeListener(DwtEvent.STATE_CHANGE, listener);
}

/**
* @param tabId - the id used to create tab button in @link DwtTabBar.addButton method
* @param listener - AjxListener
**/
DwtTabBar.prototype.addSelectionListener =
function(tabKey, listener) {
	this._buttons[tabKey].addSelectionListener(listener);
}

/**
* @param tabId - the id used to create tab button in @link DwtTabBar.addButton method
* @param listener - AjxListener
**/
DwtTabBar.prototype.removeSelectionListener =
function(tabKey, listener) {
	this._buttons[tabKey].removeSelectionListener(listener);
}

/**
* @param tabKey
* @param tabTitle
**/
DwtTabBar.prototype.addButton =
function(tabKey, tabTitle) {
	var b = this._buttons[tabKey] = new DwtTabButton(this);
	
	this._buttons[tabKey].addSelectionListener(new AjxListener(this, DwtTabBar._setActiveTab));

	if (this._btnImage != null)
		b.setImage(this._btnImage);

	if (tabTitle != null)
		b.setText(tabTitle);

	b.setEnabled(true);
	b.setData("tabKey", tabKey);

	if(parseInt(tabKey) == 1)
		b.setOpen();

	return b;
}

/**
* @param tabKey
**/
DwtTabBar.prototype.getButton = 
function (tabKey) {
	if(this._buttons[tabKey])
		return this._buttons[tabKey];
	else 
		return null;
}

DwtTabBar.prototype.openTab = 
function(tabK) {
	this._currentTabKey = tabK;
    var cnt = this._buttons.length;

    for(var ix = 0; ix < cnt; ix ++) {
		if(ix==tabK) continue;

		if(this._buttons[ix])
	    	this._buttons[ix].setClosed();
    }

    if(this._buttons[tabK])
		this._buttons[tabK].setOpen();

    var nextK = parseInt(tabK) + 1;
	if (this._eventMgr.isListenerRegistered(DwtEvent.STATE_CHANGE))
		this._eventMgr.notifyListeners(DwtEvent.STATE_CHANGE, this._stateChangeEv);
}

/**
* Greg Solovyev 1/4/2005 
* changed ev.target.offsetParent.offsetParent to
* lookup for the table up the elements stack, because the mouse down event may come from the img elements 
* as well as from the td elements.
**/
DwtTabBar._setActiveTab =
function(ev) {
    var tabK = null;
    if(ev && ev.item) {
		tabK=ev.item.getData("tabKey");
    } else if (ev && ev.target) {
		var elem = ev.target;
	    while(elem.tagName != "TABLE" && elem.offsetParent )
	    	elem = elem.offsetParent;

		tabK = elem.getAttribute("tabKey");
		if (tabK == null)
			return false;
    } else {
		return false;
    }
    this.openTab(tabK);
};

//
// Class
//

/**
* @class
* @constructor
**/
function DwtTabButton(parent) {
	DwtButton.call(this, parent, "ZTab");
}

DwtTabButton.prototype = new DwtButton;
DwtTabButton.prototype.constructor = DwtTabButton;

DwtTabButton.prototype.toString = 
function() {
	return "DwtTabButton";
}

// Data

DwtTabButton.prototype.TEMPLATE = "ajax.dwt.templates.Widgets#ZTab";

// Public methods

/**
* Changes the visual appearance to active tab and sets _isClosed to false
**/
DwtTabButton.prototype.setOpen = 
function() {
    this._isSelected = true;
    this.setDisplayState(DwtControl.SELECTED);
};

/**
* Changes the visual appearance to inactive tab and sets _isClosed to true
**/
DwtTabButton.prototype.setClosed = 
function() {
    this._isSelected = false;
    this.setDisplayState(DwtControl.NORMAL);
};

DwtTabButton.prototype.setDisplayState = function(state) {
    if (this._isSelected && state != DwtControl.SELECTED) {
        state = [ DwtControl.SELECTED, state ].join(" ");
    }
    DwtButton.prototype.setDisplayState.call(this, state);
};
