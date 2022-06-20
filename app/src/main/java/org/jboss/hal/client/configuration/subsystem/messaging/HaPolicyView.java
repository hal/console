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
package org.jboss.hal.client.configuration.subsystem.messaging;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.elemento.Elements;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.resources.CSS.clearfix;
import static org.jboss.hal.resources.CSS.clickable;
import static org.jboss.hal.resources.CSS.marginTopLarge;
import static org.jboss.hal.resources.CSS.pullRight;

public class HaPolicyView extends HalViewImpl implements HaPolicyPresenter.MyView {

    private static final Logger logger = LoggerFactory.getLogger(HaPolicyView.class);

    private final MetadataRegistry metadataRegistry;
    private final Resources resources;
    private final EmptyState emptyState;
    private final Map<HaPolicy, HTMLElement> policyElements;
    private final Map<HaPolicy, Form<ModelNode>> policyForms;
    private final HTMLElement root;

    private HaPolicyPresenter presenter;
    private Form<ModelNode> currentForm;
    private Form<ModelNode> currentPrimaryForm;
    private Form<ModelNode> currentSecondaryForm;

    @Inject
    public HaPolicyView(MetadataRegistry metadataRegistry, Resources resources) {
        this.metadataRegistry = metadataRegistry;
        this.resources = resources;

        emptyState = new EmptyState.Builder(Ids.MESSAGING_HA_POLICY_EMPTY, resources.constants().noHaPolicy())
                .icon(CSS.pfIcon("cluster"))
                .description(resources.messages().addHaPolicy())
                .primaryAction(resources.messages().addResourceTitle(Names.HA_POLICY), () -> presenter.addHaPolicy())
                .build();
        emptyState.element().classList.add(marginTopLarge);

        policyElements = new HashMap<>();
        policyForms = new HashMap<>();

        createSimple(HaPolicy.LIVE_ONLY);
        createColocated(HaPolicy.REPLICATION_COLOCATED);
        createSimple(HaPolicy.REPLICATION_PRIMARY);
        createSimple(HaPolicy.REPLICATION_SECONDARY);

        createColocated(HaPolicy.SHARED_STORE_COLOCATED);
        createSimple(HaPolicy.SHARED_STORE_PRIMARY);
        createSimple(HaPolicy.SHARED_STORE_SECONDARY);

        root = div().element();
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
                .add(form).element();

        policyForms.put(haPolicy, form);
        policyElements.put(haPolicy, element);
    }

    private void createColocated(HaPolicy haPolicy) {
        Metadata colocatedMetadata = metadataRegistry.lookup(haPolicy.template);
        Form<ModelNode> colocatedForm = form(haPolicy);
        Form<ModelNode> primaryForm = form(haPolicy.primary);
        Form<ModelNode> secondaryForm = form(haPolicy.secondary);

        Tabs tabs = new Tabs(Ids.build(haPolicy.baseId, Ids.TAB_CONTAINER));
        tabs.add(Ids.build(haPolicy.baseId, Ids.TAB), resources.constants().attributes(),
                colocatedForm.element());
        tabs.add(Ids.build(haPolicy.primary.baseId, Ids.TAB), Names.PRIMARY, primaryForm.element());
        tabs.add(Ids.build(haPolicy.secondary.baseId, Ids.TAB), Names.SECONDARY, secondaryForm.element());

        HTMLElement element = section().css(clearfix)
                .add(h(1).textContent(haPolicy.type))
                .add(a().css(clickable, pullRight)
                        .on(click, event -> presenter.resetHaPolicy())
                        .textContent(resources.constants().remove()))
                .add(p().textContent(colocatedMetadata.getDescription().getDescription()))
                .add(tabs).element();

        policyForms.put(haPolicy, colocatedForm);
        policyForms.put(haPolicy.primary, primaryForm);
        policyForms.put(haPolicy.secondary, secondaryForm);
        policyElements.put(haPolicy, element);
    }

    private Form<ModelNode> form(HaPolicy haPolicy) {
        Metadata metadata = metadataRegistry.lookup(haPolicy.template);
        return new ModelNodeForm.Builder<>(Ids.build(haPolicy.baseId, Ids.FORM), metadata)
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
        if (currentPrimaryForm != null) {
            currentPrimaryForm.detach();
        }
        if (currentSecondaryForm != null) {
            currentSecondaryForm.detach();
        }
    }

    @Override
    public void setPresenter(HaPolicyPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void empty() {
        detachForms();
        Elements.removeChildrenFrom(root);
        root.appendChild(emptyState.element());
    }

    @Override
    public void update(HaPolicy haPolicy, ModelNode modelNode) {
        HTMLElement element = policyElements.get(haPolicy);
        Form<ModelNode> form = policyForms.get(haPolicy);

        if (element != null && form != null) {
            detachForms();
            Elements.removeChildrenFrom(root);

            currentForm = form;
            root.appendChild(element);
            currentForm.attach();
            currentForm.view(modelNode);

            if (haPolicy.primary != null && policyForms.containsKey(haPolicy.primary)) {
                currentPrimaryForm = policyForms.get(haPolicy.primary);
                currentPrimaryForm.attach();
                currentPrimaryForm.view(failSafeGet(modelNode, "configuration/primary")); // NON-NLS
            }
            if (haPolicy.secondary != null && policyForms.containsKey(haPolicy.secondary)) {
                currentSecondaryForm = policyForms.get(haPolicy.secondary);
                currentSecondaryForm.attach();
                currentSecondaryForm.view(failSafeGet(modelNode, "configuration/secondary")); // NON-NLS
            }

        } else {
            logger.error("Unable to update HA policy {}: policyElements.get({}) == null || policyForms.get({}) == null",
                    haPolicy, haPolicy, haPolicy);
        }
    }
}
