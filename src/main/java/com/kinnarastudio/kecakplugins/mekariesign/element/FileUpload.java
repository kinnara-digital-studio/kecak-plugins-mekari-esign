package com.kinnarastudio.kecakplugins.mekariesign.element;

import org.joget.apps.form.model.*;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.UuidGenerator;
import org.joget.plugin.base.PluginWebSupport;
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

import static org.joget.workflow.util.WorkflowUtil.getHttpServletRequest;

public class FileUpload extends Element implements FormBuilderPaletteElement, FormStoreBinder, PluginWebSupport {

    private static final String UPLOAD_DIR = "/path/to/upload/dir/";

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String template = "<input type='file' name='pdfFile' accept='application/pdf' />";
        return template;
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
        return "<input type='file' name='pdfFile' accept='application/pdf' />";
    }

    @Override
    public FormRowSet store(Element element, FormRowSet formRowSet, FormData formData) {
        HttpServletRequest request = getHttpServletRequest();

        if (request instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            MultipartFile file = multipartRequest.getFile("pdfFile");

            if (file != null && !file.isEmpty()) {
                try {
                    String filename = UuidGenerator.getInstance().getUuid() + "_" + file.getOriginalFilename();
                    Files.copy(file.getInputStream(), Paths.get(UPLOAD_DIR + filename));

                    // Optionally, save the filename in the form data
                    FormRow row = new FormRow();
                    row.setProperty("pdfFile", filename);
                    formRowSet.add(row);

                } catch (IOException e) {
                    e.printStackTrace();
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
        return "1.0.0";
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
        return "{}";
    }

    public FormRowSet load(Element element, String primaryKey, FormData formData) {
        // Implement loading logic if needed
        return null;
    }

    public File getFile(String fileName) {
        File file = new File(UPLOAD_DIR + fileName);
        if (file.exists()) {
            return file;
        } else {
            return null;
        }
    }

    @Override
    public void webService(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {

    }
}
