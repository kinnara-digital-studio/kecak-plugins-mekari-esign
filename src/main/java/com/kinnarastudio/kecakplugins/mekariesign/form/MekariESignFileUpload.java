package com.kinnarastudio.kecakplugins.mekariesign.form;

import com.kinnarastudio.commons.mekarisign.MekariSign;
import com.kinnarastudio.commons.mekarisign.model.AuthenticationToken;
import com.kinnarastudio.commons.mekarisign.model.ServerType;
import com.kinnarastudio.commons.mekarisign.model.TokenType;
import com.kinnarastudio.commons.mekarisign.exception.BuildingException;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.lib.FileUpload;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRowSet;
import org.joget.plugin.base.PluginManager;
import org.joget.commons.util.FileManager;
import org.joget.workflow.util.WorkflowUtil;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

            // Get authorized code for the file
            String primaryKey = "your-primary-key"; // Replace with the actual primary key logic if needed
            String authorizedCode = getAuthorizedCode(primaryKey);
            if (authorizedCode != null) {
                formData.addFormResult("authorizedCode", authorizedCode);
            } else {
                formData.addFormError("file", "Failed to get authorization code.");
            }
        }
        return rowSet;
    }

    private boolean isPDF(File file) {
        if (file != null) {
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                if (fis.read(buffer) != 4096) {
                    return false;
                }
                String header = new String(buffer);
                return header.equals("%PDF");
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
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

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/FileUpload.json", null, true, "/messages/HierarchicalCrudMenu");
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

    protected String getAuthorizedCode(String primaryKey) {
        // Implementasi untuk mendapatkan kode otorisasi
        HttpSession session = WorkflowUtil.getHttpServletRequest().getSession();
        String token = (String) session.getAttribute("MekariToken");
        AuthenticationToken authToken = new AuthenticationToken(token, TokenType.BEARER, 3600, token, getServerType());

        MekariSign mekariSign;
        try {
            mekariSign = MekariSign.getBuilder()
                    .setAuthenticationToken(authToken)
                    .authenticateAndBuild();
            // Replace the following with the actual logic to get the authorization code from MekariSign API
            String authorizedCode = "dummy-authorization-code"; // Placeholder for actual authorization code logic
            return authorizedCode;
        } catch (BuildingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
