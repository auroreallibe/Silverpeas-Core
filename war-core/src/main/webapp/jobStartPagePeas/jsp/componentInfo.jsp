<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page import="org.apache.commons.lang3.BooleanUtils"%>

<%@page import="com.silverpeas.admin.localized.LocalizedOption"%>
<%@page import="com.silverpeas.admin.localized.LocalizedParameter"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="com.stratelia.webactiv.beans.admin.ProfileInst"%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ include file="check.jsp" %>

<%!

void displayParameter(LocalizedParameter parameter, ResourcesWrapper resource, JspWriter out) throws java.io.IOException
{
	String help = parameter.getHelp();
	if (help != null) {
	  	help = EncodeHelper.javaStringToHtmlString(help);
		out.println("<li class='field' id='"+parameter.getName()+"'>");

		out.print("<img src=\""+resource.getIcon("JSPP.instanceHelpInfo")+"\" title=\""+help+"\" class=\"parameterInfo\"/>");

	} else {
		out.println("<li class='field' id='"+parameter.getName()+"'>");

	}

	out.println("<label class='txtlibform'>");
	out.println(parameter.getLabel());
	out.println("</label>");
	out.println("<div class='champs'>");
	
	// Value
	boolean isSelect = parameter.isSelect() || parameter.isXmlTemplate();
	if (parameter.isCheckbox()) {
		String checked = "";
		if (StringUtil.getBooleanValue(parameter.getValue())){
			checked = "checked=\"checked\"";
        }
		out.println("<input type=\"checkbox\" name=\""+parameter.getName()+"\" value=\""+parameter.getValue()+"\" "+checked+" disabled=\"disabled\"/>");
	}
	else if (isSelect)
	{
		List options = parameter.getOptions();
		if (options != null)
		{
			for (int i=0; i<options.size(); i++)
			{
				LocalizedOption option = (LocalizedOption) options.get(i);
				String name = option.getName();
				String value = option.getValue();
				if (parameter.getValue() != null && parameter.getValue().toLowerCase().equals(value.toLowerCase())) {
				  	out.println(name);
				}
			}		
		}
	}
	else if (parameter.isRadio())
	{
		List radios = parameter.getOptions();
		if (radios != null)
		{
	      for (int i=0; i<radios.size(); i++)
	      {
	      		LocalizedOption radio = (LocalizedOption) radios.get(i);
				String name = radio.getName();
				String value = radio.getValue();
				String checked = "";
				if (parameter.getValue() != null && parameter.getValue().toLowerCase().equals(value) || i==0) {
					checked = "checked=\"checked\"";
                }
				out.println("<input type=\"radio\" name=\""+parameter.getName()+"\" value=\""+value+"\" "+checked+" disabled=\"disabled\"/>");
				out.println(name+"&nbsp;");
			}		
		}
	}
	else {
	  	if (StringUtil.isDefined(parameter.getValue())) {
			out.println(parameter.getValue());
	  	}
	}
	out.println("</div></li>");
}
%>

<%
ComponentInst 	compoInst 			= (ComponentInst) request.getAttribute("ComponentInst");
String 			m_JobPeas 			= (String) request.getAttribute("JobPeas");
List<LocalizedParameter> parameters = (List<LocalizedParameter>) request.getAttribute("Parameters");
List<ProfileInst> m_Profiles 		= (List<ProfileInst>) request.getAttribute("Profiles");
boolean 		isInHeritanceEnable = ((Boolean)request.getAttribute("IsInheritanceEnable")).booleanValue();
int				scope				= ((Integer) request.getAttribute("Scope")).intValue();
int	 			maintenanceState 	= (Integer) request.getAttribute("MaintenanceState");
boolean			popupMode			= BooleanUtils.toBoolean((Boolean) request.getAttribute("PopupMode"));

if (scope == JobStartPagePeasSessionController.SCOPE_FRONTOFFICE) {
  //use default breadcrumb
  browseBar.setSpaceJavascriptCallback(null);
  browseBar.setComponentJavascriptCallback(null);
}

String m_ComponentIcon = iconsPath+"/util/icons/component/"+compoInst.getName()+"Small.gif";

