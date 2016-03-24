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
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.editor.AceEditor;
import org.jboss.hal.ballroom.editor.Options;
import org.jboss.hal.ballroom.layout.LayoutBuilder;
import org.jboss.hal.ballroom.listview.ItemAction;
import org.jboss.hal.ballroom.listview.ItemDisplay;
import org.jboss.hal.ballroom.listview.ListView;
import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.core.ui.Skeleton;
import org.jboss.hal.dmr.macro.Macro;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static elemental.css.CSSStyleDeclaration.Unit.PX;
import static java.lang.Math.max;
import static java.util.Collections.singletonList;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.core.ui.Skeleton.MARGIN_BIG;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public class MacroEditorView extends PatternFlyViewImpl implements MacroEditorPresenter.MyView {

    private static final int MIN_HEIGHT = 70;

    private final Resources resources;
    private final EmptyState empty;
    private final ListView<Macro> macroList;
    private final AceEditor editor;
    private final Iterable<Element> elements;
    private MacroEditorPresenter presenter;

    @Inject
    public MacroEditorView(Resources resources) {
        this.resources = resources;

        empty = new EmptyState.Builder(resources.constants().noMacros())
                .icon(CSS.fontAwesome("dot-circle-o"))
                .description(resources.messages().noMacrosDescription(resources.constants().startMacro()))
                .build();
        empty.asElement().getClassList().add(noMacros);

        macroList = new ListView<>(Ids.MACRO_LIST, macro -> new ItemDisplay<Macro>() {
            @Override
            public String getTitle() {
                return macro.getName();
            }

            @Override
            public String getDescription() {
                return macro.getDescription();
            }

            @Override
            public boolean stacked() {
                return true;
            }

            @Override
            public List<ItemAction<Macro>> actions() {
                return Arrays.asList(
                        new ItemAction<Macro>(resources.constants().play(), macro -> presenter.play(macro)),
                        new ItemAction<Macro>(resources.constants().rename(), macro -> presenter.rename(macro)),
                        new ItemAction<Macro>(resources.constants().remove(), macro -> presenter.remove(macro)));
            }
        });
        macroList.onSelect(this::loadMacro);
        macroList.asElement().getClassList().add(CSS.macroList);

        Options editorOptions = new Options();
        editorOptions.readOnly = true;
        editorOptions.showPrintMargin = false;
        editor = new AceEditor(Ids.MACRO_EDITOR, editorOptions);
        // @formatter:off
        Element editorContainer = new Elements.Builder()
            .div().css(macroEditor)
                .button().css(btn, btnDefault, copy).on(click, event -> copyToClipboard())
                    .span().css(fontAwesome("clipboard")).end()
                .end()
                .add(editor.asElement())
            .end()
        .build();
        // @formatter:on

        // @formatter:off
        elements = new LayoutBuilder()
            .row()
                .column(0, 4).add(macroList.asElement()).end()
                .column(0, 8).add(editorContainer).end()
            .end()
        .elements();
        // @formatter:on

        registerAttachable(editor);
        initElements(Iterables.concat(singletonList(empty.asElement()), elements));
    }

    @Override
    public void attach() {
        super.attach();
        Browser.getWindow().setOnresize(event -> adjustHeight());
        adjustHeight();
    }

    private void adjustHeight() {
        int height = max(Skeleton.applicationHeight() - 2 * MARGIN_BIG - 1, MIN_HEIGHT);
        macroList.asElement().getStyle().setHeight(height, PX);
        editor.asElement().getStyle().setHeight(height, PX);
        editor.getEditor().resize();
    }

    @Override
    public void setPresenter(final MacroEditorPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void empty() {
        Elements.setVisible(empty.asElement(), true);
        for (Element element : elements) {
            Elements.setVisible(element, false);
        }
    }

    @Override
    public void setMacros(Iterable<Macro> macros) {
        Elements.setVisible(empty.asElement(), false);
        for (Element element : elements) {
            Elements.setVisible(element, true);
        }
        macroList.setItems(macros);
    }

    @Override
    public void selectMacro(final Macro macro) {
        macroList.selectItem(macro);
    }

    private void loadMacro(Macro macro) {
        String operations = Joiner.on('\n').join(Lists.transform(macro.getOperations(), Operation::asCli));
        editor.getEditor().getSession().setValue(operations);
    }

    private void copyToClipboard() {
        editor.getEditor().selectAll();
        editor.getEditor().focus();
    }
}
