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
package org.jboss.hal.ballroom.form;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.typeahead.Typeahead;

import java.util.List;

import static org.jboss.hal.ballroom.IdBuilder.build;
import static org.jboss.hal.ballroom.IdBuilder.uniquId;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public class ListItem extends AbstractFormItem<List<String>> {

    private ListElement listElement;
    private Element tagsContainer;

    public ListItem(final String name, final String label) {
        super(name, label);
    }

    @Override
    protected InputElement<List<String>> newInputElement() {
        listElement = new ListElement();
        listElement.setClassName(formControl + " " + tags);
        TagsManager.Bridge.element(listElement.asElement()).onRefresh((event, cst) -> {
            List<String> value = Splitter.on(',')
                    .trimResults()
                    .omitEmptyStrings()
                    .splitToList(cst);
            setModified(true);
            setUndefined(value.isEmpty());
            signalChange(value);
        });
        return listElement;
    }

    @Override
    void assembleUI() {
        super.assembleUI();

        //noinspection DuplicateStringLiteralInspection
        tagsContainer = new Elements.Builder().div()
                .id(build("tags", "container", uniquId()))
                .css(tagManagerContainer)
                .end()
                .build();
        inputContainer.insertBefore(tagsContainer, errorText);
    }

    @Override
    public void registerSuggestHandler(final SuggestHandler suggestHandler) {
        super.registerSuggestHandler(suggestHandler);
        if (suggestHandler instanceof Typeahead) {
            Typeahead typeahead = (Typeahead) suggestHandler;
            Typeahead.Bridge.select(getId(EDITING)).onSelect((event, data) -> {
                TagsManager.Bridge.element(listElement.asElement()).addTag(typeahead.getDataset().display.render(data));
            });
            TagsManager.Bridge.element(listElement.asElement()).onRefresh((event, cst) -> {
                Typeahead.Bridge.select(getId(EDITING)).setValue("");
                Typeahead.Bridge.select(getId(EDITING)).close();
            });
        }
    }

    @Override
    public void attach() {
        super.attach();
        TagsManager.Options options = TagsManager.Defaults.get();
        options.tagsContainer = "#" + tagsContainer.getId();
        TagsManager.Bridge.element(listElement.asElement()).tagsManager(options);
    }

    @Override
    public boolean supportsExpressions() {
        return false;
    }


    static class ListElement extends InputElement<List<String>> {

        final elemental.html.InputElement element;

        ListElement() {
            element = Browser.getDocument().createInputElement();
            element.setType("text"); //NON-NLS
        }

        @Override
        public List<String> getValue() {
            return TagsManager.Bridge.element(asElement()).getTags();
        }

        @Override
        public void setValue(final List<String> value) {
            TagsManager.Bridge.element(asElement()).setTags(value);
        }

        @Override
        public void clearValue() {
            TagsManager.Bridge.element(asElement()).removeAll();
        }

        @Override
        public int getTabIndex() {
            return element.getTabIndex();
        }

        @Override
        public void setAccessKey(final char c) {
            element.setAccessKey(String.valueOf(c));
        }

        @Override
        public void setFocus(final boolean b) {
            if (b) {
                element.focus();
            } else {
                element.blur();
            }
        }

        @Override
        public void setTabIndex(final int i) {
            element.setTabIndex(i);
        }

        @Override
        public boolean isEnabled() {
            return !element.isDisabled();
        }

        @Override
        public void setEnabled(final boolean b) {
            element.setDisabled(!b);
        }

        @Override
        public void setName(final String s) {
            element.setName(s);
        }

        @Override
        public String getName() {
            return element.getName();
        }

        @Override
        public String getText() {
            return Joiner.on(',').join(getValue());
        }

        @Override
        public void setText(final String s) {
            // not supported
        }

        @Override
        public Element asElement() {
            return element;
        }
    }
}
