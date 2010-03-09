/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Web Client
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Zimbra Public License
 * Version 1.3 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * ***** END LICENSE BLOCK *****
 */

ZaSearchField = function(parent, className, size, posStyle) {

	DwtComposite.call(this, parent, className, posStyle);
	this._containedObject = new ZaSearch();
	this._initForm(ZaSearch.myXModel,this._getMyXForm());
	this._localXForm.setInstance(this._containedObject);
	this._app = ZaApp.getInstance();
}

ZaSearchField.prototype = new DwtComposite;
ZaSearchField.prototype.constructor = ZaSearchField;

ZaSearchField.prototype.toString = 
function() {
	return "ZaSearchField";
}

ZaSearchField.UNICODE_CHAR_RE = /\S/;

ZaSearchField.prototype.registerCallback =
function(callbackFunc, obj) {
	this._callbackFunc = callbackFunc;
	this._callbackObj = obj;
}

ZaSearchField.prototype.setObject = 
function (searchObj) {
	this._containedObject = searchObj;
	this._localXForm.setInstance(this._containedObject);
}

ZaSearchField.prototype.getObject = 
function() {
	return this._containedObject;
}


ZaSearchField.prototype.invokeCallback =
function() {
	var query = this._containedObject[ZaSearch.A_query] = this._localXForm.getItemsById(ZaSearch.A_query)[0].getElement().value;

	if (query.indexOf("$set:") == 0) {
		ZaApp.getInstance().getAppCtxt().getClientCmdHdlr().execute((query.substr(5)).split(" "));
		return;
	}
		
	var params = {};
	var sb_controller = ZaApp.getInstance().getSearchBuilderController();
	var isAdvanced = sb_controller.isAdvancedSearch (query) ;
	var searchListController = ZaApp.getInstance().getSearchListController() ;
	searchListController._isAdvancedSearch = isAdvanced ;
	
	params.types = this.getSearchTypes();
	
	if (isAdvanced) {
		DBG.println(AjxDebug.DBG1, "Advanced Search ... " ) ;
		//Use the text in the search field to do a search
		//params.query = sb_controller.getQuery ();
		params.query = query;
		DBG.println(AjxDebug.DBG1, "Query = " + params.query) ;
		//params.types = sb_controller.getAddressTypes ();
	}else {
		DBG.println(AjxDebug.DBG1, "Basic Search ....") ;
		searchListController._searchFieldInput = query ;
		params.query = ZaSearch.getSearchByNameQuery(query, params.types);      
	}
	
	//set the currentController's _currentQuery
	
	ZaApp.getInstance().getSearchListController()._currentQuery = params.query ;
	searchListController._currentQuery = params.query ;
	
	this._isSearchButtonClicked = false ;
	
	if (this._callbackFunc != null) {
		if (this._callbackObj != null) {
			//this._callbackFunc.call(this._callbackObj, this, params);
			ZaApp.getInstance().getCurrentController().switchToNextView(this._callbackObj,
		 this._callbackFunc, params);
		} else {
			ZaApp.getInstance().getCurrentController().switchToNextView(ZaApp.getInstance().getSearchListController(), this._callbackFunc, params);
//			this._callbackFunc(this, params);
		}
	}
}

ZaSearchField.prototype.getSearchTypes =
function () {
		var sb_controller = ZaApp.getInstance().getSearchBuilderController();
		var query = this._localXForm.getItemsById(ZaSearch.A_query)[0].getElement().value ;
		var isAdvancedSearch = sb_controller.isAdvancedSearch (query) ;
		
		var objList = new Array();
		if (isAdvancedSearch) {
			objList = sb_controller.getAddressTypes();
		}else{
			if(this._containedObject[ZaSearch.A_fAccounts] == "TRUE") {
				objList.push(ZaSearch.ACCOUNTS);
			}
			if(this._containedObject[ZaSearch.A_fAliases] == "TRUE") {
				objList.push(ZaSearch.ALIASES);
			}
			if(this._containedObject[ZaSearch.A_fdistributionlists] == "TRUE") {
				objList.push(ZaSearch.DLS);
			}

            if (ZaSettings.ENABLED_UI_COMPONENTS[ZaSettings.RESOURCE_LIST_VIEW] || ZaSettings.ENABLED_UI_COMPONENTS[ZaSettings.CARTE_BLANCHE_UI]) { 
                if(this._containedObject[ZaSearch.A_fResources] == "TRUE") {
                    objList.push(ZaSearch.RESOURCES);
                }
            }
            if (ZaSettings.ENABLED_UI_COMPONENTS[ZaSettings.DOMAIN_LIST_VIEW] || ZaSettings.ENABLED_UI_COMPONENTS[ZaSettings.CARTE_BLANCHE_UI]) { 
				if(this._containedObject[ZaSearch.A_fDomains] == "TRUE") {
					objList.push(ZaSearch.DOMAINS);
				}	
			}
		}
		
		return objList;
}