TabbedPane tabbedPane = gef.getTabbedPane();

browseBar.setComponentId(compoInst.getId());
browseBar.setExtraInformation(resource.getString("GML.description"));	
browseBar.setI18N(compoInst, resource.getLanguage());

operationPane.addOperation(resource.getIcon("JSPP.instanceUpdate"),resource.getString("JSPP.ComponentPanelModifyTitle"),"javascript:onClick=updateInstance()");
if (scope == JobStartPagePeasSessionController.SCOPE_BACKOFFICE) {
	operationPane.addOperation(resource.getIcon("JSPP.ComponentOrder"),resource.getString("JSPP.ComponentOrder"),"javascript:onClick=openPopup('PlaceComponentAfter', 750, 230)");
	operationPane.addLine();
	if (JobStartPagePeasSettings.useComponentsCopy) {
		operationPane.addOperation(resource.getIcon("JSPP.CopyComponent"),resource.getString("JSPP.component.copy"),"javascript:onclick=clipboardCopy()");
		if (maintenanceState >= JobStartPagePeasSessionController.MAINTENANCE_PLATFORM) {
			operationPane.addOperation(resource.getIcon("JSPP.CopyComponent"),resource.getString("JSPP.component.cut"),"javascript:onclick=clipboardCut()");
		}
		operationPane.addLine();
	}
	operationPane.addOperation(resource.getIcon("JSPP.instanceDel"),resource.getString("JSPP.ComponentPanelDeleteTitle"),"javascript:onClick=deleteInstance()");
}

tabbedPane.addTab(resource.getString("GML.description"),"#",true);

for (ProfileInst theProfile : m_Profiles) {
	String profile = theProfile.getLabel();
	String prof = resource.getString(profile.replace(' ', '_'));
	if (prof.equals("")) {
		prof = profile;
	}
	
	tabbedPane.addTab(prof,"RoleInstance?IdProfile="+theProfile.getId()+"&NameProfile="+theProfile.getName()+"&LabelProfile="+theProfile.getLabel(),false);
}

window.setPopup(popupMode);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<link type="text/css" href="/silverpeas/util/styleSheets/fieldset.css" rel="stylesheet" />
<view:includePlugin name="qtip"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="javascript/component.js"></script>
<script type="text/javascript">
var currentLanguage = "<%=compoInst.getLanguage()%>";
<%
	for (String lang : compoInst.getTranslations().keySet()) {
		out.println("var name_"+lang+" = \""+EncodeHelper.javaStringToJsString(EncodeHelper.javaStringToHtmlString(compoInst.getLabel(lang)))+"\";\n");
		out.println("var desc_"+lang+" = \""+EncodeHelper.javaStringToJsString(EncodeHelper.javaStringToHtmlString(compoInst.getDescription(lang)))+"\";\n");
	}
%>

function showTranslation(lang) {
	<%=I18NHelper.updateHTMLLinks(compoInst)%>
	
	document.getElementById("compoName").innerHTML = eval("name_"+lang);
	document.getElementById("compoDesc").innerHTML = eval("desc_"+lang);
	
	currentLanguage = lang;
}

function openPopup(action, larg, haut) {
	url = action;
	windowName = "actionWindow";
	windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars,resizable";
	actionWindow = SP_openWindow(url, windowName, larg, haut, windowParams, false);
}

function deleteInstance() {	
    if (window.confirm("<%=resource.getString("JSPP.MessageSuppressionInstanceBegin")+" "+EncodeHelper.javaStringToJsString(compoInst.getLabel())+" "+resource.getString("JSPP.MessageSuppressionInstanceEnd")%>")) { 
      var $form = jQuery('#infoInstance');
      jQuery('#ComponentNum', $form).val('<%=compoInst.getId()%>');
      $form.attr('action', 'DeleteInstance').submit();
	}
}

function updateInstance() {
  var $form = jQuery('#infoInstance');
  jQuery('#ComponentNum', $form).val('<%=compoInst.getId()%>');
  jQuery('#Translation', $form).val(currentLanguage);
  $form.attr('action', 'UpdateInstance').submit();
}

