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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import org.jdom2.Element;

import static org.jboss.hal.processor.mbui.XmlHelper.xmlAsString;

/**
 * An element which contains some 'content' such as tables, (fail-safe-)forms or tabs. Most often this is a
 * {@code <metadata/>} element.
 *
 * @author Harald Pehl
 */
public class Content {

    private static final Escaper JAVA_STRING_ESCAPER = Escapers.builder()
            .addEscape('"', "\\\"")
            .addEscape('\n', "")
            .addEscape('\r', "")
            .build();

    static int counter = 0;

    @SuppressWarnings("HardCodedStringLiteral")
    static List<Content> parse(Element element, MbuiViewContext context) {
        List<Content> contents = new ArrayList<>();

        MetadataInfo metadataInfo = null;
        Element contentElement = element;
        if (element.getChild(XmlTags.METADATA) != null) {
            contentElement = element.getChild(XmlTags.METADATA);
            metadataInfo = context.getMetadataInfo(contentElement.getAttributeValue("address"));
        }
        StringBuilder htmlBuilder = new StringBuilder();
        for (org.jdom2.Element childElement : contentElement.getChildren()) {
            if (XmlTags.TABLE.equals(childElement.getName()) || XmlTags.FORM.equals(childElement.getName())
                    || XmlTags.TAB.equals(childElement.getName())) {
                if (htmlBuilder.length() != 0) {
                    String html = htmlBuilder.toString();
                    htmlBuilder.setLength(0);
                    contents.add(htmlContent(html, metadataInfo));
                }
                Content content = new Content(childElement.getAttributeValue("id"), null);
                contents.add(content);
/*
                if (XmlTags.TAB.equals(childElement.getName())) {
                    content.setTab(true);
                    // from the template perspective, registering only one tab is ok, 
                    // as the tab id will be used to lookup the tab object name created at runtime
                    // in MbuiViewProcessor.processTabs (TabsInfo class)
                    break;
                }
*/
            } else {
                // do not directly add the html, but collect it until a table or form is about to be processed
                htmlBuilder.append(JAVA_STRING_ESCAPER.escape(xmlAsString(childElement)));
            }
        }

        // is there any html content left?
        if (htmlBuilder.length() != 0) {
            String html = htmlBuilder.toString();
            htmlBuilder.setLength(0);
            contents.add(htmlContent(html, metadataInfo));
        }

        return contents;
    }

    private static Content htmlContent(String html, MetadataInfo metadataInfo) {
        if (metadataInfo != null) {
            html = html.replace("metadata", metadataInfo.getName());
        }
        return new Content(null, html);
    }


    private String reference;
    private final String name;
    private final String html;
    private final Map<String, String> handlebars;

    private Content(final String reference, final String html) {
        this.reference = reference;
        this.name = "html" + counter; //NON-NLS
        this.html = html;
        this.handlebars = Handlebars.parse(html);
        counter++;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(final String reference) {
        this.reference = reference;
    }

    public String getName() {
        return name;
    }

    public String getHtml() {
        return html;
    }

    public Map<String, String> getHandlebars() {
        return handlebars;
    }

    @Override
    public String toString() {
        return "Content{" +
                "reference=" + reference + 
                ", name=" + name + 
                ", html=" + html +
                '}';
    }
}
