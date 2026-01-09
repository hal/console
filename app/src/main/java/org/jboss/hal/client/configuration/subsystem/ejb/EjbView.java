/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.ejb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.Pages;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.InlineAction;
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

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.client.configuration.subsystem.ejb.AddressTemplates.APP_SEC_DOMAIN_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.ejb.AddressTemplates.BEAN_POOL_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.ejb.AddressTemplates.CACHE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.ejb.AddressTemplates.MDB_DELIVERY_GROUP_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.ejb.AddressTemplates.PASSIVATION_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.ejb.AddressTemplates.REMOTE_HTTP_CONNECTION_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.ejb.AddressTemplates.REMOTING_EJB_RECEIVER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.ejb.AddressTemplates.REMOTING_PROFILE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.ejb.AddressTemplates.RER_CHANNEL_CREATION_OPTIONS_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.ejb.AddressTemplates.SERVICE_ASYNC_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.ejb.AddressTemplates.SERVICE_IDENTITY_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.ejb.AddressTemplates.SERVICE_IIOP_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.ejb.AddressTemplates.SERVICE_REMOTE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.ejb.AddressTemplates.SERVICE_TIMER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.ejb.AddressTemplates.THREAD_POOL_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT_SECURITY_DOMAIN;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT_SFSB_CACHE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT_SFSB_PASSIVATION_DISABLED_CACHE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT_SLSB_INSTANCE_POOL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PASSIVATION_STORE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVICE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.CSS.fontAwesome;

