package com.kinnarastudio.kecakplugins.mekariesign;

import java.util.ArrayList;
import java.util.Collection;

import com.kinnarastudio.kecakplugins.mekariesign.datalist.MekariESignInboxDataListBinder;
import com.kinnarastudio.kecakplugins.mekariesign.form.MekariESignFileUpload;
import com.kinnarastudio.kecakplugins.mekariesign.form.MekariESignFormLoadBinder;
import com.kinnarastudio.kecakplugins.mekariesign.form.SignatureType;
import com.kinnarastudio.kecakplugins.mekariesign.form.SignerForm;
import com.kinnarastudio.kecakplugins.mekariesign.userview.MekariESignUserviewMenu;
import com.kinnarastudio.kecakplugins.mekariesign.webservice.MekariESignWebhook;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

    protected Collection<ServiceRegistration> registrationList;

    public void start(BundleContext context) {
        registrationList = new ArrayList<ServiceRegistration>();

        //Register plugin here
        registrationList.add(context.registerService(MekariESignWebhook.class.getName(), new MekariESignWebhook(), null));
        registrationList.add(context.registerService(MekariESignFileUpload.class.getName(), new MekariESignFileUpload(), null));
        registrationList.add(context.registerService(MekariESignInboxDataListBinder.class.getName(), new MekariESignInboxDataListBinder(), null));
        registrationList.add(context.registerService(MekariESignFormLoadBinder.class.getName(), new MekariESignFormLoadBinder(), null));
        registrationList.add(context.registerService(MekariESignUserviewMenu.class.getName(), new MekariESignUserviewMenu(), null));
        registrationList.add(context.registerService(SignerForm.class.getName(), new SignerForm(), null));
        registrationList.add(context.registerService(SignatureType.class.getName(), new SignatureType(), null));
    }

    public void stop(BundleContext context) {
        for (ServiceRegistration registration : registrationList) {
            registration.unregister();
        }
    }
}