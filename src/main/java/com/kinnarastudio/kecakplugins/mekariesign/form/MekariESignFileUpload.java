package com.kinnarastudio.kecakplugins.mekariesign.form;

import com.kinnarastudio.commons.mekarisign.MekariSign;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.lib.FileUpload;
import org.joget.apps.form.model.*;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.FileManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.model.PropertyEditable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.Map;
import java.util.ResourceBundle;

import static org.joget.workflow.util.WorkflowUtil.getHttpServletRequest;

public class MekariESignFileUpload extends FileUpload implements FormBuilderPaletteElement, FileDownloadSecurity, FormStoreBinder, PropertyEditable {

    public final static int BYTE_ARRAY_BUFFER_SIZE = 4096;
    private static final String PREVIEW_DIR = "/path/to/upload/dir/";
    private static final String LABEL = "Mekari E-Sign File Upload";

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String template = "MekariESignFileUpload.ftl";
        final String primaryKeyValue = getPrimaryKeyValue(formData);
        final String value = FormUtil.getElementPropertyValue(this, formData);
        String encodedFileName = value;

        final AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
        dataModel.put("appId", appDefinition.getAppId());
        dataModel.put("appVersion", appDefinition.getVersion());

        try {
            encodedFileName = URLEncoder.encode(value, "UTF8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException ignored) {
        }

        final AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        final Form form = FormUtil.findRootForm(this);

        if (appDef != null && form != null) {
            final String appId = appDef.getId();
            final String appVersion = appDef.getVersion().toString();
            final String formDefId = form.getPropertyString(FormUtil.PROPERTY_ID);
            final String pdfPath = "/web/client/app/" + appId + "/" + appVersion + "/form/download/" + formDefId + "/" + primaryKeyValue + "/" + encodedFileName + ".";
            dataModel.put("pdfFile", pdfPath);
        }

        dataModel.put("className", getClassName());

        final String stampFile;
        if (isSignature()) {
            stampFile = "/web/json/plugin/" + MekariSign.class.getName() + "/service";
        } else if (isMeterai()) {
            stampFile = "/web/json/plugin/" + MekariSign.class.getName() + "/service";
        } else {
            stampFile = "";
        }

        dataModel.put("stampFile", stampFile);

        String nonce = SecurityUtil.generateNonce(new String[]{getClassName(), appDef.getAppId(), appDef.getVersion().toString()}, 1);
        dataModel.put("nonce", nonce);

        return FormUtil.generateElementHtml(this, formData, template, dataModel);
    }

    protected boolean isSignature() {
        return "signature".equalsIgnoreCase(getPropertyString("stampType"));
    }

    protected boolean isMeterai() {
        return "meterai".equalsIgnoreCase(getPropertyString("stampType"));
    }

    @Override
    public FormRowSet formatData(FormData formData) {
        FormRowSet rowSet = super.formatData(formData);
        if (rowSet != null && !rowSet.isEmpty()) {
            FormRow row = rowSet.iterator().next();
            //row.forEach((key,value) -> {LogUtil.info(getClassName(),"key: " + key + "value: " + value);});
            String filePath = FileManager.getBaseDirectory() + "/" + row.getTempFilePath(getPropertyString("id"));

//            String filePath = rowSet.iterator().next().getProperty("filePath");
            File file = new File(filePath);
//            LogUtil.info(getClassName(), "File: " + file.getName());
            System.out.println(file.getAbsolutePath());
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
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[4];
                if (fis.read(buffer) != 4) {
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
    public boolean isDownloadAllowed(Map dataModel) {
        // Implement your security logic here
        return true;
    }

    @Override
    public String getFormBuilderCategory() {
        return "Custom Elements";
    }

    @Override
    public int getFormBuilderPosition() {
        return 200;
    }

    @Override
    public String getFormBuilderIcon() {
        return "<i class='fas fa-file'></i>";
    }

    @Override
    public String getFormBuilderTemplate() {
        return "<label class='label' style='position:absolute;top:10px;left:10px;'>" + getName() + "</label><div style='border: 5px solid grey;height:100px;background-color:#EFF1F2;color:#C4C7CB;align:center;'><span style='position:absolute;top:10px;left:270px;font-weight:bold;font-size:70px;align:center;'>PDF</span><div>";
    }

    @Override
    public FormRowSet store(Element element, FormRowSet formRowSet, FormData formData) {
        HttpServletRequest request = getHttpServletRequest();
        String filePath = request.getParameter("filePath");
        LogUtil.info(filePath, "req");
        if (filePath != null && !filePath.isEmpty()) {
            FormRow row = new FormRow();
            row.setProperty("filePath", filePath);
            formRowSet.add(row);
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
        return resourceBundle.getString("buildNumber");
    }

    @Override
    public String getDescription() {
        return getClass().getPackage().getImplementationTitle();
    }

    @Override
    public String getLabel() {
        return getName();
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "properties/form/MekariESignFileUpload.json", null, true, "/messages/MekariESignTool");
    }

    // Other methods as needed

    public String getEditable() {
        // Implement your logic for editable properties
        return "true";
    }

    public void setEditable(String editable) {
        // Implement your logic for setting editable properties
    }

    @Override
    public void webService(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        String caller = httpServletRequest.getParameter("_caller");
        LogUtil.info(getClassName(), "webSercice : _caller [" + caller + "]");

        if (MekariESignFileUpload.class.getName().equals(caller)) {
            AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
            String nonce = httpServletRequest.getParameter("_nonce");
            String appId = appDefinition.getAppId();
            String appVersion = appDefinition.getVersion().toString();

            if (!SecurityUtil.verifyNonce(nonce, new String[]{getClassName(), appId, appVersion})) {
                httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, ResourceBundleUtil.getMessage("general.error.error403"));
                return;
            }

            OutputStream os = httpServletResponse.getOutputStream();
            httpServletResponse.setHeader("Content-Type", "application/pdf");
            httpServletResponse.setHeader("Content-Disposition", "inline; filename=test.pdf");

            String path = httpServletRequest.getParameter("_path");
            Files.copy(new File(FileManager.getBaseDirectory() + "/" + path).toPath(), os);

        } else {
            super.webService(httpServletRequest, httpServletResponse);
        }
    }
}
