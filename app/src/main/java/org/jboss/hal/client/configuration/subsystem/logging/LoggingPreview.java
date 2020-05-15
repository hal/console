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
package org.jboss.hal.client.configuration.subsystem.logging;

import java.util.function.Supplier;

import com.google.gwt.resources.client.ExternalTextResource;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttribute;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.stream.Collectors.joining;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HANDLERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LEVEL;
import static org.jboss.hal.resources.Names.ROOT_LOGGER;

/** Used for both logging configuration and logging profiles. */
class LoggingPreview<T> extends PreviewContent<T> {

    private final Dispatcher dispatcher;
    private final Supplier<Operation> operation;
    private final PreviewAttributes<ModelNode> attributes;
    private final HTMLElement undefined;

    LoggingPreview(Dispatcher dispatcher, Resources resources,
            String header, ExternalTextResource description, Supplier<Operation> operation) {
        super(header, description);
        this.dispatcher = dispatcher;
        this.operation = operation;

        LabelBuilder labelBuilder = new LabelBuilder();
        attributes = new PreviewAttributes<>(new ModelNode(), Names.ROOT_LOGGER)
                .append(LEVEL)
                .append(model -> {
                    String handlers = "";
                    if (model.hasDefined(HANDLERS)) {
                        handlers = model.get(HANDLERS).asList().stream()
                                .map(ModelNode::asString)
                                .collect(joining(", "));
                    }
                    return new PreviewAttribute(labelBuilder.label(HANDLERS), handlers);
                });
        previewBuilder().addAll(attributes);

        previewBuilder()
                .add(undefined = div()
                        .add(h(2).textContent(ROOT_LOGGER))
                        .add(p().textContent(resources.constants().noRootLoggerDescription())).element());
        Elements.setVisible(undefined, false);
    }

    @Override
    public void update(T whatever) {
        dispatcher.execute(operation.get(),
                (model) -> {
                    for (HTMLElement element : attributes) {
                        Elements.setVisible(element, true);
                    }
                    Elements.setVisible(undefined, false);
                    attributes.refresh(model);
                },
                (operation1, failure) -> {
                    for (HTMLElement element : attributes) {
                        Elements.setVisible(element, false);
                    }
                    Elements.setVisible(undefined, true);
                });
    }
}
