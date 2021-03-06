<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="mttag" %>

<%--@elvariable id="appUrl" type="java.lang.String"--%>
<%--@elvariable id="servletUrl" type="java.lang.String"--%>
<%--@elvariable id="permFeedServletUrl" type="java.lang.String"--%>
<%--@elvariable id="auth" type="java.lang.String"--%>
<%--@elvariable id="authUser" type="de.codewave.mytunesrss.config.User"--%>
<%--@elvariable id="globalConfig" type="de.codewave.mytunesrss.config.MyTunesRssConfig"--%>
<%--@elvariable id="config" type="de.codewave.mytunesrss.servlet.WebConfig"--%>

<%--@elvariable id="albums" type="java.util.List"--%>
<%--@elvariable id="stateEditPlaylist" type="java.lang.Boolean"--%>
<%--@elvariable id="editablePlaylists" type="java.util.List"--%>
<%--@elvariable id="simpleNewPlaylist" type="java.lang.Boolean"--%>
<%--@elvariable id="albumPager" type="de.codewave.mytunesrss.Pager"--%>
<%--@elvariable id="indexPager" type="de.codewave.mytunesrss.Pager"--%>
<%--@elvariable id="singleArtist" type="de.codewave.mytunesrss.Pager"--%>
<%--@elvariable id="singleGenre" type="de.codewave.mytunesrss.Pager"--%>
<%--@elvariable id="msgUnknownArtist" type="java.lang.String"--%>
<%--@elvariable id="allArtistGenreTrackCount" type="java.lang.Integer"--%>
<%--@elvariable id="allAlbumsTrackCount" type="java.lang.Integer"--%>

<c:set var="backUrl" scope="request">${servletUrl}/browseAlbum/${auth}/<mt:encrypt>artist=${cwfn:encodeUrl(param.artist)}/matchAlbumArtist=${cwfn:encodeUrl(param.matchAlbumArtist)}/genre=${cwfn:encodeUrl(param.genre)}/page=${param.page}/index=${param.index}</mt:encrypt></c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

    <c:if test="${authUser.rss}">
        <c:forEach items="${albums}" var="album">
            <c:choose>
                <c:when test="${mtfn:unknown(album.name)}">
                    <c:set var="albumName"><fmt:message key="unknownAlbum"/></c:set>
                </c:when>
                <c:otherwise>
                    <c:set var="albumName" value="${album.name}"/>
                </c:otherwise>
            </c:choose>
            <link href="${permFeedServletUrl}/createRSS/${auth}/<mt:encrypt>album=${mtfn:encode64(album.name)}/_cdi=${cwfn:encodeUrl(mtfn:virtualAlbumName(album))}.xml</mt:encrypt>" rel="alternate" type="application/rss+xml" title="<c:out value="${albumName}" />" />
        </c:forEach>
    </c:if>

</head>

<body class="browse">

<div class="body">

<div class="head">
    <h1 class="artists">
        <a class="portal" href="${servletUrl}/showPortal/${auth}"><span id="linkPortal"><fmt:message key="portal"/></span></a>
        <span><fmt:message key="myTunesRss"/></span>
    </h1>
</div>

<div class="content">

<div class="content-inner">

<ul class="menu">
    <li class="first">
        <a id="linkBrowseArtist" href="${servletUrl}/browseArtist/${auth}/<mt:encrypt>page=${cwfn:choose(empty param.artist, param.page, '1')}</mt:encrypt>"><fmt:message key="browseArtist"/></a>
    </li>
    <li class="active">
        <span><fmt:message key="browseAlbums"/></span>
    </li>
    <li>
        <a id="linkBrowseGenre" href="${servletUrl}/browseGenre/${auth}/<mt:encrypt>page=${param.page}</mt:encrypt>"><fmt:message key="browseGenres"/></a>
    </li>
    <c:if test="${!stateEditPlaylist && authUser.createPlaylists}">
        <li>
            <c:choose>
                <c:when test="${empty editablePlaylists || simpleNewPlaylist}">
                    <a id="linkStartPlaylist" href="${servletUrl}/startNewPlaylist/${auth}/backUrl=${mtfn:encode64(backUrl)}"><fmt:message key="newPlaylist"/></a>
                </c:when>
                <c:otherwise>
                    <a id="linkEditPlaylist" style="cursor:pointer" onclick="openDialog('#editPlaylistDialog')"><fmt:message key="editExistingPlaylist"/></a>
                </c:otherwise>
            </c:choose>
        </li>
    </c:if>
    <li class="spacer">&nbsp;</li>
    <c:if test="${!empty param.backUrl}">
        <li class="back">
            <a id="linkBack" href="${mtfn:decode64(param.backUrl)}"><fmt:message key="back"/></a>
        </li>
    </c:if>
