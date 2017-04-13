/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.core.finder;

import java.util.List;

import com.google.gwt.core.client.GWT;
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.meta.security.AuthorisationDecision;
import org.jboss.hal.meta.security.ElementGuard;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.UIConstants;

import static java.util.stream.Collectors.toList;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.core.finder.Finder.DATA_BREADCRUMB;
import static org.jboss.hal.core.finder.Finder.DATA_FILTER;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.Names.NOT_AVAILABLE;
import static org.jboss.hal.resources.UIConstants.data;

/**
 * UI class for a single row in in a finder column. Only used internally in the finder.
 * <p>
 * TODO Add an option to activate an inline progress element which sets a striped background for long running
 * actions like 'restart server group'. Think about replacing the actions with a cancel button
 *
 * @author Harald Pehl
 */
class FinderRow<T> implements IsElement {

    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final String FOLDER_ELEMENT = "folderElement";
    private static final String BUTTON_CONTAINER = "buttonContainer";
    private static final String PREVENT_SET_ITEMS = "preventSetItems";

    private final Finder finder;
    private final FinderColumn<T> column;
    private final ItemDisplay<T> display;
    private final List<ItemAction<T>> actions;
    private final String nextColumn;
    private ItemActionHandler<T> primaryAction;
    private final PreviewContent<T> previewContent;
    private String id;
    private T item;

    private Element root;
    private Element folderElement;
    private Element buttonContainer;

    FinderRow(final Finder finder,
            final FinderColumn<T> column,
            final T item,
            final boolean pinned,
            final ItemDisplay<T> display,
            final PreviewCallback<T> previewCallback) {

        this.finder = finder;
        this.column = column;
        this.display = display;
        this.actions = allowedActions(display.actions());
        this.nextColumn = display.nextColumn();
        this.id = display.getId();
        this.primaryAction = actions.isEmpty() ? null : actions.get(0).handler;
        this.previewContent = previewCallback != null ? previewCallback.onPreview(item) : new PreviewContent<>(
                display.getTitle());

        root = Browser.getDocument().createLIElement();
        if (column.isPinnable()) {
            root.setClassName(pinned ? CSS.pinned : unpinned);
        }
        updateItem(item);
        drawItem();
        root.setOnclick(event -> onClick(((Element) event.getTarget())));
    }

    private List<ItemAction<T>> allowedActions(final List<ItemAction<T>> actions) {
        return actions.stream()
                .filter(action -> AuthorisationDecision.strict(finder.environment(),
                        finder.securityContextRegistry()).isAllowed(action.constraints))
                .collect(toList());
    }

    private void updateItem(final T item) {
        this.id = display.getId();
        this.item = item;
    }

