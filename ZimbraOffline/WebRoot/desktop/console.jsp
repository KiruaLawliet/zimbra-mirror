<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="com.zimbra.i18n" %>

<fmt:setBundle basename="/desktop/ZdMsg" scope="request"/>

<jsp:useBean id="bean" class="com.zimbra.cs.offline.jsp.ConsoleBean"/>
<jsp:setProperty name="bean" property="*"/>
<jsp:setProperty name="bean" property="locale" value="${pageContext.request.locale}"/>

<c:set var="accounts" value="${bean.accounts}"/>

<c:if test="${param.loginOp != 'logout' && (param.client == 'advanced' || (param.client == 'standard' && fn:length(accounts) == 1))}">
    <jsp:forward page="/desktop/login.jsp"/>
</c:if>

<c:set var='onDeleteWarn'><fmt:message key='OnDeleteWarn'/></c:set>

<html>
<head>
<meta http-equiv="refresh" content="15;url=/zimbra/desktop/console.jsp" >
<meta http-equiv="CACHE-CONTROL" content="NO-CACHE">
<link rel="shortcut icon" href="/zimbra/favicon.ico" type="image/vnd.microsoft.icon">
<title><fmt:message key="ZimbraDesktop"/></title>

<style type="text/css">
    @import url(/zimbra/desktop/css/offline.css);
</style>
<script type="text/javascript" src="js/desktop.js"></script>
<script type="text/javascript">

function OnAccount(id, zmail) {
    document.hidden_form.accountId.value = id;
    if (zmail)
    	document.hidden_form.action = "/zimbra/desktop/accsetup.jsp";
    else
    	document.hidden_form.action = "/zimbra/desktop/accsetup.jsp?accntType=OtherAcct";
    document.hidden_form.submit();
}

function OnPromote(id) {
    document.hidden_form.accountId.value = id;
    document.hidden_form.action = "/zimbra/desktop/console.jsp";
    document.hidden_form.submit();
}

function OnNew() {
    window.location = "/zimbra/desktop/accsetup.jsp";
}

function OnLogin() {
    window.location = "/zimbra/desktop/login.jsp";
}

function OnLoginTo(username) {
    document.hidden_form.username.value = username;
    document.hidden_form.action = "/zimbra/desktop/login.jsp";
    document.hidden_form.submit();
}

function OnDelete(id, zmail) {
    if (confirm("${onDeleteWarn}")) {
        if (zmail)
    	    document.hidden_form.action = "/zimbra/desktop/accsetup.jsp";
        else
    	    document.hidden_form.action = "/zimbra/desktop/accsetup.jsp?accntType=OtherAcct";
        document.hidden_form.accountId.value = id;
        document.hidden_form.verb.value = "del";
        document.hidden_form.submit();
    }
}

function OnReset(id, zmail) {
    if (confirm("<fmt:message key='OnResetWarn'/>")) {
        if (zmail)
    	    document.hidden_form.action = "/zimbra/desktop/accsetup.jsp";
        else
    	    document.hidden_form.action = "/zimbra/desktop/accsetup.jsp?accntType=OtherAcct";
        document.hidden_form.accountId.value = id;
        document.hidden_form.verb.value = "rst";
        document.hidden_form.submit();
    }
}
    
</script>
</head>

<body>
<c:set var='moveup' scope='application'><fmt:message key='MoveUp'/></c:set>
<br><br>
<div align="center">
<img src="/zimbra/desktop/img/YahooZimbraLogo.gif" border="0">
<br><br>

    
<c:if test="${not empty param.accntVerb && not empty param.srvcName}">
<c:choose>
    <c:when test="${param.accntVerb eq 'add'}">
		<div id="serviceCreated" class="infoBg">
            
            <p><b><fmt:message key='ServiceCreated'/></b></p>
		
		    <fmt:message key='ServiceAdded'><fmt:param>${param.srvcName}</fmt:param></fmt:message>
            
		    <p><fmt:message key='ServiceAddedNote'/></p>
            
        
		</div>
    </c:when>
    <c:otherwise>
        <c:choose>
            <c:when test="${param.accntVerb eq 'del'}">
                <c:set var="key" value="ServiceDeleted"/>
            </c:when>
            <c:when test="${param.accntVerb eq 'export'}">
                <c:set var="key" value="ServiceExported"/>
            </c:when>
            <c:when test="${param.accntVerb eq 'import'}">
                <c:set var="key" value="ServiceImported"/>
            </c:when>
            <c:when test="${param.accntVerb eq 'mod'}">
                <c:set var="key" value="ServiceUpdated"/>
            </c:when>
            <c:when test="${param.accntVerb eq 'rst'}">
                <c:set var="key" value="ServiceReset"/>
            </c:when>
        </c:choose>

        <div id="serviceDeleted" class="infoBg">
            <div><b><fmt:message key='ManageService'/></b></div>

                <p><fmt:message key="${key}"><fmt:param>${param.srvcName}</fmt:param></fmt:message></p>
			            
        </div>
    </c:otherwise>
