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
import elemental.dom.Element;
import elemental.html.PreElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.resources.HalConstants;

public class BootstrapFailed implements IsElement {

    private static final HalConstants CONSTANTS = GWT.create(HalConstants.class);

    private final Element element;

    public BootstrapFailed(String message, String details, boolean asCode) {
        // @formatter:off
        Elements.Builder builder =  new Elements.Builder()
            .div().css("container-fluid")
                .div().css("row")
                    .div().css("col-md-offset-2 col-md-8 bootstrap-error")
                        .h(1).innerText(CONSTANTS.bootstrap_failed()).end()
                        .div().css("panel panel-danger")
                            .div().css("panel-heading")
                                .h(3).css("panel-title").innerText(message).end()
                            .end()
                            .div().css("panel-body")
                                .p().rememberAs("body").end()
                                .start("pre").rememberAs("code").end()
                            .end()
                        .end()
                    .end()
                .end()
            .end();
        // @formatter:on

        element = builder.build();
        Element body = builder.referenceFor("body");
        PreElement code = builder.referenceFor("code");
        if (asCode) {
            code.setInnerText(details);
        } else {
            body.setInnerText(details);
        }
    }

    @Override
    public Element asElement() {
        return element;
    }
}
