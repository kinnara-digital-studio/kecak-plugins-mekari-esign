package com.kinnarastudio.kecakplugins.mekariesign.form;

import com.kinnarastudio.commons.mekarisign.model.ServerType;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.lib.FileUpload;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRowSet;
import org.joget.plugin.base.PluginManager;

import java.util.ResourceBundle;

public class MekariESignFileUpload extends FileUpload {
    public final static String LABEL = "Mekari eSign File Upload";

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
    public String getDescription() {
        return getClass().getPackage().getImplementationTitle();
    }

    @Override
    public FormRowSet formatData(FormData formData) {

        return super.formatData(formData);
    }

    @Override
    public String getLabel() {
        return LABEL;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    protected String getAuthorizedCode() {
        // TODO
        return "";
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/MekariESignTool.json", null, true, "/messages/HierarchicalCrudMenu");
    }

    protected String getClientId () {
        return getPropertyString("clientId");
    }

    protected String getClientSecret() {
        return getPropertyString("clientSecret");
    }

    protected ServerType getServerType() {
        switch(getPropertyString("serverType")) {
            case "sandbox":
                return ServerType.SANDBOX;
            case "staging":
                return ServerType.STAGING;
            case "production":
                return ServerType.PRODUCTION;
            default:
                return ServerType.MOCK;
        }
    }
}