ZaSearchField.srchButtonHndlr = 
function(evt) {	
	var fieldObj = this.getForm().parent;
	//reset the search list toolbar parameters

	
	var currentController = ZaApp.getInstance().getCurrentController ();
	if (currentController && currentController.setPageNum) {
		currentController.setPageNum (1) ;		
	}
		
	//fieldObj._isSearchButtonClicked = true ; //to Distinguish the action from the overveiw tree items
	fieldObj.invokeCallback(evt);
}

ZaSearchField.helpSrchButtonHndlr =
function (evt) {
	var helpQuery = this.getForm().getItemsById(ZaSearch.A_query)[0].getElement().value ;
	if (helpQuery && helpQuery.length > 0){
			var url = "http://support.zimbra.com/help/index.php"
			var args = [];
			args.push("query=" + helpQuery) ;
			if (typeof (ZaLicense) == typeof (_UNDEFINED_)) { //FOSS version
				args.push("FOSS=1") ;
			}
			
			if (ZaServerVersionInfo.version) {
				args.push("version=" + ZaServerVersionInfo.version ) ;
			}
			
			url = url + "?" + AjxStringUtil.urlEncode(args.join("&"));
			window.open(url, "_blank");
	}
}

ZaSearchField.saveSrchButtonHndlr =
function (evt) {
	var form =this.getForm() ;
	var searchField = form.parent ;
	var query = form.getItemsById(ZaSearch.A_query)[0].getElement().value ;
	if (AjxEnv.hasFirebug) {
		console.log("Save current query: " + query) ;
		//console.log("Current Search types = " + searchField.getSearchTypes()) ;
	}
	if (query && query.length > 0) {
		searchField.getSaveAndEditSeachDialog().show(null, query) ;
	}
}

ZaSearchField.prototype.getSaveAndEditSeachDialog =
function() {
	if (!this._savedAndEditSearchDialog) {
			this._savedAndEditSearchDialog = 
					new ZaSaveSearchDialog (this) ;
	}
	
	return this._savedAndEditSearchDialog ;
}

ZaSearchField.prototype.showSavedSearchButtonHndlr =
function (evt) {
	if (AjxEnv.hasFirebug) console.log("Show saved Searches") ;
	var searchField = this.getForm().parent ;
	searchField.showSavedSearchMenus() ;
}

ZaSearchField.prototype.showSavedSearchMenus =
function () {
	//if (this._savedSearchMenu) this._savedSearchMenu.popdown(); //force popdown
	
	if (this._savedSearchMenu && this._savedSearchMenu.isPoppedUp()) {
		return ;
	}
	if (ZaSearch.SAVED_SEARCHES.length <= 0 || ZaSearch._savedSearchToBeUpdated) {
		var callback = new AjxCallback (this, this.popupSavedSearch) ;
		ZaSearch.getSavedSearches(null, callback); //TODO, we may want to provide the autocomplete feature to return the saved results when user is typing
	}else{
		this.popupSavedSearch(null);
	}
}

