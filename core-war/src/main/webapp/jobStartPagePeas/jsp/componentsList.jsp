<%-- Copyright (C) 2000 - 2024 Silverpeas This program is free software: you can redistribute it and/or modify it under
	the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of
	the License, or (at your option) any later version. As a special exception to the terms and conditions of version
	3.0 of the GPL, you may redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
	applications as described in Silverpeas's FLOSS exception. You should have received a copy of the text describing
	the FLOSS exception, and it is also available here: "https://www.silverpeas.org/legal/floss_exception.html" This
	program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
	warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
	details. You should have received a copy of the GNU Affero General Public License along with this program. If not,
	see <https://www.gnu.org/licenses />.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ include file="check.jsp" %>
<% String spaceId=(String) request.getAttribute("CurrentSpaceId"); browseBar.setSpaceId(spaceId);
browseBar.setExtraInformation(resource.getString("JSPP.creationInstance")); %>

<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">

<head>
	<title>
		<%=resource.getString("GML.popupTitle")%>
	</title>
	<view:looknfeel />
	<script type="text/javascript">

		$(document).ready(function () {

			/* Pour mettre en ordre alphabétique en fonction du libellé utilisé*/
			$('.applications-list').html(
				$('.applications-list li')
					.get()
					.sort(function (a, b) {
						return (a.innerHTML.replace(/^\s*/, '').toLowerCase() >= b.innerHTML.replace(/^\s*/, '').toLowerCase()) ? 1 : -1;
					})
			);

			

			$(".deploy").click(function () {
				$(this).parents('.app').toggleClass("deployed");
				return false;
			});

			
			$('.checkbox-wrapper').on('click', function(event) {
			toggleCheckbox(this);
			});

			$('.checkbox-wrapper').on('keydown', function(event) {
			if (event.key === 'Enter') {
			toggleCheckbox(this);
			}
			});

			function toggleCheckbox(element) {
			$(element).find('.custom-checkbox').toggleClass('checked');
			var checkboxValue = $(element).data('value');
			if ($(element).find('.custom-checkbox').hasClass('checked')) {
			$("." + checkboxValue).show();
			$(element).attr('aria-checked', 'true');
			} else {
			$("." + checkboxValue).hide();
			$(element).attr('aria-checked', 'false');
			}
			}

		});
	</script>
</head>

<body class="page_content_admin">
	<% out.print(window.printBefore()); %>
		<c:set var="currentSuite" value="null" scope="page" />

		<!-- sousNav Filtre  -->
		<div class="sousNavBulle">
			<div class="filter-AppByType">
				<%=resource.getString("JSPP.FilterAppByType")%> :
				<!-- ICI IL FAUT LISTER les listTypes Comme construit ci dessous en dure-->
				<div class="checkbox-wrapper" role="checkbox" aria-checked="true" tabindex="0" data-value="type_01" >
					<div class="custom-checkbox checked"></div>
					<label>Gestion Documentaire</label>
				</div>
				<div class="checkbox-wrapper" role="checkbox" aria-checked="true" tabindex="0" data-value="type_02" >
					<div class="custom-checkbox checked"></div>
					<label>Gestion Collaborative</label>
				</div>
			</div>
		</div>
		<!-- /sousNav  -->
		

		<ul class="applications-list">
			<c:forEach items="${requestScope.ListComponents}" var="component" varStatus="loop">
				<c:if test="${component.visible}">
					<c:set var="currentSuite" value="${component.suite}" scope="page" />
					<c:set var="currentSuitePrefix" value="${fn:substring(currentSuite, 0, 2)}" scope="page" />
					<c:set var="currentSuiteLabel" value="${fn:substring(currentSuite, 3, 30)}" scope="page" />
					<li  class="app type_<c:out value="${currentSuitePrefix}" />">

						<h3 class="<c:out value="${component.label}" /> app-name">
							<a href="CreateInstance?ComponentName=<c:out value="${component.name}" />"
								title="<%=resource.getString("JSPP.applications.add")%>">
								<c:out value="${component.label}" />
							</a>	
						</h3>
						<p class="app-description">
							<c:out value="${component.description}" />
						</p>
						<p class="app-type">
							<span class="hashtag"><c:out value="${currentSuiteLabel}" /></span>
						</p>

						<c:if test="${component.suite == '05 Workflow'}">
							<img src="<%=iconsPath%>/util/icons/component/processManagerBig.png"
								class="component-icon" alt="" />
						</c:if>
						<c:if test="${component.suite != '05 Workflow'}">
							<img src="<%=iconsPath%>/util/icons/component/<c:out value="${component.name}" />Big.png" class="component-icon" alt=""/>
						</c:if>

						<a href="CreateInstance?ComponentName=<c:out value="${component.name}" />"
						class="add-app" title="<%=resource.getString("JSPP.applications.add")%>">
							<span>
								<%=resource.getString("JSPP.applications.add")%>
							</span>
						</a>
						
						<a class="deploy" title="<%=resource.getString("JSPP.DeployForMoreDetails")%>" href="#"><span><%=resource.getString("JSPP.DeployForMoreDetails")%></span></a>
					</li>
				</c:if>
			</c:forEach>
		</ul>
		<br />
		<% out.print(window.printAfter()); %>
</body>

</html>