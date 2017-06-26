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
import java.util.Map;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLUListElement;
import org.jboss.gwt.elemento.core.InputType;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.*;

/**
 * An item inside a {@link ListView}.
 *
 * @author Harald Pehl
 */
class ListItem<T> implements IsElement {

    private final HTMLElement root;
    private final Map<String, HTMLElement> actions;

    final String id;
    final T item;


    ListItem(final ListView<T> listView, final T item, final boolean checkbox, final ItemDisplay<T> display) {
        this.id = display.getId();
        this.item = item;
        this.actions = new HashMap<>();

        // root and checkbox
        String css = listGroupItem;
        if (display.stacked()) {
            css = css + " " + listViewPfStacked;
        }
        root = div().id(id).css(css).asElement();
        if (checkbox) {
            root.appendChild(div().css(listViewPfCheckbox)
                    .add(input(InputType.checkbox)
                            .on(click, event -> {
                                HTMLInputElement element = (HTMLInputElement) event.target;
                                listView.select(ListItem.this, element.checked);
                            }))
                    .asElement());
        } else {
            bind(root, click, event -> listView.select(this, true));
        }

        // actions
        if (!display.actions().isEmpty()) {
            HTMLElement actionsContainer = div().css(listViewPfActions).asElement();
            root.appendChild(actionsContainer);

            int index = 0;
            for (ItemAction<T> action : display.actions()) {
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
                    HTMLUListElement ul = null;
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

        // main info and status
        HTMLElement mainInfo = div().css(listViewPfMainInfo).asElement();
        root.appendChild(mainInfo);
        if (display.status() != null) {
            mainInfo.appendChild(div().css(listViewPfLeft)
                    .add(display.status())
                    .asElement());
        }

        // body
        //     description
        //         heading, text
        //     additional-info
        //         item, item, ...
        HTMLElement body = div().css(listViewPfBody).asElement();
        mainInfo.appendChild(body);

        HTMLElement description = div().css(listViewPfDescription).asElement();
        body.appendChild(description);

        HTMLElement heading = div().css(listGroupItemHeading).asElement();
        description.appendChild(heading);
        if (display.getTitleElements() != null) {
            for (HTMLElement element : display.getTitleElements().asElements()) {
                heading.appendChild(element);
            }
        } else if (!Strings.isNullOrEmpty(display.getTitle())) {
            heading.textContent = display.getTitle();
        }

        HTMLElement text = div().css(listGroupItemText).asElement();
        description.appendChild(text);
        if (display.getDescriptionElements() != null) {
            for (HTMLElement element : display.getDescriptionElements().asElements()) {
                text.appendChild(element);
            }
        } else if (!Strings.isNullOrEmpty(display.getDescription())) {
            text.textContent = display.getDescription();
        }

        // additional info
        if (display.getAdditionalInfo() != null && !Iterables.isEmpty(display.getAdditionalInfo().asElements())) {
            HTMLElement additionalInfos = div().css(listViewPfAdditionalInfo).asElement();
            body.appendChild(additionalInfos);

            for (HTMLElement additionalInfo : display.getAdditionalInfo().asElements()) {
                if (!additionalInfo.classList.contains(listViewPfAdditionalInfoItem)) {
                    additionalInfo.classList.add(listViewPfAdditionalInfoItem);
                }
                additionalInfos.appendChild(additionalInfo);
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
