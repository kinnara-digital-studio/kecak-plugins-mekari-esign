package com.kinnarastudio.kecakplugins.mekariesign.element;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.*;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.UuidGenerator;
import org.joget.plugin.base.PluginManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.ResourceBundle;

import static org.joget.workflow.util.WorkflowUtil.getHttpServletRequest;

public class FileUpload extends org.joget.apps.form.lib.FileUpload{

    private static final String UPLOAD_DIR_PROPERTY = "uploadPath";
    private static final String ACCEPTED_FILE_TYPES = "acceptedFileTypes";
    private static final String DEFAULT_UPLOAD_DIR = "/path/to/upload/dir/";

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        return "<input type='file' name='pdfFile' accept='" + getPropertyString(ACCEPTED_FILE_TYPES) + "' />";
    }

    @Override
    public String getFormBuilderCategory() {
        return "Custom Elements";
    }

    @Override
    public int getFormBuilderPosition() {
        return 1;
    }

    @Override
    public String getFormBuilderIcon() {
        return "<i class='fas fa-file-upload'></i>";
    }

    @Override
    public String getFormBuilderTemplate() {
        return "<input type='file' name='pdfFile' accept='" + getPropertyString(ACCEPTED_FILE_TYPES) + "' />";
    }

    public FormRowSet store(Element element, FormRowSet formRowSet, FormData formData) {
        HttpServletRequest request = getHttpServletRequest();

        if (request instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            MultipartFile file = multipartRequest.getFile("pdfFile");

            if (file != null && !file.isEmpty()) {
                try {
                    String uploadDir = getPropertyString(UPLOAD_DIR_PROPERTY);
                    if (uploadDir == null || uploadDir.isEmpty()) {
                        uploadDir = DEFAULT_UPLOAD_DIR;
                    }

                    String filename = UuidGenerator.getInstance().getUuid() + "_" + file.getOriginalFilename();
                    Files.copy(file.getInputStream(), Paths.get(uploadDir + filename));

                    // Optionally, save the filename in the form data
                    FormRow row = new FormRow();
                    row.setProperty("pdfFile", filename);
                    formRowSet.add(row);

                } catch (IOException e) {
                    LogUtil.error(getClassName(), e, e.getMessage());
                }
            }
        }

        return formRowSet;
    }

    @Override
    public String getName() {
        return "FileUpload";
    }

    @Override
    public String getVersion() {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        ResourceBundle resourceBundle = pluginManager.getPluginMessageBundle(getClassName(), "/message/BuildNumber");
//        String buildNumber = resourceBundle.getString("buildNumber");
        return "Form Element";
    }

    @Override
    public String getDescription() {
        return "A custom element for uploading PDF files";
    }

    @Override
    public String getLabel() {
        return "PDF File Upload";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/form/FileUpload.json");
    }

    public FormRowSet load(Element element, String primaryKey, FormData formData) {
        return null;
    }

    public File getFile(String fileName) {
        String uploadDir = getPropertyString(UPLOAD_DIR_PROPERTY);
        if (uploadDir == null || uploadDir.isEmpty()) {
            uploadDir = DEFAULT_UPLOAD_DIR;
        }

        File file = new File(uploadDir + fileName);
        if (file.exists()) {
            return file;
        } else {
            return null;
        }
    }

    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if ("POST".equalsIgnoreCase(request.getMethod()) && request instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            MultipartFile file = multipartRequest.getFile("pdfFile");

            if (file != null && !file.isEmpty()) {
                try {
                    String uploadDir = getPropertyString(UPLOAD_DIR_PROPERTY);
                    if (uploadDir == null || uploadDir.isEmpty()) {
                        uploadDir = DEFAULT_UPLOAD_DIR;
                    }

                    String filename = UuidGenerator.getInstance().getUuid() + "_" + file.getOriginalFilename();
                    Files.copy(file.getInputStream(), Paths.get(uploadDir + filename));

                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("{\"status\":\"success\",\"message\":\"File uploaded successfully\",\"fileName\":\"" + filename + "\"}");
                } catch (IOException e) {
                    LogUtil.error(getClassName(), e, e.getMessage());
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"status\":\"error\",\"message\":\"File is empty or not provided\"}");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Only POST method is allowed\"}");
        }
    }
}
