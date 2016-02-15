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
package org.jboss.hal.client.runtime;

import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.core.finder.StaticItemColumn;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;

import javax.inject.Inject;
import java.util.Arrays;

/**
 * @author Harald Pehl
 */
@Column(Ids.DOMAIN_BROWSE_BY)
public class BrowseByColumn extends StaticItemColumn {

    @Inject
    public BrowseByColumn(final Finder finder,
            final Resources resources) {
        super(finder, Ids.DOMAIN_BROWSE_BY, resources.constants().browseBy(),
                Arrays.asList(
                        new StaticItem.Builder(Names.HOSTS).build(),
                        new StaticItem.Builder(Names.SERVER_GROUPS).build()
                ));
    }
}
