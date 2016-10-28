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
package org.jboss.hal.client.configuration.subsystem.jmx;

import java.util.List;
import javax.inject.Inject;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.ListItem;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.mbui.form.FailSafeModelNodeForm;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.configuration.subsystem.jmx.AddressTemplates.AUDIT_LOG_HANDLER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jmx.AddressTemplates.AUDIT_LOG_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jmx.AddressTemplates.JMX_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jmx.AddressTemplates.REMOTING_CONNECTOR_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HANDLER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;

/**
 * Implementation note: Not based on MBUI XML due to special handling of handlers of the audit-log configuration.
 *
 * @author Harald Pehl
 */
public class JmxView extends HalViewImpl implements JmxPresenter.MyView {

    private final ModelNodeForm<ModelNode> configForm;
    private final ListItem handlerItem;
    private final FailSafeModelNodeForm<ModelNode> failSafeAlForm;
    private final FailSafeModelNodeForm<ModelNode> failSafeRcForm;

    private JmxPresenter presenter;

    @Inject
    public JmxView(Dispatcher dispatcher,
            StatementContext statementContext,
            CrudOperations crudOperations,
            MetadataRegistry metadataRegistry,
            Resources resources) {

        LabelBuilder labelBuilder = new LabelBuilder();
        VerticalNavigation navigation = new VerticalNavigation();
        registerAttachable(navigation);

        // ------------------------------------------------------ configuration

        Metadata configMetadata = metadataRegistry.lookup(JMX_TEMPLATE);
        configForm = new ModelNodeForm.Builder<>(Ids.JMX_CONFIGURATION_FORM, configMetadata)
                .onSave((form, changedValues) -> crudOperations
                        .saveSingleton(Names.CONFIGURATION, JMX_TEMPLATE, changedValues, () -> presenter.load()))
                .build();

        // @formatter:off
        Element configLayout = new Elements.Builder()
            .div()
                .h(1).textContent(Names.CONFIGURATION).end()
                .p().textContent(configMetadata.getDescription().getDescription()).end()
                .add(configForm)
            .end()
        .build();
        // @formatter:on

        navigation.addPrimary(Ids.JMX_CONFIGURATION_ENTRY, Names.CONFIGURATION, pfIcon("settings"), configLayout);
        registerAttachable(configForm);

        // ------------------------------------------------------ audit log

        Metadata handlerMetadata = metadataRegistry.lookup(AUDIT_LOG_HANDLER_TEMPLATE);
        SafeHtml handlerDescription = SafeHtmlUtils.fromString(handlerMetadata.getDescription().getDescription());

        handlerItem = new ListItem(HANDLER, labelBuilder.label(HANDLER));
        List<AddressTemplate> templates = asList(
                AddressTemplate.of("{domain.controller}/core-service=management/access=audit/file-handler=*"),
                AddressTemplate.of("{domain.controller}/core-service=management/access=audit/syslog-handler=*"));
        handlerItem.registerSuggestHandler(new ReadChildrenAutoComplete(dispatcher, statementContext,
                templates));

        Metadata alMetadata = metadataRegistry.lookup(AUDIT_LOG_TEMPLATE);
        Form<ModelNode> alForm = new ModelNodeForm.Builder<>(Ids.JMX_AUDIT_LOG_FORM, alMetadata)
                .unboundFormItem(handlerItem, Integer.MAX_VALUE, handlerDescription)
                .onSave((form, changedValues) -> presenter
                        .saveAuditLog(changedValues, handlerItem.isModified(), handlerItem.getValue()))
                .build();
        failSafeAlForm = new FailSafeModelNodeForm<>(dispatcher,
                new Operation.Builder(READ_RESOURCE_OPERATION, AUDIT_LOG_TEMPLATE.resolve(statementContext)).build(),
                alForm, () -> crudOperations.addSingleton(Names.AUDIT_LOG, AUDIT_LOG_TEMPLATE, () -> presenter.load()));

        // @formatter:off
        Element alLayout = new Elements.Builder()
            .div()
                .h(1).textContent(Names.AUDIT_LOG).end()
                .p().textContent(alMetadata.getDescription().getDescription()).end()
                .add(failSafeAlForm)
            .end()
        .build();
        // @formatter:on

        navigation.addPrimary(Ids.JMX_AUDIT_LOG_ENTRY, Names.AUDIT_LOG, fontAwesome("file-text-o"), alLayout);
        registerAttachable(failSafeAlForm);

        // ------------------------------------------------------ remoting connector

        String type = labelBuilder.label(REMOTING_CONNECTOR_TEMPLATE.lastKey());
        Metadata rcMetadata = metadataRegistry.lookup(REMOTING_CONNECTOR_TEMPLATE);
        Form<ModelNode> rcForm = new ModelNodeForm.Builder<>(Ids.JMX_REMOTING_CONNECTOR_FORM, rcMetadata)
                .onSave((form, changedValues) -> crudOperations.saveSingleton(type, REMOTING_CONNECTOR_TEMPLATE,
                        changedValues, () -> presenter.load()))
                .build();
        failSafeRcForm = new FailSafeModelNodeForm<>(dispatcher,
                new Operation.Builder(READ_RESOURCE_OPERATION, REMOTING_CONNECTOR_TEMPLATE.resolve(statementContext))
                        .build(),
                rcForm, () -> crudOperations.addSingleton(type, REMOTING_CONNECTOR_TEMPLATE, () -> presenter.load()));

        // @formatter:off
        Element rcLayout = new Elements.Builder()
            .div()
                .h(1).textContent(type).end()
                .p().textContent(rcMetadata.getDescription().getDescription()).end()
                .add(failSafeRcForm)
            .end()
        .build();
        // @formatter:on

        navigation.addPrimary(Ids.JMX_REMOTING_CONNECTOR_ENTRY, type, pfIcon("topology"), rcLayout);
        registerAttachable(failSafeRcForm);

        // ------------------------------------------------------ main layout

        // @formatter:off
        LayoutBuilder layoutBuilder = new LayoutBuilder()
            .row()
                .column()
                    .addAll(navigation.panes())
                .end()
            .end();
        // @formatter:on

        Element root = layoutBuilder.build();
        initElement(root);
    }

    @Override
    public void setPresenter(final JmxPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public void update(final ModelNode payload) {
        configForm.view(payload);
        failSafeAlForm.view(failSafeGet(payload, "configuration/audit-log"));
        List<Property> handler = failSafePropertyList(payload, "configuration/audit-log/handler");
        if (handler.isEmpty()) {
            handlerItem.clearValue();
        } else {
            handlerItem.setValue(handler.stream().map(Property::getName).collect(toList()));
        }
        failSafeRcForm.view(failSafeGet(payload, "remoting-connector/jmx"));
    }
}
