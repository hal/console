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

import static org.jboss.hal.client.configuration.subsystem.ejb.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVICE;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

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

    @MbuiElement("ejb-configuration-form") Form<ModelNode> configurationForm;

    @MbuiElement("ejb-thread-pool-table") DataTable<NamedNode> threadPoolTable;
    @MbuiElement("ejb-thread-pool-form") Form<NamedNode> threadPoolForm;

    @MbuiElement("ejb-remoting-profile-table") DataTable<NamedNode> remotingProfileTable;
    @MbuiElement("ejb-remoting-profile-form") Form<NamedNode> remotingProfileForm;

    @MbuiElement("ejb-bean-pool-table") DataTable<NamedNode> beanPoolTable;
    @MbuiElement("ejb-bean-pool-form") Form<NamedNode> beanPoolForm;

    @MbuiElement("ejb-cache-table") DataTable<NamedNode> cacheTable;
    @MbuiElement("ejb-cache-form") Form<NamedNode> cacheForm;

    @MbuiElement("ejb-passivation-table") DataTable<NamedNode> passivationTable;
    @MbuiElement("ejb-passivation-form") Form<NamedNode> passivationForm;

    @MbuiElement("ejb-service-async-form") Form<ModelNode> serviceAsyncForm;
    @MbuiElement("ejb-service-iiop-form") Form<ModelNode> serviceIiopForm;
    @MbuiElement("ejb-service-remote-form") Form<ModelNode> serviceRemoteForm;
    @MbuiElement("ejb-service-timer-form") Form<ModelNode> serviceTimerForm;

    @MbuiElement("ejb-mdb-delivery-group-table") DataTable<NamedNode> mdbDeliveryGroupTable;
    @MbuiElement("ejb-mdb-delivery-group-form") Form<NamedNode> mdbDeliveryGroupForm;

    @MbuiElement("ejb-app-security-domain-table") DataTable<NamedNode> appSecurityDomainTable;
    @MbuiElement("ejb-app-security-domain-form") Form<NamedNode> appSecurityDomainForm;

    EjbView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }


    // ------------------------------------------------------ update from DMR

    @Override
    public void update(final ModelNode payload) {
        configurationForm.view(payload);

        threadPoolTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(payload, THREAD_POOL_TEMPLATE.lastKey())))
                .refresh(RefreshMode.RESET);
        threadPoolForm.clear();

        remotingProfileTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(payload, REMOTING_PROFILE_TEMPLATE.lastKey())))
                .refresh(RefreshMode.RESET);
        remotingProfileForm.clear();

        beanPoolTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(payload, BEAN_POOL_TEMPLATE.lastKey())))
                .refresh(RefreshMode.RESET);
        beanPoolForm.clear();

        cacheTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(payload, CACHE_TEMPLATE.lastKey())))
                .refresh(RefreshMode.RESET);
        cacheForm.clear();

        passivationTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(payload, PASSIVATION_TEMPLATE.lastKey())))
                .refresh(RefreshMode.RESET);
        passivationForm.clear();

        serviceAsyncForm.view(payload.get(SERVICE).get(SERVICE_ASYNC_TEMPLATE.lastValue()));
        serviceIiopForm.view(payload.get(SERVICE).get(SERVICE_IIOP_TEMPLATE.lastValue()));
        serviceRemoteForm.view(payload.get(SERVICE).get(SERVICE_REMOTE_TEMPLATE.lastValue()));
        serviceTimerForm.view(payload.get(SERVICE).get(SERVICE_TIMER_TEMPLATE.lastValue()));

        mdbDeliveryGroupTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(payload, MDB_DELIVERY_GROUP_TEMPLATE.lastKey())))
                .refresh(RefreshMode.RESET);
        mdbDeliveryGroupForm.clear();

        appSecurityDomainTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(payload, APP_SEC_DOMAIN_TEMPLATE.lastKey())))
                .refresh(RefreshMode.RESET);
        appSecurityDomainForm.clear();
    }
}
