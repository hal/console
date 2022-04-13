/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.core.finder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Provider;

import org.jboss.elemento.Elements;
import org.jboss.elemento.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.config.Environment;
import org.jboss.hal.flow.Flow;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.security.SecurityContextRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.spi.Footer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.lang.Math.min;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.jboss.elemento.Elements.div;
import static org.jboss.hal.ballroom.Skeleton.applicationOffset;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.resources.CSS.column;
import static org.jboss.hal.resources.CSS.finder;
import static org.jboss.hal.resources.CSS.finderPreview;
import static org.jboss.hal.resources.CSS.row;
import static org.jboss.hal.resources.CSS.vh;
import static org.jboss.hal.resources.Ids.FINDER;

/**
 * The one and only finder which is shared across all different top level categories in HAL. The very same finder instance gets
 * injected into the different top level presenters. Only the columns will change when navigating between the different places
 */
public class Finder implements IsElement<HTMLDivElement>, Attachable {

    static final String DATA_BREADCRUMB = "breadcrumb";
    static final String DATA_FILTER = "filter";
    /**
     * The maximum number of simultaneously visible columns. If there are more columns, the left-most column is hidden. TODO
     * Reduce the number of visible columns if the viewport gets smaller and change col-??-2 to col-??-3
     */
    private static final int MAX_VISIBLE_COLUMNS = 4;

    private static final int MAX_COLUMNS = 12;
    private static final Logger logger = LoggerFactory.getLogger(Finder.class);

    private final Environment environment;
    private final EventBus eventBus;
    private final PlaceManager placeManager;
    private final ColumnRegistry columnRegistry;
    private final SecurityContextRegistry securityContextRegistry;
    private final Provider<Progress> progress;
    private final FinderContext context;
    private final LinkedHashMap<String, FinderColumn<?>> columns;
    private final Map<String, String> initialColumnsByToken;
    private final Map<String, PreviewContent<?>> initialPreviewsByToken;
    private final HTMLDivElement root;
    private final HTMLElement previewColumn;
    private PreviewContent<?> currentPreview;

    // ------------------------------------------------------ ui

    @Inject
    public Finder(Environment environment,
            EventBus eventBus,
            PlaceManager placeManager,
            ColumnRegistry columnRegistry,
            SecurityContextRegistry securityContextRegistry,
            @Footer Provider<Progress> progress) {

        this.environment = environment;
        this.eventBus = eventBus;
        this.placeManager = placeManager;
        this.columnRegistry = columnRegistry;
        this.securityContextRegistry = securityContextRegistry;
        this.progress = progress;

        this.context = new FinderContext();
        this.columns = new LinkedHashMap<>();
        this.initialColumnsByToken = new HashMap<>();
        this.initialPreviewsByToken = new HashMap<>();

        this.root = div().id(FINDER).css(row, finder)
                .add(previewColumn = div()
                        .id(Ids.PREVIEW_ID)
                        .css(finderPreview, column(12)).element())
                .element();
    }

    @Override
    public HTMLDivElement element() {
        return root;
    }

    @Override
    public void attach() {
        root.style.height = vh(applicationOffset());
    }

    @Override
    public void detach() {
        columns.values().forEach(Attachable::detach);
    }

    private FinderColumn<?> initialColumn() {
        String columnId = initialColumnsByToken.get(context.getToken());
        if (columnId != null) {
            return columns.get(columnId);
        }
        return null;
    }

    private void resizePreview() {
        long visibleColumns = Elements.stream(root).filter(Elements::isVisible).count() - 1;
        int previewSize = MAX_COLUMNS - 2 * min((int) visibleColumns, MAX_VISIBLE_COLUMNS);
        previewColumn.className = finderPreview + " " + column(previewSize);
    }

    // ------------------------------------------------------ internal API

