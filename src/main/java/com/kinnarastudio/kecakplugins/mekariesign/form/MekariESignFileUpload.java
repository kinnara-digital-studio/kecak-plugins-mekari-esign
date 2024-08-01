package com.kinnarastudio.kecakplugins.mekariesign.form;

import com.kinnarastudio.commons.mekarisign.MekariSign;
import com.kinnarastudio.commons.mekarisign.exception.BuildingException;
import com.kinnarastudio.commons.mekarisign.model.AuthenticationToken;
import com.kinnarastudio.commons.mekarisign.model.ServerType;
import com.kinnarastudio.commons.mekarisign.model.TokenType;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.lib.FileUpload;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRowSet;
import org.joget.plugin.base.PluginManager;
import org.joget.commons.util.FileManager;
import org.joget.workflow.util.WorkflowUtil;

import javax.servlet.http.HttpSession;
import java.io.File;
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
        // Get the uploaded file
        FormRowSet rowSet = super.formatData(formData);
        if (rowSet != null && !rowSet.isEmpty()) {
            String filePath = rowSet.iterator().next().getProperty("filePath");
            File file = FileManager.getFileByPath(filePath);

            // Check if the file is a PDF
            if (!isPDF(file)) {
                formData.addFormError("file", "Only PDF files are allowed.");
                return null;
            }
        }
        return rowSet;
    }

    private boolean isPDF(File file) {
        if (file != null) {
            String fileName = file.getName().toLowerCase();
            return fileName.endsWith(".pdf");
        }
        return false;
    }

    @Override
    public String getLabel() {
        return LABEL;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    protected String getAuthorizedCode(String primaryKey) {
        // Implementasi untuk mendapatkan kode otorisasi
        MekariSign mekariSign;
        HttpSession session = WorkflowUtil.getHttpServletRequest().getSession();
        String token = (String) session.getAttribute("MekariToken");
        AuthenticationToken authToken = new AuthenticationToken(token, TokenType.BEARER, 3600, token, getServerType());

        try {
            mekariSign = MekariSign.getBuilder()
                    .setAuthenticationToken(authToken)
                    .authenticateAndBuild();
        } catch (BuildingException e) {
            throw new RuntimeException(e);
        }

        try {
            String authorizedCode = getAuthorizedCode(primaryKey);
            return authorizedCode;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/MekariESignTool.json", null, true, "/messages/HierarchicalCrudMenu");
    }

    protected String getClientId() {
        return getPropertyString("clientId");
    }

    protected String getClientSecret() {
        return getPropertyString("clientSecret");
    }

    protected ServerType getServerType() {
        switch (getPropertyString("serverType")) {
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