ZaSearchField.prototype.popupSavedSearch =
function (resp, searchName) {
	if (AjxEnv.hasFirebug) console.debug("popup saved searches ...") ;
	
	if (resp){
		ZaSearch.updateSavedSearch (resp);
	}
	
	if (ZaSearch.SAVED_SEARCHES.length <=0) {
		if (this._savedSearchMenu) this._savedSearchMenu.popdown() ; //force popdown if the saved-search is 0
		return ;
	}	
	
	this._queryFieldElement = this._localXForm.getItemsById(ZaSearch.A_query)[0].getElement(); 
	var b = Dwt.getBounds(this._queryFieldElement);
	
	/*
	if (!this._savedSearchMenu || resp != null) {
		this._savedSearchMenu = new DwtMenu(this);
		
		//add the menu items
		for (var i=0; i < ZaSearch.SAVED_SEARCHES.length; i ++) {
			var n = ZaSearch.SAVED_SEARCHES[i].name ;
			var q = ZaSearch.SAVED_SEARCHES[i].query ;
			var mItem =  new DwtMenuItem (this._savedSearchMenu) ;
			mItem.setText(n + " .......... " + q) ;
			mItem.setSize(b.width) ;
			mItem.addSelectionListener(new AjxListener(this, ZaSearchField.prototype.selectSavedSearch, [n, q]));
			mItem.addListener(DwtEvent.ONMOUSEUP, new AjxListener(this, this._savedSearchItemMouseUpListener, [n, q] ));
		}
	}*/
	this.getSavedSearchMenu().popup(0, b.x, b.y + b.height);
	//this._savedSearchMenu.setBounds( b.x, b.y + b.height, b.width);
	
}

ZaSearchField.prototype.getSearchFieldElement =
function () {
	return this._localXForm.getItemsById(ZaSearch.A_query)[0].getElement(); 
}

ZaSearchField.prototype.selectSavedSearch =
function (name, query, event){
	if (AjxEnv.hasFirebug) console.debug("Item " + name + " is selected - " + query);
	this.getSearchFieldElement().value = ZaSearch.parseSavedSearchQuery(query) ;
	this.invokeCallback() ; //do the real search call (simulate the search button click)
}

ZaSearchField.prototype.getSavedSearchActionMenu =
function () {
	if (!this._savedSearchActionMenu) {
		this._popupOperations = [];
		this._popupOperations[ZaOperation.EDIT] = new ZaOperation(ZaOperation.EDIT, ZaMsg.TBB_Edit, ZaMsg.ACTBB_Edit_tt, "Properties", "PropertiesDis", 
				new AjxListener(this, this._editSavedSearchListener));
		this._popupOperations[ZaOperation.DELETE] = new ZaOperation(ZaOperation.DELETE, ZaMsg.TBB_Delete, ZaMsg.ACTBB_Delete_tt, "Delete", "DeleteDis", 
				new AjxListener(this, this._deleteSavedSearchListener));
		this._savedSearchActionMenu = 
			new ZaPopupMenu(this, "ActionMenu", null, this._popupOperations);
	}
	
	return this._savedSearchActionMenu ;
}

ZaSearchField.prototype._savedSearchItemMouseUpListener =
function(name, query, ev) {
	this.getSavedSearchActionMenu().popdown();
	if (ev.button == DwtMouseEvent.RIGHT){
		if (AjxEnv.hasFirebug) console.debug("Right Button of Mouse Up: Item " + name + " is selected - " + query);
		
		this._currentSavedSearch = {name: name, query: query};
		if (AjxEnv.hasFirebug) console.debug("Saved Search Menu ZIndex = " + this._savedSearchMenu.getZIndex());
		this.getSavedSearchActionMenu().popup(0, ev.docX, ev.docY);
		this.getSavedSearchActionMenu().setZIndex(this._savedSearchMenu.getZIndex() + 1) ;
		if (AjxEnv.hasFirebug) console.debug("Saved Search Action Menu ZIndex = " + this.getSavedSearchActionMenu().getZIndex());
	}
}

ZaSearchField.prototype._editSavedSearchListener =
function (ev) {
	if (AjxEnv.hasFirebug) console.debug("Edit a saved search item");
	this._savedSearchActionMenu.popdown();
	this.getSaveAndEditSeachDialog().show(this._currentSavedSearch.name, this._currentSavedSearch.query);
}

