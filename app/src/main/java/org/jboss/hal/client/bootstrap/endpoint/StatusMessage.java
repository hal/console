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
package org.jboss.hal.client.bootstrap.endpoint;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;

/**
 * @author Harald Pehl
 */
public final class StatusMessage {

    private static final Templates TEMPLATES = GWT.create(Templates.class);

    public static SafeHtml success(String message) {
        return TEMPLATES.success(message);
    }

    public static SafeHtml info(String message) {
        return TEMPLATES.info(message);
    }

    public static SafeHtml warning(String message) {
        return TEMPLATES.warning(message);
    }

    public static SafeHtml error(String message) {
        return TEMPLATES.error(message);
    }

    interface Templates extends SafeHtmlTemplates {
        @Template("<div class=\"hal-statusMessage hal-statusMessage-success\"><i class=\"icon-ok-circle\"></i> {0}</div>")
        SafeHtml success(String message);

        @Template("<div class=\"hal-statusMessage hal-statusMessage-info\"><i class=\"icon-info-sign\"></i> {0}</div>")
        SafeHtml info(String message);

        @Template("<div class=\"hal-statusMessage hal-statusMessage-warning\"><i class=\"icon-warning-sign\"></i> {0}</div>")
        SafeHtml warning(String message);

        @Template("<div class=\"hal-statusMessage hal-statusMessage-error\"><i class=\"icon-exclamation-sign\"></i> {0}</div>")
        SafeHtml error(String message);

        @Template("<div class=\"hal-statusMessage hal-statusMessage-undefined\"><i class=\"icon-question\"></i> {0}</div>")
        SafeHtml undefined(String message);
    }
}
