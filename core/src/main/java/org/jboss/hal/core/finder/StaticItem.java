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
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextAware;
import org.jboss.hal.resources.Constants;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public class StaticItem implements FinderItem, SecurityContextAware {

    public static class Builder {

        private final String id;
        private final String title;
        private final boolean folder;
        private String subtitle;
        private SelectCallback<StaticItem> selectCallback;
        private PreviewCallback<StaticItem> previewCallback;
        private final List<ActionStruct<StaticItem>> actions;

        public Builder(final String id, final String title, final boolean folder) {
            this.id = id;
            this.title = title;
            this.folder = folder;
            this.actions = new ArrayList<>();
        }

        public Builder subtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        public Builder selectCallback(SelectCallback<StaticItem> selectCallback) {
            this.selectCallback = selectCallback;
            return this;
        }

        public Builder preview(PreviewCallback<StaticItem> previewCallback) {
            this.previewCallback = previewCallback;
            return this;
        }

        public Builder itemAction(String title, ItemAction<StaticItem> action) {
            this.actions.add(new ActionStruct<>(title, action));
            return this;
        }

        public Builder itemAction(Element content, ItemAction<StaticItem> action) {
            this.actions.add(new ActionStruct<>(content, action));
            return this;
        }

        public StaticItem build() {
            return new StaticItem(this);
        }
    }


    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final String FOLDER_ELEMENT = "folderElement";
    private static final String BUTTON_CONTAINER = "buttonContainer";

    private final String id;
    private final SelectCallback<StaticItem> selectCallback;
    private final Element root;
    private final Element folderElement;
    private final Element buttonContainer;

    StaticItem(final Builder builder) {
        this.id = builder.id;
        this.selectCallback = builder.selectCallback;

        Elements.Builder eb = new Elements.Builder().li().id(id).span().css(content).title(builder.title);
        if (builder.subtitle != null) {
            // @formatter:off
            eb.span().innerText(builder.title).end()
              .start("small").css(subtitle).innerText(builder.subtitle).end();
            // @formatter:on
        } else {
            eb.innerText(builder.title);
        }
        eb.end(); // </span>

        if (builder.folder) {
            eb.span().css(folder, fontAwesome("angle-right")).rememberAs(FOLDER_ELEMENT).end();
        }
        if (!builder.actions.isEmpty()) {
            if (builder.actions.size() == 1) {
                ActionStruct<StaticItem> action = builder.actions.get(0);
                eb.button()
                        .css(btn, btnFinder)
                        .innerText(action.title)
                        .on(click, event -> action.itemAction.execute(this))
                        .rememberAs(BUTTON_CONTAINER)
                        .end();
            } else {
                boolean firstAction = true;
                boolean ulCreated = false;
                eb.div().css(btnGroup, pullRight).rememberAs(BUTTON_CONTAINER);
                for (ActionStruct<StaticItem> action : builder.actions) {
                    if (firstAction) {
                        // @formatter:off
                        eb.button()
                                .css(btn, btnFinder)
                                .innerText(action.title)
                                .on(click, event -> action.itemAction.execute(this))
                        .end();
                        eb.button()
                                .css(btn, btnFinder, dropdownToggle)
                                .data("toggle", "dropdown")
                                .aria("haspopup", "true")
                                .aria("expanded", "false")
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
                                .on(click, event -> action.itemAction.execute(this))
                                .end().end();
                    }
                }
                eb.end().end(); // </ul> && </div>
            }
        }
        eb.end(); // </li>

        root = eb.build();
        folderElement = builder.folder ? eb.referenceFor(FOLDER_ELEMENT) : null;
        buttonContainer = builder.actions.isEmpty() ? null : eb.referenceFor(BUTTON_CONTAINER);
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

            if (builder.selectCallback != null) {
                List children = builder.selectCallback.onSelect(this);
                // TODO children need to implement an interface
            }

            if (builder.previewCallback != null) {
                PreviewContent content = builder.previewCallback.onPreview(this);
                Finder.preview(content);
            }
        });
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof StaticItem)) { return false; }

        StaticItem that = (StaticItem) o;
        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "StaticItem(" + id + ")";
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Element asElement() {
        return root;
    }

    @Override
    public void onSecurityContextChange(final SecurityContext securityContext) {

    }
}