    <C extends FinderColumn<T>, T> Promise<C> appendColumn(String columnId) {
        Promise<C> lookup = columnRegistry.lookup(columnId);
        return lookup.then(this::appendColumn);
    }

    private <C extends FinderColumn<T>, T> Promise<C> appendColumn(C column) {
        column.resetSelection();
        column.markHiddenColumns(false);
        Elements.setVisible(column.element(), true);

        columns.put(column.getId(), column);
        if (visibleColumns() >= MAX_VISIBLE_COLUMNS) {
            int index = 0;
            int hideUntilHere = columns.size() - MAX_VISIBLE_COLUMNS;
            for (FinderColumn<?> c : columns.values()) {
                Elements.setVisible(c.element(), index >= hideUntilHere);
                index++;
            }
            if (hideUntilHere > 0) {
                for (FinderColumn<?> c : columns.values()) {
                    if (Elements.isVisible(c.element())) {
                        c.markHiddenColumns(true);
                        break;
                    }
                }
            }
        }

        root.insertBefore(column.element(), previewColumn);
        column.attach();
        resizePreview();
        // noinspection unchecked
        return (Promise<C>) column.setItems();
    }

    private long visibleColumns() {
        return columns.values().stream().filter(column -> Elements.isVisible(column.element())).count();
    }

    private void markHiddenColumns() {
        Optional<FinderColumn<?>> hiddenColumn = columns.values().stream()
                .filter(column -> !Elements.isVisible(column.element()))
                .findAny();
        if (hiddenColumn.isPresent()) {
            columns.values().stream()
                    .filter(column -> Elements.isVisible(column.element()))
                    .findAny()
                    .ifPresent(firstVisibleColumn -> firstVisibleColumn.markHiddenColumns(true));
        }
    }

    <C extends FinderColumn<T>, T> void revealHiddenColumns(C firstVisibleColumn) {
        // show the last hidden column
        List<FinderColumn<?>> hiddenColumns = columns.values().stream()
                .filter(column -> !Elements.isVisible(column.element()))
                .collect(toList());
        if (!hiddenColumns.isEmpty()) {
            Elements.setVisible(Iterables.getLast(hiddenColumns).element(), true);
        }
        firstVisibleColumn.markHiddenColumns(false);
        firstVisibleColumn.selectedRow().click();
        markHiddenColumns();
    }

    private void reduceAll() {
        for (Iterator<HTMLElement> iterator = Elements.children(root).iterator(); iterator.hasNext();) {
            HTMLElement element = iterator.next();
            if (element == previewColumn) {
                break;
            }
            FinderColumn<?> removeColumn = columns.remove(element.id);
            iterator.remove();
            removeColumn.detach();
        }
    }

    <C extends FinderColumn<T>, T> void reduceTo(C column) {
        boolean removeFromHere = false;
        for (Iterator<HTMLElement> iterator = Elements.children(root).iterator(); iterator.hasNext();) {
            HTMLElement element = iterator.next();
            if (element == column.element()) {
                removeFromHere = true;
                continue;
            }
            if (element == previewColumn) {
                break;
            }
            if (removeFromHere) {
                FinderColumn<?> removeColumn = columns.remove(element.id);
                iterator.remove();
                removeColumn.detach();
            }
        }
        Elements.setVisible(column.element(), true);
        resizePreview();
    }

    void updateContext() {
        context.getPath().clear();

        for (HTMLElement columnElement : Elements.children(root)) {
            if (columnElement == previewColumn) {
                break;
            }
            String key = columnElement.id;
            FinderColumn<?> column = columns.get(key);
            context.getPath().append(column);
        }
        eventBus.fireEvent(new FinderContextEvent(context));
    }

    void updateHistory() {
        // only finder tokens of the same type please
        PlaceRequest current = placeManager.getCurrentPlaceRequest();
        if (context.getToken().equals(current.getNameToken())) {
            PlaceRequest update = context.toPlaceRequest();
            if (!update.equals(current)) {
                logger.debug("Update history: {}", "#" + context.getToken() +
                        (context.getPath().isEmpty() ? "" : ";path=" + context.getPath()));
                placeManager.updateHistory(update, true);
            }
        }
    }

