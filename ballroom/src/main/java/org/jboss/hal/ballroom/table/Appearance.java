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
package org.jboss.hal.ballroom.table;

import elemental.dom.Element;
import elemental.html.InputElement;
import org.jboss.gwt.elemento.core.DataElement;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.gwt.elemento.core.Templated;
import org.jboss.hal.resources.I18n;

/**
 * @author Harald Pehl
 */
@Templated("Appearance.html#data-table")
abstract class Appearance implements IsElement {

    // @formatter:off
    static Appearance create(final String id, final I18n i18n) {
        return new Templated_Appearance(id, i18n);
    }

    abstract String id();
    abstract I18n i18n();
    // @formatter:on


    @DataElement Element buttonToolbar;
    @DataElement Element navInfo;
    @DataElement Element cellTableHolder;
    @DataElement Element navFirst;
    @DataElement Element navPrev;
    @DataElement InputElement navCurrentPage;
    @DataElement Element navPages;
    @DataElement Element navNext;
    @DataElement Element navLast;
}
