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
package org.jboss.hal.client.configuration.subsystem.infinispan;

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
import org.jboss.hal.meta.MissingMetadataException;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.stream.Collectors.toList;
import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.hal.ballroom.JQuery.$;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HASH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MEMORY;
import static org.jboss.hal.resources.CSS.bootstrapSelect;
import static org.jboss.hal.resources.CSS.btnGroup;
import static org.jboss.hal.resources.CSS.selectpicker;
import static org.jboss.hal.resources.CSS.width;

/** Element to view and modify the {@code memory=*} singletons of a cache. */
class MemoryElement implements IsElement<HTMLElement>, Attachable, HasPresenter<CachePresenter<?, ?>> {

    private final HTMLElement currentMemory;
    private final HTMLElement headerForm;
    private final String selectMemoryId;
    private final HTMLSelectElement selectMemory;
    private final Map<Memory, Form<ModelNode>> memoryForms;
    private final HTMLElement root;
    private CachePresenter<?, ?> presenter;

    MemoryElement(CacheType cacheType, MetadataRegistry metadataRegistry, Resources resources) {
        memoryForms = new HashMap<>();

        selectMemoryId = Ids.build(cacheType.baseId, MEMORY, "select");
        selectMemory = memorySelect();
        selectMemory.id = selectMemoryId;

        for (Memory memory : Memory.values()) {
            Metadata metadata;
            try {
                metadata = metadataRegistry.lookup(cacheType.template.append(MEMORY + "=" + memory.resource));
            } catch (MissingMetadataException ex) {
                continue;
            }
            appendChild(memory);
            Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(cacheType.baseId, memory.baseId, Ids.FORM),
                    metadata)
                    .onSave((f, changedValues) -> presenter.saveMemory(memory, changedValues))
                    .prepareReset(f -> presenter.resetMemory(memory, f))
                    .build();
            Elements.setVisible(form.element(), false);
            memoryForms.put(memory, form);
        }

        root = section()
                .add(headerForm = div().css(CSS.headerForm)
                        .add(label()
                                .apply(l -> l.htmlFor = selectMemoryId)
                                .textContent(resources.constants().switchMemory()))
                        .add(selectMemory).element())
                .add(h(1).textContent(Names.MEMORY)
                        .add(currentMemory = span().element()))
                .add(p().textContent(resources.constants().cacheMemory()))
                .addAll(memoryForms.values().stream().map(Form::element).collect(toList())).element();
    }

    private HTMLSelectElement memorySelect() {
        HTMLSelectElement select = select().css(selectpicker)
                .apply(s -> {
                    s.multiple = false;
                    s.size = 1;
                }).element();

        SelectBoxBridge.Single.element(select).onChange((event, index) -> {
            String value = SelectBoxBridge.Single.element(select).getValue();
            Memory memory = Memory.fromResource(value);
            presenter.switchMemory(memory);
        });

        return select;
    }

    private void appendChild (Memory memory) {
        selectMemory.appendChild(option()
                .apply(o -> {
                    o.value = memory.resource;
                    o.text = memory.type;
                }).element());
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public void attach() {
        SelectBoxBridge.Options options = SelectBoxBridge.Defaults.get();
        $(HASH + selectMemoryId).selectpicker(options);
        autoWidth(headerForm);
        memoryForms.values().forEach(Attachable::attach);
    }

    private void autoWidth(HTMLElement element) {
        HTMLElement select = (HTMLElement) element.querySelector("." + btnGroup + "." + bootstrapSelect);
        if (select != null) {
            select.style.width = width("auto"); //NON-NLS
        }
    }

    @Override
    public void detach() {
        memoryForms.values().forEach(Attachable::detach);
    }

    @Override
    public void setPresenter(CachePresenter<?, ?> presenter) {
        this.presenter = presenter;
    }

    void update(List<Property> memories) {
        Memory memory = Memory.fromResource(memories.get(0).getName());
        if (memory != null) {
            currentMemory.textContent = ": " + memory.type;
            SelectBoxBridge.Single.element(selectMemory).setValue(memory.resource);

            ModelNode memoryNode = memories.get(0).getValue();
            memoryForms.get(memory).view(memoryNode);
        }
        memoryForms.forEach((m, f) -> Elements.setVisible(f.element(), m == memory));
    }
}
