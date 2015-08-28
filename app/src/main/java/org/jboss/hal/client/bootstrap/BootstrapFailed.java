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
import org.jboss.gwt.elemento.core.DataElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.gwt.elemento.core.Templated;
import org.jboss.hal.resources.HalConstants;

import javax.annotation.PostConstruct;

@Templated
public abstract class BootstrapFailed implements IsElement {

    static final HalConstants CONSTANTS = GWT.create(HalConstants.class);

    // @formatter:off
    public static BootstrapFailed create(String error, String details, boolean asCode) {
        return new Templated_BootstrapFailed(error, details, asCode);
    }

    public abstract String error();
    public abstract String details();
    public abstract boolean asCode();
    // @formatter:on

    @DataElement Element body;
    @DataElement Element code;

    @PostConstruct
    void init() {
        Elements.setVisible(code, asCode());
        Elements.setVisible(body, !asCode());
        if (asCode()) {
            code.setInnerText(details());
        } else {
            body.setInnerText(details());
        }
    }
}
