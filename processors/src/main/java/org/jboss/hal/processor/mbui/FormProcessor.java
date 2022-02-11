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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.meta.AddressTemplate;
import org.jdom2.Element;
import org.jdom2.xpath.XPathFactory;

import com.google.common.base.Strings;

class FormProcessor extends AbstractMbuiElementProcessor implements MbuiElementProcessor {

    FormProcessor(MbuiViewProcessor processor, Elements elementUtils, XPathFactory xPathFactory) {
        super(processor, elementUtils, xPathFactory);
    }

    @Override
    public void process(VariableElement field, Element element, String selector, MbuiViewContext context) {
        String title = element.getAttributeValue(XmlTags.TITLE);
        boolean autoSave = Boolean.parseBoolean(element.getAttributeValue(XmlTags.AUTO_SAVE));
        String addHandler = element.getAttributeValue(XmlTags.ADD_HANDLER);
        boolean reset = Boolean.parseBoolean(element.getAttributeValue(XmlTags.RESET));
        boolean includeRuntime = Boolean.parseBoolean(element.getAttributeValue(XmlTags.INCLUDE_RUNTIME));
        boolean singleton = XmlTags.SINGLETON_FORM.equalsIgnoreCase(element.getName());
        String onSave = element.getAttributeValue(XmlTags.ON_SAVE);
        String prepareReset = element.getAttributeValue(XmlTags.PREPARE_RESET);
        String nameResolver = element.getAttributeValue(XmlTags.NAME_RESOLVER);
        MetadataInfo metadata = findMetadata(field, element, context);
        AddressTemplate template = AddressTemplate.of(metadata.getTemplate());

        if (autoSave && !Strings.isNullOrEmpty(onSave)) {
            processor.error(field, "Please choose either auto-save or on-save not both.");

        } else if (autoSave) {
            if (title == null) {
                title = new LabelBuilder().label(template.lastName());
            }
            if (nameResolver != null && !ExpressionParser.isExpression(nameResolver)) {
                processor.error(field, "Name resolver in form#%s has to be an expression.", selector);
            }
            if ("*".equals(template.lastValue()) && nameResolver == null) {
                processor.error(field, "Auto save is enabled for form#%s and related metadata address ends in \"*\", " +
                        "but no name resolver is is provided.", selector);
            }

        } else if (!Strings.isNullOrEmpty(onSave)) {
            if (!ExpressionParser.isExpression(onSave)) {
                processor.error(field, "on-save handler in form#%s has to be an expression.", selector);
            }
        }

        FormInfo formInfo = new FormInfo(field.getSimpleName().toString(), selector, getTypeParameter(field),
                metadata, title, addHandler, autoSave, onSave, reset, prepareReset, nameResolver, includeRuntime, singleton);
        context.addFormInfo(formInfo);

        org.jdom2.Element attributesContainer = element.getChild(XmlTags.ATTRIBUTES);
        if (attributesContainer != null) {
            List<Element> groupElements = attributesContainer.getChildren(XmlTags.GROUP);
            if (groupElements.isEmpty()) {
                processAttributes(field, attributesContainer).forEach(formInfo::addAttribute);

            } else {
                Map<String, Element> excludes = new HashMap<>();
                Map<String, FormInfo.Group> groupsById = new HashMap<>();

                // round one: process groups
                for (Element groupElement : groupElements) {
                    String id = groupElement.getAttributeValue(XmlTags.ID);
                    String name = groupElement.getAttributeValue(XmlTags.NAME);
                    String groupTitle = groupElement.getAttributeValue(XmlTags.TITLE);
                    FormInfo.Group group = new FormInfo.Group(id, name, groupTitle);
                    groupElement.getChildren(XmlTags.ATTRIBUTE)
                            .forEach(attributeElement -> group.addAttribute(processAttribute(field, attributeElement)));
                    formInfo.addGroup(group);
                    if (id != null) {
                        groupsById.put(id, group);
                    }
                    Element excludeElement = groupElement.getChild(XmlTags.EXCLUDE);
                    if (id != null && excludeElement != null) {
                        excludes.put(id, excludeElement);
                    }
                }

                // round two: process group excludes (which might reference other groups)
                excludes.forEach((id, excludeElement) -> {
                    FormInfo.Group group = groupsById.get(id);
                    if (group != null) {
                        excludeElement.getChildren(XmlTags.ATTRIBUTE).forEach(attributeElement -> {
                            String name = attributeElement.getAttributeValue(XmlTags.NAME);
                            if (name != null) {
                                group.exclude(name);
                            }
                        });
                        excludeElement.getChildren(XmlTags.GROUP).forEach(groupElement -> {
                            String excludedGroupId = groupElement.getAttributeValue(XmlTags.ID);
                            if (excludedGroupId != null && groupsById.get(excludedGroupId) != null) {
                                FormInfo.Group excludedGroup = groupsById.get(excludedGroupId);
                                group.exclude(excludedGroup);
                            }
                        });
                    }
                });
            }
        }
    }
}
