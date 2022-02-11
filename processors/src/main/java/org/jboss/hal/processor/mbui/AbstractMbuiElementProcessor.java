/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.processor.mbui;

import java.util.List;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import com.google.auto.common.MoreTypes;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.processor.mbui.XmlHelper.xmlAsString;

abstract class AbstractMbuiElementProcessor implements MbuiElementProcessor {

    final MbuiViewProcessor processor;
    private final Elements elementUtils;
    final XPathFactory xPathFactory;

    AbstractMbuiElementProcessor(MbuiViewProcessor processor, Elements elementUtils, XPathFactory xPathFactory) {
        this.processor = processor;
        this.elementUtils = elementUtils;
        this.xPathFactory = xPathFactory;
    }

    MetadataInfo findMetadata(VariableElement field, org.jdom2.Element element,
            MbuiViewContext context) {
        MetadataInfo metadataInfo = null;
        // noinspection HardCodedStringLiteral
        XPathExpression<Element> expression = xPathFactory.compile("ancestor::" + XmlTags.METADATA, Filters.element());
        org.jdom2.Element metadataElement = expression.evaluateFirst(element);
        if (metadataElement == null) {
            processor.error(field,
                    "Missing metadata ancestor for %s#%s. Please make sure the there's a <%s/> ancestor element.",
                    element.getName(), element.getAttributeValue(XmlTags.ID), XmlTags.METADATA);
        } else {
            metadataInfo = context.getMetadataInfo(metadataElement.getAttributeValue(XmlTags.ADDRESS));
            if (metadataInfo == null) {
                processor
                        .error(field, "No metadata found for %s#%s. Please make sure there's a <%s/> ancestor element.",
                                element.getName(), element.getAttributeValue("id"), XmlTags.METADATA);
            }
        }
        return metadataInfo;
    }

    TypeParameter getTypeParameter(VariableElement field) {
        TypeMirror type = elementUtils.getTypeElement("org.jboss.hal.dmr.ModelNode").asType();
        DeclaredType declaredType = MoreTypes.asDeclared(field.asType());
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        if (!typeArguments.isEmpty()) {
            type = typeArguments.get(0);
        }
        return new TypeParameter(MoreTypes.asTypeElement(type).getQualifiedName().toString());
    }

    List<Attribute> processAttributes(VariableElement field, org.jdom2.Element attributesContainer) {
        return attributesContainer.getChildren(XmlTags.ATTRIBUTE).stream()
                .map(element -> processAttribute(field, element))
                .collect(toList());
    }

    Attribute processAttribute(VariableElement field, org.jdom2.Element attributeElement) {
        String name = attributeElement.getAttributeValue(XmlTags.NAME);
        String provider = attributeElement.getAttributeValue(XmlTags.PROVIDER);
        String formItem = attributeElement.getAttributeValue(XmlTags.FORM_ITEM);
        String validationHandler = attributeElement.getAttributeValue(XmlTags.VALIDATION_HANDLER);
        String suggestHandlerAttribute = attributeElement.getAttributeValue(XmlTags.SUGGEST_HANDLER);
        org.jdom2.Element suggestHandlerElement = attributeElement.getChild(XmlTags.SUGGEST_HANDLER);

        if (name == null && formItem == null) {
            processor.error(field, "Invalid attribute \"%s\": name is mandatory.", xmlAsString(attributeElement));
        }
        if (provider != null && !ExpressionParser.isExpression(provider)) {
            processor.error(field, "Provider for attribute \"%s\" has to be an expression.",
                    xmlAsString(attributeElement));
        }
        if (formItem != null && !ExpressionParser.isExpression(formItem)) {
            processor.error(field, "FormItem for attribute \"%s\" has to be an expression.",
                    xmlAsString(attributeElement));
        }
        if (validationHandler != null && !ExpressionParser.isExpression(validationHandler)) {
            processor.error(field, "Validation handler for attribute \"%s\" has to be an expression.",
                    xmlAsString(attributeElement));
        }
        if (suggestHandlerAttribute != null && !ExpressionParser.isExpression(suggestHandlerAttribute)) {
            processor.error(field, "Suggestion handler for attribute \"%s\" has to be an expression.",
                    xmlAsString(attributeElement));
        }
        if (suggestHandlerAttribute != null && suggestHandlerElement != null) {
            processor.error(field, "Invalid suggest handler for attribute \"%s\": " +
                    "Please specify suggest handler as attribute or child element, not both",
                    xmlAsString(attributeElement));
        }

        Attribute attribute = new Attribute(name, provider, formItem, validationHandler, suggestHandlerAttribute);
        if (suggestHandlerElement != null) {
            org.jdom2.Element templatesContainer = suggestHandlerElement.getChild(XmlTags.TEMPLATES);
            if (templatesContainer != null) {
                for (org.jdom2.Element templateElement : templatesContainer.getChildren(XmlTags.TEMPLATE)) {
                    String address = templateElement.getAttributeValue(XmlTags.ADDRESS);
                    attribute.addSuggestHandlerTemplate(address);
                }
            }
        }
        return attribute;
    }
}
