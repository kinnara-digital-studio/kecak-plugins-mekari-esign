package com.kinnarastudio.kecakplugins.mekariesign.userview;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.kinnarastudio.kecakplugins.mekariesign.datalist.MekariESignDatalistAction;
import com.kinnarastudio.kecakplugins.mekariesign.datalist.MekariESignInboxDataListBinder;
import org.joget.apps.app.dao.PluginDefaultPropertiesDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.*;
import org.joget.apps.datalist.service.DataListService;
import org.joget.apps.userview.model.Userview;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.context.ApplicationContext;

import com.kinnarastudio.commons.mekarisign.exception.RequestException;
import com.kinnarastudio.commons.mekarisign.model.ServerType;

public class MekariESignUserviewMenu extends UserviewMenu {
    public final static String LABEL = "Mekari eSign Userview Menu";

    private DataList cachedDataList = null;


    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getLabel() {
        return LABEL;
    }

    @Override
    public String getPropertyOptions() {
        return "";
        // return AppUtil.readPluginResource(getClassName(), "/properties/userview/MekariEsignUserviewMenu.json");
    }

    @Override
    public String getPropertyString(String property) {
        PluginDefaultPropertiesDao pluginDefaultPropertiesDao = (PluginDefaultPropertiesDao) AppUtil.getApplicationContext().getBean("pluginDefaultPropertiesDao");
        AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();

        return Optional.ofNullable(pluginDefaultPropertiesDao.getPluginDefaultPropertiesList(getClassName(), appDefinition, null, null, null, 1))
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
    public String getDescription() {
        return getClass().getPackage().getImplementationTitle();
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
    public String getCategory() {
        return "Mekari";
    }

    @Override
    public String getDecoratedMenu() {
        return null;
    }

    @Override
    public String getIcon() {
        return null;
    }

    @Override
    public String getRenderPage() {
        ApplicationContext appContext = AppUtil.getApplicationContext();
        PluginManager pluginManager = (PluginManager) appContext.getBean("pluginManager");

        final Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("clientId", getPropertyString("clientId"));
        dataModel.put("serverUrl", ServerType.valueOf(getPropertyString("serverType")).getSsoBaseUrl());

        return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClass().getName(), "/templates/mekariUserview.ftl", null);
    }

    @Override
    public String getJspPage() {
        String mekariToken = (String) WorkflowUtil.getHttpServletRequest().getSession().getAttribute("MekariToken");
        if (mekariToken == null || mekariToken.isEmpty() || mekariToken.equals("")) {
            return null;
        } else {
            return getJspPage("userview/plugin/form.jsp", "userview/plugin/datalist.jsp", "userview/plugin/unauthorized.jsp");
        }
    }

    protected String getJspPage(String jspFormFile, String jspListFile, String jspUnauthorizedFile) {
        String mode = Optional.ofNullable(getRequestParameterString("_mode")).orElse("");
        switch (mode) {
            case "newRequest":
                return jspUnauthorizedFile;
            default:
                getJspDataList();
                return jspListFile;
        }
    }


    protected String getJspForm(String jspFormFile, String jspUnauthorizedFile) {
        return jspUnauthorizedFile;
    }

    protected void getJspDataList() {
        try {
            DataList dataList = getDataList();
            if (dataList != null) {
                dataList.setCheckboxPosition("no");
                DataListActionResult ac = dataList.getActionResult();
                if (ac != null) {
                    if (ac.getMessage() != null && !ac.getMessage().isEmpty()) {
                        setAlertMessage(ac.getMessage());
                    }
                    if (ac.getType() != null && "REDIRECT".equals(ac.getType()) && ac.getUrl() != null && !ac.getUrl().isEmpty()) {
                        if ("REFERER".equals(ac.getUrl())) {
                            HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
                            if (request != null && request.getHeader("Referer") != null) {
                                setRedirectUrl(request.getHeader("Referer"));
                            } else {
                                setRedirectUrl("REFERER");
                            }
                        } else {
                            setRedirectUrl(ac.getUrl());
                        }
                    }
                }
                setProperty("dataList", dataList);
            } else {
                setProperty("error", ("Data List \"" + getPropertyString("datalistId") + "\" not exist."));
            }
        } catch (Exception ex) {
            StringWriter out = new StringWriter();
            ex.printStackTrace(new PrintWriter(out));
            String message = ex.toString();
            message = message + "\r\n<pre class=\"stacktrace\">" + out.getBuffer() + "</pre>";
            setProperty("error", message);
        }
    }

    protected DataList getDataList() {
        ApplicationContext ac = AppUtil.getApplicationContext();
        DataListService dataListService = (DataListService) ac.getBean("dataListService");
        if (cachedDataList == null) {
            final Object[] args = new Object[]{
                    MekariESignDatalistAction.class.getName(),
                    MekariESignInboxDataListBinder.class.getName()
            };

            String dataListJson = AppUtil.readPluginResource(getClassName(), "/jsonDefinitions/mekariDocsList.json", args, true);
            cachedDataList = dataListService.fromJson(dataListJson);
            if (getPropertyString(Userview.USERVIEW_KEY_NAME) != null && getPropertyString(Userview.USERVIEW_KEY_NAME).trim().length() > 0) {
                cachedDataList.addBinderProperty(Userview.USERVIEW_KEY_NAME, getPropertyString(Userview.USERVIEW_KEY_NAME));
            }
            if (getKey() != null && getKey().trim().length() > 0) {
                cachedDataList.addBinderProperty(Userview.USERVIEW_KEY_VALUE, getKey());
            }
        }
        return cachedDataList;
    }

    @Override
    public boolean isHomePageSupported() {
        return true;
    }

    protected String getSessionAttribute(String name) {
        return Optional.ofNullable(WorkflowUtil.getHttpServletRequest())
                .map(HttpServletRequest::getSession)
                .map(s -> s.getAttribute(name))
                .map(String::valueOf)
                .orElse("");
    }
}
