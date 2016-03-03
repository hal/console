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
package org.jboss.hal.client.bootstrap;

import com.google.gwt.core.client.GWT;
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.resources.Constants;

import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public class LoadingPanel implements IsElement {

    private final static Constants CONSTANTS = GWT.create(Constants.class);

    static public LoadingPanel get() {
        if (instance == null) {
            instance = new LoadingPanel();
            instance.off();
            Browser.getDocument().getBody().appendChild(instance.asElement());
        }
        return instance;
    }

    private static LoadingPanel instance;

    private final Element root;

    private LoadingPanel() {
        // @formatter:off
        root = new Elements.Builder()
            .div().css(loadingContainer)
                .div().css(loading)
                    .h(3).textContent(CONSTANTS.loading()).end()
                    .div().css(spinner).end()
                .end()
            .end()
        .build();
        // @formatter:on
    }

    @Override
    public Element asElement() {
        return root;
    }

    public void on() {
        Elements.setVisible(root, true);
    }

    public void off() {
        Elements.setVisible(root, false);
    }
}
