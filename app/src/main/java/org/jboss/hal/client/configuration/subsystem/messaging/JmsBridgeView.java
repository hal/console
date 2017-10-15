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
package org.jboss.hal.client.configuration.subsystem.messaging;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.core.elytron.CredentialReference;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.JMS_BRIDGE_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

public class JmsBridgeView extends HalViewImpl implements JmsBridgePresenter.MyView {

    private Form<NamedNode> attributesForm;
    private Form<NamedNode> sourceForm;
    private Form<NamedNode> targetForm;
    private Form<ModelNode> crSource;
    private Form<ModelNode> crTarget;
    private JmsBridgePresenter presenter;

    @Inject
    JmsBridgeView(final MbuiContext mbuiContext, CredentialReference cr) {

        Metadata jmsBridgeMetadata = mbuiContext.metadataRegistry().lookup(JMS_BRIDGE_TEMPLATE);

        List<String> attributes = new ArrayList<>();
        List<String> sourceAttrs = new ArrayList<>();
        List<String> targetAttrs = new ArrayList<>();
        jmsBridgeMetadata.getDescription().getAttributes(ATTRIBUTES).forEach(p -> {
            if (p.getName().startsWith(SOURCE)) {
                sourceAttrs.add(p.getName());
            } else if (p.getName().startsWith(TARGET)) {
                targetAttrs.add(p.getName());
            } else {
                attributes.add(p.getName());
            }
        });

        attributesForm = new ModelNodeForm.Builder<NamedNode>(Ids.JMS_BRIDGE_FORM, jmsBridgeMetadata)
                .include(attributes)
                .onSave((form, changedValues) -> presenter.saveJmsBridge(changedValues))
                .prepareReset(form -> presenter.resetJmsBridge(form))
                .build();

        sourceForm = new ModelNodeForm.Builder<NamedNode>(Ids.build(Ids.JMS_BRIDGE_FORM, SOURCE), jmsBridgeMetadata)
                .include(sourceAttrs)
                .onSave((form, changedValues) -> presenter.saveJmsBridge(changedValues))
                .prepareReset(form -> presenter.resetJmsBridge(form))
                .build();

        targetForm = new ModelNodeForm.Builder<NamedNode>(Ids.build(Ids.JMS_BRIDGE_FORM, TARGET), jmsBridgeMetadata)
                .include(targetAttrs)
                .onSave((form, changedValues) -> presenter.saveJmsBridge(changedValues))
                .prepareReset(form -> presenter.resetJmsBridge(form))
                .build();

        crSource = cr.form(Ids.JMS_BRIDGE, jmsBridgeMetadata, SOURCE_CREDENTIAL_REFERENCE, SOURCE_PASSWORD,
                () -> sourceForm.<String>getFormItem(SOURCE_PASSWORD).getValue(),
                () -> presenter.resourceAddress(),
                () -> presenter.reload());
        sourceForm.addFormValidation(
                new CredentialReference.AlternativeValidation<>(SOURCE_PASSWORD, () -> crSource.getModel(),
                        mbuiContext.resources()));
        crTarget = cr.form(Ids.JMS_BRIDGE, jmsBridgeMetadata, TARGET_CREDENTIAL_REFERENCE, TARGET_PASSWORD,
                () -> targetForm.<String>getFormItem(TARGET_PASSWORD).getValue(),
                () -> presenter.resourceAddress(),
                () -> presenter.reload());
        targetForm.addFormValidation(
                new CredentialReference.AlternativeValidation<>(TARGET_PASSWORD, () -> crTarget.getModel(),
                        mbuiContext.resources()));


        registerAttachable(attributesForm, sourceForm, targetForm, crSource, crTarget);

        LabelBuilder labelBuilder = new LabelBuilder();
        Tabs tabs = new Tabs();
        tabs.add(Ids.JMS_BRIDGE_TAB, mbuiContext.resources().constants().attributes(), attributesForm.asElement());
        tabs.add(Ids.build(Ids.JMS_BRIDGE, SOURCE, Ids.TAB), Names.SOURCE, sourceForm.asElement());
        tabs.add(Ids.build(Ids.JMS_BRIDGE, SOURCE_CREDENTIAL_REFERENCE, Ids.TAB),
                labelBuilder.label(SOURCE_CREDENTIAL_REFERENCE), crSource.asElement());
        tabs.add(Ids.build(Ids.JMS_BRIDGE, TARGET, Ids.TAB), Names.TARGET, targetForm.asElement());
        tabs.add(Ids.build(Ids.JMS_BRIDGE, TARGET_CREDENTIAL_REFERENCE, Ids.TAB),
                labelBuilder.label(TARGET_CREDENTIAL_REFERENCE), crTarget.asElement());

        HTMLElement htmlSection = section()
                .add(h(1).textContent(Names.JMS_BRIDGE))
                .add(p().textContent(jmsBridgeMetadata.getDescription().getDescription()))
                .add(tabs)
                .asElement();

        initElement(htmlSection);
    }

    @Override
    public void update(final NamedNode model) {
        attributesForm.view(model);
        sourceForm.view(model);
        targetForm.view(model);
        crSource.view(model.asModelNode().get(SOURCE_CREDENTIAL_REFERENCE));
        crTarget.view(model.asModelNode().get(TARGET_CREDENTIAL_REFERENCE));
    }

    @Override
    public void setPresenter(final JmsBridgePresenter presenter) {
        this.presenter = presenter;
    }
}
