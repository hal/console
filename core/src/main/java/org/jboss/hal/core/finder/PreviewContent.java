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
package org.jboss.hal.core.finder;

import com.google.gwt.safehtml.shared.SafeHtml;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextAware;
import org.jboss.hal.resources.CSS;

/**
 * @author Harald Pehl
 */
public class PreviewContent implements SecurityContextAware {

    private Elements.Builder builder;

    public PreviewContent(final String header, final SafeHtml content) {
        this(header, null, content);
    }

    public PreviewContent(final String header, final String lead, final SafeHtml content) {
        builder = new Elements.Builder().header().h(1).innerText(header).end();
        if (lead != null) {
            builder.p().css(CSS.lead).innerText(lead).end();
        }
        builder.end(); // </header>
        builder.section().innerHtml(content).end();
    }

    public PreviewContent(final String header, final Element content) {
        this(header, null, content);
    }

    public PreviewContent(final String header, final String lead, final Element content) {
        builder = new Elements.Builder().header().h(1).innerText(header).end();
        if (lead != null) {
            builder.p().css(CSS.lead).innerText(lead).end();
        }
        builder.end(); // </header>
        builder.section().add(content).end();
    }

    public Iterable<Element> elements() {
        return builder.elements();
    }

    @Override
    public void onSecurityContextChange(final SecurityContext securityContext) {

    }
}