</ul>

<jsp:include page="/incl_error.jsp" />

<jsp:include page="incl_playlist.jsp" />

<c:if test="${param.sortByYear != 'true'}">
    <c:set var="pager" scope="request" value="${albumPager}" />
    <c:set var="pagerCommand" scope="request" value="${servletUrl}/browseAlbum/${auth}/page={index}" />
    <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.artist || !empty param.genre, '*', param.page)}" />
    <c:set var="filterToggle" scope="request" value="true" />
    <jsp:include page="incl_pager.jsp" />
</c:if>

<c:set var="displayFilterUrl" scope="request">${servletUrl}/browseAlbum/${auth}/<mt:encrypt>page=${param.page}/artist=${cwfn:encodeUrl(param.artist)}/matchAlbumArtist=${cwfn:encodeUrl(param.matchAlbumArtist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/backUrl=${param.backUrl}</c:set>
<c:set var="filterYearActive" scope="request" value="true"/>
<c:set var="filterAlbumTypeActive" scope="request" value="true"/>
<jsp:include page="/incl_display_filter.jsp"/>

<table class="tracklist" cellspacing="0">
    <tr>
        <th class="active">
            <c:if test="${!empty param.genre}">${mtfn:capitalize(mtfn:decode64(param.genre))}</c:if>
            <fmt:message key="albums"/>
            <c:if test="${!empty param.artist}"> <fmt:message key="with"/> "${cwfn:choose(mtfn:unknown(mtfn:decode64(param.artist)), msgUnknownArtist, mtfn:decode64(param.artist))}"</c:if>
        </th>
        <th><fmt:message key="artist"/></th>
        <th><fmt:message key="tracks"/></th>
        <th class="actions">&nbsp;</th>
    </tr>
    <mt:initFlipFlop value1="odd" value2="even"/>
    <c:forEach items="${albums}" var="album" varStatus="loopStatus">
        <tr class="<mt:flipFlop/>">
            <td id="functionsDialogName${loopStatus.index}" <c:if test="${!empty(album.imageHash)}">class="coverThumbnailColumn"</c:if>">
            <div class="trackName">
                <c:if test="${!empty(album.imageHash)}">
                    <div class="albumCover">
                        <img id="albumthumb_${loopStatus.index}" src="${servletUrl}/showImage/${auth}/<mt:encrypt>hash=${album.imageHash}/size=${config.albumImageSize}</mt:encrypt>" onmouseover="showTooltip(this)" onmouseout="hideTooltip(this)" alt=""/>
                        <div class="tooltip" id="tooltip_albumthumb_${loopStatus.index}"><img src="${servletUrl}/showImage/${auth}/<mt:encrypt>hash=${album.imageHash}/size=${config.albumImageSize}</mt:encrypt>" alt=""/></div>
                    </div>
                </c:if>
                <c:choose>
                    <c:when test="${mtfn:unknown(album.name)}">
                        <fmt:message key="unknownAlbum"/>
                    </c:when>
                    <c:otherwise>
                        <a id="linkAlbumName${loopStatus.index}" href="${servletUrl}/browseTrack/${auth}/<mt:encrypt>album=${mtfn:encode64(album.name)}/albumartist=${mtfn:encode64(album.artist)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"><c:out value="${album.name}"/></a>
                    </c:otherwise>
                </c:choose>
            </div>
            </td>
            <td>
                <c:choose>
                    <c:when test="${album.artistCount == 1}">
                        <c:choose>
                            <c:when test="${singleArtist}">
                                <c:out value="${cwfn:choose(mtfn:unknown(album.artist), msgUnknownArtist, album.artist)}" />
                            </c:when>
                            <c:otherwise>
                                <a id="linkArtistName${loopStatus.index}" href="${servletUrl}/browseAlbum/${auth}/<mt:encrypt>artist=${mtfn:encode64(album.artist)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">
                                    <c:out value="${cwfn:choose(mtfn:unknown(album.artist), msgUnknownArtist, album.artist)}" /></a>
                            </c:otherwise>
                        </c:choose>
                    </c:when>
                    <c:otherwise><fmt:message key="variousArtists"/></c:otherwise>
                </c:choose>
            </td>
            <td class="tracks">
                <a id="linkTrackCount${loopStatus.index}" href="${servletUrl}/browseTrack/${auth}/<mt:encrypt>album=${mtfn:encode64(album.name)}/albumartist=${mtfn:encode64(album.artist)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"> ${album.trackCount} </a>
            </td>
            <td class="actions">
                <c:choose>
                    <c:when test="${!stateEditPlaylist}">
                        <c:set var="addRemoteControl">album:'${mtfn:escapeJs(album.name)}',albumArtist:'${mtfn:escapeJs(album.artist)}'</c:set>
                        <mttag:actions index="${loopStatus.index}"
                                       backUrl="${mtfn:encode64(backUrl)}"
                                       linkFragment="album=${mtfn:encode64(album.name)}/albumartist=${mtfn:encode64(album.artist)}"
                                       addRemoteControl="${addRemoteControl}"
                                       filename="${mtfn:virtualAlbumName(album)}"
                                       zipFileCount="${album.trackCount}"
                                       externalSitesFlag="${mtfn:externalSites('album') && !mtfn:unknown(album.name) && authUser.externalSites}"
                                       defaultPlaylistName="${album.name}"
                                       shareText="${album.name}"
                                       shareImageHash="${album.imageHash}"/>

                    </c:when>
                    <c:otherwise>
                        <c:if test="${globalConfig.flashPlayer && authUser.player && config.showPlayer}">
                            <a id="linkEditPlaylistFlash${loopStatus.index}" class="flash" onclick="openPlayer('${servletUrl}/showJukebox/${auth}/<mt:encrypt>playlistParams=<mt:encode64>album=${mtfn:encode64(album.name)}/albumartist=${mtfn:encode64(album.artist)}</mt:encode64></mt:encrypt>/playerId='); return false;" title="<fmt:message key="tooltip.flashplayer"/>"><span>Flash Player</span></a>
                        </c:if>
                        <a id="linkAddToPlaylist${loopStatus.index}" class="add" onclick="addAlbumsToPlaylist(jQuery.makeArray(['${mtfn:escapeJs(album.name)}']), jQuery.makeArray(['${mtfn:escapeJs(album.artist)}']))" title="<fmt:message key="playlist.addAlbum"/>"><span><fmt:message key="playlist.addAlbum"/></span></a>
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
    </c:forEach>
    <c:if test="${singleArtist || singleGenre}">
        <c:if test="${fn:length(albums) > 1 && allAlbumsTrackCount > allArtistGenreTrackCount}">
            <tr class="<mt:flipFlop/>">
                <td colspan="2" id="functionsDialogName${fn:length(albums) + 1}"><em>
                    <a id="linkAlbumName${fn:length(albums) + 1}" href="${servletUrl}/browseTrack/${auth}/<mt:encrypt>fullAlbums=true/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"><fmt:message key="allTracksOfAboveAlbums"/></a>
                </em></td>
                <td class="tracks">
                    <a id="linkTrackCount${fn:length(albums) + 1}" href="${servletUrl}/browseTrack/${auth}/<mt:encrypt>fullAlbums=true/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">${allAlbumsTrackCount}</a>
                </td>
                <td class="actions">
                    <c:choose>
                        <c:when test="${!stateEditPlaylist}">
                            <mttag:actions index="${fn:length(albums) + 1}"
                                           backUrl="${mtfn:encode64(backUrl)}"
                                           linkFragment="fullAlbums=true/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}"
                                           filename="${mtfn:decode64(param.artist)}"
                                           zipFileCount="${allAlbumsTrackCount}"
                                           defaultPlaylistName="${cwfn:choose(singleArtist, param.artist, param.genre)}"
                                           shareText="${cwfn:choose(singleArtist, param.artist, param.genre)}" />
                        </c:when>
                        <c:otherwise>
                            <c:if test="${globalConfig.flashPlayer && authUser.player && config.showPlayer}">
                                <a id="linkEditPlaylistFlash${fn:length(albums) + 1}" class="flash" onclick="openPlayer('${servletUrl}/showJukebox/${auth}/<mt:encrypt>playlistParams=<mt:encode64>fullAlbums=true/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encode64></mt:encrypt>/playerId='); return false;" title="<fmt:message key="tooltip.flashplayer"/>"><span>Flash Player</span></a>
                            </c:if>
                            <%--a id="linkAddToPlaylist${fn:length(albums) + 1}" class="add" onclick="addToPlaylist(jQuery.makeArray(['${mtfn:escapeJs(cwfn:decode64(param.artist))}']), jQuery.makeArray(['${mtfn:escapeJs(cwfn:decode64(param.genre))}']), null, true)"><span>Add</span></a--%>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:if>
        <c:if test="${fn:length(albums) > 1 || allAlbumsTrackCount > allArtistGenreTrackCount}">
            <tr class="<mt:flipFlop/>">
                <td colspan="2" id="functionsDialogName${fn:length(albums) + 2}"><em>
                    <c:choose>
                        <c:when test="${singleArtist}">
                            <mt:array var="params">
                                <mt:arrayElement value="${mtfn:decode64(param.artist)}"/>
                            </mt:array>
                            <fmt:message var="localizedMessage" key="allTracksOfArtist"/>
                        </c:when>
                        <c:otherwise>
                            <mt:array var="params">
                                <mt:arrayElement value="${mtfn:decode64(param.genre)}"/>
                            </mt:array>
                            <fmt:message var="localizedMessage" key="allTracksOfGenre"/>
                        </c:otherwise>
                    </c:choose>
                    <a id="linkAlbumName${fn:length(albums) + 2}" href="${servletUrl}/browseTrack/${auth}/<mt:encrypt>artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"><c:out value="${cwfn:message(localizedMessage, params)}"/></a>
                </em></td>
                <td class="tracks">
                    <a id="linkTrackCount${fn:length(albums) + 2}" href="${servletUrl}/browseTrack/${auth}/<mt:encrypt>artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">${allArtistGenreTrackCount}</a>
                </td>
                <td class="actions">
                    <c:choose>
                        <c:when test="${!stateEditPlaylist}">
                            <mttag:actions index="${fn:length(albums) + 2}"
                                           backUrl="${mtfn:encode64(backUrl)}"
                                           linkFragment="artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}/fullAlbums=false"
                                           filename="${mtfn:decode64(param.artist)}"
                                           zipFileCount="${allArtistGenreTrackCount}"
                                           defaultPlaylistName="${cwfn:choose(singleArtist, param.artist, param.genre)}"
                                           shareText="${cwfn:choose(singleArtist, param.artist, param.genre)}" />
                        </c:when>
                        <c:otherwise>
                            <c:if test="${globalConfig.flashPlayer && authUser.player && config.showPlayer}">
                                <a id="linkEditPlaylistFlash${fn:length(albums) + 2}" class="flash" onclick="openPlayer('${servletUrl}/showJukebox/${auth}/<mt:encrypt>playlistParams=<mt:encode64>artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}/fullAlbums=false</mt:encode64></mt:encrypt>/playerId='); return false;" title="<fmt:message key="tooltip.flashplayer"/>"><span>Flash Player</span></a>
                            </c:if>
                            <a id="linkAddToPlaylist${fn:length(albums) + 2}" class="add" onclick="addToPlaylist(jQuery.makeArray(['${mtfn:escapeJs(cwfn:decode64(param.artist))}']), jQuery.makeArray(['${mtfn:escapeJs(cwfn:decode64(param.genre))}']), null)"><span>Add</span></a>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:if>
    </c:if>
</table>

<c:if test="${!empty indexPager}">
    <c:set var="pager" scope="request" value="${indexPager}" />
    <c:set var="pagerCommand" scope="request">${servletUrl}/browseAlbum/${auth}/<mt:encrypt>page=${param.page}/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}/backUrl=${param.backUrl}/currentListId=${currentListId}</mt:encrypt>/index={index}</c:set>
    <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}" />
    <jsp:include page="incl_bottomPager.jsp" />
</c:if>

</div>

</div>

<div class="footer">
    <div class="inner"></div>
</div>

</div>

<jsp:include page="incl_select_flashplayer_dialog.jsp"/>
<jsp:include page="incl_edit_playlist_dialog.jsp"/>

<c:set var="externalSiteDefinitions" scope="request" value="${mtfn:externalSiteDefinitions('album')}"/>
<jsp:include page="incl_external_sites_dialog.jsp"/>
<jsp:include page="incl_functions_menu.jsp"/>

</body>

</html>
