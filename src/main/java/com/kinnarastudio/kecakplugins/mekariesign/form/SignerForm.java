package com.kinnarastudio.kecakplugins.mekariesign.form;

import com.kinnarastudio.kecakplugins.mekariesign.webservice.MekariESignWebhook;
import org.joget.apps.app.dao.PluginDefaultPropertiesDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.*;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.util.WorkflowUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public class SignerForm extends FormBinder implements FormBuilderPaletteElement {
    public final static String LABEL = "Mekari Signers Form Element";

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
    public String getLabel() {
        return LABEL;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getName(), "/properties/form/signers.json");
    }

    @Override
    public Object getProperty(String property) {
        PluginDefaultPropertiesDao pluginDefaultPropertiesDao = (PluginDefaultPropertiesDao) AppUtil.getApplicationContext().getBean("pluginDefaultPropertiesDao");
        AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();

        return Optional.ofNullable(pluginDefaultPropertiesDao.getPluginDefaultPropertiesList(MekariESignWebhook.class.getName(), appDefinition, null, null, null, 1))
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
        PluginDefaultPropertiesDao pluginDefaultPropertiesDao = (PluginDefaultPropertiesDao) AppUtil.getApplicationContext();
        AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();

        return Optional.ofNullable(pluginDefaultPropertiesDao.getPluginDefaultPropertiesList(MekariESignWebhook.class.getName(), appDefinition, null, null, null, 1))
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

        return Optional.ofNullable(pluginDefaultPropertiesDao.getPluginDefaultPropertiesList(MekariESignWebhook.class.getName(), appDefinition, null, null, null, 1))
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .findFirst()
                .map(PluginDefaultProperties::getPluginProperties)
                .map(PropertyUtil::getPropertiesValueFromJson)
                .orElseGet(super::getProperties);
    }

    protected String getSessionAttribute(String name) {
        return Optional.ofNullable(WorkflowUtil.getHttpServletRequest())
                .map(HttpServletRequest::getSession)
                .map(s -> s.getAttribute(name))
                .map(String::valueOf)
                .orElse("");
    }

    @Override
    public String getFormBuilderCategory() {
        return "signer form";
    }

    @Override
    public int getFormBuilderPosition() {
        return 100;
    }

    @Override
    public String getFormBuilderIcon() {
        return null;
    }

    @Override
    public String getDefaultPropertyValues() {
        return "signer form";
    }

    @Override
    public String getFormBuilderTemplate() {
        return "<h1>Signer Form</h1>";
    }
}
