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
package org.jboss.hal.core.modelbrowser;

import java.util.Iterator;
import java.util.List;

import org.jboss.elemento.Elements;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.ballroom.StabilityLabel;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.StabilityLevel;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.p;
import static org.jboss.hal.core.modelbrowser.ModelBrowser.PLACE_HOLDER_ELEMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.resources.CSS.lead;

/**
 * Panel which holds the resource description, the model node form and a detailed description of the attributes and operations.
 */
class ResourcePanel implements Iterable<HTMLElement> {

    private static final String RESOURCE = "resource";

    private final ModelBrowser modelBrowser;
    private final Dispatcher dispatcher;
    private final Environment environment;
    private final Resources resources;
    private final Iterable<HTMLElement> elements;
    private final HTMLElement description;
    private final HTMLElement empty;
    private final String dataId;
    private final String attributesId;
    private final String operationsId;
    final Tabs tabs;

    ResourcePanel(ModelBrowser modelBrowser,
            Dispatcher dispatcher,
            Environment environment,
            Resources resources) {
        this.modelBrowser = modelBrowser;
        this.dispatcher = dispatcher;
        this.environment = environment;
        this.resources = resources;

        dataId = Ids.build(Ids.MODEL_BROWSER, RESOURCE, "data", Ids.TAB);
        attributesId = Ids.build(Ids.MODEL_BROWSER, RESOURCE, "attributes", Ids.TAB);
        operationsId = Ids.build(Ids.MODEL_BROWSER, RESOURCE, "operations", Ids.TAB);

        tabs = new Tabs(Ids.build(Ids.MODEL_BROWSER, RESOURCE, Ids.TAB_CONTAINER));
        tabs.add(dataId, resources.constants().data(), PLACE_HOLDER_ELEMENT);
        tabs.add(attributesId, resources.constants().attributes(), PLACE_HOLDER_ELEMENT);
        tabs.add(operationsId, resources.constants().operations(), PLACE_HOLDER_ELEMENT);

        elements = Elements.bag()
                .add(description = p().css(lead).element())
                .add(empty = p().textContent(resources.constants().noAttributes()).element())
                .add(tabs.element()).elements();
        Elements.setVisible(empty, false);
    }

    @Override
    public Iterator<HTMLElement> iterator() {
        return elements.iterator();
    }

    void update(Node<Context> node, ResourceAddress address, Metadata metadata) {
        StabilityLevel stabilityLevel = metadata.getDescription().getStability();

        SafeHtml safeHtml = SafeHtmlUtils.fromSafeConstant(metadata.getDescription().getDescription());
        if (environment.highlightStabilityLevel(stabilityLevel)) {
            SafeHtmlBuilder html = new SafeHtmlBuilder();
            html.append(StabilityLabel.stabilityLevelHtml(stabilityLevel, false));
            html.append(safeHtml);
            Elements.innerHtml(this.description, html.toSafeHtml());
        } else {
            Elements.innerHtml(this.description, safeHtml);
        }

        tabs.setContent(dataId, PLACE_HOLDER_ELEMENT);
        tabs.setContent(attributesId, PLACE_HOLDER_ELEMENT);
        tabs.setContent(operationsId, PLACE_HOLDER_ELEMENT);
        Elements.setVisible(tabs.element(), description.hasAttributes());
        Elements.setVisible(empty, !description.hasAttributes());

        if (description.hasAttributes()) {
            Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            dispatcher.execute(operation, result -> {
                flattenModel(metadata.getDescription().get(ATTRIBUTES).asPropertyList(), result);
                ModelNodeForm<ModelNode> form = new ModelNodeForm.Builder<>(
                        Ids.build(Ids.MODEL_BROWSER, node.id, Ids.FORM), metadata)
                        .includeRuntime()
                        .showDeprecated()
                        .onSave((f, changedValues) -> modelBrowser.save(address, changedValues, metadata))
                        .prepareReset(f -> modelBrowser.reset(address, f, metadata))
                        .build();
                tabs.setContent(dataId, form.element());
                PatternFly.initComponents();
                form.attach();
                form.view(result);
            });

            tabs.setContent(attributesId,
                    new AttributesTable(metadata.getDescription().getAttributes(ATTRIBUTES), environment, resources).element());
            if (!metadata.getDescription().getOperations().isEmpty()) {
                tabs.setContent(operationsId,
                        new OperationsTable(metadata.getDescription().getOperations(), environment, resources).element());
            }
        }
    }

    void show() {
        Elements.setVisible(description, true); // the remaining elements are managed in update()
    }

    void hide() {
        for (HTMLElement element : elements) {
            Elements.setVisible(element, false);
        }
    }

    private void flattenModel(List<Property> attributes, ModelNode model) {
        for (Property attr : attributes) {
            if (attr.getName().contains(".")) {
                String parentName = attr.getName().substring(0, attr.getName().indexOf('.'));
                if (model.hasDefined(parentName)) {
                    ModelNode value = model.remove(parentName);
                    for (Property nested : value.asPropertyList()) {
                        model.get(parentName + "." + nested.getName()).set(nested.getValue());
                    }
                }
            }
        }
    }
}
