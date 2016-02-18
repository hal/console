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
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.hal.ballroom.IdBuilder;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.ballroom.tab.Tabs;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.resources.CSS.lead;

/**
 * @author Harald Pehl
 */
class ResourceView implements HasElements {

    private static final String DESCRIPTION_ELEMENT = "descriptionElement";
    private static final Element PLACE_HOLDER = Browser.getDocument().createDivElement();

    private final Dispatcher dispatcher;
    private final Elements.Builder builder;
    private final Tabs tabs;
    private final String dataId;
    private final String descriptionId;
    private final Element description;

    ResourceView(final Dispatcher dispatcher, final Resources resources) {
        this.dispatcher = dispatcher;

        tabs = new Tabs();
        dataId = IdBuilder.build(Ids.MODEL_BROWSER, "data");
        tabs.add(dataId, resources.constants().data(), PLACE_HOLDER);
        descriptionId = IdBuilder.build(Ids.MODEL_BROWSER, "description");
        tabs.add(descriptionId, resources.constants().description(), PLACE_HOLDER);

        // @formatter:off
        builder = new Elements.Builder()
            .p().css(lead).rememberAs(DESCRIPTION_ELEMENT).end()
            .add(tabs.asElement());
        // @formatter:on

        description = builder.referenceFor(DESCRIPTION_ELEMENT);
    }

    @Override
    public Iterable<Element> asElements() {
        return builder.elements();
    }

    void update(ResourceAddress address, ResourceDescription description) {
        SafeHtml safeHtml = SafeHtmlUtils.fromSafeConstant(description.getDescription());
        this.description.setInnerHTML(safeHtml.asString());

        tabs.setContent(dataId, PLACE_HOLDER);
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
                tabs.setContent(dataId, form.asElement());
                PatternFly.initComponents();
                form.attach();
                form.view(result);
            });
        }
    }
}
