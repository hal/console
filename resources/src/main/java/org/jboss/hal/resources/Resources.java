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
package org.jboss.hal.resources;

import com.google.gwt.resources.client.ExternalTextResource;

import javax.inject.Inject;

/**
 * @author Harald Pehl
 */
public class Resources implements Ids, Names, UIConstants, CSS {

    private final Constants constants;
    private final Messages messages;
    private final Previews previews;
    private final Images images;

    @Inject
    public Resources(final Constants constants, final Messages messages, final Previews previews, final Images images) {
        this.constants = constants;
        this.messages = messages;
        this.previews = previews;
        this.images = images;
    }

    public Constants constants() {
        return constants;
    }

    public Messages messages() {
        return messages;
    }

    public Previews previews() {
        return previews;
    }

    public ExternalTextResource preview(String name) {
        return (ExternalTextResource) previews.getResource(name);
    }

    public Images images() {
        return images;
    }
}
