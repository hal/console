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

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.resources.CSS.clearfix;
import static org.jboss.hal.resources.CSS.clickable;
import static org.jboss.hal.resources.CSS.marginTopLarge;
import static org.jboss.hal.resources.CSS.pullRight;

public class HaPolicyView extends HalViewImpl implements HaPolicyPresenter.MyView {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(HaPolicyView.class);

    private final MetadataRegistry metadataRegistry;
    private final Resources resources;
    private final EmptyState emptyState;
    private final Map<HaPolicy, HTMLElement> policyElements;
    private final Map<HaPolicy, Form<ModelNode>> policyForms;
    private final HTMLElement root;

    private HaPolicyPresenter presenter;
    private Form<ModelNode> currentForm;
    private Form<ModelNode> currentMasterForm;
    private Form<ModelNode> currentSlaveForm;

    @Inject
    public HaPolicyView(final MetadataRegistry metadataRegistry, final Resources resources) {
        this.metadataRegistry = metadataRegistry;
        this.resources = resources;

        emptyState = new EmptyState.Builder(resources.constants().noHaPolicy())
                .icon(CSS.pfIcon("cluster"))
                .description(resources.messages().addHaPolicy())
                .primaryAction(resources.messages().addResourceTitle(Names.HA_POLICY), () -> presenter.addHaPolicy())
                .build();
        emptyState.asElement().classList.add(marginTopLarge);

        policyElements = new HashMap<>();
        policyForms = new HashMap<>();

        createSimple(HaPolicy.LIVE_ONLY);
        createColocated(HaPolicy.REPLICATION_COLOCATED);
        createSimple(HaPolicy.REPLICATION_MASTER);
        createSimple(HaPolicy.REPLICATION_SLAVE);

        createColocated(HaPolicy.SHARED_STORE_COLOCATED);
        createSimple(HaPolicy.SHARED_STORE_MASTER);
        createSimple(HaPolicy.SHARED_STORE_SLAVE);

        root = div().asElement();
        initElement(root);
    }

    private void createSimple(HaPolicy haPolicy) {
        Metadata metadata = metadataRegistry.lookup(haPolicy.template);
        Form<ModelNode> form = form(haPolicy);

        HTMLElement element = section().css(clearfix)
                .add(h(1).textContent(haPolicy.type))
                .add(a().css(clickable, pullRight)
                        .on(click, event -> presenter.resetHaPolicy())
                        .textContent(resources.constants().remove()))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(form)
                .asElement();

        policyForms.put(haPolicy, form);
        policyElements.put(haPolicy, element);
    }

    private void createColocated(HaPolicy haPolicy) {
        Metadata colocatedMetadata = metadataRegistry.lookup(haPolicy.template);
        Form<ModelNode> colocatedForm = form(haPolicy);
        Form<ModelNode> masterForm = form(haPolicy.master);
        Form<ModelNode> slaveForm = form(haPolicy.slave);

        Tabs tabs = new Tabs();
        tabs.add(Ids.build(haPolicy.baseId, Ids.TAB_SUFFIX), resources.constants().attributes(),
                colocatedForm.asElement());
        tabs.add(Ids.build(haPolicy.master.baseId, Ids.TAB_SUFFIX), Names.MASTER, masterForm.asElement());
        tabs.add(Ids.build(haPolicy.slave.baseId, Ids.TAB_SUFFIX), Names.SLAVE, slaveForm.asElement());

        HTMLElement element = section().css(clearfix)
                .add(h(1).textContent(haPolicy.type))
                .add(a().css(clickable, pullRight)
                        .on(click, event -> presenter.resetHaPolicy())
                        .textContent(resources.constants().remove()))
                .add(p().textContent(colocatedMetadata.getDescription().getDescription()))
                .add(tabs)
                .asElement();

        policyForms.put(haPolicy, colocatedForm);
        policyForms.put(haPolicy.master, masterForm);
        policyForms.put(haPolicy.slave, slaveForm);
        policyElements.put(haPolicy, element);
    }

    private Form<ModelNode> form(HaPolicy haPolicy) {
        Metadata metadata = metadataRegistry.lookup(haPolicy.template);
        return new ModelNodeForm.Builder<>(Ids.build(haPolicy.baseId, Ids.FORM_SUFFIX), metadata)
                .onSave((f, changedValues) -> presenter.saveHaPolicy(haPolicy, changedValues))
                .prepareReset(f -> presenter.resetHaPolicy(haPolicy, f))
                .build();
    }

    @Override
    public void detach() {
        super.detach();
        detachForms();
    }

    private void detachForms() {
        if (currentForm != null) {
            currentForm.detach();
        }
        if (currentMasterForm != null) {
            currentMasterForm.detach();
        }
        if (currentSlaveForm != null) {
            currentSlaveForm.detach();
        }
    }

    @Override
    public void setPresenter(final HaPolicyPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void empty() {
        detachForms();
        Elements.removeChildrenFrom(root);
        root.appendChild(emptyState.asElement());
    }

    @Override
    public void update(final HaPolicy haPolicy, final ModelNode modelNode) {
        HTMLElement element = policyElements.get(haPolicy);
        Form<ModelNode> form = policyForms.get(haPolicy);

        if (element != null && form != null) {
            detachForms();
            Elements.removeChildrenFrom(root);

            currentForm = form;
            root.appendChild(element);
            currentForm.attach();
            currentForm.view(modelNode);

            if (haPolicy.master != null && policyForms.containsKey(haPolicy.master)) {
                currentMasterForm = policyForms.get(haPolicy.master);
                currentMasterForm.attach();
                currentMasterForm.view(failSafeGet(modelNode, "configuration/master")); //NON-NLS
            }
            if (haPolicy.slave != null && policyForms.containsKey(haPolicy.slave)) {
                currentSlaveForm = policyForms.get(haPolicy.slave);
                currentSlaveForm.attach();
                currentSlaveForm.view(failSafeGet(modelNode, "configuration/slave")); //NON-NLS
            }

        } else {
            logger.error("Unable to update HA policy {}: policyElements.get({}) == null || policyForms.get({}) == null",
                    haPolicy, haPolicy, haPolicy);
        }
    }
}
