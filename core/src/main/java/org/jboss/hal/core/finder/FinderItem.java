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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import java.util.List;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.core.finder.Finder.BREADCRUMB_VALUE;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.Names.*;

/**
 * @author Harald Pehl
 */
class FinderItem<T> implements IsElement, SecurityContextAware {

    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final String FOLDER_ELEMENT = "folderElement";
    private static final String BUTTON_CONTAINER = "buttonContainer";
    private static final Logger logger = LoggerFactory.getLogger(FinderItem.class);

    private final Element root;
    private final Element folderElement;
    private final Element buttonContainer;

    public FinderItem(final Provider<Finder> finder,
            final FinderColumn<T> column,
            final T item,
            final ItemDisplay display,
            final List<ActionStruct<T>> actions,
            final SelectCallback<T> selectCallback,
            final PreviewCallback<T> previewCallback) {

        Elements.Builder eb = new Elements.Builder().li()
                .data(BREADCRUMB_VALUE, display.getText())
                .data(filter, display.getFilterData());

        if (display.getTooltip() != null) {
            eb.title(display.getTooltip())
                    .data(TOGGLE, TOOLTIP)
                    .data(PLACEMENT, "top");
        }

        if (display.getMarker() != null) {
            eb.css(display.getMarker().name().toLowerCase() + "-marker");
        }

        if (display.asElement() != null) {
            eb.add(display.asElement());
        } else if (display.getText() != null) {
            eb.span().css(itemText).innerText(display.getText()).end();
        } else {
            eb.span().css(itemText).innerText(NOT_AVAILABLE).end();
        }

        if (display.isFolder()) {
            eb.span().css(folder, fontAwesome("angle-right")).rememberAs(FOLDER_ELEMENT).end();
        }

        if (!actions.isEmpty()) {
            if (actions.size() == 1) {
                ActionStruct<T> action = actions.get(0);
                eb.button()
                        .css(btn, btnFinder)
                        .innerText(action.title)
                        .on(click, event -> action.itemAction.execute(item))
                        .rememberAs(BUTTON_CONTAINER)
                        .end();
            } else {
                boolean firstAction = true;
                boolean ulCreated = false;
                eb.div().css(btnGroup, pullRight).rememberAs(BUTTON_CONTAINER);
                for (ActionStruct<T> action : actions) {
                    if (firstAction) {
                        // @formatter:off
                        eb.button()
                                .css(btn, btnFinder)
                                .innerText(action.title)
                                .on(click, event -> action.itemAction.execute(item))
                        .end();
                        eb.button()
                                .css(btn, btnFinder, dropdownToggle)
                                .data(TOGGLE,  DROPDOWN)
                                .aria(HAS_POPUP, String.valueOf(true))
                                .aria(EXPANDED, String.valueOf(false))
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
                                .on(click, event -> action.itemAction.execute(item))
                                .end().end();
                    }
                }
                eb.end().end(); // </ul> && </div>
            }
        }
        eb.end(); // </li>

        root = eb.build();
        folderElement = display.isFolder() ? eb.referenceFor(FOLDER_ELEMENT) : null;
        buttonContainer = actions.isEmpty() ? null : eb.referenceFor(BUTTON_CONTAINER);
        Elements.setVisible(buttonContainer, false);

        root.setOnclick(event -> {
            for (Element sibling : Elements.children(root.getParentElement())) {
                if (sibling == root) {
                    sibling.getClassList().add(active);
                    if (folderElement != null && buttonContainer != null) {
                        Elements.setVisible(folderElement, false);
                        Elements.setVisible(buttonContainer, true);
                    }

                } else {
                    sibling.getClassList().remove(active);
                    Elements.setVisible(buttonContainer, false);
                    Elements.setVisible(folderElement, true);
                }
            }

            Finder finderInstance = finder.get();
            if (finderInstance != null) {
                finderInstance.reduceTo(column);
                finderInstance.updateBreadcrumb();
                if (selectCallback != null) {
                    selectCallback.onSelect(finderInstance, item);
                }
                if (previewCallback != null) {
                    PreviewContent content = previewCallback.onPreview(item);
                    finderInstance.preview(content);
                }
            } else {
                logger.error("Finder instance for selected item in column '{}' is null", column.getId()); //NON-NLS
            }
        });
    }

    @Override
    public Element asElement() {
        return root;
    }

    @Override
    public void onSecurityContextChange(final SecurityContext securityContext) {

    }
}
