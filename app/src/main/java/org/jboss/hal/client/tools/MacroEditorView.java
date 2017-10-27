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

import java.util.List;

import javax.inject.Inject;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.hal.ballroom.Clipboard;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.Skeleton;
import org.jboss.hal.ballroom.Tooltip;
import org.jboss.hal.ballroom.dataprovider.DataProvider;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.editor.AceEditor;
import org.jboss.hal.ballroom.editor.Options;
import org.jboss.hal.ballroom.listview.ItemAction;
import org.jboss.hal.ballroom.listview.ItemDisplay;
import org.jboss.hal.ballroom.listview.ItemRenderer;
import org.jboss.hal.ballroom.listview.ListView;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.macro.Macro;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;

import static elemental2.dom.DomGlobal.setTimeout;
import static elemental2.dom.DomGlobal.window;
import static java.lang.Math.max;
import static java.util.Arrays.asList;
import static org.jboss.gwt.elemento.core.Elements.button;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.elements;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.ballroom.Skeleton.MARGIN_BIG;
import static org.jboss.hal.resources.CSS.*;

public class MacroEditorView extends HalViewImpl implements MacroEditorPresenter.MyView {

    private static final String PLAY_ACTION = "play";
    private static final String RENAME_ACTION = "rename";
    private static final String REMOVE_ACTION = "remove";
    private static final int MIN_HEIGHT = 70;

    private final Resources resources;
    private final EmptyState empty;
    private final DataProvider<Macro> dataProvider;
    private final ListView<Macro> macroList;
    private final AceEditor editor;
    private final HTMLButtonElement copyToClipboard;
    private final HTMLElement row;
    private MacroEditorPresenter presenter;

    @Inject
    public MacroEditorView(Resources resources) {
        this.resources = resources;

        dataProvider = new DataProvider<>(Macro::getName, false);
        dataProvider.onSelect(this::loadMacro);

        empty = new EmptyState.Builder(Ids.MACRO_EMPTY, resources.constants().noMacros())
                .icon(CSS.fontAwesome("dot-circle-o"))
                .description(resources.messages().noMacrosDescription(resources.constants().startMacro()))
                .build();
        empty.asElement().classList.add(noMacros);

        ItemRenderer<Macro> itemRenderer = macro -> new ItemDisplay<Macro>() {
            @Override
            public String getTitle() {
                return macro.getName();
            }

            @Override
            public String getDescription() {
                return macro.getDescription();
            }

            @Override
            public HasElements getAdditionalInfoElements() {
                return elements().add(div()
                        .add(span().css(pfIcon("image"), marginRight5))
                        .add(span().textContent(resources.messages().operations(macro.getOperationCount()))));
            }

            @Override
            public List<ItemAction<Macro>> actions() {
                return asList(
                        new ItemAction<Macro>(PLAY_ACTION, resources.constants().play(),
                                macro -> presenter.play(macro)),
                        new ItemAction<Macro>(RENAME_ACTION, resources.constants().rename(),
                                macro -> presenter.rename(macro)),
                        new ItemAction<Macro>(REMOVE_ACTION, resources.constants().remove(),
                                macro -> DialogFactory.showConfirmation(
                                        resources.messages().removeConfirmationTitle(macro.getName()),
                                        resources.messages().removeConfirmationQuestion(macro.getName()),
                                        () -> presenter.remove(macro))));
            }
        };
        macroList = new ListView<>(Ids.MACRO_LIST, dataProvider, itemRenderer, true, false);
        macroList.asElement().classList.add(CSS.macroList);
        dataProvider.addDisplay(macroList);

        Options editorOptions = new Options();
        editorOptions.readOnly = true;
        editorOptions.showGutter = true;
        editorOptions.showLineNumbers = true;
        editorOptions.showPrintMargin = false;
        editor = new AceEditor(Ids.MACRO_EDITOR, editorOptions);

        HTMLElement editorContainer = div().css(macroEditor)
                .add(copyToClipboard = button().css(btn, btnDefault, copy)
                        .data(UIConstants.TOGGLE, UIConstants.TOOLTIP)
                        .data(UIConstants.PLACEMENT, "left") //NON-NLS
                        .title(resources.constants().copyToClipboard())
                        .add(span().css(fontAwesome("clipboard")))
                        .asElement())
                .add(editor)
                .asElement();
        Clipboard clipboard = new Clipboard(copyToClipboard);
        clipboard.onCopy(event -> copyToClipboard(event.client));

        row = row()
                .add(column(4)
                        .add(macroList))
                .add(column(8)
                        .add(editorContainer))
                .asElement();

        registerAttachable(editor);
        initElements(asList(empty.asElement(), row));
    }

    @Override
    public void attach() {
        super.attach();
        adjustHeight();
        adjustEditorHeight();
        window.onresize = event -> {
            adjustEditorHeight();
            return null;
        };
    }

    @Override
    public void detach() {
        super.detach();
        window.onresize = null;
    }

    private void adjustHeight() {
        int offset = Skeleton.applicationOffset() + 2 * MARGIN_BIG + 1;
        macroList.asElement().style.height = vh(offset);
    }

    private void adjustEditorHeight() {
        int height = max(Skeleton.applicationHeight() - 2 * MARGIN_BIG - 1, MIN_HEIGHT);
        editor.asElement().style.height = CSS.height(px(height));
        editor.getEditor().resize();
    }

    @Override
    public void setPresenter(final MacroEditorPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void empty() {
        Elements.setVisible(empty.asElement(), true);
        Elements.setVisible(row, false);
    }

    @Override
    public void setMacros(Iterable<Macro> macros) {
        Elements.setVisible(empty.asElement(), false);
        Elements.setVisible(row, true);
        dataProvider.update(macros);
    }

    @Override
    public void selectMacro(final Macro macro) {
        dataProvider.select(macro, true);
    }

    @Override
    public void enableMacro(final Macro macro) {
        macroList.enableAction(macro, PLAY_ACTION);
        macroList.enableAction(macro, RENAME_ACTION);
        macroList.enableAction(macro, REMOVE_ACTION);
    }

    @Override
    public void disableMacro(final Macro macro) {
        macroList.disableAction(macro, PLAY_ACTION);
        macroList.disableAction(macro, RENAME_ACTION);
        macroList.disableAction(macro, REMOVE_ACTION);
    }

    private void loadMacro(Macro macro) {
        editor.getEditor().getSession().setValue(macro.asCli());
    }

    private void copyToClipboard(Clipboard clipboard) {
        if (dataProvider.getSelectionInfo().getSingleSelection() != null) {
            clipboard.setText(dataProvider.getSelectionInfo().getSingleSelection().asCli());
            Tooltip tooltip = Tooltip.element(copyToClipboard);
            tooltip.hide()
                    .setTitle(resources.constants().copied())
                    .show()
                    .onHide(() -> tooltip.setTitle(resources.constants().copyToClipboard()));
            setTimeout((o) -> tooltip.hide(), 1000);
        }
    }
}
