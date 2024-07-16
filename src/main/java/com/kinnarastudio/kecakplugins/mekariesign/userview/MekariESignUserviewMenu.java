package com.kinnarastudio.kecakplugins.mekariesign.userview;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.plugin.base.PluginManager;
import org.springframework.context.ApplicationContext;

import com.kinnarastudio.commons.mekarisign.model.ServerType;

public class MekariESignUserviewMenu extends UserviewMenu{
    public final static String LABEL = "Mekari eSign Userview Menu";

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getLabel() {
        return LABEL;
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/userview/MekariEsignUserviewMenu.json");
    }

    @Override
    public String getDescription() {
        return "kecak-plugins-mekari-esign";
    }

    @Override
    public String getName() {
        return LABEL;
    }

    @Override
    public String getVersion() {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        ResourceBundle resourceBundle = pluginManager.getPluginMessageBundle(getClassName(), "/messages/BuildNumber");
        String buildNumber = resourceBundle.getString("buildNumber");
        return buildNumber;
    }

    @Override
    public String getCategory() {
        return "Mekari";
    }

    @Override
    public String getDecoratedMenu() {
        return null;
    }

    @Override
    public String getIcon() {
        return null;
    }

    @Override
    public String getRenderPage() {
        ApplicationContext appContext = AppUtil.getApplicationContext();
        PluginManager pluginManager = (PluginManager) appContext.getBean("pluginManager");
        final Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("clientId", getPropertyString("clientId"));
        dataModel.put("serverUrl", ServerType.valueOf(getPropertyString("serverType")).getSsoBaseUrl());
        return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClass().getName(), "/templates/mekari.ftl", null);
    }

    @Override
    public boolean isHomePageSupported() {
        return true;
    }
    
}
