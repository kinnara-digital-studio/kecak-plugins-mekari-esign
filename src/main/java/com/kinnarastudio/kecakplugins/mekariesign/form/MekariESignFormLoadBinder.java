package com.kinnarastudio.kecakplugins.mekariesign.form;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.*;
import org.joget.plugin.base.PluginManager;

import com.kinnarastudio.commons.mekarisign.MekariSign;
import com.kinnarastudio.commons.mekarisign.exception.BuildingException;
import com.kinnarastudio.commons.mekarisign.exception.RequestException;
import com.kinnarastudio.commons.mekarisign.model.AuthenticationToken;
import com.kinnarastudio.commons.mekarisign.model.ResponseData;
import com.kinnarastudio.commons.mekarisign.model.ServerType;
import com.kinnarastudio.commons.mekarisign.model.TokenType;
import com.kinnarastudio.kecakplugins.mekariesign.userview.MekariESignUserviewMenu;

import java.text.ParseException;
import java.util.ResourceBundle;

public class MekariESignFormLoadBinder extends FormBinder implements FormLoadElementBinder {
    public final static String LABEL = "Mekari eSign Form Load Binder";

    @Override
    public FormRowSet load(Element element, String primaryKey, FormData formData) {
        MekariSign mekariSign;
        FormRowSet formRowSet = new FormRowSet();
        try {
            String token = new MekariESignUserviewMenu().getToken();
            String serverType = new MekariESignUserviewMenu().getServerType();
            AuthenticationToken authToken = new AuthenticationToken(token, TokenType.BEARER, 3600, getPropertyString("refreshToken"), ServerType.valueOf(serverType));
    
            mekariSign = MekariSign.getBuilder()
                        .setAuthenticationToken(authToken)
                        .authenticateAndBuild();
            
            ResponseData document = mekariSign.getDocDetail(primaryKey);
            
            FormRow formRow = new FormRow();
            formRow.setProperty("id", document.getId());
            formRow.setProperty("type", document.getType());
            formRow.setProperty("filename", document.getAttributes().getFilename());
            formRow.setProperty("category", document.getAttributes().getCategory().toString());
            formRow.setProperty("docUrl", document.getAttributes().getDocUrl());
            formRowSet.add(formRow);
            
        } catch (BuildingException | RequestException | ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return formRowSet;
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
        return "kecak-plugins-mekari-esign";
    }

    @Override
    public String getLabel() {
        return LABEL;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/form/MekariEsignFormLoadBinder.json");
    }
}
