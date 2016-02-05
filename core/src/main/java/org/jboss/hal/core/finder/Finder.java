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

import com.google.web.bindery.event.shared.EventBus;
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.IdBuilder;
import org.jboss.hal.core.Breadcrumb;
import org.jboss.hal.core.BreadcrumbEvent;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextAware;
import org.jboss.hal.resources.CSS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static elemental.css.CSSStyleDeclaration.Unit.PX;
import static java.lang.Math.min;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public class Finder implements IsElement, SecurityContextAware, Attachable {

    /**
     * The maximum number of visible columns. If there are more columns given the first column is hidden when column
     * {@code MAX_VISIBLE_COLUMNS + 1} is shown.
     */
    public static final int MAX_VISIBLE_COLUMNS = 4;
    static final String BREADCRUMB_KEY = "breadcrumbKey";
    static final String BREADCRUMB_VALUE = "breadcrumbValue";

    private static final int MAX_COLUMNS = 12;
    private static final String PREVIEW_COLUMN = "previewColumn";

    private final String id;
    private final EventBus eventBus;
    private final FinderColumn initialColumn;
    private final PreviewContent initialPreview;
    private final Map<String, FinderColumn> columns;
    private final Element root;
    private final Element previewColumn;

    public Finder(String id, final EventBus eventBus,
            final FinderColumn initialColumn, final PreviewContent initialPreview) {
        this.id = id;
        this.eventBus = eventBus;
        this.initialColumn = initialColumn;
        this.initialPreview = initialPreview;
        this.columns = new HashMap<>();

        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .div().id(this.id).css(row, finder)
                .div()
                    .id(IdBuilder.build(id, "preview"))
                    .css(finderPreview, column(12))
                    .rememberAs(PREVIEW_COLUMN)
                .end()
            .end();
        // @formatter:off

        root = builder.build();
        previewColumn = builder.referenceFor(PREVIEW_COLUMN);
        Browser.getWindow().setOnresize(event -> resize());
    }

    @Override
    public Element asElement() {
        return root;
    }

    @Override
    public void attach() {
        appendColumn(initialColumn);
        preview(initialPreview);
        resize();
    }

    private void resize() {
        int window = Browser.getWindow().getInnerHeight();
        int navigation = 0, footer = 0;
        Element element = Browser.getDocument().querySelector("nav." + navbar); //NON-NLS
        if (element != null) {
            navigation = element.getOffsetHeight();
        }
        element = Browser.getDocument().querySelector("footer > nav." + navbar); //NON-NLS
        if (element != null) {
            footer = element.getOffsetHeight();
        }
        if (navigation > 0 && footer > 0) {
            int finder = window - navigation - footer;
            root.getStyle().setHeight(finder, PX);
        }
    }

    void preview(PreviewContent preview) {
        Elements.removeChildrenFrom(previewColumn);
        for (Element element : preview.elements()) {
            previewColumn.appendChild(element);
        }
    }

    void reduceTo(FinderColumn column) {
        List<Element> reverseColumns = new ArrayList<>();
        for (Element element : Elements.children(root)) {
            if (element == previewColumn) {
                continue;
            }
            reverseColumns.add(element);
        }
        Collections.reverse(reverseColumns);
        for (Element element : reverseColumns) {
            if (element == column.asElement()) {
                break;
            }
            columns.remove(element.getId());
            root.removeChild(element);
        }
    }

    void updateBreadcrumb() {
        Breadcrumb breadcrumb = Breadcrumb.empty();
        for (Element column : Elements.children(root)) {
            if (column == previewColumn) {
                break;
            }
            String key = String.valueOf(column.getDataset().at(BREADCRUMB_KEY));
            Element activeItem = column.querySelector("li." + CSS.active); //NON-NLS
            if (activeItem != null) {
                String value = String.valueOf(activeItem.getDataset().at(BREADCRUMB_VALUE));
                if (key != null && value != null) {
                    breadcrumb.append(key, value);
                }
            }
        }
        eventBus.fireEvent(new BreadcrumbEvent(breadcrumb));
    }

    public void appendColumn(FinderColumn column) {
        // always make sure the finder instance is there
        column.setFinder(this);
        columns.put(column.getId(), column);
        root.insertBefore(column.asElement(), previewColumn);

        int columns = root.getChildren().length() - 1;
        int previewSize = MAX_COLUMNS - 2 * min(columns, MAX_VISIBLE_COLUMNS);
        previewColumn.setClassName(finderPreview + " " + column(previewSize));
    }

    @Override
    public void onSecurityContextChange(final SecurityContext securityContext) {
        for (FinderColumn column : columns.values()) {
            column.onSecurityContextChange(securityContext);
        }
    }

    public String getId() {
        return id;
    }
}
