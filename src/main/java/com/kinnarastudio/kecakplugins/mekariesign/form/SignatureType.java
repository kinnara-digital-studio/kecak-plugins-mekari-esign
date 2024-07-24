package com.kinnarastudio.kecakplugins.mekariesign.form;

import com.kinnarastudio.commons.mekarisign.model.ServerType;
import org.joget.apps.app.dao.DatalistDefinitionDao;
import org.joget.apps.app.dao.UserviewDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListCollection;
import org.joget.apps.datalist.service.DataListService;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormBinder;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.userview.model.Userview;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.apps.userview.service.UserviewService;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class SignatureType extends FormBinder implements PluginWebSupport {
    public final static String LABEL = "Mekari Upload File";

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
        return "Handles signature elements in the form.";
    }

    @Override
    public void webService(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        String dataListId = httpServletRequest.getParameter("datalistId");
        String userviewId = httpServletRequest.getParameter("userviewId");
        String menuId = httpServletRequest.getParameter("menuId");
        String signatureType = httpServletRequest.getParameter("signatureType");

        Userview userview = getUserview(userviewId);
        UserviewMenu userviewMenu = getUserviewMenu(userview, menuId);
        DataList dataList = getDataList(dataListId);
        DataListCollection rows = dataList != null ? dataList.getRows() : null;

        String action = httpServletRequest.getParameter("actions");

        // Set the content type to HTML
        httpServletResponse.setContentType("text/html");

        // Render the dropdown for signature types
        httpServletResponse.getWriter().write("<select name='signatureType'>");
        httpServletResponse.getWriter().write("<option value='initial'" + ("initial".equals(signatureType) ? " selected" : "") + ">Initial</option>");
        httpServletResponse.getWriter().write("<option value='signature'" + ("signature".equals(signatureType) ? " selected" : "") + ">Signature</option>");
        httpServletResponse.getWriter().write("<option value='stamp'" + ("stamp".equals(signatureType) ? " selected" : "") + ">Stamp</option>");
        httpServletResponse.getWriter().write("</select>");
    }

    private Userview getUserview(String userviewId) {
        AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
        ApplicationContext applicationContext = AppUtil.getApplicationContext();
        UserviewService userviewService = (UserviewService) applicationContext.getBean("userviewService");
        UserviewDefinitionDao userviewDefinitionDao = (UserviewDefinitionDao) applicationContext.getBean("userviewDefinitionDao");
        return Optional.ofNullable(userviewId)
                .map(s -> userviewDefinitionDao.loadById(s, appDefinition))
                .map(UserviewDefinition::getJson)
                .map(s -> AppUtil.processHashVariable(s, null, null, null))
                .map(s -> userviewService.createUserview(s, null, false, AppUtil.getRequestContextPath(), null, null, false))
                .orElse(null);
    }

    protected DataList getDataList(String dataListId) {
        ApplicationContext applicationContext = AppUtil.getApplicationContext();
        DatalistDefinitionDao datalistDefinitionDao = (DatalistDefinitionDao) applicationContext.getBean("datalistDefinitionDao");
        DataListService dataListService = (DataListService) applicationContext.getBean("dataListService");
        AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
        DatalistDefinition datalistDefinition = datalistDefinitionDao.loadById(dataListId, appDefinition);
        if (datalistDefinition == null) {
            LogUtil.warn(getClassName(), "DataList Definition [" + dataListId + "] not found");
            return null;
        }

        DataList dataList = dataListService.fromJson(datalistDefinition.getJson());
        if (dataList == null) {
            LogUtil.warn(getClassName(), "DataList [" + dataListId + "] not found");
            return null;
        }

        dataList.setPageSize(DataList.MAXIMUM_PAGE_SIZE);
        return dataList;
    }

    private UserviewMenu getUserviewMenu(Userview userview, String userviewId) {
        return userview.getCategories().stream()
                .flatMap(c -> c.getMenus().stream())
                .filter(m -> !userviewId.isEmpty() && userviewId.equalsIgnoreCase(m.getPropertyString("id")))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public String getClassName() {
        return SignatureType.class.getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/signature.json");
    }

    public FormRowSet load(Element element, String primaryKey, FormData formData) {
        // Implement the logic to load form data
        return null;
    }

    public String store(Element element, FormRowSet rowSet, FormData formData) {
        // Implement the logic to store form data
        return null;
    }

}
