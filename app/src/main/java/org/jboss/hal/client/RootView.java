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
package org.jboss.hal.client;

import com.google.gwt.user.client.ui.IsWidget;
import com.gwtplatform.mvp.client.ViewImpl;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.DivElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.core.Slots;
import org.jboss.hal.resources.HalIds;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static org.jboss.hal.client.RootPresenter.SLOT_FOOTER_CONTENT;
import static org.jboss.hal.client.RootPresenter.SLOT_HEADER_CONTENT;

/**
 * @author Harald Pehl
 */
public class RootView extends ViewImpl implements RootPresenter.MyView {

    private final Map<Object, Element> slots;
    private final DivElement rootContainer;
    private boolean initialized;

    @Inject
    public RootView(HalIds ids) {
        slots = new HashMap<>();
        rootContainer = new Elements.Builder().div().id(ids.root_container()).css("container-fluid").end().build();
        initWidget(Elements.asWidget(rootContainer));
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        Element element = Elements.asElement(content);

        if (slot == SLOT_HEADER_CONTENT || slot == SLOT_FOOTER_CONTENT) {
            slots.put(slot, element);
        }

        else if (slot == Slots.MAIN) {
            Elements.removeChildrenFrom(rootContainer);
            rootContainer.appendChild(element);
        }

        else if (slot == Slots.APPLICATION) {
            // TODO Hide top level tabs
        }

        else if (slot == Slots.PREVIEW) {
            // TODO Implement preview
        }

        if (!initialized && slots.containsKey(SLOT_HEADER_CONTENT) && slots.containsKey(SLOT_FOOTER_CONTENT)) {
            // append all three building blocks to the document body
            Element body = Browser.getDocument().getBody();
            body.appendChild(slots.get(SLOT_HEADER_CONTENT));
            body.appendChild(rootContainer);
            body.appendChild(slots.get(SLOT_FOOTER_CONTENT));
            initialized = true;
        }
    }
}