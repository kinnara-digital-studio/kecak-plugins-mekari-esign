package com.kinnarastudio.kecakplugins.mekariesign.userview;

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

public class SignatureType extends UserviewMenu implements PluginWebSupport {
    public final static String LABEL = "Mekari Upload File";
    @Override
    public String getCategory() {
        return "Mekari";
    }

    @Override
    public String getIcon() {
        return null;  // Provide icon if available
    }

    @Override
    public String getRenderPage() {
        ApplicationContext appContext = AppUtil.getApplicationContext();
        PluginManager pluginManager = (PluginManager) appContext.getBean("pluginManager");
        final Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("signatureType", getPropertyString("signatureType"));

        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClass().getName(), "/templates/uploadFile.ftl", null);
    }

    @Override
    public boolean isHomePageSupported() {
        return true;
    }

    @Override
    public String getDecoratedMenu() {
        return null;  // Implement if menu decoration is required
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
        return "Handles signature elements in the userview.";
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

        // Additional logic for handling the selected signature type can be added here
        // For example, based on signatureType, you could query specific data or render different UI elements
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
        return LABEL;
    }

    @Override
    public String getClassName() {
        return SignatureType.class.getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/userview/signature.json");
    }
}
