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

import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.IdBuilder;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextAware;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.lang.Math.min;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public class Finder implements IsElement, SecurityContextAware {

    /**
     * The maximum number of visible columns. If there are more columns given the first column is hidden when column
     * {@code MAX_VISIBLE_COLUMNS + 1} is shown.
     */
    public static final int MAX_VISIBLE_COLUMNS = 4;
    private static final int MAX_COLUMNS = 12;
    private static final String PREVIEW_COLUMN = "previewColumn";

    private final String id;
    private final Set<FinderColumn<?>> columns;
    private final Element root;
    private final Element previewColumn;

    public Finder(final String id, final PreviewContent initialPreview, final List<FinderColumn<?>> columns) {
        this.id = id;
        this.columns = new LinkedHashSet<>();
        this.columns.addAll(columns);

        Element firstColumn = null;
        Elements.Builder builder = new Elements.Builder().div()
                .id(this.id)
                .css(row, finder)
                .style("height: 555px");
        for (FinderColumn column : columns) {
            Element columnElement = column.asElement();
            if (firstColumn == null) {
                firstColumn = columnElement;
            }
            Elements.setVisible(columnElement, false);
            builder.add(columnElement);
        }
        builder.div()
                .id(IdBuilder.build(id, "preview"))
                .css(finderPreview, column(10)) // initial class
                .rememberAs(PREVIEW_COLUMN)
                .end();

        builder.end(); // </div>
        root = builder.build();
        previewColumn = builder.referenceFor(PREVIEW_COLUMN);

        Elements.setVisible(firstColumn, true);
        Elements.removeChildrenFrom(previewColumn);
        for (Element element : initialPreview.elements()) {
            previewColumn.appendChild(element);
        }
    }

    @Override
    public Element asElement() {
        return root;
    }

    private void resize(int visibleColumns) {
        int previewSize = MAX_COLUMNS - min(visibleColumns, MAX_VISIBLE_COLUMNS);
        previewColumn.setClassName(finderPreview + " " + column(previewSize));
    }

    static void preview(PreviewContent preview) {
        Element previewColumn = Browser.getDocument().querySelector("." + finderPreview);
        if (previewColumn != null) {
            Elements.removeChildrenFrom(previewColumn);
            for (Element element : preview.elements()) {
                previewColumn.appendChild(element);
            }
        }
    }

    @Override
    public void onSecurityContextChange(final SecurityContext securityContext) {
        for (FinderColumn column : columns) {
            column.onSecurityContextChange(securityContext);
        }
    }

    public String getId() {
        return id;
    }
}
