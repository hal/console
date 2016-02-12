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

import com.google.gwt.core.client.GWT;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextAware;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.core.finder.Finder.DATA_BREADCRUMB;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.Names.*;

/**
 * UI class for a single row in in a finder column. Only used internally in the finder.
 *
 * @author Harald Pehl
 */
class FinderRow<T> implements IsElement, SecurityContextAware {

    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final String FOLDER_ELEMENT = "folderElement";
    private static final String BUTTON_CONTAINER = "buttonContainer";
    private static final String TOOLTIP_TARGET = "tooltipTarget";

    private final Finder finder;
    private final FinderColumn<T> column;
    private final String nextColumn;
    private final String id;
    private final T item;
    private ItemActionHandler<T> primaryAction;
    private final PreviewContent previewContent;

    private final Element root;
    private final Element folderElement;
    private final Element buttonContainer;

    FinderRow(final Finder finder,
            final FinderColumn<T> column,
            final T item,
            final ItemDisplay<T> display,
            final PreviewCallback<T> previewCallback) {

        this.finder = finder;
        this.column = column;
        this.nextColumn = display.nextColumn();
        this.id = display.getId();
        this.item = item;
        this.primaryAction = display.actions().isEmpty() ? null : display.actions().iterator().next().handler;
        this.previewContent = previewCallback != null ? previewCallback.onPreview(item) : new PreviewContent(
                display.getTitle());

        Elements.Builder eb = new Elements.Builder().li()
                .id(display.getId())
                .data(DATA_BREADCRUMB, display.getTitle())
                .data(filter, display.getFilterData());

        if (display.getMarker() != null) {
            eb.css(display.getMarker().name().toLowerCase() + "-marker");
        }

        Element tooltipTarget;
        if (display.asElement() != null) {
            eb.add(display.asElement());
            tooltipTarget = display.asElement();
        } else if (display.getTitle() != null) {
            eb.span().css(itemText).innerText(display.getTitle()).rememberAs(TOOLTIP_TARGET).end();
            tooltipTarget = eb.referenceFor(TOOLTIP_TARGET);
        } else {
            eb.span().css(itemText).innerText(NOT_AVAILABLE).rememberAs(TOOLTIP_TARGET).end();
            tooltipTarget = eb.referenceFor(TOOLTIP_TARGET);
        }

        if (display.getTooltip() != null && tooltipTarget != null) {
            tooltipTarget.setTitle(display.getTooltip());
            tooltipTarget.getDataset().setAt(UIConstants.TOGGLE, UIConstants.TOOLTIP);
            tooltipTarget.getDataset().setAt(UIConstants.PLACEMENT, "top");
        }

        if (display.nextColumn() != null) {
            eb.span().css(folder, fontAwesome("angle-right")).rememberAs(FOLDER_ELEMENT).end();
        }

        if (!display.actions().isEmpty()) {
            if (display.actions().size() == 1) {
                ItemAction<T> action = display.actions().get(0);
                eb.button()
                        .css(btn, btnFinder)
                        .innerText(action.title)
                        .on(click, event -> action.handler.execute(item))
                        .rememberAs(BUTTON_CONTAINER)
                        .end();
            } else {
                boolean firstAction = true;
                boolean ulCreated = false;
                eb.div().css(btnGroup, pullRight).rememberAs(BUTTON_CONTAINER);
                for (ItemAction<T> action : display.actions()) {
                    if (firstAction) {
                        // @formatter:off
                        eb.button()
                                .css(btn, btnFinder)
                                .innerText(action.title)
                                .on(click, event -> action.handler.execute(item))
                        .end();
                        eb.button()
                                .css(btn, btnFinder, dropdownToggle)
                                .data(UIConstants.TOGGLE,  "dropdown") //NON-NLS
                                .aria(UIConstants.HAS_POPUP, String.valueOf(true))
                                .aria(UIConstants.EXPANDED, String.valueOf(false))
                            .span().css(caret).end()
                            .span().css(srOnly).innerText(CONSTANTS.toggleDropdown()).end()
                        .end();
                        // @formatter:on
                        firstAction = false;

                    } else {
                        if (!ulCreated) {
                            eb.ul().css(dropdownMenu);
                            ulCreated = true;
                        }
                        eb.li().a()
                                .innerText(action.title)
                                .css(clickable)
                                .on(click, event -> action.handler.execute(item))
                                .end().end();
                    }
                }
                eb.end().end(); // </ul> && </div>
            }
        }
        eb.end(); // </li>

        root = eb.build();
        folderElement = display.nextColumn() != null ? eb.referenceFor(FOLDER_ELEMENT) : null;
        buttonContainer = display.actions().isEmpty() ? null : eb.referenceFor(BUTTON_CONTAINER);
        Elements.setVisible(buttonContainer, false);

        root.setOnclick(event -> click());
    }

    void click() {
        column.selectItem(id);
        // <keep> this in order!
        finder.reduceTo(column);
        finder.updateContext();
        finder.publishContext();
        appendNextColumn();
        // </keep>
        preview();
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

    void appendNextColumn() {
        if (nextColumn != null) {
            finder.appendColumn(nextColumn, null);
        }
    }

    void preview() {
        if (previewContent != null) {
            finder.preview(previewContent);
        }
    }

    @Override
    public Element asElement() {
        return root;
    }

    @Override
    public void onSecurityContextChange(final SecurityContext securityContext) {

    }

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
}
