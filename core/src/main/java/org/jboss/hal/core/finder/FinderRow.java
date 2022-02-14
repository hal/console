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

import java.util.List;

import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.gwt.elemento.core.builder.HtmlContentBuilder;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.meta.security.AuthorisationDecision;
import org.jboss.hal.meta.security.ElementGuard;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Strings;
import org.jboss.hal.resources.UIConstants;

import com.google.gwt.core.client.GWT;

import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLLIElement;
import elemental2.dom.HTMLUListElement;

import static java.util.stream.Collectors.toList;
import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.core.finder.Finder.DATA_BREADCRUMB;
import static org.jboss.hal.core.finder.Finder.DATA_FILTER;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.Names.NOT_AVAILABLE;
import static org.jboss.hal.resources.UIConstants.HASH;
import static org.jboss.hal.resources.UIConstants.data;

/** UI class for a single row in in a finder column. Only used internally in the finder. */
class FinderRow<T> implements IsElement<HTMLLIElement> {

    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final String PREVENT_SET_ITEMS = "preventSetItems";
    private static final int MAX_ACTION_TITLE_LENGTH = 16;

    private final Finder finder;
    private final FinderColumn<T> column;
    private final ItemDisplay<T> display;
    private final List<ItemAction<T>> actions;
    private final String nextColumn;
    private ItemActionHandler<T> primaryAction;
    private final PreviewContent<T> previewContent;
    private String id;
    private T item;

    private HTMLLIElement root;
    private HTMLElement folderElement;
    private HTMLElement buttonContainer;

    FinderRow(Finder finder,
            FinderColumn<T> column,
            T item,
            boolean pinned,
            ItemDisplay<T> display,
            PreviewCallback<T> previewCallback) {
        this.finder = finder;
        this.column = column;
        this.display = display;
        this.actions = allowedActions(display.actions());
        this.nextColumn = display.nextColumn();
        this.id = Strings.sanitize(display.getId());
        this.primaryAction = actions.isEmpty() ? null : actions.get(0).handler;
        this.previewContent = previewCallback != null ? previewCallback.onPreview(item)
                : new PreviewContent<>(
                        display.getTitle());

        root = li().element();
        folderElement = null;
        if (column.isPinnable()) {
            root.className = pinned ? CSS.pinned : unpinned;
        }
        root.classList.add(finderItem);
        updateItem(item);
        drawItem();
        bind(root, click, event -> onClick(((HTMLElement) event.target)));
    }

    private List<ItemAction<T>> allowedActions(List<ItemAction<T>> actions) {
        List<ItemAction<T>> allowed = actions.stream()
                .filter(action -> AuthorisationDecision.from(finder.environment(),
                        finder.securityContextRegistry()).isAllowed(action.constraints))
                .collect(toList());

        // after filtering remove a starting separator
        if (!allowed.isEmpty()) {
            if (allowed.get(0) == ItemAction.SEPARATOR) {
                if (allowed.size() == 1) {
                    allowed.clear();
                } else {
                    allowed = allowed.subList(1, allowed.size());
                }
            }
        }
        return allowed;
    }

    private void updateItem(T item) {
        this.item = item;
    }