    private void drawItem() {
        Elements.removeChildrenFrom(root);
        root.setId(display.getId());
        root.getDataset().setAt(DATA_BREADCRUMB, display.getTitle());
        // TODO getFilterData() causes a ReferenceError in SuperDevMode WTF?
        root.getDataset().setAt(DATA_FILTER, display.getFilterData());

        Element icon = display.getIcon();
        if (icon != null) {
            icon.getClassList().add(itemIcon);
            root.appendChild(icon);
        }

        Element itemElement;
        if (display.asElement() != null) {
            itemElement = display.asElement();
        } else if (display.getTitle() != null) {
            itemElement = new Elements.Builder().span().css(itemText).textContent(display.getTitle()).end().build();
        } else {
            itemElement = new Elements.Builder().span().css(itemText).textContent(NOT_AVAILABLE).end().build();
        }
        if (display.getTooltip() != null && itemElement != null) {
            itemElement.setTitle(display.getTooltip());
            itemElement.getDataset().setAt(UIConstants.TOGGLE, UIConstants.TOOLTIP);
            itemElement.getDataset().setAt(UIConstants.PLACEMENT, "top");
        }
        root.appendChild(itemElement);

        Elements.Builder eb = new Elements.Builder();
        boolean controls = column.isPinnable() || display.nextColumn() != null || !actions.isEmpty();
        // oder: 1) pin/unpin icon, 2) folder icon, 3) button(s)
        if (column.isPinnable()) {
            eb.span()
                    .css(unpin, pfIcon("close"))
                    .title(CONSTANTS.unpin())
                    .on(click, e -> column.unpin(FinderRow.this))
                    .data(PREVENT_SET_ITEMS, UIConstants.TRUE)
                    .end();
            eb.span()
                    .css(pin, pfIcon("thumb-tack-o"))
                    .title(CONSTANTS.pin())
                    .on(click, e -> column.pin(FinderRow.this))
                    .data(PREVENT_SET_ITEMS, UIConstants.TRUE)
                    .end();
        }
        if (display.nextColumn() != null) {
            eb.span().css(folder, fontAwesome("angle-right")).rememberAs(FOLDER_ELEMENT).end();
        }
        if (!actions.isEmpty()) {
            if (actions.size() == 1) {
                ItemAction<T> action = actions.get(0);
                actionLink(eb, action, false, BUTTON_CONTAINER);
            } else {
                boolean firstAction = true;
                boolean ulCreated = false;
                eb.div().css(btnGroup, pullRight).data(PREVENT_SET_ITEMS, UIConstants.TRUE)
                        .rememberAs(BUTTON_CONTAINER);
                for (ItemAction<T> action : actions) {
                    if (firstAction) {
                        // @formatter:off
                        actionLink(eb, action, false, null);
                        eb.button()
                                .css(btn, btnFinder, dropdownToggle)
                                .data(UIConstants.TOGGLE, UIConstants.DROPDOWN)
                                .data(PREVENT_SET_ITEMS, UIConstants.TRUE)
                                .aria(UIConstants.HAS_POPUP, UIConstants.TRUE)
                                .aria(UIConstants.EXPANDED, UIConstants.FALSE)
                            .span().css(caret).data(PREVENT_SET_ITEMS, UIConstants.TRUE).end()
                            .span()
                                .css(srOnly).data(PREVENT_SET_ITEMS, UIConstants.TRUE)
                                .textContent(CONSTANTS.toggleDropdown())
                            .end()
                        .end();
                        // @formatter:on
                        firstAction = false;

                    } else {
                        if (!ulCreated) {
                            eb.ul().css(dropdownMenu).data(PREVENT_SET_ITEMS, UIConstants.TRUE);
                            ulCreated = true;
                        }
                        eb.li().data(PREVENT_SET_ITEMS, UIConstants.TRUE);
                        actionLink(eb, action, true, null);
                        eb.end();
                    }
                }
                eb.end().end(); // </ul> && </div>
            }
        }
        folderElement = display.nextColumn() != null ? eb.referenceFor(FOLDER_ELEMENT) : null;
        buttonContainer = actions.isEmpty() ? null : eb.referenceFor(BUTTON_CONTAINER);
        if (controls) {
            eb.elements().forEach(element -> root.appendChild(element));
            Elements.setVisible(buttonContainer, isSelected());
        }
        PatternFly.initComponents("#" + display.getId());
    }

    private void actionLink(Elements.Builder builder, ItemAction<T> action, boolean li, String reference) {
        builder.a().css(clickable, li ? new String[]{} : new String[]{btn, btnFinder})
                .data(PREVENT_SET_ITEMS, UIConstants.TRUE)
                .textContent(action.title);
        if (action.handler != null) {
            builder.on(click, event -> action.handler.execute(item));
        } else if (action.href != null) {
            builder.attr(UIConstants.HREF, action.href);
        }
        if (!action.attributes.isEmpty()) {
            action.attributes.forEach(builder::attr);
        }
        if (reference != null) {
            builder.rememberAs(reference);
        }
        builder.end();
    }

    void click() {
        onClick(null);
    }

    private void onClick(final Element target) {
        if (target != null && Boolean.parseBoolean(String.valueOf(target.getDataset().at(PREVENT_SET_ITEMS)))) {
            return;
        }
        column.markSelected(id);
        // <keep> this in order!
        finder.reduceTo(column);
        finder.updateContext();
        finder.updateHistory();
        if (nextColumn != null) {
            finder.appendColumn(nextColumn, null);
        }
        // </keep>
        updatePreview();
    }

    void markSelected(boolean select) {
        if (select) {
            root.getClassList().add(active);
            if (buttonContainer != null) {
                Elements.setVisible(buttonContainer, true);
                Elements.setVisible(folderElement, false);
            }

        } else {
            root.getClassList().remove(active);
            Elements.setVisible(buttonContainer, false);
            Elements.setVisible(folderElement, true);
        }
    }

    void updatePreview() {
        if (isSelected()) {
            finder.showPreview(previewContent);
        }
        previewContent.update(item);

        AuthorisationDecision ad = AuthorisationDecision.strict(finder.environment(), finder.securityContextRegistry());
        ElementGuard.processElements(ad, "#" + Ids.PREVIEW_ID + " [" + data(UIConstants.CONSTRAINT + "]"));
    }

    private boolean isSelected() {
        return column.selectedRow() != null && column.selectedRow().getId().equals(display.getId());
    }

    @Override
    public Element asElement() {
        return root;
    }


    // ------------------------------------------------------ getter

    public String getId() {
        return id;
    }

    String getNextColumn() {
        return nextColumn;
    }

    ItemActionHandler<T> getPrimaryAction() {
        return primaryAction;
    }

    T getItem() {
        return item;
    }

    ItemDisplay<T> getDisplay() {
        return display;
    }
}
