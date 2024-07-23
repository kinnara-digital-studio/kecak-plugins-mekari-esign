package com.kinnarastudio.kecakplugins.mekariesign.datalist;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpSession;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListBinderDefault;
import org.joget.apps.datalist.model.DataListCollection;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.plugin.base.PluginManager;
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
import com.kinnarastudio.commons.mekarisign.exception.RequestException;

public class MekariESignInboxDataListBinder extends DataListBinderDefault {
    public final static String LABEL = "Mekari eSign Inbox DataList Binder";

    public final static DataListColumn [] VALID_COLUMN_TYPES = {
        new DataListColumn("id", "ID", true, true),
        new DataListColumn("type", "Type", true, true),
        new DataListColumn("filename", "Filename", true, true),
        new DataListColumn("category", "Category", true, true),
        new DataListColumn("docURL", "Document URL", true, true)
    };

    @Override
    public DataListColumn[] getColumns() {
        return VALID_COLUMN_TYPES;
    }

    @Override
    public DataListCollection getData(DataList dataList, Map map, DataListFilterQueryObject[] filterQueryObject, String sort,
            Boolean desc, Integer start, Integer rows) {
        MekariSign mekariSign;
        DataListCollection <Map<String,String>> dataListCollection = new DataListCollection<>();
        try {
            HttpSession session = WorkflowUtil.getHttpServletRequest().getSession();
            String token = (String) session.getAttribute("MekariToken");
            String serverType = (String) session.getAttribute("MekariServerType");
            AuthenticationToken authToken = new AuthenticationToken(token, TokenType.BEARER, 3600, getPropertyString("refreshToken"), ServerType.valueOf(serverType));

            mekariSign = MekariSign.getBuilder()
                        .setAuthenticationToken(authToken)
                        .authenticateAndBuild();
            
            int page = (start / 10) + 1;
            
            GetDocumentListBody documentList = mekariSign.getDoc(page, rows, DocumentCategory.valueOf(getPropertyString("documentCategory")), SigningStatus.valueOf(getPropertyString("signingStatus")), StampingStatus.valueOf(getPropertyString("stampingStatus")));

            ResponseData[] documents = documentList.getRespData();
        
            // Add each document to the DataListCollection
            for (ResponseData document : documents) {
                Map<String, String> maps = new HashMap<>();
                maps.put("id", document.getId());
                maps.put("type", document.getType());
                maps.put("filename", document.getAttributes().getFilename());
                maps.put("category", document.getAttributes().getCategory().toString());
                maps.put("docURL", document.getAttributes().getDocUrl());
                dataListCollection.add(maps);
            }
        } catch (BuildingException | RequestException | ParseException e) {
            e.printStackTrace();
        }
        return dataListCollection;
    }

    @Override
    public int getDataTotalRowCount(DataList dataList, Map properties, DataListFilterQueryObject[] filterQueryObjects) {
        // TODO Auto-generated method stub

        MekariSign mekariSign;
        
        try {
            AuthenticationToken authToken = new AuthenticationToken(getPropertyString("accessToken"), TokenType.BEARER, 3600, getPropertyString("refreshToken"), ServerType.valueOf(getPropertyString("serverType")));

            mekariSign = MekariSign.getBuilder()
                        .setAuthenticationToken(authToken)
                        .authenticateAndBuild();
            
            GetDocumentListBody documentList = mekariSign.getDoc(1, 1, DocumentCategory.valueOf(getPropertyString("documentCategory")), SigningStatus.valueOf(getPropertyString("signingStatus")), StampingStatus.valueOf(getPropertyString("stampingStatus")));

            return documentList.getDocListPagination().getDocumentCount();
        } catch (BuildingException | RequestException | ParseException e) {
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
