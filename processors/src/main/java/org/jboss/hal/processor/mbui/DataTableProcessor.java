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
package org.jboss.hal.processor.mbui;

import java.util.stream.Stream;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Types;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.processor.mbui.DataTableInfo.HandlerRef;
import org.jdom2.Element;
import org.jdom2.xpath.XPathFactory;

import static java.util.stream.Collectors.joining;
import static org.jboss.hal.processor.mbui.XmlHelper.xmlAsString;

/**
 * @author Harald Pehl
 */
@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
class DataTableProcessor extends AbstractMbuiElementProcessor implements MbuiElementProcessor {

    DataTableProcessor(final MbuiViewProcessor processor, final Types typeUtils, final XPathFactory xPathFactory) {
        super(processor, typeUtils, xPathFactory);
    }

    @Override
    public void process(final VariableElement field, final Element element, final String selector,
            final MbuiViewContext context) {
        MetadataInfo metadata = findMetadata(field, element, context);
        AddressTemplate template = AddressTemplate.of(metadata.getTemplate());
        String title = element.getAttributeValue("title");
        if (title == null) {
            title = new LabelBuilder().label(template.lastKey());
        }
        DataTableInfo tableInfo = new DataTableInfo(field.getSimpleName().toString(), selector, getTypeParameter(field),
                metadata, title);
        context.addDataTableInfo(tableInfo);

        // actions
        org.jdom2.Element actionsContainer = element.getChild("actions");
        if (actionsContainer != null) {
            for (org.jdom2.Element actionElement : actionsContainer.getChildren("action")) {
                String handler = actionElement.getAttributeValue("handler");
                String handlerRef = actionElement.getAttributeValue("handler-ref");
                String actionTitle = actionElement.getAttributeValue("title");
                String scope = actionElement.getAttributeValue("scope");
                String nameResolver = actionElement.getAttributeValue("name-resolver");
                Element attributesContainer = actionElement.getChild("attributes");

                if (handler != null && handlerRef != null) {
                    processor.error(field,
                            "Multiple handlers specified for table#%s. Please specify only one of \"handler\" or \"handler-ref\".",
                            selector);
                }
                if (handler != null) {
                    if (!Handlebars.isExpression(handler)) {
                        processor.error(field,
                                "Invalid handler \"%s\" in data-table#%s: handler has to be an expression.", handler,
                                selector);
                    }
                    if (actionTitle == null) {
                        processor.error(field, "Invalid handler \"%s\" in data-table#%s: Title is mandatory.",
                                handler, selector);
                    }
                }
                if (handlerRef != null && HandlerRef.referenceFor(handlerRef) == null) {
                    String knownHandlerRefs = Stream.of(HandlerRef.values())
                            .map(HandlerRef::getRef)
                            .collect(joining(", "));
                    processor.error(field,
                            "Unknown handler-ref \"%s\" in data-table#%s: Please choose one of %s.",
                            handlerRef, selector, knownHandlerRefs);
                }
                if (!HandlerRef.ADD_RESOURCE.getRef().equals(handlerRef) && attributesContainer != null) {
                    processor.warning(field, "Attributes specified for handler-ref \"%s\" in data-table#%s: " +
                                    "Attributes are only processed for \"%s\".",
                            handlerRef, selector, HandlerRef.ADD_RESOURCE.name());
                }
                if (nameResolver != null && !Handlebars.isExpression(nameResolver)) {
                    processor.error(field, "Name resolver in data-table#%s has to be an expression.", selector);
                }
                if (HandlerRef.REMOVE_RESOURCE.getRef().equals(handlerRef) &&
                        "*".equals(template.lastValue()) && nameResolver == null) {
                    processor.error(field,
                            "\"%s\" handler-ref specified for data-table#%s and related metadata address ends in \"*\", " +
                                    "but no name resolver is is provided.",
                            HandlerRef.REMOVE_RESOURCE.getRef(), selector);
                }
                if (scope != null && !"selected".equals(scope)) {
                    processor.error(field,
                            "Unknown scope \"%s\" in handler-ref \"%s\" in data-table#%s: Only \"selected\" is supported.",
                            scope, handlerRef, selector);
                }

                DataTableInfo.Action action = new DataTableInfo.Action(handlerRef != null ? handlerRef : handler,
                        actionTitle, scope, nameResolver);
                tableInfo.addAction(action);

                if (attributesContainer != null) {
                    processAttributes(field, attributesContainer).forEach(action::addAttribute);
                }
            }
        }

        // columns
        org.jdom2.Element columnsContainer = element.getChild("columns");
        if (columnsContainer != null) {
            for (org.jdom2.Element columnElement : columnsContainer.getChildren("column")) {
                String name = columnElement.getAttributeValue("name");
                String columnTitle = columnElement.getAttributeValue("title");
                String value = columnElement.getAttributeValue("value");

                if (name == null) {
                    processor.error(field, "Invalid column \"%s\" in data-table#%s: name is mandatory.",
                            xmlAsString(columnElement), selector);
                }
                if (value != null) {
                    if (!Handlebars.isExpression(value)) {
                        processor.error(field,
                                "Invalid column \"%s\" in data-table#%s: value has to be an expression.",
                                xmlAsString(columnElement), selector);
                    }
                    if (columnTitle == null) {
                        processor.error(field,
                                "Invalid column \"%s\" in data-table#%s: if value is given, title is mandatory.",
                                xmlAsString(columnElement), selector);
                    }
                }
                DataTableInfo.Column column = new DataTableInfo.Column(name, columnTitle, value);
                tableInfo.addColumn(column);
            }
        }
    }
}
