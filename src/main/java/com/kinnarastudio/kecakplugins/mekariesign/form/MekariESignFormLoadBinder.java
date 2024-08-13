package com.kinnarastudio.kecakplugins.mekariesign.form;

import com.kinnarastudio.kecakplugins.mekariesign.webservice.MekariESignWebhook;
import org.joget.apps.app.dao.PluginDefaultPropertiesDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.*;
import org.joget.commons.util.FileManager;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.util.WorkflowUtil;

import com.kinnarastudio.commons.mekarisign.MekariSign;
import com.kinnarastudio.commons.mekarisign.exception.BuildingException;
import com.kinnarastudio.commons.mekarisign.exception.RequestException;
import com.kinnarastudio.commons.mekarisign.model.AuthenticationToken;
import com.kinnarastudio.commons.mekarisign.model.ResponseData;
import com.kinnarastudio.commons.mekarisign.model.ServerType;
import com.kinnarastudio.commons.mekarisign.model.TokenType;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.util.WorkflowUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class MekariESignFormLoadBinder extends FormBinder implements FormLoadElementBinder, PluginWebSupport {
    public final static String LABEL = "Mekari eSign Form Load Binder";

    @Override
    public FormRowSet load(Element element, String primaryKey, FormData formData) {
        MekariSign mekariSign;
        FormRowSet formRowSet = new FormRowSet();
        try {
            HttpSession session = WorkflowUtil.getHttpServletRequest().getSession();
            String token = (String) session.getAttribute("MekariToken");
            AuthenticationToken authToken = new AuthenticationToken(token, TokenType.BEARER, 3600, token, ServerType.valueOf(getPropertyString("serverType")));

            mekariSign = MekariSign.getBuilder()
                    .setAuthenticationToken(authToken)
                    .authenticateAndBuild();

            ResponseData document = mekariSign.getDocDetail(primaryKey);

            FormRow formRow = new FormRow();
            formRow.setProperty("id", document.getId());
            formRow.setProperty("type", document.getType());
            formRow.setProperty("filename", document.getAttributes().getFilename());
            formRow.setProperty("category", document.getAttributes().getCategory().toString());
            formRow.setProperty("docUrl", document.getAttributes().getDocUrl());
            formRowSet.add(formRow);

        } catch (BuildingException | RequestException | ParseException e) {
            e.printStackTrace();
        }
        return formRowSet;
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
        return AppUtil.readPluginResource(getClassName(), "/properties/form/MekariEsignFormLoadBinder.json");
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
        PluginDefaultPropertiesDao pluginDefaultPropertiesDao = (PluginDefaultPropertiesDao) AppUtil.getApplicationContext().getBean("pluginDefaultPropertiesDao");
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
    public void webService(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        int BYTE_ARRAY_BUFFER_SIZE = 4096;
        OutputStream os = httpServletResponse.getOutputStream();
        httpServletResponse.setHeader("Content-Type", "application/pdf");
        httpServletResponse.setHeader("Content-Disposition", "inline; filename=test.pdf");

        String path = httpServletRequest.getParameter("path");

        try (
                InputStream is = Files.newInputStream(new File(FileManager.getBaseDirectory() + "/" + path).toPath());
        ) {
            final byte[] buffer = new byte[BYTE_ARRAY_BUFFER_SIZE];
            int len;
            while ((len = is.read(buffer)) >= 0) {
                os.write(buffer, 0, len);
            }
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
//            os.flush();

        }
    }
}