ZaSearchField.prototype._deleteSavedSearchListener =
function (ev) {
	if (AjxEnv.hasFirebug) console.debug("Delete a saved search item");
	this._savedSearchActionMenu.popdown();
	ZaSearch._savedSearchToBeUpdated = true ;
	var callback = new AjxCallback (this, this.modifySavedSearchCallback) ;
	
	/*
	if (this._savedSearchMenu && this._savedSearchMenu.isPoppedUp()) {
		callback = new AjxCallback (this, this.showSavedSearchMenus) ;
	}else{
		var overviewPanelCtrl = ZaApp.getInstance().getOverviewPanelController() ;
		callback = new AjxCallback (overviewPanelCtrl. overviewPanelCtrl.updateSavedSearchTreeList ()) ;
	}*/
	ZaSearch.modifySavedSearches(	
		[{name: this._currentSavedSearch.name, query: null}], callback ) ;
	//this.getSaveAndEditSeachDialog().show(this._currentSavedSearch.name, this._currentSavedSearch.query);
}

ZaSearchField.prototype.modifySavedSearchCallback =
function () {
	//update the ZaSearch.SAVED_SEARCH
	ZaSearch.updateSavedSearch (ZaSearch.getSavedSearches()); 
	
	//Update the Search Tree
	var overviewPanelCtrl = ZaApp.getInstance()._appCtxt.getAppController().getOverviewPanelController() ;
	overviewPanelCtrl.updateSavedSearchTreeList() ;
	
	//Update the SavedSearchMenu
	this.updateSavedSearchMenu() ;
}

ZaSearchField.prototype.getSavedSearchMenu =
function (refresh) {
	if (!this._savedSearchMenu  || refresh) {
		this.updateSavedSearchMenu();
	}
	return this._savedSearchMenu ;
}

ZaSearchField.prototype.updateSavedSearchMenu =
function () {
	
	var isPoppedUp = false ;
	this._queryFieldElement = this._localXForm.getItemsById(ZaSearch.A_query)[0].getElement(); 
	var b = Dwt.getBounds(this._queryFieldElement);
	
	if (this._savedSearchMenu) {
		isPopup = this._savedSearchMenu.isPoppedUp();
		this._savedSearchMenu.popdown() ;
		this._savedSearchMenu.dispose();	
	}
	
	this._savedSearchMenu = new DwtMenu(this);
	
	//add the menu items
	for (var i=0; i < ZaSearch.SAVED_SEARCHES.length; i ++) {
		var n = ZaSearch.SAVED_SEARCHES[i].name ;
		var q = ZaSearch.SAVED_SEARCHES[i].query ;
		var mItem =  new DwtMenuItem (this._savedSearchMenu) ;
		mItem.setText(n + " .......... " + q) ;
		mItem.setSize(b.width) ;
		mItem.addSelectionListener(new AjxListener(this, ZaSearchField.prototype.selectSavedSearch, [n, q]));
		mItem.addListener(DwtEvent.ONMOUSEUP, new AjxListener(this, this._savedSearchItemMouseUpListener, [n, q] ));
		//set the overflow style to hidden
		mItem.getHtmlElement().style.overflow = "hidden";
	}
	
	if (isPoppedUp) this.popupSavedSearch();
}

//only show or hide the advanced search options
ZaSearchField.advancedButtonHndlr =
function (evt) {
	//DBG.println(AjxDebug.DBG1, "Advanced Button Clicked ...") ;
	var form = this.getForm() ;

	var sb_controller = ZaApp.getInstance().getSearchBuilderController ();
	sb_controller.toggleVisible ();
	ZaApp.getInstance()._appViewMgr.showSearchBuilder (sb_controller.isSBVisible());
	
	if (sb_controller.isSBVisible()) {
		this.widget.setToolTipContent(ZaMsg.tt_advanced_search_close);
	}else{
		this.widget.setToolTipContent (ZaMsg.tt_advanced_search_open) ;
	}
	//clear the search field
	sb_controller.setQuery ();
}

ZaSearchField.prototype.getItemByName =
function (name) {
	var items = this._localXForm.getItems()[0].getItems();
	var cnt = items.length ;
	for (var i=0; i < cnt; i++){
		if (items[i].getName () == name ) 
			return items[i];	
	}
	
	return null ;
}

