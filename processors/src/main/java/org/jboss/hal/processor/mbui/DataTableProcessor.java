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
class DataTableProcessor extends AbstractMbuiElementProcessor implements MbuiElementProcessor {

    DataTableProcessor(final MbuiViewProcessor processor, final Types typeUtils, final XPathFactory xPathFactory) {
        super(processor, typeUtils, xPathFactory);
    }

    @Override
    public void process(final VariableElement field, final Element element, final String selector,
            final MbuiViewContext context) {
        MetadataInfo metadata = findMetadata(field, element, context);
        DataTableInfo tableInfo = new DataTableInfo(field.getSimpleName().toString(), selector, getTypeParameter(field),
                metadata);
        context.addDataTableInfo(tableInfo);

        org.jdom2.Element columnsContainer = element.getChild("columns");
        if (columnsContainer != null) {
            for (org.jdom2.Element columnElement : columnsContainer.getChildren("column")) {
                String name = columnElement.getAttributeValue("name");
                String title = columnElement.getAttributeValue("title");
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
                    if (title == null) {
                        processor.error(field,
                                "Invalid column \"%s\" in data-table#%s: if value is given, title is mandatory.",
                                xmlAsString(columnElement), selector);
                    }
                }
                DataTableInfo.Column column = new DataTableInfo.Column(name, title, value);
                tableInfo.addColumn(column);
            }
        }
    }
}