    private void drawItem() {
        Elements.removeChildrenFrom(root);
        root.id = id;
        root.dataset.set(DATA_BREADCRUMB, display.getTitle());
        // TODO getFilterData() causes a ReferenceError in SuperDevMode WTF?
        if (display.getFilterData() != null) {
            root.dataset.set(DATA_FILTER, display.getFilterData());
        }

        HTMLElement icon = display.getIcon();
        if (icon != null) {
            icon.classList.add(itemIcon);
            root.appendChild(icon);
        }

        HTMLElement itemElement;
        if (display.element() != null) {
            itemElement = display.element();
        } else if (display.getTitle() != null) {
            itemElement = span().css(itemText).textContent(display.getTitle()).element();
        } else {
            itemElement = span().css(itemText).textContent(NOT_AVAILABLE).element();
        }
        if (display.getTooltip() != null && itemElement != null) {
            itemElement.title = display.getTooltip();
            itemElement.dataset.set(UIConstants.TOGGLE, UIConstants.TOOLTIP);
            itemElement.dataset.set(UIConstants.PLACEMENT, "top");
        }
        root.appendChild(itemElement);

        // oder: 1) pin/unpin icon, 2) folder icon, 3) button(s)
        if (column.isPinnable()) {
            root.appendChild(span().css(CSS.unpin, pfIcon("close"))
                    .title(CONSTANTS.unpin())
                    .on(click, e -> column.unpin(FinderRow.this))
                    .data(PREVENT_SET_ITEMS, UIConstants.TRUE)
                    .element());
            root.appendChild(span().css(CSS.pin, pfIcon("thumb-tack-o"))
                    .title(CONSTANTS.pin())
                    .on(click, e -> column.pin(FinderRow.this))
                    .data(PREVENT_SET_ITEMS, UIConstants.TRUE)
                    .element());
        }

        if (display.nextColumn() != null) {
            folderElement = span().css(folder, fontAwesome("angle-right")).element();
            root.appendChild(folderElement);
        }

        if (!actions.isEmpty()) {
            if (actions.size() == 1) {
                ItemAction<T> action = actions.get(0);
                buttonContainer = actionLink(action, false);

            } else {
                HTMLUListElement ul = null;
                boolean firstAction = true;
                boolean ulCreated = false;
                buttonContainer = div().css(btnGroup, pullRight).data(PREVENT_SET_ITEMS, UIConstants.TRUE).element();
                for (ItemAction<T> action : actions) {
                    if (firstAction) {
                        buttonContainer.appendChild(actionLink(action, false));
                        buttonContainer.appendChild(button()
                                .css(btn, btnFinder, dropdownToggle)
                                .data(UIConstants.TOGGLE, UIConstants.DROPDOWN)
                                .data(PREVENT_SET_ITEMS, UIConstants.TRUE)
                                .aria(UIConstants.HAS_POPUP, UIConstants.TRUE)
                                .aria(UIConstants.EXPANDED, UIConstants.FALSE)
                                .add(span().css(caret)
                                        .data(PREVENT_SET_ITEMS, UIConstants.TRUE))
                                .add(span().css(srOnly)
                                        .data(PREVENT_SET_ITEMS, UIConstants.TRUE)
                                        .textContent(CONSTANTS.toggleDropdown()))
                                .element());
                        firstAction = false;
                    } else {
                        if (!ulCreated) {
                            buttonContainer.appendChild(ul = ul().css(dropdownMenu)
                                    .data(PREVENT_SET_ITEMS, UIConstants.TRUE)
                                    .element());
                            ulCreated = true;
                        }
                        if (action == ItemAction.SEPARATOR) {
                            ul.appendChild(li().css(divider).attr(UIConstants.ROLE, UIConstants.SEPARATOR).element());
                        } else {
                            ul.appendChild(li()
                                    .data(PREVENT_SET_ITEMS, UIConstants.TRUE)
                                    .add(actionLink(action, true))
                                    .element());
                        }
                    }
                }
            }
            root.appendChild(buttonContainer);
            Elements.setVisible(buttonContainer, isSelected());
        }
        PatternFly.initComponents(HASH + id);
    }

    private HTMLAnchorElement actionLink(ItemAction<T> action, boolean li) {
        String[] css = li ? new String[] { clickable } : new String[] { clickable, btn, btnFinder };
        HtmlContentBuilder<HTMLAnchorElement> builder = a().css(css)
                .data(PREVENT_SET_ITEMS, UIConstants.TRUE);
        if (!li && action.title.length() > MAX_ACTION_TITLE_LENGTH) {
            builder.textContent(Strings.abbreviateMiddle(action.title, MAX_ACTION_TITLE_LENGTH));
            builder.title(action.title);
        } else {
            builder.textContent(action.title);
        }
        if (action.handler != null) {
            builder.on(click, event -> action.handler.execute(item));
        } else if (action.href != null) {
            builder.apply(a -> a.href = action.href);
        }
        if (!action.attributes.isEmpty()) {
            action.attributes.forEach(builder::attr);
        }
        return builder.element();
    }

    void click() {
        onClick(null);
    }

    private void onClick(HTMLElement target) {
        if (target != null && Boolean.parseBoolean(String.valueOf(target.dataset.get(PREVENT_SET_ITEMS)))) {
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
            root.classList.add(active);
            if (buttonContainer != null) {
                Elements.setVisible(buttonContainer, true);
                Elements.setVisible(folderElement, false);
            }

        } else {
            root.classList.remove(active);
            Elements.setVisible(buttonContainer, false);
            Elements.setVisible(folderElement, true);
        }
    }

    void updatePreview() {
        if (isSelected()) {
            finder.showPreview(previewContent);
        }
        previewContent.update(item);

        AuthorisationDecision ad = AuthorisationDecision.from(finder.environment(), finder.securityContextRegistry());
        ElementGuard.processElements(ad, HASH + Ids.PREVIEW_ID + " [" + data(UIConstants.CONSTRAINT + "]"));
    }

    private boolean isSelected() {
        return column.selectedRow() != null && column.selectedRow().getId().equals(id);
    }

    @Override
    public HTMLLIElement element() {
        return root;
    }

    // ------------------------------------------------------ getter

    String getId() {
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
