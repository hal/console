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

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.HTMLCollection;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.IdBuilder;
import org.jboss.hal.core.finder.ColumnRegistry.LookupCallback;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextAware;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.spi.Footer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static elemental.css.CSSStyleDeclaration.Unit.PX;
import static java.lang.Math.min;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.Ids.FINDER;

/**
 * The one and only finder which is shared across all different top level categories in HAL. The same finder instance
 * gets injected into the different top level presenters. Only the columns will change when navigating between the
 * different places
 *
 * @author Harald Pehl
 */
public class Finder implements IsElement, SecurityContextAware, Attachable {

    /**
     * Function used in {@link #select(String, FinderPath, ScheduledCommand)} to select one segment in a finder path.
     */
    private class SelectFunction implements Function<FunctionContext> {

        private final FinderPath.Segment segment;
        private final Element columnElement;

        private SelectFunction(final FinderPath.Segment segment, final Element columnElement) {
            this.segment = segment;
            this.columnElement = columnElement;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            if (columnElement != null &&
                    columns.containsKey(columnElement.getId()) &&
                    segment.getKey().equals(columnElement.getId())) {
                // column is already in place just select the item
                FinderColumn finderColumn = columns.get(columnElement.getId());
                selectItem(finderColumn, control);

            } else {
                // append the column
                appendColumn(segment.getKey(), new AsyncCallback<FinderColumn>() {
                    @Override
                    public void onFailure(final Throwable throwable) {
                        control.abort();
                    }

                    @Override
                    public void onSuccess(final FinderColumn finderColumn) {
                        selectItem(finderColumn, control);
                    }
                });
            }
        }

        private void selectItem(FinderColumn finderColumn, Control<FunctionContext> control) {
            if (finderColumn.containsItem(segment.getValue())) {
                finderColumn.selectItem(segment.getValue());
                updateContext();
                control.getContext().push(finderColumn);
                control.proceed();
            } else {
                logger.error("Unable to select item '{}'", segment.getValue()); //NON-NLS
                control.abort();
            }
        }
    }


    /**
     * The maximum number of visible columns. If there are more columns given the first column is hidden when column
     * {@code MAX_VISIBLE_COLUMNS + 1} is shown.
     */
    public static final int MAX_VISIBLE_COLUMNS = 4;
    static final String DATA_BREADCRUMB = "breadcrumb";

    private static final int MAX_COLUMNS = 12;
    private static final String PREVIEW_COLUMN = "previewColumn";
    private static final Logger logger = LoggerFactory.getLogger(Finder.class);

    private final PlaceManager placeManager;
    private final EventBus eventBus;
    private final ColumnRegistry columnRegistry;
    private final Provider<Progress> progress;
    private final String id;
    private final FinderContext context;
    private final Map<String, FinderColumn> columns;
    private final Element root;
    private final Element previewColumn;
    private String initialColumn;


    // ------------------------------------------------------ ui

    @Inject
    public Finder(final PlaceManager placeManager,
            final EventBus eventBus,
            final ColumnRegistry columnRegistry,
            @Footer final Provider<Progress> progress) {
        this.placeManager = placeManager;
        this.eventBus = eventBus;
        this.columnRegistry = columnRegistry;
        this.progress = progress;

        this.id = FINDER;
        this.context = new FinderContext();
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
        // @formatter:on

        root = builder.build();
        previewColumn = builder.referenceFor(PREVIEW_COLUMN);
        Browser.getWindow().setOnresize(event -> adjustHeight());
    }

    @Override
    public Element asElement() {
        return root;
    }

    @Override
    public void attach() {
        adjustHeight();
    }