ZaSearchField.prototype.setTooltipForSearchBuildButton =
function (tooltip){
	//change the tooltip for the search build button
	var searchBuildButtonItem = this.getItemByName("searchBuildButton") ;
	if (searchBuildButtonItem) {
		searchBuildButtonItem.getWidget().setToolTipContent (tooltip);
	}
}

ZaSearchField.prototype.setTooltipForSearchButton =
function (tooltip){
	//change the tooltip for the search button
	var searchButtonItem = this.getItemByName("searchButton") ;
	if (searchButtonItem) {
		searchButtonItem.getWidget().setToolTipContent (tooltip);
	}
}


ZaSearchField.prototype.setIconForSearchMenuButton =
function (imageName){
	//change the tooltip for the search button
	var searchMenuButtonItem = this.getItemByName("searchMenuButton") ;
	if (searchMenuButtonItem) {
		searchMenuButtonItem.getWidget().setImage (imageName);
	}
}

ZaSearchField.prototype.resetSearchFilter = function () {
	this._containedObject[ZaSearch.A_fAccounts] = "FALSE";
	this._containedObject[ZaSearch.A_fdistributionlists] = "FALSE";	
	this._containedObject[ZaSearch.A_fAliases] = "FALSE";
	this._containedObject[ZaSearch.A_fResources] = "FALSE";
	this._containedObject[ZaSearch.A_fDomains] = "FALSE";		
}

ZaSearchField.prototype.allFilterSelected = function (ev) {
	ev.item.parent.parent.setImage(ev.item.getImage());
	this._containedObject[ZaSearch.A_fAccounts] = "TRUE";
	this._containedObject[ZaSearch.A_fdistributionlists] = "TRUE";	
	this._containedObject[ZaSearch.A_fAliases] = "TRUE";
	this._containedObject[ZaSearch.A_fResources] = "TRUE";
	//if(ZaSettings.DOMAINS_ENABLED) {
	this._containedObject[ZaSearch.A_fDomains] = "TRUE";	
	//}
	this.setTooltipForSearchButton (ZaMsg.searchForAll);	
}

ZaSearchField.prototype.accFilterSelected = function (ev) {
	this.resetSearchFilter();
	//ev.item.parent.parent.setImage(ev.item.getImage());	
	this.setIconForSearchMenuButton ("Account");
	this._containedObject[ZaSearch.A_fAccounts] = "TRUE";
	this.setTooltipForSearchButton (ZaMsg.searchForAccounts);	
}

ZaSearchField.prototype.aliasFilterSelected = function (ev) {
	this.resetSearchFilter();
	//ev.item.parent.parent.setImage(ev.item.getImage());
	this.setIconForSearchMenuButton ("AccountAlias");
	this._containedObject[ZaSearch.A_fAliases] = "TRUE";	
	this.setTooltipForSearchButton (ZaMsg.searchForAliases);
}

ZaSearchField.prototype.dlFilterSelected = function (ev) {
	this.resetSearchFilter();
	//ev.item.parent.parent.setImage(ev.item.getImage());
	this.setIconForSearchMenuButton ("DistributionList");
	this._containedObject[ZaSearch.A_fdistributionlists] = "TRUE";	
	this.setTooltipForSearchButton (ZaMsg.searchForDLs);	
}

ZaSearchField.prototype.resFilterSelected = function (ev) {
	this.resetSearchFilter();
	//ev.item.parent.parent.setImage(ev.item.getImage());
	this.setIconForSearchMenuButton ("Resource");
	this._containedObject[ZaSearch.A_fResources] = "TRUE";
	this.setTooltipForSearchButton (ZaMsg.searchForResources);	
}

ZaSearchField.prototype.domainFilterSelected = function (ev) {
	//if(ZaSettings.DOMAINS_ENABLED) {
		this.resetSearchFilter();
		//ev.item.parent.parent.setImage(ev.item.getImage());
		this.setIconForSearchMenuButton ("Domain");
		this._containedObject[ZaSearch.A_fDomains] = "TRUE";
		this.setTooltipForSearchButton (ZaMsg.searchForDomains);	
	//}
}

