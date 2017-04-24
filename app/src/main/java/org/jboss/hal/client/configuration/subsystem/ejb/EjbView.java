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

import javax.annotation.PostConstruct;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.NamedNodeTable;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.ManagementModel;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static org.jboss.hal.client.configuration.subsystem.ejb.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVICE;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.CSS.fontAwesome;

/**
 * @author Claudio Miranda
 */
@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral", "WeakerAccess"})
public abstract class EjbView extends MbuiViewImpl<EjbPresenter> implements EjbPresenter.MyView {

    // ------------------------------------------------------ initialization

    public static EjbView create(final MbuiContext mbuiContext, final Environment environment) {
        return new Mbui_EjbView(mbuiContext, environment);
    }

    abstract Environment environment();

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

    DataTable<NamedNode> appSecurityDomainTable;
    Form<NamedNode> appSecurityDomainForm;

    EjbView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @PostConstruct
    @SuppressWarnings("ConstantConditions")
    void init() {
        if (ManagementModel.supportsEjbApplicationSecurityDomain(environment().getManagementVersion())) {
            AddressTemplate template = AddressTemplate.of(
                    "/{selected.profile}/subsystem=ejb3/application-security-domain=*");
            Metadata metadata = mbuiContext.metadataRegistry().lookup(template);

            Options<NamedNode> options = new NamedNodeTable.Builder<>(metadata)
                    .button(mbuiContext.tableButtonFactory()
                            .add(Ids.EJB_APPLICATION_SECURITY_DOMAIN_ADD, Names.APPLICATION_SECURITY_DOMAIN, template,
                                    (name, address) -> presenter.reload()))
                    .button(mbuiContext.tableButtonFactory().remove(Names.APPLICATION_SECURITY_DOMAIN, template,
                            (api) -> api.selectedRow().getName(), () -> presenter.reload()))
                    .column(NAME, (cell, type, row, meta) -> row.getName())
                    .build();
            appSecurityDomainTable = new NamedNodeTable<>(Ids.EJB_APPLICATION_SECURITY_DOMAIN_TABLE, metadata, options);

            appSecurityDomainForm = new ModelNodeForm.Builder<NamedNode>(Ids.EJB_APPLICATION_SECURITY_DOMAIN_FORM,
                    metadata)
                    .onSave((form, changedValues) -> {
                        String name = form.getModel().getName();
                        saveForm(Names.APPLICATION_SECURITY_DOMAIN, name,
                                template.resolve(mbuiContext.statementContext(), name), changedValues, metadata);
                    })
                    .prepareReset(form -> {
                        String name = form.getModel().getName();
                        resetForm(Names.APPLICATION_SECURITY_DOMAIN, name,
                                template.resolve(mbuiContext.statementContext(), name), form, metadata);
                    })
                    .build();

            Element section = new Elements.Builder()
                    .section()
                    .h(1).textContent(Names.APPLICATION_SECURITY_DOMAIN).end()
                    .p().textContent(metadata.getDescription().getDescription()).end()
                    .add(appSecurityDomainTable)
                    .add(appSecurityDomainForm)
                    .end()
                    .build();
            navigation.insertPrimary(Ids.EJB_APPLICATION_SECURITY_DOMAIN_ENTRY, null, Names.SECURITY_DOMAIN,
                    fontAwesome("link"), section);
        }
    }

    @Override
    public void attach() {
        super.attach();
        appSecurityDomainTable.attach();
        appSecurityDomainForm.attach();
    }

    @Override
    public void detach() {
        super.detach();
        appSecurityDomainForm.detach();
        appSecurityDomainTable.detach();
    }


    // ------------------------------------------------------ update from DMR

    @Override
    public void update(final ModelNode payload) {
        configurationForm.view(payload);

        threadPoolTable.update(asNamedNodes(failSafePropertyList(payload, THREAD_POOL_TEMPLATE.lastName())));
        threadPoolForm.clear();

        remotingProfileTable.update(asNamedNodes(failSafePropertyList(payload, REMOTING_PROFILE_TEMPLATE.lastName())));
        remotingProfileForm.clear();

        beanPoolTable.update(asNamedNodes(failSafePropertyList(payload, BEAN_POOL_TEMPLATE.lastName())));
        beanPoolForm.clear();

        cacheTable.update(asNamedNodes(failSafePropertyList(payload, CACHE_TEMPLATE.lastName())));
        cacheForm.clear();

        passivationTable.update(asNamedNodes(failSafePropertyList(payload, PASSIVATION_TEMPLATE.lastName())));
        passivationForm.clear();

        serviceAsyncForm.view(payload.get(SERVICE).get(SERVICE_ASYNC_TEMPLATE.lastValue()));
        serviceIiopForm.view(payload.get(SERVICE).get(SERVICE_IIOP_TEMPLATE.lastValue()));
        serviceRemoteForm.view(payload.get(SERVICE).get(SERVICE_REMOTE_TEMPLATE.lastValue()));
        serviceTimerForm.view(payload.get(SERVICE).get(SERVICE_TIMER_TEMPLATE.lastValue()));

        mdbDeliveryGroupTable.update(
                asNamedNodes(failSafePropertyList(payload, MDB_DELIVERY_GROUP_TEMPLATE.lastName())));
        mdbDeliveryGroupForm.clear();

        if (ManagementModel.supportsEjbApplicationSecurityDomain(environment().getManagementVersion())) {
            appSecurityDomainTable.update(
                    asNamedNodes(failSafePropertyList(payload, APP_SEC_DOMAIN_TEMPLATE.lastName())));
            appSecurityDomainForm.clear();
        }
    }
}