    void selectColumn(String columnId) {
        FinderColumn<?> finderColumn = columns.get(columnId);
        if (finderColumn != null) {
            finderColumn.element().focus();
        }
    }

    void selectPreviousColumn(String columnId) {
        List<String> columnIds = new ArrayList<>(columns.keySet());
        int index = 0;
        for (String id : columnIds) {
            if (id.equals(columnId)) {
                break;
            }
            index++;
        }
        if (index > 0 && index < columnIds.size()) {
            String previousId = columnIds.get(index - 1);
            selectColumn(previousId);
            FinderColumn<?> previousColumn = columns.get(previousId);
            if (previousColumn != null) {
                FinderRow<?> selectedRow = previousColumn.selectedRow();
                if (selectedRow != null) {
                    selectedRow.click();
                }
            }
        }
    }

    int columns() {
        return columns.size();
    }

    void showPreview(PreviewContent<?> preview) {
        clearPreview();
        currentPreview = preview;
        if (preview != null) {
            for (HTMLElement element : preview) {
                previewColumn.appendChild(element);
            }
            preview.attach();
        }
    }

    private void clearPreview() {
        if (currentPreview != null) {
            currentPreview.detach();
        }
        Elements.removeChildrenFrom(previewColumn);
    }

    void showInitialPreview() {
        PreviewContent<?> previewContent = initialPreviewsByToken.get(context.getToken());
        if (previewContent != null) {
            showPreview(previewContent);
        }
    }

    Environment environment() {
        return environment;
    }

    SecurityContextRegistry securityContextRegistry() {
        return securityContextRegistry;
    }

    // ------------------------------------------------------ public API

    /**
     * Resets the finder to its initial state by showing the initial column and preview.
     */
    public void reset(String token, String initialColumn, PreviewContent<?> initialPreview) {
        initialColumnsByToken.put(token, initialColumn);
        initialPreviewsByToken.put(token, initialPreview);

        for (FinderColumn<?> column : columns.values()) {
            column.detach();
        }
        columns.clear();
        while (root.firstChild != previewColumn) {
            root.removeChild(root.firstChild);
        }
        context.reset(token);
        appendColumn(initialColumn);
        selectColumn(initialColumn);
        for (FinderColumn<?> column : columns.values()) {
            Elements.setVisible(column.element(), true);
            column.markHiddenColumns(false);
        }
        showPreview(initialPreview);
        updateHistory();
    }

    /**
     * Refreshes the current path.
     */
    public void refresh() {
        refresh(getContext().getPath());
    }

    /**
     * Refreshes the specified path.
     * <p>
     * Please note that this might be a complex and long-running operation since each segment in the path is turned into a
     * function which reloads and re-selects the items.
     */
    public void refresh(FinderPath path) {
        if (!path.isEmpty()) {

            List<Task<FlowContext>> tasks = stream(path.spliterator(), false)
                    .map(segment -> new RefreshTask(new FinderSegment<>(segment.getColumnId(), segment.getItemId())))
                    .collect(toList());
            Flow.series(new FlowContext(progress.get()), tasks)
                    .then(c -> {
                        if (!c.emptyStack()) {
                            FinderColumn<?> column = c.pop();
                            column.element().focus();
                            if (column.selectedRow() != null) {
                                column.selectedRow().click();
                            }
                        }
                        return null;
                    });
        }
    }

