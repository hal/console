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
package org.jboss.hal.ballroom.listview;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Splitter;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLUListElement;
import org.jboss.gwt.elemento.core.InputType;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.*;

/** An item inside a {@link ListView}. */
class ListItem<T> implements IsElement {

    private final HTMLElement root;
    private final Map<String, HTMLElement> actions;

    final String id;
    final T item;
    final HTMLInputElement checkbox;

    ListItem(ListView<T> listView, T item, boolean checkbox, ItemDisplay<T> display, String[] contentWidths) {
        this.id = display.getId();
        this.item = item;
        this.actions = new HashMap<>();

        // root & checkbox
        HTMLElement container;
        root = div().id(id).css(listPfItem)
                .add(container = div().css(listPfContainer).asElement())
                .asElement();
        if (checkbox) {
            container.appendChild(div().css(listPfSelect)
                    .add(this.checkbox = input(InputType.checkbox)
                            .on(click, event -> {
                                HTMLInputElement element = (HTMLInputElement) event.target;
                                listView.selectListItem(ListItem.this, element.checked);
                            })
                            .asElement())
                    .asElement());
        } else {
            this.checkbox = null;
            bind(root, click, event -> listView.selectListItem(this, true));
        }

        // status icon, title, description, additional info
        HTMLElement content;
        HTMLElement contentWrapper;
        HTMLElement mainContent;
        HTMLElement title;
        container.appendChild(content = div().css(listPfContent, listPfContentFlex).asElement());
        if (display.getStatusElement() != null) {
            content.appendChild(div().css(listPfLeft)
                    .add(display.getStatusElement())
                    .asElement());
        } else if (display.getStatusIcon() != null) {
            HTMLElement status;
            content.appendChild(div().css(listPfLeft)
                    .add(status = span().css(listPfIcon, listPfIconBordered, listPfIconSmall)
                            .asElement())
                    .asElement());
            List<String> classes = Splitter.on(' ')
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(display.getStatusIcon());
            status.classList.add(classes.toArray(new String[classes.size()]));
        }
        content.appendChild(contentWrapper = div().css(listPfContentWrapper)
                .add(mainContent = div().css(listPfMainContent, listHalMainContent)
                        .style("flex-basis:" + contentWidths[0]) //NON-NLS
                        .add(title = div().css(listPfTitle)
                                .asElement())
                        .asElement())
                .asElement());
        if (display.getTitleElements() != null) {
            for (HTMLElement element : display.getTitleElements().asElements()) {
                title.appendChild(element);
            }
        } else if (display.getTitleHtml() != null) {
            title.appendChild(h(3).innerHtml(display.getTitleHtml()).asElement());
        } else if (display.getTitle() != null) {
            title.appendChild(h(3, display.getTitle()).asElement());
        } else {
            title.appendChild(h(3, Names.NOT_AVAILABLE).asElement());
        }
        if (display.getDescriptionElements() != null ||
                display.getDescriptionHtml() != null ||
                display.getDescription() != null) {
            HTMLElement description;
            mainContent.appendChild(description = div().css(listPfDescription)
                    .asElement());
            if (display.getDescriptionElements() != null) {
                for (HTMLElement element : display.getDescriptionElements().asElements()) {
                    description.appendChild(element);
                }
            } else if (display.getDescriptionHtml() != null) {
                description.innerHTML = display.getDescriptionHtml().asString();
            } else if (display.getDescription() != null) {
                description.textContent = display.getDescription();
            }
        }
        if (display.getAdditionalInfoElements() != null ||
                display.getAdditionalInfoHtml() != null ||
                display.getAdditionalInfo() != null) {
            HTMLElement additionalInfo;
            contentWrapper.appendChild(additionalInfo = div().css(listPfAdditionalContent, listHalAdditionalContent)
                    .style("flex-basis:" + contentWidths[1]) //NON-NLS
                    .asElement());
            if (display.getAdditionalInfoElements() != null) {
                for (HTMLElement element : display.getAdditionalInfoElements().asElements()) {
                    additionalInfo.appendChild(element);
                }
            } else if (display.getAdditionalInfoHtml() != null) {
                additionalInfo.innerHTML = display.getAdditionalInfoHtml().asString();
            } else if (display.getAdditionalInfo() != null) {
                additionalInfo.textContent = display.getAdditionalInfo();
            }
        }

        List<ItemAction<T>> allowedActions = listView.allowedActions(display.actions());
        if (!allowedActions.isEmpty()) {
            HTMLElement actionsContainer;
            content.appendChild(actionsContainer = div().css(listPfActions, listHalActions)
                    .asElement());
            int index = 0;
            HTMLUListElement ul = null;
            for (ItemAction<T> action : allowedActions) {
                HTMLElement actionElement;
                String actionId = Ids.build(this.id, action.id);

                if (index == 0) {
                    // first action is a button
                    actionsContainer.appendChild(actionElement = button()
                            .id(actionId)
                            .css(btn, btnDefault)
                            .textContent(action.title)
                            .on(click, event -> action.handler.execute(item))
                            .asElement());

                } else {
                    // remaining actions are inside the kebab menu
                    if (index == 1) {
                        String id = Ids.build(display.getId(), "kebab", "menu");
                        actionsContainer.appendChild(
                                div().css(dropdown, pullRight, dropdownKebabPf)
                                        .add(button()
                                                .id(id)
                                                .css(btn, btnLink, dropdownToggle)
                                                .data(UIConstants.TOGGLE, UIConstants.DROPDOWN)
                                                .aria(UIConstants.HAS_POPUP, UIConstants.TRUE)
                                                .aria(UIConstants.EXPANDED, UIConstants.TRUE)
                                                .add(span().css(fontAwesome("ellipsis-v"))))
                                        .add(ul = ul().css(dropdownMenu, dropdownMenuRight)
                                                .aria(UIConstants.LABELLED_BY, id)
                                                .asElement())
                                        .asElement());
                    }
                    //noinspection ConstantConditions
                    ul.appendChild(actionElement = li()
                            .add(a().css(clickable)
                                    .textContent(action.title)
                                    .on(click, (event -> action.handler.execute(item))))
                            .asElement());
                }

                this.actions.put(action.id, actionElement);
                index++;
            }
        }
    }

    @Override
    public HTMLElement asElement() {
        return root;
    }

    void enableAction(final String actionId) {
        HTMLElement action = actions.get(actionId);
        if (action != null) {
            action.classList.remove(disabled);
        }
    }

    void disableAction(final String actionId) {
        HTMLElement action = actions.get(actionId);
        if (action != null) {
            action.classList.add(disabled);
        }
    }
}
