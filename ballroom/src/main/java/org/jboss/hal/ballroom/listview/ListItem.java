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
import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import elemental.dom.Element;
import elemental.html.InputElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.InputType;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.*;

/**
 * An item inside a {@link ListView}.
 *
 * @author Harald Pehl
 */
class ListItem<T> implements IsElement {

    private final Element root;
    private final Map<String, Element> actions;

    final String id;
    final T item;


    ListItem(final ListView<T> listView, final T item, final boolean checkbox, final ItemDisplay<T> display) {
        this.id = display.getId();
        this.item = item;
        this.actions = new HashMap<>();

        String css = listGroupItem;
        if (display.stacked()) {
            css = css + " " + listViewPfStacked;
        }
        Elements.Builder builder = new Elements.Builder().div().css(css).id(id);

        if (checkbox) {
            builder.div().css(listViewPfCheckbox).input(InputType.checkbox).on(click, event -> {
                InputElement element = (InputElement) event.getTarget();
                listView.select(ListItem.this, element.isChecked());
            }).end();
        }

        if (!display.actions().isEmpty()) {
            builder.div().css(listViewPfActions);
            int index = 0;
            for (Iterator<ItemAction<T>> iterator = display.actions().iterator(); iterator.hasNext(); ) {
                ItemAction<T> action = iterator.next();
                String actionId = Ids.build(this.id, action.id);

                if (index == 0) {
                    // first action is a button
                    builder.button()
                            .id(actionId)
                            .rememberAs(actionId)
                            .css(btn, btnDefault)
                            .on(click, event -> action.handler.execute(item))
                            .textContent(action.title)
                            .end();
                } else {
                    // remaining actions are inside the kebab menu
                    if (index == 1) {
                        String id = Ids.build(display.getId(), "kebab", "menu");
                        // @formatter:off
                        builder.div().css(dropdown, pullRight, dropdownKebabPf)
                            .button().css(btn, btnLink, dropdownToggle)
                                .id(id)
                                .data(UIConstants.TOGGLE, UIConstants.DROPDOWN)
                                .aria(UIConstants.HAS_POPUP, UIConstants.TRUE)
                                .aria(UIConstants.EXPANDED, UIConstants.TRUE)
                                .span().css(fontAwesome("ellipsis-v")).end()
                                .add("br")
                            .end()
                            .ul().css(dropdownMenu, dropdownMenuRight).aria(UIConstants.LABELLED_BY, id);
                        // @formatter:on
                    }
                    builder.li().rememberAs(actionId).a()
                            .css(clickable)
                            .on(click, (event -> action.handler.execute(item)))
                            .textContent(action.title)
                            .end().end();

                    if (!iterator.hasNext()) {
                        builder.end().end(); // </ul> && </div.dropdown>
                    }
                }

                actions.put(action.id, builder.referenceFor(actionId));
                index++;
            }
            builder.end();
        }

        builder.div().css(listViewPfMainInfo);
        if (display.status() != null) {
            builder.div().css(listViewPfLeft).add(display.status()).end();
        }

        builder.div().css(listViewPfBody).div().css(listViewPfDescription);

        // item.title == list view heading
        builder.div().css(listGroupItemHeading);
        if (display.getTitleElements() != null) {
            for (Element element : display.getTitleElements().asElements()) {
                builder.add(element);
            }
        } else {
            builder.textContent(display.getTitle());
        }
        builder.end(); // </div.listGroupItemHeading>

        // item.description == list view text
        builder.div().css(listGroupItemText);
        if (display.getDescriptionElements() != null) {
            for (Element element : display.getDescriptionElements().asElements()) {
                builder.add(element);
            }
        } else if (!Strings.isNullOrEmpty(display.getDescription())) {
            builder.textContent(display.getDescription());
        }
        builder.end(); // </div.listGroupItemText>
        builder.end(); // </div.listViewPfDescription>

        if (display.getAdditionalInfo() != null && !Iterables.isEmpty(display.getAdditionalInfo().asElements())) {
            builder.div().css(listViewPfAdditionalInfo);
            for (Element element : display.getAdditionalInfo().asElements()) {
                if (!element.getClassList().contains(listViewPfAdditionalInfoItem)) {
                    element.getClassList().add(listViewPfAdditionalInfoItem);
                }
                builder.add(element);
            }
            builder.end();
        }

        builder.end(); // </div.listViewPfBody>
        builder.end(); // </div.listViewPfMainInfo>

        root = builder.end().build();
        if (!checkbox) {
            root.setOnclick(event -> listView.select(this, true));
        }
    }

    @Override
    public Element asElement() {
        return root;
    }

    void enableAction(final String actionId) {
        Element action = actions.get(actionId);
        if (action != null) {
            action.getClassList().remove(disabled);
        }
    }

    void disableAction(final String actionId) {
        Element action = actions.get(actionId);
        if (action != null) {
            action.getClassList().add(disabled);
        }
    }
}