@MbuiView
@SuppressWarnings({ "DuplicateStringLiteralInspection", "HardCodedStringLiteral", "WeakerAccess" })
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

    Table<NamedNode> remotingProfileTable;
    Form<NamedNode> remotingProfileForm;

    RemotingProfileSubpage ejbReceiverPage;
    RemotingProfileSubpage httpConnectionPage;

    Table<NamedNode> channelCreationOptionsTable;
    Form<NamedNode> channelCreationOptionsForm;

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

    private final LabelBuilder labelBuilder;
    private Pages remotingProfilePages;
    private String selectedRemotingProfile;
    private String selectedEjbReceiver;
    private Map<String, RemotingProfileSubpage> subPages;

    EjbView(MbuiContext mbuiContext) {
        super(mbuiContext);
        labelBuilder = new LabelBuilder();
    }

    @PostConstruct
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
                    .nameColumn()
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
                    .add(appSecurityDomainForm).element();
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
                        APP_SEC_DOMAIN_TEMPLATE));
        cacheForm.getFormItem(PASSIVATION_STORE)
                .registerSuggestHandler(new ReadChildrenAutoComplete(dispatcher, statementContext,
                        PASSIVATION_TEMPLATE));

        // ------------------------------- remoting profile

        String rpId = REMOTING_PROFILE_TEMPLATE.lastName();
        String rpTypeLabel = labelBuilder.label(rpId);
        ejbReceiverPage = new RemotingProfileSubpage(REMOTING_EJB_RECEIVER_TEMPLATE);
        httpConnectionPage = new RemotingProfileSubpage(REMOTE_HTTP_CONNECTION_TEMPLATE);

        Metadata remotingProfileMetadata = mbuiContext.metadataRegistry().lookup(REMOTING_PROFILE_TEMPLATE);
        String remotingProfileTableId = Ids.build(rpId, Ids.TABLE);
        remotingProfileTable = new ModelNodeTable.Builder<NamedNode>(remotingProfileTableId, remotingProfileMetadata)
                .button(mbuiContext.tableButtonFactory().add(Ids.build(remotingProfileTableId, Ids.ADD),
                        rpTypeLabel,
                        REMOTING_PROFILE_TEMPLATE,
                        (name, address) -> presenter.reload()))
                .button(mbuiContext.tableButtonFactory().remove(rpTypeLabel, REMOTING_PROFILE_TEMPLATE,
                        table -> table.selectedRow().getName(),
                        () -> presenter.reload()))
                .nameColumn()
                .column(ejbReceiverPage.makeInlineAction(), "20em")
                .column(httpConnectionPage.makeInlineAction(), "20em")
                .build();

        remotingProfileForm = new ModelNodeForm.Builder<NamedNode>(Ids.build(rpId, Ids.FORM),
                remotingProfileMetadata)
                .onSave((form, changedValues) -> {
                    String name = form.getModel().getName();
                    saveForm(rpTypeLabel, name,
                            REMOTING_PROFILE_TEMPLATE.resolve(statementContext(), name), changedValues,
                            remotingProfileMetadata);
                })
                .prepareReset(form -> {
                    String name = form.getModel().getName();
                    resetForm(rpTypeLabel, name,
                            REMOTING_PROFILE_TEMPLATE.resolve(statementContext(), name), form, remotingProfileMetadata);
                })
                .build();

        HTMLElement remotingProfileItemElement = section()
                .add(h(1).textContent(rpTypeLabel))
                .add(p().textContent(remotingProfileMetadata.getDescription().getDescription()))
                .add(remotingProfileTable)
                .add(remotingProfileForm)
                .element();

        subPages = new HashMap<>();

        String rpPageId = Ids.build(rpId, Ids.PAGE);
        remotingProfilePages = new Pages(Ids.build(rpId, Ids.PAGES), rpPageId, remotingProfileItemElement);

        ejbReceiverPage.addToPages(remotingProfilePages, rpPageId, rpTypeLabel);
        subPages.put(ejbReceiverPage.getChildType(), ejbReceiverPage);

        httpConnectionPage.addToPages(remotingProfilePages, rpPageId, rpTypeLabel);
        subPages.put(httpConnectionPage.getChildType(), httpConnectionPage);

        // ----------------------- remoting-ejb-receiver/channel-creation-options

        String ccoId = RER_CHANNEL_CREATION_OPTIONS_TEMPLATE.lastName();
        String ccoTypeLabel = labelBuilder.label(ccoId);
        Metadata channelCreationOptionsMetadata = mbuiContext.metadataRegistry().lookup(RER_CHANNEL_CREATION_OPTIONS_TEMPLATE);
        String channelCreationOptionsTableId = Ids.build(ccoId, Ids.TABLE);
        channelCreationOptionsTable = new ModelNodeTable.Builder<NamedNode>(channelCreationOptionsTableId,
                channelCreationOptionsMetadata)
                .button(mbuiContext.tableButtonFactory().add(RER_CHANNEL_CREATION_OPTIONS_TEMPLATE,
                        table -> presenter.addRerChannelCreationOptions(Ids.build(channelCreationOptionsTableId, Ids.ADD),
                                ccoTypeLabel, selectedRemotingProfile, selectedEjbReceiver)))
                .button(mbuiContext.tableButtonFactory().remove(RER_CHANNEL_CREATION_OPTIONS_TEMPLATE,
                        table -> presenter.removeRerChannelCreationOptions(ccoTypeLabel, table.selectedRow().getName(),
                                selectedRemotingProfile, selectedEjbReceiver)))
                .nameColumn()
                .columns(TYPE, VALUE)
                .build();

        channelCreationOptionsForm = new ModelNodeForm.Builder<NamedNode>(Ids.build(ccoId, Ids.FORM),
                channelCreationOptionsMetadata)
                .onSave((form, changedValues) -> {
                    String name = form.getModel().getName();
                    saveForm(ccoTypeLabel, name,
                            RER_CHANNEL_CREATION_OPTIONS_TEMPLATE.resolve(statementContext(), selectedRemotingProfile,
                                    selectedEjbReceiver, name),
                            changedValues,
                            channelCreationOptionsMetadata);
                })
                .prepareReset(form -> {
                    String name = form.getModel().getName();
                    resetForm(ccoTypeLabel, name,
                            RER_CHANNEL_CREATION_OPTIONS_TEMPLATE.resolve(statementContext(), selectedRemotingProfile,
                                    selectedEjbReceiver, name),
                            form,
                            channelCreationOptionsMetadata);
                })
                .build();

        HTMLElement channelCreationOptionsItemElement = section()
                .add(h(1).textContent(ccoTypeLabel))
                .add(p().textContent(channelCreationOptionsMetadata.getDescription().getDescription()))
                .add(channelCreationOptionsTable)
                .add(channelCreationOptionsForm)
                .element();

        String rerLabel = labelBuilder.label(ejbReceiverPage.getChildType());
        remotingProfilePages.addPage(ejbReceiverPage.getPageId(), Ids.build(ejbReceiverPage.getChildType(), ccoId, Ids.PAGE),
                () -> rerLabel + ": " + selectedEjbReceiver, () -> ccoTypeLabel, channelCreationOptionsItemElement);

        navigation.insertSecondary("ejb3-container-item", Ids.build(rpId, Ids.ITEM), null,
                rpTypeLabel,
                remotingProfilePages.element());
    }

    @Override
    public void attach() {
        super.attach();
        appSecurityDomainTable.attach();
        appSecurityDomainForm.attach();
        appSecurityDomainTable.bindForm(appSecurityDomainForm);

        remotingProfileTable.attach();
        remotingProfileForm.attach();
        remotingProfileTable.bindForm(remotingProfileForm);

        ejbReceiverPage.attach();
        httpConnectionPage.attach();

        channelCreationOptionsTable.attach();
        channelCreationOptionsForm.attach();
        channelCreationOptionsTable.bindForm(channelCreationOptionsForm);
    }

    @Override
    public void detach() {
        super.detach();
        appSecurityDomainForm.detach();
        appSecurityDomainTable.detach();

        remotingProfileForm.detach();
        remotingProfileTable.detach();

        ejbReceiverPage.detach();
        httpConnectionPage.detach();

        channelCreationOptionsForm.detach();
        channelCreationOptionsTable.detach();
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

    @Override
    public void updateRemotingProfileChild(String name, String childType, ModelNode payload) {
        selectedRemotingProfile = name;
        RemotingProfileSubpage subPage = subPages.get(childType);
        subPage.update(payload);
        remotingProfilePages.showPage(subPage.getPageId());
    }

    @Override
    public void updateRerChannelCreationOptions(String name, ModelNode payload) {
        selectedEjbReceiver = name;
        String cco = RER_CHANNEL_CREATION_OPTIONS_TEMPLATE.lastName();
        channelCreationOptionsTable.update(asNamedNodes(failSafePropertyList(payload, cco)));
        remotingProfilePages.showPage(Ids.build(ejbReceiverPage.getChildType(), cco, Ids.PAGE));
    }

    // ------------------------ child resource helper class

    private class RemotingProfileSubpage {

        private String childType;
        private String label;
        private String pageId;
        private Table<NamedNode> table;
        private Form<NamedNode> form;
        private HTMLElement section;

        public RemotingProfileSubpage(AddressTemplate template) {
            childType = template.lastName();
            label = labelBuilder.label(childType);
            pageId = Ids.build(REMOTING_PROFILE_TEMPLATE.lastName(), childType, Ids.PAGE);
            Metadata metadata = mbuiContext.metadataRegistry().lookup(template);
            String mainId = Ids.build(Ids.EJB3, childType);
            String tableId = Ids.build(mainId, Ids.TABLE);

            String ER_TYPE = REMOTING_EJB_RECEIVER_TEMPLATE.lastName();
            String HC_TYPE = REMOTE_HTTP_CONNECTION_TEMPLATE.lastName();
            List<String> columnNames = null;
            if (childType.equals(ER_TYPE)) {
                columnNames = List.of("connect-timeout", "outbound-connection-ref");
            } else if (childType.equals(HC_TYPE)) {
                columnNames = List.of("uri");
            }

            ModelNodeTable.Builder<NamedNode> builder = new ModelNodeTable.Builder<NamedNode>(tableId, metadata)
                    .button(mbuiContext.tableButtonFactory().add(template,
                            table -> presenter.addRemotingProfileChild(Ids.build(mainId, Ids.ADD), label,
                                    selectedRemotingProfile, childType,
                                    template)))
                    .button(mbuiContext.tableButtonFactory().remove(template,
                            table -> presenter.removeRemotingProfileChild(label, table.selectedRow().getName(),
                                    selectedRemotingProfile, childType, template)))
                    .nameColumn()
                    .columns(columnNames);

            if (childType.equals(ER_TYPE)) {
                String ccoLabel = labelBuilder.label(RER_CHANNEL_CREATION_OPTIONS_TEMPLATE.lastName());
                builder.column(
                        new InlineAction<>(ccoLabel,
                                row -> presenter.loadRerChannelCreationOptions(selectedRemotingProfile, row.getName())),
                        "20em");
            }

            table = builder.build();

            form = new ModelNodeForm.Builder<NamedNode>(Ids.build(mainId, Ids.FORM), metadata)
                    .onSave((form, changedValues) -> {
                        String name = form.getModel().getName();
                        saveForm(label, name,
                                template.resolve(statementContext(), selectedRemotingProfile, name), changedValues,
                                metadata);
                    })
                    .prepareReset(form -> {
                        String name = form.getModel().getName();
                        resetForm(label, name,
                                template.resolve(statementContext(), selectedRemotingProfile, name), form,
                                metadata);
                    })
                    .build();

            section = section()
                    .add(h(1).textContent(label))
                    .add(p().textContent(metadata.getDescription().getDescription()))
                    .add(table)
                    .add(form)
                    .element();
        }

        public String getChildType() {
            return childType;
        }

        public String getPageId() {
            return pageId;
        }

        public void addToPages(Pages pages, String mainPageId, String mainLabel) {
            pages.addPage(mainPageId, pageId, () -> mainLabel + ": " + selectedRemotingProfile, () -> label, section);
        }

        public InlineAction<NamedNode> makeInlineAction() {
            return new InlineAction<>(label, row -> presenter.loadRemotingProfileChild(row.getName(), childType));
        }

        public void update(ModelNode payload) {
            table.update(asNamedNodes(failSafePropertyList(payload, childType)));
            form.clear();
        }

        public void attach() {
            table.attach();
            form.attach();
            table.bindForm(form);
        }

        public void detach() {
            form.detach();
            table.detach();
        }
    }
}
