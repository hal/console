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
package org.jboss.hal.client.configuration;

import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.client.NameTokens;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.jboss.hal.resources.Names.*;

/**
 * @author Harald Pehl
 */
@SuppressWarnings("HardCodedStringLiteral")
public class StandaloneConfigurationView extends PatternFlyViewImpl implements StandaloneConfigurationPresenter.MyView {

    @Inject
    public StandaloneConfigurationView(final Resources resources) {
        List<StaticItem> items = new ArrayList<>();
        items.add(new StaticItem.Builder("subsystems", SUBSYSTEMS, true)
                .preview(item -> new PreviewContent(SUBSYSTEMS, resources.messages().subsystemsPreview()))
                .build());
        items.add(new StaticItem.Builder("interfaces", INTERFACES, true)
                .preview(item -> new PreviewContent(INTERFACES, resources.messages().interfacesPreview()))
                .build());
        items.add(new StaticItem.Builder("socket-bindings", SOCKET_BINDINGS, true)
                .preview(item -> new PreviewContent(SOCKET_BINDINGS, resources.messages().socketBindingsPreview()))
                .build());
        items.add(new StaticItem.Builder("paths", PATHS, true)
                .preview(item -> new PreviewContent(PATHS, resources.messages().pathsPreview()))
                .build());
        items.add(new StaticItem.Builder("system-properties", Names.SYSTEM_PROPERTIES, true)
                .preview(
                        item -> new PreviewContent(Names.SYSTEM_PROPERTIES, resources.messages().systemPropertiesPreview()))
                .build());

        FinderColumn<StaticItem> column = new FinderColumn.Builder<StaticItem>("configuration-column", CONFIGURATION)
                .initialItems(items)
                .build();

        PreviewContent initialPreview = new PreviewContent(CONFIGURATION,
                resources.constants().homepageConfigurationStandaloneSubHeader(),
                resources.messages().configurationStandalonePreview(NameTokens.RUNTIME));
        Finder finder = new Finder("configuration-finder", initialPreview, singletonList(column));

        initWidget(Elements.asWidget(finder.asElement()));
    }
}
