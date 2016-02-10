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
package org.jboss.hal.meta.subsystem;

/**
 * @author Harald Pehl
 */
public class SubsystemMetadata {

    private final String name;
    private final String title;
    private final String subtitle;
    private final String token;
    private final String nextColumn;
    private final boolean builtIn;

    public SubsystemMetadata(final String name, final String title, final String subtitle, final String token,
            final String nextColumn, final boolean builtIn) {
        this.name = name;
        this.title = title;
        this.subtitle = subtitle;
        this.token = token;
        this.nextColumn = nextColumn;
        this.builtIn = builtIn;
    }

    @Override
    public String toString() {
        return "Subsystem(" + name + ")";
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getToken() {
        return token;
    }

    public String getNextColumn() {
        return nextColumn;
    }

    public boolean isBuiltIn() {
        return builtIn;
    }
}
