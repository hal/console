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

import java.util.List;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Types;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import static org.jboss.hal.processor.mbui.XmlHelper.xmlAsString;

/**
 * @author Harald Pehl
 */
@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
class VerticalNavigationProcessor extends AbstractMbuiElementProcessor implements MbuiElementProcessor {

    private static final Escaper JAVA_STRING_ESCAPER = Escapers.builder()
            .addEscape('"', "\\\"")
            .addEscape('\n', "")
            .addEscape('\r', "")
            .build();

    VerticalNavigationProcessor(final MbuiViewProcessor processor, final Types typeUtils,
            final XPathFactory xPathFactory) {
        super(processor, typeUtils, xPathFactory);
    }

    @Override
    public void process(final VariableElement field, final Element element, final String selector,
            final MbuiViewContext context) {
        VerticalNavigationInfo navigationInfo = new VerticalNavigationInfo(field.getSimpleName().toString(), selector);
        context.setVerticalNavigation(navigationInfo);

        XPathExpression<Element> expression = xPathFactory.compile("item", Filters.element());
        expression.evaluate(element)
                .forEach(itemElement -> navigationInfo.addItem(createItem(field, itemElement, context, 0)));
    }

    private VerticalNavigationInfo.Item createItem(final VariableElement field, org.jdom2.Element element,
            final MbuiViewContext context, int level) {
        String id = element.getAttributeValue("id");
        String title = element.getAttributeValue("title");
        String icon = element.getAttributeValue("icon");

        if (id == null) {
            processor.error(field, "Invalid item \"%s\" in vertical-navigation: id is mandatory.",
                    xmlAsString(element));
        }
        if (title == null) {
            processor.error(field, "Invalid item \"%s\" in vertical-navigation: title is mandatory.",
                    xmlAsString(element));
        }
        VerticalNavigationInfo.Item item = new VerticalNavigationInfo.Item(id, title, icon);

        List<Element> subItems = element.getChildren("sub-item");
        if (!subItems.isEmpty()) {
            if (level > 0) {
                processor.error(field, "Invalid nesting in vertical-navigation: sub items cannot have sub items.");
            }
            subItems.forEach(subItemElement -> item.addSubItem(createItem(field, subItemElement, context, level + 1)));

        } else {
            MetadataInfo metadataInfo = null;
            org.jdom2.Element contentElement = element;
            if (element.getChild(XmlTags.METADATA) != null) {
                contentElement = element.getChild(XmlTags.METADATA);
                metadataInfo = context.getMetadataInfo(contentElement.getAttributeValue("address"));
            }
            StringBuilder htmlBuilder = new StringBuilder();
            for (org.jdom2.Element childElement : contentElement.getChildren()) {
                if (XmlTags.TABLE.equals(childElement.getName()) || XmlTags.FORM.equals(childElement.getName())) {
                    if (htmlBuilder.length() != 0) {
                        String html = htmlBuilder.toString();
                        htmlBuilder.setLength(0);
                        if (metadataInfo != null) {
                            html = html.replace("metadata", metadataInfo.getName());
                        }
                        item.addContent(VerticalNavigationInfo.Content.html(html));
                    }
                    item.addContent(VerticalNavigationInfo.Content.reference(childElement.getAttributeValue("id")));

                } else {
                    // do not directly add the html, but collect it until a table or form is about to be processed
                    htmlBuilder.append(JAVA_STRING_ESCAPER.escape(xmlAsString(childElement)));
                }
            }
        }
        return item;
    }
}
