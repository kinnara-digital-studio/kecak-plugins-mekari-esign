package com.kinnarastudio.kecakplugins.mekariesign.webservice;

import org.joget.apps.app.dao.PluginDefaultPropertiesDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONException;
import org.json.JSONObject;

import com.kinnarastudio.commons.mekarisign.MekariSign;
import com.kinnarastudio.commons.mekarisign.exception.BuildingException;
import com.kinnarastudio.commons.mekarisign.exception.RequestException;
import com.kinnarastudio.commons.mekarisign.model.AuthenticationToken;
import com.kinnarastudio.commons.mekarisign.model.ServerType;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class MekariESignWebhook extends DefaultApplicationPlugin implements PluginWebSupport {
    @Override
    public void webService(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
        try {
            AuthenticationToken authToken = MekariSign.getBuilder()
                    .setClientId(getPropertyString("clientId"))
                    .setClientSecret(getPropertyString("clientSecret"))
                    .setServerType(ServerType.valueOf(getPropertyString("serverType")))
                    .setSecretCode(servletRequest.getParameter("code"))
                    .authenticate();

            servletRequest.getSession().setAttribute("MekariClientId", getPropertyString("clientId"));
            servletRequest.getSession().setAttribute("MekariToken", authToken.getAccessToken());
            servletRequest.getSession().setAttribute("MekariServerType", getPropertyString("serverType"));
            servletRequest.getSession().setMaxInactiveInterval(3600);

            // LogUtil.info(getClassName(), "Server Type: " + getPropertyString("serverType"));
            servletResponse.setStatus(301);
            
            String homeUrl = (String) WorkflowUtil.getHttpServletRequest().getSession().getAttribute("HomeURL");
            servletResponse.setHeader("Location", homeUrl);
        } catch (RequestException | BuildingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "Mekari eSign Webhook";
    }

    @Override
    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    @Override
    public String getDescription() {
        return getClass().getPackage().getImplementationTitle();
    }

    @Override
    public Object execute(Map map) {
        return null;
    }

    @Override
    public String getLabel() {
        return "Mekari eSign Webhook";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/webservice/MekariEsignWebhook.json");
    }

    @Override
    public Object getProperty(String property) {
        PluginDefaultPropertiesDao pluginDefaultPropertiesDao = (PluginDefaultPropertiesDao) AppUtil.getApplicationContext().getBean("pluginDefaultPropertiesDao");
        AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();

        return Optional.ofNullable(pluginDefaultPropertiesDao.getPluginDefaultPropertiesList(getClassName(), appDefinition, null, null, null, 1))
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .findFirst()
                .map(PluginDefaultProperties::getPluginProperties)
                .map(PropertyUtil::getPropertiesValueFromJson)
                .map(m -> m.get(property))
                .orElse(super.getProperty(property));
    }

    @Override
    public String getPropertyString(String property) {
        PluginDefaultPropertiesDao pluginDefaultPropertiesDao = (PluginDefaultPropertiesDao) AppUtil.getApplicationContext().getBean("pluginDefaultPropertiesDao");
        AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();

        return Optional.ofNullable(pluginDefaultPropertiesDao.getPluginDefaultPropertiesList(getClassName(), appDefinition, null, null, null, 1))
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .findFirst()
                .map(PluginDefaultProperties::getPluginProperties)
                .map(PropertyUtil::getPropertiesValueFromJson)
                .map(m -> m.get(property))
                .map(String::valueOf)
                .orElse(super.getPropertyString(property));
    }

    @Override
    public Map<String, Object> getProperties() {
        PluginDefaultPropertiesDao pluginDefaultPropertiesDao = (PluginDefaultPropertiesDao) AppUtil.getApplicationContext().getBean("pluginDefaultPropertiesDao");
        AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();

        return Optional.ofNullable(pluginDefaultPropertiesDao.getPluginDefaultPropertiesList(getClassName(), appDefinition, null, null, null, 1))
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .findFirst()
                .map(PluginDefaultProperties::getPluginProperties)
                .map(PropertyUtil::getPropertiesValueFromJson)
                .orElseGet(super::getProperties);
    }
}
