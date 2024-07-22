package com.kinnarastudio.kecakplugins.mekariesign.form;

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
import org.joget.plugin.base.PluginWebSupport;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class SignerElement extends UserviewMenu implements PluginWebSupport {

    @Override
    public String getCategory() {
        return "Signers";
    }

    @Override
    public String getIcon() {
        return null;
    }

    @Override
    public String getRenderPage() {
        return null;
    }

    @Override
    public boolean isHomePageSupported() {
        return true;
    }

    @Override
    public String getDecoratedMenu() {
        return null;
    }

    @Override
    public String getName() {
        return "Signers";
    }

    @Override
    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    @Override
    public String getDescription() {
        return getClass().getPackage().getImplementationVersion();
    }

    @Override
    public void webService(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        String dataListId = httpServletRequest.getParameter("datalistId");
        String userviewId = httpServletRequest.getParameter("userviewId");
        String menuId = httpServletRequest.getParameter("menuId");
        Userview userview = getUserview(userviewId);
        UserviewMenu userviewMenu = getUserviewMenu(userview, menuId);
        DataList dataList = getDataList(dataListId);
        DataListCollection rows = dataList.getRows();
        
        String action = httpServletRequest.getParameter("actions");


    }

    private Userview getUserview(String userviewId) {
        AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
        ApplicationContext applicationContext = AppUtil.getApplicationContext();
        UserviewService userviewService = (UserviewService) applicationContext.getBean("userviewService");
        UserviewDefinitionDao userviewDefinitionDao = (UserviewDefinitionDao) applicationContext.getBean("userviewDefinitionDao");
        return Optional.of(userviewId).map(s -> userviewDefinitionDao.loadById(s, appDefinition)).map(UserviewDefinition::getJson).map(s->AppUtil.processHashVariable(s,null,null,null)).map(s->userviewService.createUserview(s,null,false,AppUtil.getRequestContextPath(),null,null,false)).orElse(null);
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
        return userview.getCategories().stream().flatMap(c->c.getMenus().stream()).filter(m-> !userviewId.isEmpty() && userviewId.equalsIgnoreCase(m.getPropertyString("id"))).findFirst().orElse(null);
    }

    @Override
    public String getLabel() {
        return "Signers";
    }

    @Override
    public String getClassName() {
        return SignerElement.class.getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/signature.json");
    }
}
