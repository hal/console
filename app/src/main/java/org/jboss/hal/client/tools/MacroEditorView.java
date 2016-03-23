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
package org.jboss.hal.client.tools;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.editor.AceEditor;
import org.jboss.hal.ballroom.layout.LayoutBuilder;
import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.dmr.macro.Macro;
import org.jboss.hal.dmr.macro.Macros;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import javax.inject.Inject;

import static java.util.Arrays.asList;

/**
 * @author Harald Pehl
 */
public class MacroEditorView extends PatternFlyViewImpl implements MacroEditorPresenter.MyView {

    private final EmptyState empty;
    private final AceEditor editor;
    private final Element console;
    private final Element layout;

    @Inject
    public MacroEditorView(Resources resources) {
        empty = new EmptyState.Builder(resources.constants().noMacros())
                .description(resources.messages().noMacrosDescription(resources.constants().startMacro()))
                .build();
        editor = new AceEditor(Ids.MACRO_EDITOR);
        console = Browser.getDocument().createElement("pre");
        layout = new LayoutBuilder()
                .row()
                .column(0, 4).end()
                .column(0, 8).add(editor.asElement()).end()
                .end()
                .row()
                .column().add(console).end()
                .end()
                .build();

        registerAttachable(editor);
        initElements(asList(empty.asElement(), layout));
    }

    @Override
    public void setMacros(Macros macros) {

    }

    @Override
    public void selectMacro(final Macro macro) {
        String operations = Joiner.on('\n').join(Lists.transform(macro.getOperations(), Operation::asCli));
        editor.getEditor().getSession().setValue(operations);
    }
}
