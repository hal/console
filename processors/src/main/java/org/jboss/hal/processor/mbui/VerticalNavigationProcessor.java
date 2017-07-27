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
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import static org.jboss.hal.processor.mbui.XmlHelper.xmlAsString;

class VerticalNavigationProcessor extends AbstractMbuiElementProcessor implements MbuiElementProcessor {

    VerticalNavigationProcessor(final MbuiViewProcessor processor, final Types typeUtils,
            final Elements elementUtils, final XPathFactory xPathFactory) {
        super(processor, typeUtils, elementUtils, xPathFactory);
    }

    @Override
    public void process(final VariableElement field, final Element element, final String selector,
            final MbuiViewContext context) {
        VerticalNavigationInfo navigationInfo = new VerticalNavigationInfo(field.getSimpleName().toString(), selector);
        context.setVerticalNavigation(navigationInfo);

        XPathExpression<Element> expression = xPathFactory.compile(XmlTags.ITEM, Filters.element());
        expression.evaluate(element)
                .forEach(itemElement -> navigationInfo.addItem(createItem(field, itemElement, context, 0)));
    }

    private VerticalNavigationInfo.Item createItem(final VariableElement field, org.jdom2.Element element,
            final MbuiViewContext context, int level) {
        String id = element.getAttributeValue(XmlTags.ID);
        String title = element.getAttributeValue(XmlTags.TITLE);
        String icon = element.getAttributeValue(XmlTags.ICON);

        if (id == null) {
            processor.error(field, "Invalid item \"%s\" in vertical-navigation: id is mandatory.",
                    xmlAsString(element));
        }
        if (title == null) {
            processor.error(field, "Invalid item \"%s\" in vertical-navigation: title is mandatory.",
                    xmlAsString(element));
        }
        VerticalNavigationInfo.Item item = new VerticalNavigationInfo.Item(id, title, icon);

        // nested sub-items or metadata?
        List<Element> subItems = element.getChildren(XmlTags.SUB_ITEM);
        if (!subItems.isEmpty()) {
            if (level > 0) {
                processor.error(field,
                        "Invalid nesting in vertical-navigation: sub items cannot have nested sub items.");
            }
            subItems.forEach(subItemElement -> item.addSubItem(createItem(field, subItemElement, context, level + 1)));

        } else {
            Content.parse(element, context).forEach(item::addContent);
        }
        return item;
    }
}