function clipboardCopy() {
    top.IdleFrame.location.href = 'copy?Object=Component&Id=<%=compoInst.getId()%>';
}

function clipboardCut() {
    top.IdleFrame.location.href = 'Cut?Type=Component&Id=<%=compoInst.getId()%>';
}
</script>
</head>
<body id="admin-component">

<%
out.println(window.printBefore());
out.println(tabbedPane.print());

%>
<view:frame>
  <% if (maintenanceState >= JobStartPagePeasSessionController.MAINTENANCE_PLATFORM) { %>

  <div class="inlineMessage"> <%=resource.getString("JSPP.maintenanceStatus."+maintenanceState)%> </div>

  <br clear="all"/>
  <% } %>
  <form id="infoInstance" name="infoInstance" action="" method="post">
    <input id="ComponentNum" name="ComponentNum" type="hidden"/>
    <input id="Translation"  name="Translation"  type="hidden"/>
    <fieldset class="skinFieldset">

      <legend class="without-img"><img src="<%=m_ComponentIcon%>" class="componentIcon" alt=""/>&nbsp;<%=m_JobPeas%></legend>
      <ul class="fields">
        <li class="field entireWidth">
          <label class="txtlibform"><%=resource.getString("GML.name") %> </label>
          <div class="champs"><%=EncodeHelper.javaStringToHtmlString(compoInst.getLabel(resource.getLanguage()))%></div>
        </li>

        <% if (StringUtil.isDefined(compoInst.getDescription(resource.getLanguage()))) { %>
        <li class="field entireWidth">
          <label class="txtlibform"><%=resource.getString("GML.description") %></label>
          <div class="champs" id="compoDesc"><%=EncodeHelper.javaStringToHtmlParagraphe(compoInst.getDescription(resource.getLanguage()))%></div>
        </li>
        <% } %>


        <% if (compoInst.getCreateDate() != null) { %>
        <li class="field">

          <label class="txtlibform"><%=resource.getString("GML.creationDate") %></label>

          <div class="champs"> <%=resource.getOutputDateAndHour(compoInst.getCreateDate())%>
            <% if (compoInst.getCreator() != null) { %>
            <%=resource.getString("GML.by") %>
            <view:username userId="<%=compoInst.getCreator().getId()%>" />
            <% } %>
          </div>
        </li>
        <% } %>

        <% if (compoInst.getUpdateDate() != null) { %>
        <li class="field">

          <label class="txtlibform"><%=resource.getString("GML.updateDate") %></label>

          <div class="champs"> <%=resource.getOutputDateAndHour(compoInst.getUpdateDate())%>
            <% if (compoInst.getUpdater() != null) { %>
            <%=resource.getString("GML.by") %>
            <view:username userId="<%=compoInst.getUpdater().getId()%>" />
            <% } %>
          </div>
        </li>
        <% } %>

        <% if (isInHeritanceEnable) { %>
        <li class="field entireWidth">

          <label class="txtlibform"><%=resource.getString("JSPP.inheritanceBlockedComponent") %></label>
          <div class="champs">

            <% if (compoInst.isInheritanceBlocked()) { %>
            <%=resource.getString("JSPP.inheritanceComponentNotUsed")%>
            <% } else { %>
            <%=resource.getString("JSPP.inheritanceComponentUsed")%>
            <% } %>
          </div>
        </li>
        <% } %>
      </ul>
    </fieldset>

    <% if (parameters.size() > 0) { %>
    <fieldset class="skinFieldset parameters readOnly">

      <legend><%=resource.getString("JSPP.parameters") %></legend>

      <% } %>
      <ul class="fields">
        <%


	LocalizedParameter parameter = null;

	for(int nI=0; parameters != null && nI < parameters.size(); nI++)
		{
			parameter = parameters.get(nI);

			displayParameter(parameter, resource, out);

		}
		%>
      </ul>
      <% if (parameters.size() > 0) { %>
    </fieldset>
    <% } %>
  </form>
</view:frame>
<%

out.println(window.printAfter());
%>

</body>
</html>