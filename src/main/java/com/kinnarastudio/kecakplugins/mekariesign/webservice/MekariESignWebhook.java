package com.kinnarastudio.kecakplugins.mekariesign.webservice;

import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.plugin.base.PluginWebSupport;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;

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

            servletResponse.getWriter().write(responseBody.toString());
        } catch (JSONException e) {
            LogUtil.error(getClass().getName(), e, e.toString());
            servletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, e.toString());
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
        return "";
    }
}
