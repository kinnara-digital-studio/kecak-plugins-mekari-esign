package com.kinnarastudio.kecakplugins.mekariesign.datalist;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import javax.servlet.http.HttpSession;

import com.kinnarastudio.kecakplugins.mekariesign.webservice.MekariESignWebhook;
import org.joget.apps.app.dao.PluginDefaultPropertiesDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListBinderDefault;
import org.joget.apps.datalist.model.DataListCollection;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.util.WorkflowUtil;

import com.kinnarastudio.commons.mekarisign.model.AuthenticationToken;
import com.kinnarastudio.commons.mekarisign.model.DocumentCategory;
import com.kinnarastudio.commons.mekarisign.model.GetDocumentListBody;
import com.kinnarastudio.commons.mekarisign.model.ResponseData;
import com.kinnarastudio.commons.mekarisign.model.ServerType;
import com.kinnarastudio.commons.mekarisign.model.SigningStatus;
import com.kinnarastudio.commons.mekarisign.model.StampingStatus;
import com.kinnarastudio.commons.mekarisign.model.TokenType;
import com.kinnarastudio.commons.mekarisign.*;
import com.kinnarastudio.commons.mekarisign.exception.BuildingException;
import com.kinnarastudio.commons.mekarisign.exception.InvalidTokenException;
import com.kinnarastudio.commons.mekarisign.exception.RequestException;

public class MekariESignInboxDataListBinder extends DataListBinderDefault {
    public final static String LABEL = "Mekari eSign Inbox DataList Binder";

    public final static DataListColumn [] VALID_COLUMN_TYPES = {
        new DataListColumn("id", "ID", true, true),
        new DataListColumn("type", "Type", true, true),
        new DataListColumn("filename", "Filename", true, true),
        new DataListColumn("status", "Status", true, true),
        new DataListColumn("category", "Category", true, true),
        new DataListColumn("docURL", "Document URL", true, true),
        new DataListColumn("updated_at", "Last Modified", true, true)
    };

    @Override
    public DataListColumn[] getColumns() {
        return VALID_COLUMN_TYPES;
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
    public DataListCollection getData(DataList dataList, Map map, DataListFilterQueryObject[] filterQueryObject, String sort,
            Boolean desc, Integer start, Integer rows) {
        DataListCollection <Map<String,String>> dataListCollection = new DataListCollection<>();
        try {
            MekariSign mekariSign;

            HttpSession session = WorkflowUtil.getHttpServletRequest().getSession();
            String token = (String) session.getAttribute("MekariToken");
            AuthenticationToken authToken = new AuthenticationToken(token, TokenType.BEARER, 3600, token, ServerType.valueOf(getPropertyString("serverType")));
            
            LogUtil.info(getClassName(), "Token: " + token);

            mekariSign = MekariSign.getBuilder()
                        .setAuthenticationToken(authToken)
                        .authenticateAndBuild();
            
            int page = (start / 10) + 1;
            
            DocumentCategory documentCategory;
            SigningStatus signingStatus;

            if (!getPropertyString("documentCategory").equals("none"))
            {
                documentCategory = DocumentCategory.valueOf(getPropertyString("documentCategory"));
                
            } else {
                documentCategory = null;
            }

            if (!getPropertyString("signingStatus").equals("none"))
            {
                signingStatus = SigningStatus.valueOf(getPropertyString("signingStatus"));
            }
            else {
                signingStatus = null;
            }

            LogUtil.info(getClassName(), "Document Category: " + getPropertyString("documentCategory"));
            LogUtil.info(getClassName(), "Signing Status: " + getPropertyString("signingStatus"));

            GetDocumentListBody documentList = mekariSign.getDoc(page, rows, documentCategory, signingStatus, StampingStatus.valueOf(getPropertyString("stampingStatus")));

            // documentList = mekariSign.getDoc(page, rows, null, null, null);

            ResponseData[] documents = documentList.getRespData();
        
            // Add each document to the DataListCollection
            for (ResponseData document : documents) {
                Map<String, String> maps = new HashMap<>();
                maps.put("id", document.getId());
                maps.put("type", document.getType());
                maps.put("filename", document.getAttributes().getFilename());
                maps.put("status", document.getAttributes().getSigningStatus().toString());
                maps.put("category", document.getAttributes().getCategory().toString());
                maps.put("docURL", document.getAttributes().getDocUrl());
                maps.put("updated_at", document.getAttributes().getUpdatedAt().toString());
                dataListCollection.add(maps);
            }
        } catch (BuildingException | ParseException e) {
            LogUtil.error(getClassName(), e, e.getMessage());
        } catch (RequestException e) {
            if(e.getCause() instanceof InvalidTokenException)
            WorkflowUtil.getHttpServletRequest().getSession().setAttribute("MekariToken", "");
            String homeUrl = (String) WorkflowUtil.getHttpServletRequest().getSession().getAttribute("HomeURL");
            try {
                WorkflowUtil.getHttpServletResponse().sendRedirect(homeUrl);
            } catch (IOException e1) {
                LogUtil.error(getClassName(), e1, e1.getMessage());
            }
        }
        return dataListCollection;
    }

    @Override
    public int getDataTotalRowCount(DataList dataList, Map properties, DataListFilterQueryObject[] filterQueryObjects) {
        try {
            MekariSign mekariSign;
            String token = (String) WorkflowUtil.getHttpServletRequest().getSession().getAttribute("MekariToken");
            AuthenticationToken authToken = new AuthenticationToken(token, TokenType.BEARER, 3600, token, ServerType.valueOf(getPropertyString("serverType")));

            mekariSign = MekariSign.getBuilder()
                        .setAuthenticationToken(authToken)
                        .authenticateAndBuild();
            DocumentCategory documentCategory;
            SigningStatus signingStatus;

            if (!getPropertyString("documentCategory").equals("none"))
            {
                documentCategory = DocumentCategory.valueOf(getPropertyString("documentCategory"));
                
            } else {
                documentCategory = null;
            }

            if (!getPropertyString("signingStatus").equals("none"))
            {
                signingStatus = SigningStatus.valueOf(getPropertyString("signingStatus"));
            }
            else {
                signingStatus = null;
            }

            LogUtil.info(getClassName(), "Document Category: " + getPropertyString("documentCategory"));
            LogUtil.info(getClassName(), "Signing Status: " + getPropertyString("signingStatus"));

            GetDocumentListBody documentList = mekariSign.getDoc(1, 1, documentCategory, signingStatus, StampingStatus.valueOf(getPropertyString("stampingStatus")));

            return documentList.getDocListPagination().getDocumentCount();
        } catch (BuildingException | RequestException | ParseException e) {
            LogUtil.error(getClassName(), e, e.getMessage());
            return 0;
        }
    }

    @Override
    public String getPrimaryKeyColumnName() {
        final String value = getPropertyString("primaryKeyColumn");
        return value.isEmpty() ? "id" : value;
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
        return AppUtil.readPluginResource(getClassName(), "/properties/datalist/MekariEsignDataListBinder.json");
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

    protected ServerType getServerType() {
        switch(getPropertyString("serverType")) {
            case "sandbox":
                return ServerType.SANDBOX;
            case "staging":
                return ServerType.STAGING;
            case "production":
                return ServerType.PRODUCTION;
            default:
                return ServerType.MOCK;
        }
    }
}
