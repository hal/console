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
package org.jboss.hal.client.configuration.subsystem.distributableweb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLSelectElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.SelectBoxBridge;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.stream.Collectors.toList;
import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.hal.ballroom.JQuery.$;
import static org.jboss.hal.dmr.ModelDescriptionConstants.AFFINITY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HASH;
import static org.jboss.hal.resources.CSS.bootstrapSelect;
import static org.jboss.hal.resources.CSS.btnGroup;
import static org.jboss.hal.resources.CSS.selectpicker;
import static org.jboss.hal.resources.CSS.width;

/** Element to view and modify the {@code affinity=*} singletons of a session management */
class AffinityElement implements IsElement<HTMLElement>, Attachable, HasPresenter<DistributableWebPresenter> {

    private final HTMLElement currentAffinity;
    private final HTMLElement headerForm;
    private final String selectAffinityId;
    private final HTMLSelectElement selectAffinity;
    private final Map<Affinity, Form<ModelNode>> affinityForms;
    private final HTMLElement root;
    private DistributableWebPresenter presenter;
    private boolean disabled;

    private final Affinity[] values;

    private String mgmtName;
    private SessionManagement sessionManagement;

    AffinityElement(SessionManagement sessionManagement, MetadataRegistry metadataRegistry, Resources resources) {
        this.sessionManagement = sessionManagement;
        affinityForms = new HashMap<>();

        // hotrod session mgmt doesn't support the full range of affinities
        values = sessionManagement == SessionManagement.INFINISPAN ? Affinity.values() : new Affinity[]{Affinity.LOCAL, Affinity.NONE, Affinity.UNDEFINED};

        selectAffinityId = Ids.build("dw", sessionManagement.type, AFFINITY, "select");
        selectAffinity = affinitySelect();
        selectAffinity.id = selectAffinityId;

        for (Affinity affinity : values) {
            if (affinity == Affinity.UNDEFINED) {
                continue;
            }
            Metadata metadata = metadataRegistry.lookup(sessionManagement.template.append(AddressTemplates.AFFINITY_TEMPLATE.replaceWildcards(affinity.resource)));
            Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(affinity.baseId, Ids.FORM),
                    metadata)
                    .onSave((f, changedValues) -> presenter.saveAffinity(affinity, changedValues))
                    .prepareReset(f -> presenter.resetAffinity(affinity, f))
                    .build();
            Elements.setVisible(form.element(), false);
            affinityForms.put(affinity, form);
        }

        root = section()
                .add(headerForm = div().css(CSS.headerForm)
                        .add(label()
                                .apply(l -> l.htmlFor = selectAffinityId)
                                .textContent(resources.constants().switchAffinity()))
                        .add(selectAffinity).element())
                .add(h(2).textContent(Names.AFFINITY)
                        .add(currentAffinity = span().element()))
                .addAll(affinityForms.values().stream().map(Form::element).collect(toList())).element();
    }

    private HTMLSelectElement affinitySelect() {
        HTMLSelectElement select = select().css(selectpicker)
                .apply(s -> {
                    s.multiple = false;
                    s.size = 1;
                }).element();

        for (Affinity affinity : values) {
            select.appendChild(option()
                    .apply(o -> {
                        o.value = affinity.resource;
                        o.text = affinity.type;
                    }).element());
        }
        return select;
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public void attach() {
        SelectBoxBridge.Options options = SelectBoxBridge.Defaults.get();
        $(HASH + selectAffinityId).selectpicker(options);
        SelectBoxBridge.Single.element(selectAffinity).onChange((event, index) -> {
            if (mgmtName != null) {
                String value = SelectBoxBridge.Single.element(selectAffinity).getValue();
                Affinity affinity = Affinity.fromResource(value);
                presenter.switchAffinity(sessionManagement, mgmtName, affinity);
            }
        });

        autoWidth(headerForm);
        enable(false);
        affinityForms.values().forEach(Attachable::attach);
    }

    private void autoWidth(HTMLElement element) {
        HTMLElement select = (HTMLElement) element.querySelector("." + btnGroup + "." + bootstrapSelect);
        if (select != null) {
            select.style.width = width("auto"); //NON-NLS
        }
    }

    @Override
    public void detach() {
        affinityForms.values().forEach(Attachable::detach);
    }

    @Override
    public void setPresenter(DistributableWebPresenter presenter) {
        this.presenter = presenter;
    }

    void update(String mgmtName, List<Property> affinities) {
        this.mgmtName = mgmtName;
        if (disabled) {
            enable(true);
        }
        if (this.mgmtName == null) {
            enable(false);
        }
        Affinity affinity;
        if (affinities.isEmpty()) {
            affinity = Affinity.UNDEFINED;
        } else {
            affinity = Affinity.fromResource(affinities.get(0).getName());
        }
        currentAffinity.textContent = ": " + affinity.type;
        SelectBoxBridge.Single.element(selectAffinity).setValue(affinity.resource);

        if (affinity != Affinity.UNDEFINED) {
            ModelNode affinityNode = affinities.get(0).getValue();
            affinityForms.get(affinity).view(affinityNode);
        }
        affinityForms.forEach((a, f) -> Elements.setVisible(f.element(), a == affinity));
    }

    private void enable(boolean enable) {
        SelectBoxBridge.Single.element(selectAffinity).enable(enable);
        SelectBoxBridge.Single.element(selectAffinity).selectpicker("refresh");
        disabled = !enable;
    }
}
