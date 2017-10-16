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
import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.Form.FinishRemove;
import org.jboss.hal.ballroom.form.Form.FinishReset;
import org.jboss.hal.ballroom.form.ListItem;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
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

/** Implementation note: Not based on MBUI XML due to special handling of handlers of the audit-log configuration. */
public class JmxView extends HalViewImpl implements JmxPresenter.MyView {

    private final ModelNodeForm<ModelNode> configForm;
    private final ListItem handlerItem;
    private final Form<ModelNode> alForm;
    private final Form<ModelNode> rcForm;

    private JmxPresenter presenter;

    @Inject
    public JmxView(CrudOperations crud,
            Dispatcher dispatcher,
            MetadataRegistry metadataRegistry,
            StatementContext statementContext) {

        LabelBuilder labelBuilder = new LabelBuilder();
        VerticalNavigation navigation = new VerticalNavigation();
        registerAttachable(navigation);

        // ------------------------------------------------------ configuration

        Metadata configMetadata = metadataRegistry.lookup(JMX_TEMPLATE);
        configForm = new ModelNodeForm.Builder<>(Ids.JMX_CONFIGURATION_FORM, configMetadata)
                .onSave((form, changedValues) -> crud
                        .saveSingleton(Names.CONFIGURATION, JMX_TEMPLATE, changedValues, () -> presenter.reload()))
                .prepareReset(form -> crud.resetSingleton(Names.CONFIGURATION, JMX_TEMPLATE, form, configMetadata,
                        new FinishReset<ModelNode>(form) {
                            @Override
                            public void afterReset(final Form<ModelNode> form) {
                                presenter.reload();
                            }
                        }))
                .build();

        HTMLElement configLayout = section()
                .add(h(1).textContent(Names.CONFIGURATION))
                .add(p().textContent(configMetadata.getDescription().getDescription()))
                .add(configForm)
                .asElement();

        navigation.addPrimary(Ids.JMX_CONFIGURATION_ENTRY, Names.CONFIGURATION, pfIcon("settings"), configLayout);
        registerAttachable(configForm);

        // ------------------------------------------------------ audit log

        // The handlers are modeled as sub-resources of the audit-log configuration,
        // but in the UI here they're managed by a ListItem.
        Metadata handlerMetadata = metadataRegistry.lookup(AUDIT_LOG_HANDLER_TEMPLATE);
        SafeHtml handlerDescription = SafeHtmlUtils.fromString(handlerMetadata.getDescription().getDescription());

        handlerItem = new ListItem(HANDLER, labelBuilder.label(HANDLER));
        List<AddressTemplate> templates = asList(
                AddressTemplate.of("{domain.controller}/core-service=management/access=audit/file-handler=*"),
                AddressTemplate.of("{domain.controller}/core-service=management/access=audit/syslog-handler=*"));
        handlerItem.registerSuggestHandler(new ReadChildrenAutoComplete(dispatcher, statementContext,
                templates));

        Metadata alMetadata = metadataRegistry.lookup(AUDIT_LOG_TEMPLATE);
        this.alForm = new ModelNodeForm.Builder<>(Ids.JMX_AUDIT_LOG_FORM, alMetadata)
                .singleton(
                        () -> new Operation.Builder(AUDIT_LOG_TEMPLATE.resolve(statementContext),
                                READ_RESOURCE_OPERATION
                        ).build(),
                        () -> crud.addSingleton(Names.AUDIT_LOG, AUDIT_LOG_TEMPLATE, address -> presenter.reload()))
                .unboundFormItem(handlerItem, Integer.MAX_VALUE, handlerDescription)
                .onSave((form, changedValues) -> presenter
                        .saveAuditLog(changedValues, handlerItem.isModified(), handlerItem.getValue()))
                .prepareReset(form -> crud.resetSingleton(Names.AUDIT_LOG, AUDIT_LOG_TEMPLATE, form, alMetadata,
                        new FinishReset<ModelNode>(form) {
                            @Override
                            public void afterReset(final Form<ModelNode> form) {
                                presenter.reload();
                            }
                        }))
                .prepareRemove(form -> crud.removeSingleton(Names.AUDIT_LOG, AUDIT_LOG_TEMPLATE,
                        new FinishRemove<ModelNode>(form) {
                            @Override
                            public void afterRemove(final Form<ModelNode> form) {
                                presenter.reload();
                            }
                        }))
                .build();

        HTMLElement alLayout = section()
                .add(h(1).textContent(Names.AUDIT_LOG))
                .add(p().textContent(alMetadata.getDescription().getDescription()))
                .add(this.alForm)
                .asElement();

        navigation.addPrimary(Ids.JMX_AUDIT_LOG_ENTRY, Names.AUDIT_LOG, fontAwesome("file-text-o"), alLayout);
        registerAttachable(this.alForm);

        // ------------------------------------------------------ remoting connector

        String type = labelBuilder.label(REMOTING_CONNECTOR_TEMPLATE.lastName());
        Metadata rcMetadata = metadataRegistry.lookup(REMOTING_CONNECTOR_TEMPLATE);
        this.rcForm = new ModelNodeForm.Builder<>(Ids.JMX_REMOTING_CONNECTOR_FORM, rcMetadata)
                .singleton(
                        () -> new Operation.Builder(REMOTING_CONNECTOR_TEMPLATE.resolve(statementContext),
                                READ_RESOURCE_OPERATION
                        ).build(),
                        () -> crud.addSingleton(type, REMOTING_CONNECTOR_TEMPLATE, address -> presenter.reload()))
                .onSave((form, changedValues) -> crud.saveSingleton(type, REMOTING_CONNECTOR_TEMPLATE,
                        changedValues, () -> presenter.reload()))
                .prepareReset(form -> crud.resetSingleton(type, REMOTING_CONNECTOR_TEMPLATE, form, rcMetadata,
                        new FinishReset<ModelNode>(form) {
                            @Override
                            public void afterReset(final Form<ModelNode> form) {
                                presenter.reload();
                            }
                        }))
                .prepareRemove(form -> crud.removeSingleton(type, REMOTING_CONNECTOR_TEMPLATE,
                        new FinishRemove<ModelNode>(form) {
                            @Override
                            public void afterRemove(final Form<ModelNode> form) {
                                presenter.reload();
                            }
                        }))
                .build();

        HTMLElement rcLayout = section()
                .add(h(1).textContent(type))
                .add(p().textContent(rcMetadata.getDescription().getDescription()))
                .add(this.rcForm)
                .asElement();

        navigation.addPrimary(Ids.JMX_REMOTING_CONNECTOR_ENTRY, type, pfIcon("topology"), rcLayout);
        registerAttachable(this.rcForm);

        // ------------------------------------------------------ main layout

        initElement(row()
                .add(column()
                        .addAll(navigation.panes())));
    }

    @Override
    public void setPresenter(final JmxPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public void update(final ModelNode payload) {
        configForm.view(payload);
        alForm.view(failSafeGet(payload, "configuration/audit-log"));
        List<Property> handler = failSafePropertyList(payload, "configuration/audit-log/handler");
        if (handler.isEmpty()) {
            handlerItem.clearValue();
        } else {
            handlerItem.setValue(handler.stream().map(Property::getName).collect(toList()));
        }
        rcForm.view(failSafeGet(payload, "remoting-connector/jmx"));
    }
}
