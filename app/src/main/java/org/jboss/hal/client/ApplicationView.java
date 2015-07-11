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
import org.jboss.hal.ballroom.Elements;

/**
 * @author Harald Pehl
 */
public class ApplicationView extends ViewImpl implements ApplicationPresenter.MyView {

    private final DivElement rootContainer;
    private final Element body;

    public ApplicationView() {
        body = Browser.getDocument().getBody();
        rootContainer = new Elements.Builder().div().css("container-fluid root-container").end().build();
        body.appendChild(rootContainer);
        initWidget(Elements.asWidget(rootContainer));
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        Element contentElement = Elements.asElement(content);

        if (slot == ApplicationPresenter.SLOT_MAIN_CONTENT) {
            Elements.removeChildrenFrom(rootContainer);
            rootContainer.appendChild(contentElement);

        } else if (slot == ApplicationPresenter.SLOT_HEADER_CONTENT) {
            body.insertBefore(contentElement, rootContainer);

        } else if (slot == ApplicationPresenter.SLOT_FOOTER_CONTENT) {
            body.insertBefore(contentElement, rootContainer.getNextElementSibling());
        }
    }
}