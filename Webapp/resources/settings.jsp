<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRSSWeb" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <title><fmt:message key="applicationTitle" /> v${cwfn:sysprop('mytunesrss.version')}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="stylesheet" type="text/css" href="${appUrl}/styles/mytunesrss.css" />
    <!--[if IE]>
    <link rel="stylesheet" type="text/css" href="${appUrl}/styles/ie.css" />
  <![endif]-->

</head>

<body>

<div class="body">

    <h1 class="settings">
        <a class="portal" href="${servletUrl}/showPortal"><fmt:message key="portal"/></a> <span><fmt:message key="myTunesRss"/></span>
    </h1>

    <jsp:include page="/incl_error.jsp" />

    <form action="${servletUrl}/saveSettings" method="post">
        <table cellspacing="0">
            <tr>
                <th class="active"><fmt:message key="settings"/></th>
                <th>&nbsp;</th>
            </tr>
            <tr class="odd">
                <td><fmt:message key="settings.itemsPerPage"/></td>
                <td><input type="text" name="pageSize" maxlength="3" value="<c:out value="${cwfn:choose(config.pageSize > 0, config.pageSize, '')}"/>" style="width: 50px;" /></td>
            </tr>
            <tr>
                <td><fmt:message key="settings.fakeMp3"/></td>
                <td><input type="text" name="suffix.mp3" maxlength="20" value="<c:out value="${config.map['suffix.mp3']}"/>" style="width: 50px;" /></td>
            </tr>
            <tr class="odd">
                <td><fmt:message key="settings.fakeM4a"/></td>
                <td><input type="text" name="suffix.m4a" maxlength="20" value="<c:out value="${config.map['suffix.m4a']}"/>" style="width: 50px;" /></td>
            </tr>
            <tr>
                <td><fmt:message key="settings.rssFeedLimit"/></td>
                <td>
                    <input type="text" name="rssFeedLimit" maxlength="3" value="<c:out value="${cwfn:choose(config.rssFeedLimit > 0, config.rssFeedLimit, '')}"/>" style="width: 50px;" />
                </td>
            </tr>
            <tr class="odd">
                <td><fmt:message key="settings.playlistTypes"/></td>
                <td>
                    <input type="checkbox" name="feedType" value="rss" <c:if test="${fn:contains(fn:join(config.feedTypes, ';'), 'rss')}">
                        checked="checked"</c:if> />
												<img src="${appUrl}/images/rss.gif" alt="RSS" style="vertical-align:text-top;" />
												RSS
                    <input type="checkbox" name="feedType" value="m3u" <c:if test="${fn:contains(fn:join(config.feedTypes, ';'), 'm3u')}">
                        checked="checked"</c:if> style="margin-left: 15px;" />
												<img src="${appUrl}/images/m3u.gif" alt="M3U" style="vertical-align:text-top;"/>
												M3U
												</td>
            </tr>
        </table>

        <div class="buttons">
            <input type="submit" value="<fmt:message key="doSave"/>" />
						<input type="button" value="<fmt:message key="doCancel"/>" onclick="document.location.href='${servletUrl}/showPortal'"/>
        </div>
    </form>
</div>

</body>

</html>
