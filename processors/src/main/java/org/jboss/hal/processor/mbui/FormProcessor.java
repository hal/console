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

import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Types;

import org.jdom2.Element;
import org.jdom2.xpath.XPathFactory;

import static org.jboss.hal.processor.mbui.XmlHelper.xmlAsString;

/**
 * @author Harald Pehl
 */
@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
class FormProcessor extends AbstractMbuiElementProcessor implements MbuiElementProcessor {

    FormProcessor(final MbuiViewProcessor processor, final Types typeUtils, final XPathFactory xPathFactory) {
        super(processor, typeUtils, xPathFactory);
    }

    @Override
    public void process(final VariableElement field, final Element element, final String selector,
            final MbuiViewContext context) {
        MetadataInfo metadata = findMetadata(field, element, context);
        FormInfo formInfo = new FormInfo(field.getSimpleName().toString(), selector, getTypeParameter(field), metadata);
        context.addFormInfo(formInfo);

        org.jdom2.Element attributesContainer = element.getChild("attributes");
        if (attributesContainer != null) {
            for (org.jdom2.Element attributeElement : attributesContainer.getChildren("attribute")) {
                String name = attributeElement.getAttributeValue("name");
                if (name == null) {
                    processor.error(field, "Invalid attribute \"%s\" in form#%s: name is mandatory.",
                            xmlAsString(attributeElement), selector);
                }

                FormInfo.Attribute attribute = new FormInfo.Attribute(name);
                org.jdom2.Element suggestHandler = attributeElement.getChild("suggest-handler");
                if (suggestHandler != null) {
                    org.jdom2.Element templatesContainer = suggestHandler.getChild("templates");
                    if (templatesContainer != null) {
                        for (org.jdom2.Element templateElement : templatesContainer.getChildren("template")) {
                            String address = templateElement.getAttributeValue("address");
                            attribute.addSuggestHandlerTemplate(address);
                        }
                    }
                }
                formInfo.addAttribute(attribute);
            }
        }
    }
}
