/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.ejb;

import java.util.List;

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Api.RefreshMode;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

/**
 * @author Claudio Miranda
 */
@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral", "WeakerAccess"})
public abstract class EjbView extends MbuiViewImpl<EjbPresenter> implements EjbPresenter.MyView {

    // ------------------------------------------------------ initialization

    public static EjbView create(final MbuiContext mbuiContext) {
        return new Mbui_EjbView(mbuiContext);
    }

    @MbuiElement("ejb-vertical-navigation") VerticalNavigation navigation;
    
    @MbuiElement("configuration-form") Form<ModelNode> configurationForm;
    
    @MbuiElement("thread-pool-table") DataTable<NamedNode> threadPoolTable;
    @MbuiElement("thread-pool-form") Form<NamedNode> threadPoolForm;
    
    @MbuiElement("remoting-profile-table") DataTable<NamedNode> remotingProfileTable;
    @MbuiElement("remoting-profile-form") Form<NamedNode> remotingProfileForm;
    
    @MbuiElement("bean-pool-table") DataTable<NamedNode> beanPoolTable;
    @MbuiElement("bean-pool-form") Form<NamedNode> beanPoolForm;
    
    @MbuiElement("cache-table") DataTable<NamedNode> cacheTable;
    @MbuiElement("cache-form") Form<NamedNode> cacheForm;

    @MbuiElement("passivation-table") DataTable<NamedNode> passivationTable;
    @MbuiElement("passivation-form") Form<NamedNode> passivationForm;

    @MbuiElement("service-async-form") Form<ModelNode> serviceAsyncForm;
    @MbuiElement("service-iiop-form") Form<ModelNode> serviceIiopForm;
    @MbuiElement("service-remote-form") Form<ModelNode> serviceRemoteForm;
    @MbuiElement("service-timer-form") Form<ModelNode> serviceTimerForm;

    @MbuiElement("mdb-delivery-group-table") DataTable<NamedNode> mdbDeliveryGroupTable;
    @MbuiElement("mdb-delivery-group-form") Form<NamedNode> mdbDeliveryGroupForm;

    @MbuiElement("app-security-domain-table") DataTable<NamedNode> appSecurityDomainTable;
    @MbuiElement("app-security-domain-form") Form<NamedNode> appSecurityDomainForm;


    EjbView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }


    // ------------------------------------------------------ form and table updates from DMR

    @Override
    public void updateConfiguration(ModelNode configuration) {
        configurationForm.view(configuration);
    }

    @Override
    public void updateThreadPool(final List<NamedNode> items) {
        threadPoolTable.api().clear().add(items).refresh(RefreshMode.RESET);
        threadPoolForm.clear();
    }

    @Override
    public void updateRemotingProfile(final List<NamedNode> items) {
        remotingProfileTable.api().clear().add(items).refresh(RefreshMode.RESET);
        remotingProfileForm.clear();
    }

    @Override
    public void updateBeanPool(final List<NamedNode> items) {
        beanPoolTable.api().clear().add(items).refresh(RefreshMode.RESET);
        beanPoolForm.clear();
    }

    @Override
    public void updateCache(final List<NamedNode> items) {
        cacheTable.api().clear().add(items).refresh(RefreshMode.RESET);
        cacheForm.clear();
    }

    @Override
    public void updatePassivation(final List<NamedNode> items) {
        passivationTable.api().clear().add(items).refresh(RefreshMode.RESET);
        passivationForm.clear();
    }

    @Override
    public void updateServiceAsync(ModelNode node) {
        serviceAsyncForm.view(node);
    }

    @Override
    public void updateServiceIiop(ModelNode node) {
        serviceIiopForm.view(node);
    }
    
    @Override
    public void updateServiceRemote(ModelNode node) {
        serviceRemoteForm.view(node);
    }

    @Override
    public void updateServiceTimer(ModelNode node) {
        serviceTimerForm.view(node);
    }

    @Override
    public void updateMdbDeliveryGroup(List<NamedNode> items) {
        mdbDeliveryGroupTable.api().clear().add(items).refresh(RefreshMode.RESET);
        mdbDeliveryGroupForm.clear();
    }

    @Override
    public void updateApplicationSecurityDomain(List<NamedNode> items) {
        appSecurityDomainTable.api().clear().add(items).refresh(RefreshMode.RESET);
        appSecurityDomainForm.clear();
    }

    // ------------------------------------------------------ view / mbui contract

    @Override
    public VerticalNavigation getVerticalNavigation() {
        return navigation;
    }
}