    /**
     * Shows the finder associated with the specified token and selects the columns and items according to the given finder
     * path.
     * <p>
     * Please note that this might be a complex and long running operation since each segment in the path is turned into a
     * function. The function will load and initialize the column and select the item as specified in the segment.
     * <p>
     * If the path is empty, the fallback operation is executed.
     */
    public void select(String token, FinderPath path, Runnable fallback) {
        if (path.isEmpty()) {
            fallback.run();

        } else {
            if (!token.equals(context.getToken())) {
                context.reset(token);
                reduceAll();

            } else {
                // clear the preview right away, otherwise the previous (wrong) preview would be visible until all
                // select functions have been finished
                clearPreview();

                // Find the last common column between the new and the current path
                String match = null;
                FinderPath newPath = path.reversed();
                FinderPath currentPath = context.getPath().reversed();
                for (FinderSegment<?> newSegment : newPath) {
                    for (FinderSegment<?> currentSegment : currentPath) {
                        if (newSegment.getColumnId().equals(currentSegment.getColumnId())) {
                            match = newSegment.getColumnId();
                            break;
                        }
                    }
                    if (match != null) {
                        break;
                    }
                }
                FinderColumn<?> lastCommonColumn = match != null ? columns.get(match) : initialColumn();
                if (lastCommonColumn != null) {
                    reduceTo(lastCommonColumn);
                }
            }

            List<Task<FlowContext>> tasks = stream(path.spliterator(), false)
                    .map(segment -> new SelectTask(new FinderSegment<>(segment.getColumnId(), segment.getItemId())))
                    .collect(toList());
            Flow.series(new FlowContext(progress.get()), tasks)
                    .then(c -> {
                        FinderColumn<?> column = c.pop();
                        column.element().focus();
                        column.refresh(RESTORE_SELECTION);
                        return null;
                    })
                    .catch_(error -> {
                        if (Finder.this.context.getPath().isEmpty()) {
                            fallback.run();
                        }
                        return null;
                    });
        }
    }

    public <C extends FinderColumn<T>, T> C getColumn(String columnId) {
        // noinspection unchecked
        return (C) columns.get(columnId);
    }

    public FinderContext getContext() {
        return context;
    }

    private final class SelectTask implements Task<FlowContext> {

        private final FinderSegment<?> segment;

        private SelectTask(final FinderSegment<?> segment) {
            this.segment = segment;
        }

        @Override
        public Promise<FlowContext> apply(final FlowContext context) {
            return new Promise<>((resolve, reject) -> appendColumn(segment.getColumnId())
                    .then(column -> {
                        if (column.contains(segment.getItemId())) {
                            column.markSelected(segment.getItemId());
                            column.row(segment.getItemId()).element().scrollIntoView(false);
                            updateContext();
                            context.push(column);
                        } else {
                            // Ignore items which cannot be selected. If a deployment was disabled
                            // runtime items might no longer be available.
                            logger.warn("Unable to select item '{} in column '{}'", segment.getItemId(),
                                    segment.getColumnId());
                        }
                        resolve.onInvoke(context);
                        return null;
                    }));
        }
    }

    private final class RefreshTask implements Task<FlowContext> {

        private final FinderSegment<?> segment;

        private RefreshTask(final FinderSegment<?> segment) {
            this.segment = segment;
        }

        @Override
        public Promise<FlowContext> apply(final FlowContext context) {
            FinderColumn<?> column = getColumn(segment.getColumnId());
            return new Promise<>((resolve, reject) -> {
                if (column != null) {
                    column.refresh(() -> {
                        selectItem(column, context);
                        resolve.onInvoke(context);
                    });
                } else {
                    appendColumn(segment.getColumnId()).then(c -> {
                        selectItem(c, context);
                        resolve.onInvoke(context);
                        return null;
                    });
                }
            });
        }

        private void selectItem(FinderColumn<?> column, FlowContext context) {
            if (column.contains(segment.getItemId())) {
                column.markSelected(segment.getItemId());
                context.push(column);
            } else {
                throw new RuntimeException("Error in Finder.RefreshTask: Unable to select item '" +
                        segment.getItemId() + "' in column '" + segment.getColumnId() + "'");
            }
        }
    }
}
