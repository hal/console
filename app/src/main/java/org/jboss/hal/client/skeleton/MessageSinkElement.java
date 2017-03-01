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
package org.jboss.hal.client.skeleton;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Message;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
class MessageSinkElement implements IsElement {

    private static final String ICON_CONTAINER = "iconContainer";

    private final Element root;

    MessageSinkElement(final MessageSink messageSink, final Message message, final Resources resources) {
        String id = Ids.uniqueId();
        String dropdownId = Ids.build("dropdown", id);

        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .div().id(id).css(drawerPfNotification, unread)
                .div().css(dropdown, pullRight, dropdownKebabPf)
                    .button().id(dropdownId)
                            .css(btn, btnLink, dropdownToggle)
                            .data(UIConstants.TOGGLE, UIConstants.DROPDOWN)
                            .aria(UIConstants.HAS_POPUP, UIConstants.TRUE)
                            .aria(UIConstants.EXPANDED, UIConstants.FALSE)
                        .span().css(CSS.fontAwesome("ellipsis-v")).end()
                    .end()
                    .ul().css(dropdownMenu, dropdownMenuRight)
                            .aria(UIConstants.LABELLED_BY, dropdownId)
                        .li()
                            .a().css(clickable)
                                .on(click, event -> view(message))
                                .textContent(resources.constants().view())
                            .end()
                        .end()
                        .li()
                            .a().css(clickable)
                                .on(click, event -> messageSink.remove(id))
                                .textContent(resources.constants().remove())
                            .end()
                        .end()
                    .end()
                .end()
                .span().css(pullLeft).rememberAs(ICON_CONTAINER).end()
                .span().css(drawerPfNotificationMessage).innerHtml(message.getMessage()).end()
                .span().css(drawerPfNotificationInfo)
                    .span().css(date).textContent(message.getDate()).end()
                    .span().css(time).textContent(message.getTime()).end()
                .end()
            .end();
        // @formatter:on

        Element iconContainer = builder.referenceFor(ICON_CONTAINER);
        String css = null;
        switch (message.getLevel()) {
            case ERROR:
                css = Icons.ERROR;
                break;
            case WARNING:
                css = Icons.WARNING;
                break;
            case INFO:
                css = Icons.INFO;
                break;
            case SUCCESS:
                css = Icons.OK;
                break;
        }
        iconContainer.setClassName(css + " " + pullLeft);
        root = builder.build();
    }

    @Override
    public Element asElement() {
        return root;
    }

    private void view(Message message) {
        root.getClassList().remove(unread);
        new MessageDialog(message).show();
    }
}
