/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.core.modelbrowser;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.IdBuilder;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.resources.Ids;

import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.resources.CSS.lead;

/**
 * @author Harald Pehl
 */
class ResourceView implements IsElement {

    private static final String DESCRIPTION_ELEMENT = "descriptionElement";
    private static final String FORM_ELEMENT = "formElement";

    private final Dispatcher dispatcher;
    private final Element description;
    private final Element formContainer;
    private final Element root;

    ResourceView(final Dispatcher dispatcher) {
        this.dispatcher = dispatcher;

        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .div()
                .p().css(lead).rememberAs(DESCRIPTION_ELEMENT).end()
                .div().rememberAs(FORM_ELEMENT).end()
            .end();
        // @formatter:on

        description = builder.referenceFor(DESCRIPTION_ELEMENT);
        formContainer = builder.referenceFor(FORM_ELEMENT);
        root = builder.build();
    }

    @Override
    public Element asElement() {
        return root;
    }

    void update(ResourceAddress address, ResourceDescription description) {
        SafeHtml safeHtml = SafeHtmlUtils.fromSafeConstant(description.getDescription());
        this.description.setInnerHTML(safeHtml.asString());

        Elements.removeChildrenFrom(formContainer);
        if (description.hasAttributes()) {
            Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION, address)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            dispatcher.execute(operation, result -> {
                ModelNodeForm<ModelNode> form = new ModelNodeForm.Builder<>(
                        IdBuilder.build(Ids.MODEL_BROWSER, address.lastValue()),
                        SecurityContext.RWX, description)
                        .includeRuntime()
                        .build();
                formContainer.appendChild(form.asElement());
                PatternFly.initComponents();
                form.attach();
                form.view(result);
            });
        }
    }
}
