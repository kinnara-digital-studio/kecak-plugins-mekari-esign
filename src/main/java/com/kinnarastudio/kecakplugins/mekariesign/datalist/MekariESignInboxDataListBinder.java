package com.kinnarastudio.kecakplugins.mekariesign.datalist;

import java.text.ParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListBinderDefault;
import org.joget.apps.datalist.model.DataListCollection;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.plugin.base.PluginManager;

import com.kinnarastudio.commons.mekarisign.model.DocumentListResponse;
import com.kinnarastudio.commons.mekarisign.model.ServerType;
import com.kinnarastudio.commons.mekarisign.service.DocumentListService;
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
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'getData'");
        
        MekariSign mekariSign;
        try {
            mekariSign = MekariSign.getBuilder()
                        .setClientId(getPropertyString("clientId"))
                        .setClientSecret(getPropertyString("clientSecret"))
                        .setServerType(ServerType.SANDBOX)
                        .setSecretCode(getPropertyString("secretCode"))
                        .build();
                    
            mekariSign.getDoc(1, 10, null, null, null);
        } catch (BuildingException | RequestException | ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        DataListCollection <String> id = new DataListCollection<>();
        
        return id;
    }

    @Override
    public int getDataTotalRowCount(DataList dataList, Map properties, DataListFilterQueryObject[] filterQueryObjects) {
        // TODO Auto-generated method stub
        return 0;
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
