package com.kinnarastudio.kecakplugins.mekariesign.datalist;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.joget.apps.app.dao.PluginDefaultPropertiesDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListColumnFormatDefault;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.util.WorkflowUtil;

import com.kinnarastudio.commons.mekarisign.MekariSign;
import com.kinnarastudio.commons.mekarisign.exception.BuildingException;
import com.kinnarastudio.commons.mekarisign.exception.RequestException;
import com.kinnarastudio.commons.mekarisign.model.AuthenticationToken;
import com.kinnarastudio.commons.mekarisign.model.ServerType;
import com.kinnarastudio.commons.mekarisign.model.TokenType;
import com.kinnarastudio.kecakplugins.mekariesign.webservice.MekariESignWebhook;

public class MekariESignDatalistColumnFormatter extends DataListColumnFormatDefault{
    public final static String LABEL = "Mekari eSign Datalist Formatter";

    @Override
    public String format(DataList dataList, DataListColumn dataListColumn, Object arg2, Object arg3) {
        String filename = arg3.toString();
        try {
            HttpSession session = WorkflowUtil.getHttpServletRequest().getSession();
            String token = (String) session.getAttribute("MekariToken");
            AuthenticationToken authToken = new AuthenticationToken(token, TokenType.BEARER, 3600, token, ServerType.valueOf(getPropertyString("serverType")));
        
            MekariSign mekariSign = MekariSign.getBuilder()
                        .setAuthenticationToken(authToken)
                        .authenticateAndBuild();

            HttpServletResponse servletResponse = WorkflowUtil.getHttpServletResponse();

            String contentType = "application/pdf";
            servletResponse.setContentType(contentType);
            servletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            File file = File.createTempFile("test", ".pdf", new File("/home/user/Documents/"));
            file.setWritable(true);

            mekariSign.downloadDoc("2fde019a-5d5f-414d-b47a-6100dc117d02", file);
        } catch (BuildingException | IOException | RequestException e) {
            e.printStackTrace();
        }
        return "<a href=\"" + filename + "\">" + filename + "</a>";
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
    
}