ZaSearchField.searchChoices = new XFormChoices([],XFormChoices.OBJECT_REFERENCE_LIST, null, "labelId");
ZaSearchField.prototype._getMyXForm = function() {	
	var newMenuOpList = new Array();

	newMenuOpList.push(new ZaOperation(ZaOperation.SEARCH_ACCOUNTS, ZaMsg.SearchFilter_Accounts, ZaMsg.searchForAccounts, "Account", "AccountDis", new AjxListener(this,this.accFilterSelected)));	
	newMenuOpList.push(new ZaOperation(ZaOperation.SEARCH_DLS, ZaMsg.SearchFilter_DLs, ZaMsg.searchForDLs, "DistributionList", "DistributionListDis", new AjxListener(this,this.dlFilterSelected)));		
	newMenuOpList.push(new ZaOperation(ZaOperation.SEARCH_ALIASES, ZaMsg.SearchFilter_Aliases, ZaMsg.searchForAliases, "AccountAlias", "AccountAlias", new AjxListener(this, this.aliasFilterSelected)));		
	newMenuOpList.push(new ZaOperation(ZaOperation.SEARCH_RESOURCES, ZaMsg.SearchFilter_Resources, ZaMsg.searchForResources, "Resource", "ResourceDis", new AjxListener(this, this.resFilterSelected)));		
	//if(ZaSettings.DOMAINS_ENABLED) {
		newMenuOpList.push(new ZaOperation(ZaOperation.SEARCH_DOMAINS, ZaMsg.SearchFilter_Domains, ZaMsg.searchForDomains, "Domain", "DomainDis", new AjxListener(this, this.domainFilterSelected)));			
	//}
	newMenuOpList.push(new ZaOperation(ZaOperation.SEP));				
	newMenuOpList.push(new ZaOperation(ZaOperation.SEARCH_ALL, ZaMsg.SearchFilter_All, ZaMsg.searchForAll, "SearchAll", "SearchAll", new AjxListener(this, this.allFilterSelected)));		
	ZaSearchField.searchChoices.setChoices(newMenuOpList);
	
	var numCols = 4;
	var colSizes;
	if(ZaSettings.ENABLED_UI_COMPONENTS[ZaSettings.CARTE_BLANCHE_UI]) {
		numCols = 7;
		colSizes = ["59", "*", Dwt_Button_XFormItem.estimateMyWidth(ZaMsg.search, true, 0), Dwt_Button_XFormItem.estimateMyWidth(ZaMsg.help_search, true, 0), "28", "12", Dwt_Button_XFormItem.estimateMyWidth(ZaMsg.advanced_search, false, 0)];
	} else {
		colSizes = ["59", "*", Dwt_Button_XFormItem.estimateMyWidth(ZaMsg.search, true, 0), Dwt_Button_XFormItem.estimateMyWidth(ZaMsg.help_search, true, 0)];
		if(ZaSettings.ENABLED_UI_COMPONENTS[ZaSettings.SAVE_SEARCH]) {
			numCols++;
			colSizes.push("28");
		}
		
		if(ZaSettings.ENABLED_UI_COMPONENTS[ZaSettings.ACCOUNT_LIST_VIEW] || ZaSettings.ENABLED_UI_COMPONENTS[ZaSettings.DL_LIST_VIEW] || ZaSettings.ENABLED_UI_COMPONENTS[ZaSettings.ALIAS_LIST_VIEW] || ZaSettings.ENABLED_UI_COMPONENTS[ZaSettings.RESOURCE_LIST_VIEW]) {
			numCols+=2;
			colSizes.push("12");
			colSizes.push(Dwt_Button_XFormItem.estimateMyWidth(ZaMsg.advanced_search, false, 0));
		}
	}
	var xFormObject = {
		tableCssStyle:"width:100%;padding:2px;",numCols:numCols,width:"100%",
		colSizes:colSizes,
		items: [
			{type:_MENU_BUTTON_, label:null, choices:ZaSearchField.searchChoices, 
				name: "searchMenuButton",
				toolTipContent:ZaMsg.searchToolTip, 
				icon:"SearchAll", cssClass:"DwtToolbarButton"},
			
			{type: _GROUP_,  numCols: 2, width: "100%", cssClass: "oselect",
				//cssStyle:"margin-left: 5px; height: 22px; border: 1px solid; ",
				items: [	
				{type:_TEXTFIELD_, ref:ZaSearch.A_query, containerCssClass:"search_field_container", label:null, 
					elementChanged: function(elementValue,instanceValue, event) {
						var charCode = event.charCode;
						if (charCode == 13 || charCode == 3) {
						   this.getForm().parent.invokeCallback();
						} else {
							this.getForm().itemChanged(this, elementValue, event);
						}
					},
					visibilityChecks:[],
					enableDisableChecks:[],
					//cssClass:"search_input", 
					cssStyle:"overflow: hidden;", width:"100%"
				},
				{type:_DWT_BUTTON_, label:"", toolTipContent:ZaMsg.tt_savedSearch, 
					icon: "SelectPullDownArrow", name: "showSavedSearchButton",
					onActivate:  ZaSearchField.prototype.showSavedSearchButtonHndlr,
					cssClass: "ZaShowSavedSearchArrowButton",
                    enableDisableChecks: [[ZaSearchField.canViewSavedSearch]],
					visibilityChecks:["(ZaSettings.ENABLED_UI_COMPONENTS[ZaSettings.SAVE_SEARCH] || ZaSettings.ENABLED_UI_COMPONENTS[ZaSettings.CARTE_BLANCHE_UI])"] 
				}	
			]},
					
			{type:_DWT_BUTTON_, label:ZaMsg.search, toolTipContent:ZaMsg.searchForAll, icon:"Search", name: "searchButton",
				onActivate:ZaSearchField.srchButtonHndlr, 
				cssStyle: AjxEnv.isIE ? "marginLeft: 2px;" : "marginLeft: 5px;",
				cssClass:"DwtToolbarButton"
			},
			
			{type:_DWT_BUTTON_, label: ZaMsg.help_search , toolTipContent:ZaMsg.tt_help_search, icon:"Help", name: "helpSearchButton",
				cssStyle:"overflow: hidden" ,	
                onActivate:ZaSearchField.helpSrchButtonHndlr, cssClass:"DwtToolbarButton"
            }
		]
	};
	//Save button
	if(ZaSettings.ENABLED_UI_COMPONENTS[ZaSettings.SAVE_SEARCH] || ZaSettings.ENABLED_UI_COMPONENTS[ZaSettings.CARTE_BLANCHE_UI]) {
		xFormObject.items.push({type:_DWT_BUTTON_, label: null , toolTipContent:ZaMsg.tt_save_search, icon:"Save", name: "saveSearchButton",
				onActivate:ZaSearchField.saveSrchButtonHndlr, cssClass:"DwtToolbarButton",
                enableDisableChecks: [[ZaSearchField.canSaveSearch]],
                visibilityChecks:["(ZaSettings.ENABLED_UI_COMPONENTS[ZaSettings.SAVE_SEARCH] || ZaSettings.ENABLED_UI_COMPONENTS[ZaSettings.CARTE_BLANCHE_UI])"] 
			});	
	}
	
	//advanced search button	
	if(ZaSettings.ENABLED_UI_COMPONENTS[ZaSettings.CARTE_BLANCHE_UI] || ZaSettings.ENABLED_UI_COMPONENTS[ZaSettings.ACCOUNT_LIST_VIEW] || ZaSettings.ENABLED_UI_COMPONENTS[ZaSettings.DL_LIST_VIEW] || ZaSettings.ENABLED_UI_COMPONENTS[ZaSettings.ALIAS_LIST_VIEW] || ZaSettings.ENABLED_UI_COMPONENTS[ZaSettings.RESOURCE_LIST_VIEW]) {
		xFormObject.items.push({type: _OUTPUT_, value: ZaToolBar.getSeparatorHtml()});
		xFormObject.items.push({type:_DWT_BUTTON_, label:ZaMsg.advanced_search, toolTipContent: ZaMsg.tt_advanced_search_open, name: "searchBuildButton",
				cssStyle:"overflow: hidden" ,	
                onActivate:ZaSearchField.advancedButtonHndlr,
				cssClass: "DwtToolbarButton ZaAdvancedSearchButton" 
			});
	}
	return xFormObject;
};

