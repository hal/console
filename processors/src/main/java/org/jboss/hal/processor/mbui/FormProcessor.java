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

import com.google.common.base.Strings;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.meta.AddressTemplate;
import org.jdom2.Element;
import org.jdom2.xpath.XPathFactory;

/**
 * @author Harald Pehl
 */
@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
class FormProcessor extends AbstractMbuiElementProcessor implements MbuiElementProcessor {

    private static final String ON_SAVE_SIGNATURE = "(form, changedValues)";

    FormProcessor(final MbuiViewProcessor processor, final Types typeUtils, final XPathFactory xPathFactory) {
        super(processor, typeUtils, xPathFactory);
    }

    @Override
    public void process(final VariableElement field, final Element element, final String selector,
            final MbuiViewContext context) {
        String title = element.getAttributeValue("title");
        boolean autoSave = Boolean.parseBoolean(element.getAttributeValue("auto-save"));
        String onSave = element.getAttributeValue("on-save");
        String nameResolver = element.getAttributeValue("name-resolver");
        MetadataInfo metadata = findMetadata(field, element, context);
        AddressTemplate template = AddressTemplate.of(metadata.getTemplate());

        if (autoSave && !Strings.isNullOrEmpty(onSave)) {
            processor.error(field, "Please choose either auto-save or on-save not both.");

        } else if (autoSave) {
            if (title == null) {
                title = new LabelBuilder().label(template.lastKey());
            }
            if (nameResolver != null && !Handlebars.isExpression(nameResolver)) {
                processor.error(field, "Name resolver in form#%s has to be an expression.", selector);
            }
            if ("*".equals(template.lastValue()) && nameResolver == null) {
                processor.error(field, "Auto save is enabled for form#%s and related metadata address ends in \"*\", " +
                        "but no name resolver is is provided.", selector);
            }

        } else if (!Strings.isNullOrEmpty(onSave)) {
            if (!Handlebars.isExpression(onSave)) {
                processor.error(field, "on-save handler in form#%s has to be an expression.", selector);
            }
            // TODO Should we accept other signatures as well?
            if (!onSave.contains(ON_SAVE_SIGNATURE)) {
                processor.error(field,
                        "Invalid signature for on-save handler in form#%s. Signature has to follow \"%s\".",
                        selector, ON_SAVE_SIGNATURE);
            }
        }

        FormInfo formInfo = new FormInfo(field.getSimpleName().toString(), selector, getTypeParameter(field), metadata,
                title, autoSave, onSave, nameResolver);
        context.addFormInfo(formInfo);

        org.jdom2.Element attributesContainer = element.getChild("attributes");
        if (attributesContainer != null) {
            processAttributes(field, attributesContainer).forEach(formInfo::addAttribute);
        }
    }
}