</c:choose>
</c:if>

<c:choose>
<c:when test="${empty accounts}">
    <div id="welcome" class='whiteBg'>
                   
      <div align="center"><h2><fmt:message key='WizardTitle'/></h2></div> 
      
        <hr>
<fmt:message key='WizardDesc'/><br><br>
<span class="padding">
    <p>
    <ol>
        <li>
            <b><fmt:message key='WizardDescP1'/></b><br>
            <fmt:message key='WizardDescInfo1'/>
        </li>

        <li><b><fmt:message key='WizardDescP2'/></b><br>
            <fmt:message key='WizardDescInfo2'/>
        </li>

        <li><b><fmt:message key='WizardDescP3'/></b><br><fmt:message key='WizardDescInfo3'/>
        <li><b><fmt:message key='WizardDescP4'/></b><br><fmt:message key='WizardDescInfo4'/>

        </li>
    </ol>
    </p>
</span>
<br><br>
            <div align="center">
                <a href="#" onclick="OnNew()"><img src="/zimbra/desktop/img/CreateNewAccount.gif" border="0"></a>
            </div>


</div>

</c:when>
<c:otherwise>

<form name="hidden_form" method="POST">
    <input type="hidden" name="accountId">
    <input type="hidden" name="username">
    <input type="hidden" name="verb">
</form>
<div align="right" style="width: 680px; margin-bottom: 10px;"><a href="#" onclick="OnLogin()"><b><fmt:message key='GotoDesktop'/></b></a></div>
<div id="console" class="whiteBg">

					<div align="center"><h2 style="display: inline;"><fmt:message key='HeadTitle' /></h2> &nbsp; ( <a href="#" onclick="OnNew()"><fmt:message key='SetupAnotherAcct'/></a> )</div>

<hr>

	<!-- p><fmt:message key='Instruction' /></p -->
	<br><br>
    <table cellpadding=5 border=0 align="center" width="90%">
    	<!--tr><th><fmt:message key='AccountName'/></th><th><fmt:message key='EmailAddress'/></th><th><fmt:message key='LastSync'/></th><th><fmt:message key='Status'/></th><th><fmt:message key='Order'/></th></tr-->

    	<c:forEach items="${accounts}" var="account">
	        <tr class="rowline">
	        <td style="line-height: 18px;">

	        <h3 style="display:inline;">${account.name}</h3> &nbsp;${account.email}<br>
	        <a href="javascript:OnAccount('${account.id}',${account.zmail})"><fmt:message key="Edit"/></a> &nbsp;<a href="javascript:OnDelete('${account.id}',${account.zmail});"  id='deleteButton'><fmt:message key="Delete"/></a> &nbsp;<a href="javascript:OnReset('${account.id}',${account.zmail});"  id='resetButton'><fmt:message key="ResetData"/></a> 
				<br>
		            <c:choose>
	                   <c:when test="${account.statusUnknown}">
	                     <i><img src="/zimbra/img/im/ImgOffline.gif" align="absmiddle"> <fmt:message key='StatusUnknown'/></i>
	                   </c:when>
	                   <c:when test="${account.statusOffline}">
	                       <i><img src="/zimbra/img/im/ImgImAway.gif" align="absmiddle">  <fmt:message key='StatusOffline'/></i>
	                   </c:when>
	                   <c:when test="${account.statusOnline}">
	                       <i><img src="/zimbra/img/im/ImgImAvailable.gif" align="absmiddle">  <fmt:message key='StatusOnline'/></i>
	                   </c:when>
	                   <c:when test="${account.statusRunning}">
	                       <i><img src="/zimbra/img/animated/Imgwait_16.gif" align="absmiddle">  <fmt:message key='StatusInProg'/></i>
	                   </c:when>
	                   <c:when test="${account.statusAuthFailed}">
	                       <i><img src="/zimbra/img/im/ImgImDnd.gif" align="absmiddle">  <fmt:message key='StatusCantLogin'/></i>
	                   </c:when>
	                   <c:when test="${account.statusError}">
	                       <i><img height="14" width="14" src="/zimbra/img/dwt/ImgCritical.gif" align="absmiddle">  <fmt:message key='StatusErr'/></i>
	                   </c:when>
		           </c:choose>
		           <br>
					<c:choose>
						<c:when test='${account.lastSync != null}'>
							<i class="ZHint"><fmt:message key='LastSync'/>&nbsp;<fmt:formatDate value="${account.lastSync}" type="both" dateStyle="short" timeStyle="short"/></i>
						</c:when>
					</c:choose>
	           </td>
		       <td align="center">&nbsp;
		           <c:if test="${not account.first}">
		               <a href="javascript:OnPromote('${account.id}')"><img src="/zimbra/desktop/img/sortArrow.gif" border="0" alt="${moveup}"></a>
		           </c:if>
		       </td>
	        </tr>
    	</c:forEach>
    </table>
<br>

</div>

</c:otherwise>
</c:choose>
</div>
</body>
</html>

