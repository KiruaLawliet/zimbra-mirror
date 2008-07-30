<%@ tag body-content="empty" %>
<%@ attribute name="export" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="com.zimbra.i18n" %>

<fmt:setBundle basename="/desktop/ZdMsg" scope="request"/>

<script type="text/javascript">
<!--
function toggleFolder() {
    zd.toggle("folders");
}

function folderName() {
    var folder = document.getElementsByName("folder");
    var folders = document.getElementsByName("folderName");

    if (folder[0].checked) {
        for (var i = 0; i < folders.length; i++) {
            if (folders[i].checked)
                return folders[i].value;
        }
    }
    return "";
}

//-->
</script>

<table width="100%">
    <tr>
        <td>
            <table>
                <tr>
                    <td width="1%"><input type="checkbox" name="folder" onClick="toggleFolder()"></td>
                    <td><nobr><fmt:message key="${export ? 'ExportFolder' : 'ImportFolder'}"/></nobr></td>
                </tr>
            </table>
        </td>
    </tr>
    <tr align="left" id="folders" style="display:none">
        <td>
            <table align="left" style="margin-left: 50px;">
                <c:set var="first" value="${true}"/>
                <c:forEach items="${export ? bean.exportList : bean.importList}" var="folderName">
                <tr>
                    <td width="1%"><input type="radio" name="folderName" value="${folderName}" ${first ? "checked" : ""}></td>
                    <td><nobr>${folderName}</nobr></td>
                </tr>
                <c:set var="first" value="${false}"/>
                </c:forEach>
            </table>
        </td>
    </tr>
</table>
