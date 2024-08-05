package com.kinnarastudio.kecakplugins.mekariesign.element;

import com.kinnarastudio.commons.Try;
import com.kinnarastudio.commons.mekarisign.MekariSign;
import com.kinnarastudio.kecakplugins.mekariesign.form.MekariESignFormLoadBinder;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.*;
import org.joget.apps.form.service.FormUtil;
import org.joget.directory.model.Department;
import org.joget.directory.model.Employment;
import org.joget.directory.model.Organization;
import org.joget.directory.model.User;
import org.joget.directory.model.service.ExtDirectoryManager;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.WorkflowUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.joget.workflow.util.WorkflowUtil.getHttpServletRequest;

public class FilePreview extends Element implements FormBuilderPaletteElement, FileDownloadSecurity, FormStoreBinder, PropertyEditable {

    private static final String PREVIEW_DIR = "/path/to/upload/dir/";
    private FormStoreBinder secondaryBinder = null;

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String template = "filePreview.ftl";
        final String primaryKeyValue = getPrimaryKeyValue(formData);
        final String value = FormUtil.getElementPropertyValue(this, formData);
        String encodedFileName = value;
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
        } else if (isMeterai()){
            stampFile = "/web/json/plugin/" + MekariSign.class.getName() + "/service";
        } else {
            stampFile = "";
        }

        dataModel.put("stampFile", stampFile);
        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }

    protected boolean isSignature() {
        return "signature".equalsIgnoreCase(getPropertyString("stampType"));
    }

    protected boolean isInitial() {
        return "initial".equalsIgnoreCase(getPropertyString("stampType"));
    }

    protected boolean isMeterai() {
        return "meterai".equalsIgnoreCase(getPropertyString("stampType"));
    }

    protected String getPdfFileName(FormData formData) {
        return Optional.of(formData)
                .map(fd -> fd.getLoadBinderData(this))
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .findFirst()
                .map(r -> r.getProperty(getPropertyString(FormUtil.PROPERTY_ID)))
                .orElseThrow(() -> new RuntimeException("File not found"));
    }

    protected int getPagePosition(String positions) {
        return getPositionIndex(positions, 0, Try.onFunction(Integer::parseInt, (RuntimeException e) -> 1));
    }

    protected float getTopPosition(String positions) {
        return getPositionIndex(positions, 1, Try.onFunction(Float::parseFloat, (RuntimeException e) -> 0f));
    }

    protected float getLeftPosition(String positions) {
        return getPositionIndex(positions, 2, Try.onFunction(Float::parseFloat, (RuntimeException e) -> 0f));
    }

    protected float getScaleXPosition(String positions) {
        return getPositionIndex(positions, 4, Try.onFunction(Float::parseFloat, (RuntimeException e) -> 1f));
    }

    protected float getScaleYPosition(String positions) {
        return getPositionIndex(positions, 3, Try.onFunction(Float::parseFloat, (RuntimeException e) -> 1f));
    }

    protected <T> T getPositionIndex(String positions, int index, Function<String, T> parser) {
        return Optional.of(positions)
                .map(s -> s.split(";"))
                .map(Arrays::stream)
                .orElseGet(Stream::empty)
                .skip(index)
                .findFirst()
                .map(parser)
                .orElseThrow(() -> new RuntimeException("Invalid positions [" + positions + "] at index [" + index + "]"));
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

        if (filePath != null && !filePath.isEmpty()) {
            FormRow row = new FormRow();
            row.setProperty("filePath", filePath);
            formRowSet.add(row);
        }

        return formRowSet;
    }

    @Override
    public String getName() {
        return "FilePreview";
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
        final String[] args = new String[]{ MekariESignFormLoadBinder.class.getName() };
        return AppUtil.readPluginResource(getClass().getName(), "properties/form/filePreview.json", args, true, "/message/DigitalSignature");
    }

    protected String getPdfFileName(Element element, FormData formData) {
        return Optional.of(formData)
                .map(fd -> fd.getLoadBinderData(element))
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .findFirst()
                .map(r -> {
                    final String elementId = element.getPropertyString(FormUtil.PROPERTY_ID);
                    return r.getProperty(elementId);
                })
                .orElseThrow(() -> new RuntimeException("File not found"));
    }

    protected boolean overrideSignature() {
        // return "true".equalsIgnoreCase(getPropertyString("override"));
        return false;
    }

    protected String getReason(FormData formData) {
        final String propValue = getPropertyString("reason");
        if (!propValue.isEmpty()) {
            return propValue;
        }

        WorkflowManager wm = (WorkflowManager) WorkflowUtil.getApplicationContext().getBean("workflowManager");
        return Optional.of(formData)
                .map(FormData::getActivityId)
                .map(wm::getActivityById)
                .map(WorkflowActivity::getName)
                .orElse("");
    }

    protected String getStateOrProvince() {
        return getPropertyString("stateOrProvince");
    }

    protected String getCountry() {
        return getPropertyString("country");
    }

    protected String getLocality() {
        return getPropertyString("locality");
    }

    protected String getOrganizationalUnit() {
        final String propValue = getPropertyString("organizationalUnit");
        if (!propValue.isEmpty()) {
            return propValue;
        }

        final ExtDirectoryManager directoryManager = (ExtDirectoryManager) AppUtil.getApplicationContext().getBean("directoryManager");

        final String username = WorkflowUtil.getCurrentUsername();
        final User user = directoryManager.getUserById(username);
        final Set<Employment> employments = (Set<Employment>) user.getEmployments();

        return Optional.ofNullable(employments)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .map(Employment::getDepartment)
                .filter(Objects::nonNull)
                .map(Department::getName)  // Correct use of Department class
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("");
    }

    protected String getOrganization() {
        final String propValue = getPropertyString("organization");
        if (!propValue.isEmpty()) {
            return propValue;
        }

        final ExtDirectoryManager directoryManager = (ExtDirectoryManager) AppUtil.getApplicationContext().getBean("directoryManager");

        final String orgId = WorkflowUtil.getCurrentUserOrgId();
        return Optional.of(orgId)
                .map(directoryManager::getOrganization)
                .map(Organization::getName)  // Correct use of Organization class
                .orElse("");
    }

    protected boolean useTimeStamp() {
        return "true".equalsIgnoreCase(getPropertyString("useTimeStamp"));
    }

    protected String getTsaUrl() {
        return getPropertyString("tsaUrl");
    }

    protected String getTsaUsername() {
        return getPropertyString("tsaUsername");
    }

    protected String getTsaPassword() {
        return getPropertyString("tsaPassword");
    }
}
