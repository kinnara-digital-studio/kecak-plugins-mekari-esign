package com.kinnarastudio.kecakplugins.mekariesign;

import java.util.ArrayList;
import java.util.Collection;

import com.kinnarastudio.kecakplugins.mekariesign.datalist.MekariESignInboxDataListBinder;
import com.kinnarastudio.kecakplugins.mekariesign.form.MekariESignFileUpload;
import com.kinnarastudio.kecakplugins.mekariesign.form.MekariESignFormLoadBinder;
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
    }

    public void stop(BundleContext context) {
        for (ServiceRegistration registration : registrationList) {
            registration.unregister();
        }
    }
}