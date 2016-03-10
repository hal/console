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
package org.jboss.hal.client.configuration.subsystem.datasource;

import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.core.finder.StaticItemColumn;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;

import javax.inject.Inject;

import static java.util.Arrays.asList;

/**
 * @author Harald Pehl
 */
@AsyncColumn(Ids.DATA_SOURCE_TYPE_COLUMN)
public class DataSourceTypeColumn extends StaticItemColumn {

    static final String NON_XA = "non-xa";
    static final String XA = "xa";

    @Inject
    public DataSourceTypeColumn(final Finder finder,
            final Resources resources) {
        super(finder, Ids.DATA_SOURCE_TYPE_COLUMN, resources.constants().type(), asList(

                new StaticItem.Builder("Non-XA") //NON-NLS
                        .id(NON_XA)
                        .nextColumn(ModelDescriptionConstants.DATA_SOURCE)
                        .onPreview(new PreviewContent("Non-XA Datasource", resources.previews().nonXa())) //NON-NLS
                        .build(),

                new StaticItem.Builder("XA") //NON-NLS
                        .id(XA)
                        .nextColumn(ModelDescriptionConstants.DATA_SOURCE) // re-use the same column
                        .onPreview(new PreviewContent("XA Datasource", resources.previews().xa())) //NON-NLS
                        .build()));
    }
}
