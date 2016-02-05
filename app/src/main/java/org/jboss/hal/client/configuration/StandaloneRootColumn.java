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

import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.resources.Resources;

import java.util.ArrayList;
import java.util.List;

import static com.google.gwt.safehtml.shared.SafeHtmlUtils.fromSafeConstant;
import static org.jboss.hal.resources.Ids.CONFIGURATION_COLUMN;
import static org.jboss.hal.resources.Names.*;

/**
 * @author Harald Pehl
 */
public class StandaloneRootColumn extends FinderColumn<StaticItem> {

    public StandaloneRootColumn(final StandaloneConfigurationPresenter presenter, final Resources resources) {
        super(new Builder<StaticItem>(CONFIGURATION_COLUMN, CONFIGURATION,
                item -> new ItemDisplay() {
                    @Override
                    public String getText() {
                        return item.getTitle();
                    }

                    @Override
                    public boolean isFolder() {
                        return item.isFolder();
                    }
                })
                .onPreview(StaticItem::getPreviewContent)
                .onSelect((finder, item) -> {
                    if (item.getSelectCallback() != null) {
                        item.getSelectCallback().onSelect(finder, item);
                    }
                }));

        List<StaticItem> items = new ArrayList<>();
        items.add(new StaticItem(SUBSYSTEMS, true, (finder, item) -> presenter.loadSubsystems(),
                new PreviewContent(SUBSYSTEMS, fromSafeConstant(resources.previews().subsystems().getText()))));
        items.add(new StaticItem(INTERFACES, true, (finder, item) -> presenter.loadInterfaces(),
                new PreviewContent(INTERFACES, fromSafeConstant(resources.previews().interfaces().getText()))));
        items.add(new StaticItem(SOCKET_BINDINGS, true, (finder, item) -> presenter.loadSocketBindings(),
                new PreviewContent(SOCKET_BINDINGS,
                        fromSafeConstant(resources.previews().socketBindings().getText()))));
        items.add(new StaticItem(PATHS, false, null,
                new PreviewContent(PATHS, fromSafeConstant(resources.previews().paths().getText()))));
        items.add(new StaticItem(SYSTEM_PROPERTIES, false, null,
                new PreviewContent(SYSTEM_PROPERTIES,
                        fromSafeConstant(resources.previews().systemProperties().getText()))));
        setItems(items);
    }
}
