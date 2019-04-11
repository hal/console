/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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
import com.google.gwt.core.client.GWT;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLUListElement;
import elemental2.dom.MouseEvent;
import org.jboss.gwt.elemento.core.EventCallbackFn;
import org.jboss.gwt.elemento.core.InputType;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.UIConstants.HASH;

/**
 * An item inside a {@link ListView}.
 */
class ListItem<T> implements IsElement {

    final String id;
    final T item;
    final HTMLInputElement checkbox;
    private final HTMLElement root;
    private final Map<String, HTMLElement> actions;
    private final Constants CONSTANTS = GWT.create(Constants.class);


    ListItem(ListView<T> listView, T item, boolean checkbox, ItemDisplay<T> display, String[] contentWidths) {
        this.id = display.getId();
        this.item = item;
        this.actions = new HashMap<>();
        String idLongPanel = id + "-panel";

        // root & checkbox
        HTMLElement container;
        if (display.hideDescriptionWhenLarge()) {
            root = div().id(id).css(listPfItem, listPfHeader)
                    .add(a("#" + idLongPanel)
                            .data(UIConstants.TOGGLE, UIConstants.COLLAPSE)
                            .data("parent", HASH + this.id)
                            .aria(UIConstants.CONTROLS, idLongPanel)
                            .attr(UIConstants.ROLE, UIConstants.BUTTON)
                            .add(container = div().css(listPfContainer)
                                    .get()))
                    .get();
        } else {
            root = div().id(id).css(listPfItem)
                    .add(container = div().css(listPfContainer).get())
                    .get();

        }
        if (checkbox) {
            container.appendChild(div().css(listPfSelect)
                    .add(this.checkbox = input(InputType.checkbox)
                            .on(click, event -> {
                                HTMLInputElement element = (HTMLInputElement) event.target;
                                listView.selectListItem(ListItem.this, element.checked);
                            })
                            .get())
                    .get());
        } else {
            this.checkbox = null;
            bind(root, click, event -> listView.selectListItem(this, true));
        }

        // status icon, title, description, additional info
        HTMLElement content;
        HTMLElement contentWrapper;
        HTMLElement mainContent;
        HTMLElement title;
        container.appendChild(content = div().css(listPfContent, listPfContentFlex).get());
        if (display.getStatusElement() != null) {
            content.appendChild(div().css(listPfLeft)
                    .add(display.getStatusElement())
                    .get());
        } else if (display.getStatusIcon() != null) {
            HTMLElement status;
            content.appendChild(div().css(listPfLeft)
                    .add(status = span().css(listPfIcon, listPfIconBordered, listPfIconSmall)
                            .get())
                    .get());
            //noinspection UnstableApiUsage
            List<String> classes = Splitter.on(' ')
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(display.getStatusIcon());
            status.classList.add(classes.toArray(new String[0]));
        }
        content.appendChild(contentWrapper = div().css(listPfContentWrapper)
                .add(mainContent = div().css(listPfMainContent, listHalMainContent)
                        .style("flex-basis:" + contentWidths[0]) //NON-NLS
                        .add(title = div().css(listPfTitle)
                                .get())
                        .get())
                .get());
        if (display.getTitleElements() != null) {
            for (HTMLElement element : display.getTitleElements()) {
                title.appendChild(element);
            }
        } else if (display.getTitleHtml() != null) {
            title.appendChild(h(3).innerHtml(display.getTitleHtml()).get());
        } else if (display.getTitle() != null) {
            title.appendChild(h(3, display.getTitle()).get());
        } else {
            title.appendChild(h(3, Names.NOT_AVAILABLE).get());
        }

        // logic to display the description content
        if (display.getDescriptionElements() != null ||
                display.getDescriptionHtml() != null ||
                display.getDescription() != null) {
            HTMLElement description;
            mainContent.appendChild(description = div().css(listPfDescription)
                    .get());

            HTMLDivElement textContentElem = div().id(idLongPanel).css(listPfContainer, listPfContainerLong)
                    .get();
            if (display.hideDescriptionWhenLarge()) {
                description.textContent = CONSTANTS.messageLarge();
            }

            if (display.getDescriptionElements() != null) {

                if (display.hideDescriptionWhenLarge()) {
                    for (HTMLElement element : display.getDescriptionElements()) {
                        textContentElem.appendChild(element);
                    }
                } else {
                    for (HTMLElement element : display.getDescriptionElements()) {
                        description.appendChild(element);
                    }
                }

            } else if (display.getDescriptionHtml() != null) {

                if (display.hideDescriptionWhenLarge()) {
                    textContentElem.innerHTML = display.getDescriptionHtml().asString();
                } else {
                    description.innerHTML = display.getDescriptionHtml().asString();
                }

            } else if (display.getDescription() != null) {

                if (display.hideDescriptionWhenLarge()) {
                    textContentElem.textContent = display.getDescription();
                } else {
                    description.textContent = display.getDescription();
                }
            }

            if (display.hideDescriptionWhenLarge()) {
                root.appendChild(textContentElem);
            }
        }
        if (display.getAdditionalInfoElements() != null ||
                display.getAdditionalInfoHtml() != null ||
                display.getAdditionalInfo() != null) {
            HTMLElement additionalInfo;
            contentWrapper.appendChild(additionalInfo = div().css(listPfAdditionalContent, listHalAdditionalContent)
                    .style("flex-basis:" + contentWidths[1]) //NON-NLS
                    .get());
            if (display.getAdditionalInfoElements() != null) {
                for (HTMLElement element : display.getAdditionalInfoElements()) {
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
                    .get());
            int index = 0;
            HTMLUListElement ul = null;
            for (ItemAction<T> action : allowedActions) {
                HTMLElement actionElement;
                String actionId = Ids.build(this.id, action.id);

                EventCallbackFn<MouseEvent> eventHandler = event -> action.handler.execute(item);
                if (index == 0) {
                    // first action is a button
                    actionsContainer.appendChild(actionElement = button()
                            .id(actionId)
                            .css(btn, btnDefault)
                            .textContent(action.title)
                            .on(click, eventHandler)
                            .get());

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
                                                .get())
                                        .get());
                    }
                    //noinspection ConstantConditions
                    ul.appendChild(actionElement = li()
                            .add(a().css(clickable)
                                    .textContent(action.title)
                                    .on(click, eventHandler))
                            .get());
                }

                this.actions.put(action.id, actionElement);
                index++;
            }
        }
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    void enableAction(String actionId) {
        HTMLElement action = actions.get(actionId);
        if (action != null) {
            action.classList.remove(disabled);
        }
    }

    void disableAction(String actionId) {
        HTMLElement action = actions.get(actionId);
        if (action != null) {
            action.classList.add(disabled);
        }
    }
}
