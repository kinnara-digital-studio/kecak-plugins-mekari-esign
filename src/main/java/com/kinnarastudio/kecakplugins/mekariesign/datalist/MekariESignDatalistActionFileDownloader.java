package com.kinnarastudio.kecakplugins.mekariesign.datalist;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
import org.joget.apps.datalist.model.DataListActionDefault;
import org.joget.apps.datalist.model.DataListActionResult;
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

public class MekariESignDatalistActionFileDownloader extends DataListActionDefault{
    public final static String LABEL = "Mekari eSign Datalist Action File Downloader";

    @Override
    public DataListActionResult executeAction(DataList dataList, String[] rowKeys) {
        DataListActionResult result = new DataListActionResult();
        
        // only allow POST
        HttpServletRequest servletRequest = WorkflowUtil.getHttpServletRequest();
        HttpServletResponse servletResponse = WorkflowUtil.getHttpServletResponse();
        if (servletRequest != null && !"POST".equalsIgnoreCase(servletRequest.getMethod())) {
            return null;
        }
    
        try {
            HttpSession session = WorkflowUtil.getHttpServletRequest().getSession();
            String token = (String) session.getAttribute("MekariToken");
            AuthenticationToken authToken = new AuthenticationToken(token, TokenType.BEARER, 3600, token, ServerType.valueOf(getPropertyString("serverType")));
            MekariSign mekariSign = MekariSign.getBuilder()
                        .setAuthenticationToken(authToken)
                        .authenticateAndBuild();

            String id = rowKeys[0];
            File file = File.createTempFile("test", ".pdf");
            file.setWritable(true);

            LogUtil.info(getClassName(), "ID: " + id);

            mekariSign.downloadDoc(id, file);

            servletResponse.setContentType("application/pdf");
            servletResponse.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
            servletResponse.setContentLength((int) file.length());

            Files.copy(file.toPath(), servletResponse.getOutputStream());
            servletResponse.getOutputStream().flush();
        } 
        catch (BuildingException | IOException | RequestException e) 
        {
            e.printStackTrace();
        }

        return result;
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
    public String getConfirmation() {
        return getPropertyString("confirmationMessage");
    }

    @Override
    public String getHref() {
        return getPropertyString("href");
    }

    @Override
    public String getHrefColumn() {
        return getPropertyString("hrefColumn");
    }

    @Override
    public String getHrefParam() {
        return getPropertyString("hrefParam");
    }

    @Override
    public String getLinkLabel() {
        String label = getPropertyString("label");
		return !"".equals(label) ? getPropertyString("label") : getLabel();
    }

    @Override
    public String getTarget() {
        return "post";
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

    @Override
    public Boolean getVisibleOnNoRecord() {
        return true;
    }
}