    private void adjustHeight() {
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

    private FinderColumn firstColumn() {
        return columns.get(initialColumn);
    }


    // ------------------------------------------------------ internal API

    void appendColumn(String columnId, AsyncCallback<FinderColumn> callback) {
        columnRegistry.lookup(columnId, new LookupCallback() {
            @Override
            public void found(final FinderColumn column) {
                appendColumn(column, callback);
            }

            @Override
            public void error(final String failure) {
                logger.error(failure);
                if (callback != null) {
                    callback.onFailure(new RuntimeException(failure));
                }
            }
        });
    }

    private void appendColumn(FinderColumn column, AsyncCallback<FinderColumn> callback) {
        columns.put(column.getId(), column);
        root.insertBefore(column.asElement(), previewColumn);

        int columns = root.getChildren().length() - 1;
        int previewSize = MAX_COLUMNS - 2 * min(columns, MAX_VISIBLE_COLUMNS);
        previewColumn.setClassName(finderPreview + " " + column(previewSize));

        column.setItems(callback);
    }

    void reduceTo(FinderColumn column) {
        boolean removeFromHere = false;
        for (Iterator<Element> iterator = Elements.children(root).iterator(); iterator.hasNext(); ) {
            Element element = iterator.next();
            if (element == column.asElement()) {
                removeFromHere = true;
                continue;
            }
            if (element == previewColumn) {
                break;
            }
            if (removeFromHere) {
                columns.remove(element.getId());
                iterator.remove();
            }
        }
    }

    void updateContext() {
        context.getPath().clear();
        context.getBreadcrumb().clear();

        for (Element column : Elements.children(root)) {
            if (column == previewColumn) {
                break;
            }
            String key = column.getId();
            String breadcrumbKey = String.valueOf(column.getDataset().at(DATA_BREADCRUMB));
            Element activeItem = column.querySelector("li." + CSS.active); //NON-NLS
            if (activeItem != null) {
                String value = activeItem.getId();
                String breadcrumbValue = String.valueOf(activeItem.getDataset().at(DATA_BREADCRUMB));
                if (key != null && value != null) {
                    context.getPath().append(key, value);
                }
                if (breadcrumbKey != null && breadcrumbValue != null) {
                    context.getBreadcrumb().append(breadcrumbKey, breadcrumbValue);
                }
            }
        }
    }

    void publishContext() {
        PlaceRequest current = placeManager.getCurrentPlaceRequest();
        PlaceRequest.Builder builder = new PlaceRequest.Builder().nameToken(current.getNameToken());
        if (!context.getPath().isEmpty()) {
            builder.with("path", context.getPath().toString());
        }
        PlaceRequest update = builder.build();
        if (!current.equals(update)) {
            placeManager.updateHistory(update, true);
        }
        eventBus.fireEvent(new FinderContextEvent(context));
    }

    void selectColumn(final String column) {
        FinderColumn finderColumn = columns.get(column);
        finderColumn.asElement().focus();
    }

    void preview(PreviewContent preview) {
        Elements.removeChildrenFrom(previewColumn);
        for (Element element : preview.elements()) {
            previewColumn.appendChild(element);
        }
    }


    // ------------------------------------------------------ public interface

    public void reset(final String token, final String initialColumn, final PreviewContent initialPreview) {
        this.initialColumn = initialColumn;

        while (root.getFirstChild() != previewColumn) {
            root.removeChild(root.getFirstChild());
        }
        context.reset(token);
        appendColumn(initialColumn, null);
        selectColumn(initialColumn);
        preview(initialPreview);
        publishContext();
    }

    public void select(final String token, final FinderPath path, final ScheduledCommand fallback) {
        if (path.isEmpty()) {
            fallback.execute();

        } else {
            context.setToken(token);

            // Find the last common column between the new and the current path
            String match = null;
            FinderPath newPath = path.reversed();
            FinderPath currentPath = context.getPath().reversed();
            for (FinderPath.Segment newSegment : newPath) {
                for (FinderPath.Segment currentSegment : currentPath) {
                    if (newSegment.getKey().equals(currentSegment.getKey())) {
                        match = newSegment.getKey();
                        break;
                    }
                }
                if (match != null) {
                    break;
                }
            }
            FinderColumn lastCommonColumn = match != null ? columns.get(match) : firstColumn();
            if (lastCommonColumn != null) {
                reduceTo(lastCommonColumn);
            }

            int index = 0;
            Function[] functions = new Function[path.size()];
            HTMLCollection columns = root.getChildren();
            for (FinderPath.Segment segment : path) {
                Element column = index < columns.getLength() ? (Element) columns.item(index) : null;
                functions[index] = new SelectFunction(segment, column);
                index++;
            }
            new Async<FunctionContext>(progress.get()).waterfall(new FunctionContext(), new Outcome<FunctionContext>() {
                @Override
                public void onFailure(final FunctionContext context) {
                    if (Finder.this.context.getPath().isEmpty()) {
                        fallback.execute();

                    } else if (!context.emptyStack()) {
                        FinderColumn column = context.pop();
                        processLastColumnSelection(column);
                        publishContext();
                    }
                }

                @Override
                public void onSuccess(final FunctionContext context) {
                    FinderColumn column = context.pop();
                    processLastColumnSelection(column);
                    publishContext();
                }

                private void processLastColumnSelection(FinderColumn column) {
                    selectColumn(column.getId());
                    FinderRow row = column.getSelectedRow();
                    if (row != null) {
                        row.appendNextColumn();
                        row.preview();
                    }
                }
            }, functions);
        }
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

    public FinderContext getContext() {
        return context;
    }
}
