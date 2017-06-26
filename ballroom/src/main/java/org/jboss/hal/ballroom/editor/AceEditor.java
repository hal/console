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
package org.jboss.hal.ballroom.editor;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLPreElement;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.resources.CSS;

import static org.jboss.gwt.elemento.core.Elements.pre;

/**
 * @author Harald Pehl
 */
public class AceEditor implements IsElement, Attachable {

    private static final String MODE_LIST_EXTENSION = "ace/ext/modelist";

    private final String id;
    private final Options options;
    private final HTMLPreElement preElement;
    private Editor editor;
    private ModeList modeList;

    public AceEditor(final String id) {
        this(id, null);
    }

    public AceEditor(final String id, final Options options) {
        this.id = id;
        this.options = options;
        this.preElement = pre().id(id).css(CSS.editor).asElement();
    }

    @Override
    public HTMLElement asElement() {
        return preElement;
    }

    @Override
    public void attach() {
        if (editor == null) {
            Ace.init();
            editor = Ace.edit(id);
            modeList = Ace.require(MODE_LIST_EXTENSION);
            if (options != null) {
                editor.setOptions(options);
                editor.$blockScrolling = null;
            }
        }
    }

    /**
     * Getter for the {@link Editor} instance.
     *
     * @return The editor instance
     *
     * @throws IllegalStateException if the editor wasn't initialized using {@link #attach()}
     */
    public Editor getEditor() {
        if (editor == null) {
            throw new IllegalStateException(unattached());
        }
        return editor;
    }

    private ModeList getModeList() {
        if (modeList == null) {
            throw new IllegalStateException(unattached());
        }
        return modeList;
    }

    @SuppressWarnings("HardCodedStringLiteral")
    private String unattached() {
        return "AceEditor('" + id + "') is not attached. Call AceEditor.attach() before using any of the editor methods!";
    }

    public void setModeFromPath(String path) {
        String mode = getModeList().getModeForPath(path).mode;
        getEditor().getSession().setMode(mode);
    }
}
