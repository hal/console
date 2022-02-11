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
package org.jboss.hal.client.management;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttribute;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Names;

import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.joining;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.Icons.flag;

class ManagementInterfacePreview extends PreviewContent<StaticItem> {

    private static final AddressTemplate TEMPLATE = AddressTemplate.of(
            "{domain.controller}/core-service=management/management-interface=http-interface");

    private final CrudOperations crud;
    private final StatementContext statementContext;
    private final PreviewAttributes<ModelNode> attributes;

    ManagementInterfacePreview(final CrudOperations crud, final StatementContext statementContext,
            Environment environment) {
        super(Names.MANAGEMENT_INTERFACE);
        this.crud = crud;
        this.statementContext = statementContext;

        LabelBuilder labelBuilder = new LabelBuilder();
        this.attributes = new PreviewAttributes<>(new ModelNode(), Names.HTTP)
                .append(model -> {
                    String allowedOrigins = ModelNodeHelper.failSafeList(model, ALLOWED_ORIGINS)
                            .stream()
                            .map(ModelNode::asString)
                            .collect(joining(", "));
                    return new PreviewAttribute(labelBuilder.label(ALLOWED_ORIGINS), allowedOrigins);
                })
                .append(model -> {
                    String label = labelBuilder.label(HTTP_UPGRADE);
                    boolean httpUpgrade = ModelNodeHelper.failSafeBoolean(model, HTTP_UPGRADE + "/" + ENABLED);
                    HTMLElement element = span().css(flag(httpUpgrade)).element();
                    return new PreviewAttribute(label, element);
                });
        if (environment.isStandalone()) {
            attributes.append(SECURE_SOCKET_BINDING)
                    .append(SOCKET_BINDING);
        } else {
            attributes.append(INTERFACE)
                    .append(PORT)
                    .append(SECURE_INTERFACE)
                    .append(SECURE_PORT);

        }
        attributes.append(SASL_PROTOCOL)
                .append(SECURITY_REALM)
                .append(SSL_CONTEXT);

        previewBuilder().addAll(attributes);
    }

    @Override
    public void update(final StaticItem item) {
        crud.read(TEMPLATE.resolve(statementContext), attributes::refresh);
    }
}