ZaSearchField.canSaveSearch = function () {
    return ZaItem.hasWritePermission ("zimbraAdminSavedSearches", 
            ZaZimbraAdmin.currentAdminAccount) ;
}

ZaSearchField.canViewSavedSearch = function () {
    return ZaItem.hasReadPermission ("zimbraAdminSavedSearches",
                ZaZimbraAdmin.currentAdminAccount) ;
}


/**
* @param xModelMetaData - XModel metadata that describes data model
* @param xFormMetaData - XForm metadata that describes the form
**/
ZaSearchField.prototype._initForm = 
function (xModelMetaData, xFormMetaData) {
	if(xModelMetaData == null || xFormMetaData == null)
		throw new AjxException("Metadata for XForm and/or XModel are not defined", AjxException.INVALID_PARAM, "ZaSearchField.prototype._initForm");

	this._localXModel = new XModel(xModelMetaData);
	this._localXForm = new XForm(xFormMetaData, this._localXModel, null, this);
	this._localXForm.draw();
	this._drawn = true;
}

//The popup dialog to allow user to specify the name/query of the search to be saved.
ZaSaveSearchDialog = function(searchField) {
	if (!searchField) return ; 
	this._searchField = searchField
	DwtDialog.call(this, searchField.shell);
	this._okButton = this.getButton(DwtDialog.OK_BUTTON);
	this.registerCallback (DwtDialog.OK_BUTTON, ZaSaveSearchDialog.prototype.okCallback, this );		
}

