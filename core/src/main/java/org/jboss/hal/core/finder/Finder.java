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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import elemental.client.Browser;
import elemental.dom.Element;
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
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextAware;
import org.jboss.hal.resources.CSS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static elemental.css.CSSStyleDeclaration.Unit.PX;
import static java.lang.Math.min;
import static org.jboss.hal.core.finder.ColumnRegistry.LookupResult.ASYNC;
import static org.jboss.hal.core.finder.ColumnRegistry.LookupResult.READY;
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

    private class SelectFunction implements Function<FunctionContext> {

        final FinderPath.Segment segment;

        private SelectFunction(final FinderPath.Segment segment) {this.segment = segment;}

        @Override
        public void execute(final Control<FunctionContext> control) {
            appendColumn(segment.getKey());
            FinderColumn column = columnRegistry.getColumn(segment.getKey());
            if (column != null) {
                appendColumn(column);
                AsyncProvider<List> itemProvider = columnRegistry.getItemProvider(segment.getKey());
                if (itemProvider != null) {
                    itemProvider.get(new AsyncCallback<List>() {
                        @Override
                        public void onFailure(final Throwable throwable) {
                            control.abort();
                        }

                        @Override
                        public void onSuccess(final List list) {
                            //noinspection unchecked
                            column.setItems(list);
                            column.markSelected(segment.getValue());
                            control.proceed();
                        }
                    });
                } else {
                    control.proceed();
                }

            } else {
                control.abort();
            }
        }
    }


    /**
     * The maximum number of visible columns. If there are more columns given the first column is hidden when column
     * {@code MAX_VISIBLE_COLUMNS + 1} is shown.
     */
    public static final int MAX_VISIBLE_COLUMNS = 4;
    static final String BREADCRUMB_KEY = "breadcrumbKey";
    static final String BREADCRUMB_VALUE = "breadcrumbValue";

    private static final int MAX_COLUMNS = 12;
    private static final String PREVIEW_COLUMN = "previewColumn";

    private static final Logger logger = LoggerFactory.getLogger(Finder.class);

    private final PlaceManager placeManager;
    private final EventBus eventBus;
    private final ColumnRegistry columnRegistry;
    private final String id;
    private final FinderContext context;
    private final Map<String, FinderColumn> columns;
    private final Element root;
    private final Element previewColumn;
    private String finderToken; // the token of the presenter containing the finder


    // ------------------------------------------------------ ui setup

    @Inject
    public Finder(final PlaceManager placeManager,
            final EventBus eventBus,
            final ColumnRegistry columnRegistry) {
        this.placeManager = placeManager;
        this.eventBus = eventBus;
        this.columnRegistry = columnRegistry;

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
        // @formatter:off

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


    // ------------------------------------------------------ internal API

    void <T> appendColumn(String columnId, AsyncCallback<FinderColumn<T>> callback) {
        ColumnRegistry.LookupResult lookupResult = columnRegistry.lookup(columnId);
        if (lookupResult == READY) {
            FinderColumn column = columnRegistry.getColumn(columnId);
            appendColumn(column);
            if (callback != null) {
                callback.onSuccess(null);
            }

        } else if (lookupResult == ASYNC) {
            columnRegistry.loadColumn(columnId, (column) -> appendColumn(column, callback));

        } else {
            //noinspection HardCodedStringLiteral
            logger.error("Unknown column '{}'. Please make sure to register all columns in the column registry, before appending them.");
        }
    }

    private <T> void appendColumn(FinderColumn<T> column, AsyncCallback<FinderColumn<T>> callback) {
        columns.put(column.getId(), column);
        root.insertBefore(column.asElement(), previewColumn);

        int columns = root.getChildren().length() - 1;
        int previewSize = MAX_COLUMNS - 2 * min(columns, MAX_VISIBLE_COLUMNS);
        previewColumn.setClassName(finderPreview + " " + column(previewSize));

        if (!column.getInitialItems().isEmpty()) {
            column.setItems(column.getInitialItems());

        } else if (column.getItemsProvider() != null) {
            column.getItemsProvider().get(context, new AsyncCallback<List<T>>() {
                @Override
                public void onFailure(final Throwable throwable) {
                    //noinspection HardCodedStringLiteral
                    logger.error("Unable to provide items for column '{}': {}", column.getId(), throwable.getMessage());
                }

                @Override
                public void onSuccess(final List<T> items) {
                    column.setItems(items);
                }
            });
        }
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
        FinderPath path = FinderPath.empty();
        Breadcrumb breadcrumb = Breadcrumb.empty();
        for (Element column : Elements.children(root)) {
            if (column == previewColumn) {
                break;
            }
            String key = column.getId();
            String breadcrumbKey = String.valueOf(column.getDataset().at(BREADCRUMB_KEY));
            Element activeItem = column.querySelector("li." + CSS.active); //NON-NLS
            if (activeItem != null) {
                String value = activeItem.getId();
                String breadcrumbValue = String.valueOf(activeItem.getDataset().at(BREADCRUMB_VALUE));
                if (key != null && value != null) {
                    path.append(key, value);
                }
                if (breadcrumbKey != null && breadcrumbValue != null) {
                    breadcrumb.append(breadcrumbKey, breadcrumbValue);
                }
            }
        }
//        if (finderToken != null) {
//            PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken(finderToken).with("path", path.toString())
//                    .build();
//            placeManager.revealPlace(placeRequest);
//        }
//        eventBus.fireEvent(new BreadcrumbEvent(breadcrumb));
    }

    void firePlaceRequest() {

    }

    void preview(PreviewContent preview) {
        Elements.removeChildrenFrom(previewColumn);
        for (Element element : preview.elements()) {
            previewColumn.appendChild(element);
        }
    }


    // ------------------------------------------------------ public interface

    public void reset(final String finderToken, final String initialColumn, final PreviewContent initialPreview) {
        this.finderToken = finderToken;
        while (root.getFirstChild() != previewColumn) {
            root.removeChild(root.getFirstChild());
        }
        context.reset();
        appendColumn(initialColumn);
        preview(initialPreview);
    }

    public void select(FinderPath path) {
        if (!path.isEmpty()) {

            int index = 0;
            Function[] functions = new Function[path.size()];
            for (FinderPath.Segment segment : path) {
                functions[index] = new SelectFunction(segment);
                index++;
            }
            new Async<FunctionContext>(Progress.NOOP).waterfall(new FunctionContext(), new Outcome<FunctionContext>() {
                        @Override
                        public void onFailure(final FunctionContext context) {
                            logger.error("Unable to select finder path {}: {}", path, context.getErrorMessage()); //NON-NLS
                        }

                        @Override
                        public void onSuccess(final FunctionContext context) {}
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
}
