package com.kinnarastudio.kecakplugins.mekariesign.webservice;

import org.joget.apps.app.dao.PluginDefaultPropertiesDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.plugin.property.service.PropertyUtil;
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
import java.io.*;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class MekariESignWebhook extends DefaultApplicationPlugin implements PluginWebSupport {
    @Override
    public void webService(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
        try {
            final String method = servletRequest.getMethod();
            final String code = servletRequest.getParameter("code");

            final JSONObject responseBody = new JSONObject() {{
                put("method", method);
                put("code", code);
            }};

            String content = "<h1>Content</h1>";

            AuthenticationToken authToken = MekariSign.getBuilder()
                .setClientId("UlfiHMoyfAcD0yZ4")
                .setClientSecret("8LFQ5UOuPpxUQygA1e2kqY60t9ihZWYX")
                .setServerType(ServerType.SANDBOX)
                .setSecretCode(code)
                .authenticate();

            LogUtil.info(getClassName(), "Client ID: " + getPropertyString("clientId"));
            LogUtil.info(getClassName(), "Client Secret: " + getPropertyString("clientSecret"));

            servletRequest.getSession().setAttribute("MekariToken", authToken.getAccessToken());

            servletResponse.setStatus(301);
            servletResponse.setHeader("Location", "/web/userview/mekarisign/mekari/_/mekari");
        } catch (JSONException e) {
            LogUtil.error(getClass().getName(), e, e.toString());
            servletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, e.toString());
        } catch (RequestException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BuildingException e) {
            // TODO Auto-generated catch block
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
    public void setProperties(Map<String, Object> properties) {
        PluginDefaultPropertiesDao pluginDefaultPropertiesDao = (PluginDefaultPropertiesDao) AppUtil.getApplicationContext().getBean("pluginDefaultPropertiesDao");
        AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();

        super.setProperties(Optional.ofNullable(pluginDefaultPropertiesDao.getPluginDefaultPropertiesList(getClassName(), appDefinition, null, null, null, 1))
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .findFirst()
                .map(PluginDefaultProperties::getPluginProperties)
                .map(PropertyUtil::getPropertiesValueFromJson)
                .orElse(properties));
    }
}
