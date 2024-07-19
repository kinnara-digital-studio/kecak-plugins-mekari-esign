package com.kinnarastudio.kecakplugins.mekariesign.form;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormBuilderPaletteElement;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.service.FormUtil;

import java.util.Map;

public class SignerElement extends Element implements FormBuilderPaletteElement {
    @Override
    public String renderTemplate(FormData formData, Map map) {
        final String value = FormUtil.getElementPropertyValue(this,formData);
        map.put("value", value);
        return FormUtil.generateElementHtml(this,formData,"mekari.ftl",map);
    }

    @Override
    public String getFormBuilderCategory() {
        return "Mekari";
    }

    @Override
    public int getFormBuilderPosition() {
        return 100;
    }

    @Override
    public String getFormBuilderIcon() {
        return null;
    }

    @Override
    public String getFormBuilderTemplate() {
        return "<h1>Form</h1>";
    }

    @Override
    public String getName() {
        return "SignersElement";
    }

    @Override
    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    @Override
    public String getDescription() {
        return getClass().getPackage().getImplementationTitle();
    }

    @Override
    public String getLabel() {
        return "SignersElement";
    }

    @Override
    public String getClassName() {
        return SignerElement.class.getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(),"/properties/form/signers.json");
    }
}
