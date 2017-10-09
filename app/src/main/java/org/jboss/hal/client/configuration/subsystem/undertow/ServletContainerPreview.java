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
package org.jboss.hal.client.configuration.subsystem.undertow;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttribute;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.resources.Names;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MIME_MAPPING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WELCOME_FILE;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

class ServletContainerPreview extends PreviewContent<NamedNode> {

    @SuppressWarnings("HardCodedStringLiteral")
    ServletContainerPreview(NamedNode servletContainer) {
        super(servletContainer.getName());

        LabelBuilder labelBuilder = new LabelBuilder();
        PreviewAttributes<NamedNode> attributes = new PreviewAttributes<>(servletContainer,
                asList("default-encoding", "default-session-timeout", "directory-listing", "max-sessions"));

        attributes.append(model -> {
            List<Property> properties = failSafePropertyList(model, MIME_MAPPING);
            if (!properties.isEmpty()) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                for (Iterator<Property> iterator = properties.iterator(); iterator.hasNext(); ) {
                    Property property = iterator.next();
                    builder.appendEscaped(property.getName())
                            .appendEscaped(" ")
                            .appendHtmlConstant("&rArr;")
                            .appendEscaped(" ")
                            .appendEscaped(property.getValue().get(VALUE).asString());
                    if (iterator.hasNext()) {
                        builder.appendEscaped(", ");
                    }
                }
                return new PreviewAttribute(labelBuilder.label(MIME_MAPPING), builder.toSafeHtml());
            }
            return new PreviewAttribute(labelBuilder.label(MIME_MAPPING), Names.NOT_AVAILABLE);
        });

        attributes.append(model -> {
            List<Property> files = failSafePropertyList(model, WELCOME_FILE);
            if (!files.isEmpty()) {
                String csv = files.stream().map(Property::getName).collect(joining(", "));
                return new PreviewAttribute(labelBuilder.label(WELCOME_FILE), csv);
            }
            return new PreviewAttribute(labelBuilder.label(WELCOME_FILE), Names.NOT_AVAILABLE);
        });

        previewBuilder().addAll(attributes);
    }
}
