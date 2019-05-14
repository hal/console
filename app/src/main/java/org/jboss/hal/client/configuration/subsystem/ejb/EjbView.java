/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.ejb;

import javax.annotation.PostConstruct;

import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.ManagementModel;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.client.configuration.subsystem.ejb.AddressTemplates.*;
import static org.jboss.hal.client.configuration.subsystem.security.AddressTemplates.SECURITY_DOMAIN_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.CSS.fontAwesome;

@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral", "WeakerAccess"})
public abstract class EjbView extends MbuiViewImpl<EjbPresenter> implements EjbPresenter.MyView {

    // ------------------------------------------------------ initialization

    public static EjbView create(MbuiContext mbuiContext, Environment environment) {
        return new Mbui_EjbView(mbuiContext, environment);
    }

    abstract Environment environment();

    @MbuiElement("ejb3-vertical-navigation") VerticalNavigation navigation;

    @MbuiElement("ejb3-configuration-form") Form<ModelNode> configurationForm;

    @MbuiElement("ejb3-thread-pool-table") Table<NamedNode> threadPoolTable;
    @MbuiElement("ejb3-thread-pool-form") Form<NamedNode> threadPoolForm;

    @MbuiElement("ejb3-remoting-profile-table") Table<NamedNode> remotingProfileTable;
    @MbuiElement("ejb3-remoting-profile-form") Form<NamedNode> remotingProfileForm;

    @MbuiElement("ejb3-bean-pool-table") Table<NamedNode> beanPoolTable;
    @MbuiElement("ejb3-bean-pool-form") Form<NamedNode> beanPoolForm;

    @MbuiElement("ejb3-cache-table") Table<NamedNode> cacheTable;
    @MbuiElement("ejb3-cache-form") Form<NamedNode> cacheForm;

    @MbuiElement("ejb3-passivation-table") Table<NamedNode> passivationTable;
    @MbuiElement("ejb3-passivation-form") Form<NamedNode> passivationForm;

    @MbuiElement("ejb3-service-async-form") Form<ModelNode> serviceAsyncForm;
    @MbuiElement("ejb3-service-identity-form") Form<ModelNode> serviceIdentityForm;
    @MbuiElement("ejb3-service-iiop-form") Form<ModelNode> serviceIiopForm;
    @MbuiElement("ejb3-service-remote-form") Form<ModelNode> serviceRemoteForm;
    @MbuiElement("ejb3-service-timer-form") Form<ModelNode> serviceTimerForm;

    @MbuiElement("ejb3-mdb-delivery-group-table") Table<NamedNode> mdbDeliveryGroupTable;
    @MbuiElement("ejb3-mdb-delivery-group-form") Form<NamedNode> mdbDeliveryGroupForm;

    Table<NamedNode> appSecurityDomainTable;
    Form<NamedNode> appSecurityDomainForm;

    EjbView(MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @PostConstruct
    @SuppressWarnings("ConstantConditions")
    void init() {
        StatementContext statementContext = mbuiContext.statementContext();
        if (ManagementModel.supportsEjbApplicationSecurityDomain(environment().getManagementVersion())) {
            AddressTemplate template = AddressTemplate.of(
                    "/{selected.profile}/subsystem=ejb3/application-security-domain=*");
            Metadata metadata = mbuiContext.metadataRegistry().lookup(template);

            appSecurityDomainTable = new ModelNodeTable.Builder<NamedNode>(Ids.EJB3_APPLICATION_SECURITY_DOMAIN_TABLE,
                    metadata)
                    .button(mbuiContext.tableButtonFactory()
                            .add(Ids.EJB3_APPLICATION_SECURITY_DOMAIN_ADD, Names.APPLICATION_SECURITY_DOMAIN, template,
                                    (name, address) -> presenter.reload()))
                    .button(mbuiContext.tableButtonFactory().remove(Names.APPLICATION_SECURITY_DOMAIN, template,
                            (api) -> api.selectedRow().getName(), () -> presenter.reload()))
                    .column(NAME, (cell, type, row, meta) -> row.getName())
                    .build();

            appSecurityDomainForm = new ModelNodeForm.Builder<NamedNode>(Ids.EJB3_APPLICATION_SECURITY_DOMAIN_FORM,
                    metadata)
                    .onSave((form, changedValues) -> {
                        String name = form.getModel().getName();
                        saveForm(Names.APPLICATION_SECURITY_DOMAIN, name,
                                template.resolve(statementContext, name), changedValues, metadata);
                    })
                    .prepareReset(form -> {
                        String name = form.getModel().getName();
                        resetForm(Names.APPLICATION_SECURITY_DOMAIN, name,
                                template.resolve(statementContext, name), form, metadata);
                    })
                    .build();

            HTMLElement section = section()
                    .add(h(1).textContent(Names.APPLICATION_SECURITY_DOMAIN))
                    .add(p().textContent(metadata.getDescription().getDescription()))
                    .add(appSecurityDomainTable)
                    .add(appSecurityDomainForm)
                    .get();
            navigation.insertPrimary(Ids.EJB3_APPLICATION_SECURITY_DOMAIN_ITEM, null, Names.SECURITY_DOMAIN,
                    fontAwesome("link"), section);
        }
        Dispatcher dispatcher = mbuiContext.dispatcher();
        configurationForm.getFormItem(DEFAULT_SFSB_CACHE)
                .registerSuggestHandler(new ReadChildrenAutoComplete(dispatcher, statementContext, CACHE_TEMPLATE));
        configurationForm.getFormItem(DEFAULT_SFSB_PASSIVATION_DISABLED_CACHE)
                .registerSuggestHandler(new ReadChildrenAutoComplete(dispatcher, statementContext, CACHE_TEMPLATE));
        configurationForm.getFormItem(DEFAULT_SLSB_INSTANCE_POOL)
                .registerSuggestHandler(new ReadChildrenAutoComplete(dispatcher, statementContext, BEAN_POOL_TEMPLATE));
        configurationForm.getFormItem(DEFAULT_SECURITY_DOMAIN)
                .registerSuggestHandler(new ReadChildrenAutoComplete(dispatcher, statementContext,
                        SECURITY_DOMAIN_TEMPLATE));
        cacheForm.getFormItem(PASSIVATION_STORE)
                .registerSuggestHandler(new ReadChildrenAutoComplete(dispatcher, statementContext,
                        PASSIVATION_TEMPLATE));
    }

    @Override
    public void attach() {
        super.attach();
        appSecurityDomainTable.attach();
        appSecurityDomainForm.attach();
        appSecurityDomainTable.bindForm(appSecurityDomainForm);
    }

    @Override
    public void detach() {
        super.detach();
        appSecurityDomainForm.detach();
        appSecurityDomainTable.detach();
    }


    // ------------------------------------------------------ update from DMR

    @Override
    public void update(ModelNode payload) {
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
        serviceIdentityForm.view(payload.get(SERVICE).get(SERVICE_IDENTITY_TEMPLATE.lastValue()));
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
