package com.kinnarastudio.kecakplugins.mekariesign.datalist;

import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListActionDefault;
import org.joget.apps.datalist.model.DataListActionResult;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.util.WorkflowUtil;

public class MekariESignDatalistActionFileDownloader extends DataListActionDefault{
    public final static String LABEL = "Mekari eSign Datalist Action File Downloader";

    @Override
    public DataListActionResult executeAction(DataList arg0, String[] arg1) {
        DataListActionResult result = new DataListActionResult();
        result.setType(DataListActionResult.TYPE_REDIRECT);
        
        // only allow POST
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (request != null && !"POST".equalsIgnoreCase(request.getMethod())) {
            return null;
        }

        return result;
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