ZaSaveSearchDialog.prototype = new DwtDialog ;
ZaSaveSearchDialog.prototype.constructor = ZaSaveSearchDialog ;

ZaSaveSearchDialog.prototype.okCallback =
function() {
	if (AjxEnv.hasFirebug) console.debug("Ok button of saved search dialog is clicked.");
	var savedSearchArr = [] ;
	var nameValue = this._nameInput.value;
	var queryValue =  this._queryInput.value ;
	
	savedSearchArr.push({
			name: nameValue,
			query: queryValue
		})
	
	if (this._isEditMode && this._origNameOfEdittedSearch != nameValue) { //saved search name is changed
		savedSearchArr.push({
			name: this._origNameOfEdittedSearch,
			query: ""
		}); 
	}
	
	
	ZaSearch.modifySavedSearches(savedSearchArr, 
			new AjxCallback(this._searchField, this._searchField.modifySavedSearchCallback )) ;
	//ZaSearch._savedSearchToBeUpdated = true ;
	this.popdown();
}

ZaSaveSearchDialog.prototype.show =
function (name, query){
	if (!this._createUI) {
		this._nameInputId = Dwt.getNextId();
		this._queryInputId = Dwt.getNextId();
		var html = [
			"<table><tr>",
			"<td>",  ZaMsg.saved_search_editor_name, "</td>",
			"<td><div style='overflow:auto;'><input id='", this._nameInputId, "' type=text size=50 maxlength=50 /></div></td></tr>",
			//"<td>", this._queryInput.getHtmlElement().innerHTML ,"</td></tr>",
			
			"<tr><td>",  ZaMsg.saved_search_editor_query, "</td>",	
			"<td><div style='overflow:auto;'><input id='", this._queryInputId, "' type=text size=50 maxlength=200 /><div></td>",
			//"<td>", this._nameInput.getHtmlElement().innerHTML ,"</td></tr>",
			"</tr></table>"
		] ; 
		this.setContent (html.join("")) ;			
		this._createUI = true ;
	}
	
	if (!name) {
		this.setTitle (ZaMsg.t_saved_search) ;
		this._isEditMode = false ; 
	}else{
		this.setTitle (ZaMsg.t_edit_saved_search) ;
		this._isEditMode = true;
		this._origNameOfEdittedSearch = name ;
	}
		
	this.popup() ;
	
	if (!this._nameInput) {
		this._nameInput = document.getElementById(this._nameInputId);
	}
	this._nameInput.value = name || "";
	
	if (!this._queryInput) {
		this._queryInput = document.getElementById(this._queryInputId) ;
	}
	this._queryInput.value = query || "" ;
}


