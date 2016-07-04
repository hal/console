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
package org.jboss.hal.client.runtime.logging;

import javax.annotation.PostConstruct;

import com.google.common.base.Strings;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.events.KeyboardEvent;
import elemental.events.KeyboardEvent.KeyCode;
import elemental.html.InputElement;
import org.jboss.gwt.elemento.core.DataElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.EventHandler;
import org.jboss.gwt.elemento.core.Templated;
import org.jboss.hal.ballroom.Clipboard;
import org.jboss.hal.ballroom.Tooltip;
import org.jboss.hal.ballroom.editor.AceEditor;
import org.jboss.hal.ballroom.editor.Options;
import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.core.ui.Skeleton;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static elemental.css.CSSStyleDeclaration.Unit.PX;
import static java.lang.Math.max;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.gwt.elemento.core.EventType.keyup;
import static org.jboss.hal.core.ui.Skeleton.MARGIN_BIG;
import static org.jboss.hal.resources.CSS.logFileLoading;

/**
 * @author Harald Pehl
 */
@Templated("LogFileView.html#root")
public abstract class LogFileView extends PatternFlyViewImpl implements LogFilePresenter.MyView {

    // @formatter:off
    public static LogFileView create(final Resources resources) {
        return new Templated_LogFileView(resources);
    }

    public abstract Resources resources();
    // @formatter:on


    private static final int MIN_HEIGHT = 70;

    private AceEditor editor;
    private LogFilePresenter presenter;

    @DataElement Element header;
    @DataElement Element logFileControls;
    @DataElement InputElement searchInput;
    @DataElement Element clear;
    @DataElement Element status;
    @DataElement Element tailMode;
    @DataElement Element copyToClipboard;
    @DataElement Element editorContainer;
    @DataElement Element editorPlaceholder;
    @DataElement Element loading;

    @PostConstruct
    void init() {
        Elements.setVisible(clear, false);

        Options editorOptions = new Options();
        editorOptions.readOnly = true;
        editorOptions.showGutter = true;
        editorOptions.showLineNumbers = true;
        editorOptions.showPrintMargin = false;
        editor = new AceEditor(Ids.LOG_FILE_EDITOR, editorOptions);
        registerAttachable(editor);
        editorContainer.replaceChild(editor.asElement(), editorPlaceholder);

        Clipboard clipboard = new Clipboard(copyToClipboard);
        clipboard.onCopy(event -> copyToClipboard(event.client));
    }

    private void copyToClipboard(Clipboard clipboard) {
        String value = editor.getEditor().getSession().getValue();
        if (!Strings.isNullOrEmpty(value)) {
            clipboard.setText(value);
            Tooltip tooltip = Tooltip.element(copyToClipboard);
            tooltip.hide()
                    .setTitle(resources().constants().copied())
                    .show()
                    .onHide(() -> tooltip.setTitle(resources().constants().copyToClipboard()));
            Browser.getWindow().setTimeout(tooltip::hide, 1000);
        }
    }

    @Override
    public void attach() {
        super.attach();

        editor.getEditor().$blockScrolling = 1;
        editor.getEditor().setTheme("ace/theme/logfile"); //NON-NLS
        editor.getEditor().getSession().setMode("ace/mode/logfile"); //NON-NLS

        Browser.getWindow().setOnresize(event -> adjustHeight());
        adjustHeight();
    }

    private void adjustHeight() {
        int height = max(Skeleton.applicationHeight() - 2 * MARGIN_BIG - 1, MIN_HEIGHT);
        height -= (header.getOffsetHeight() + logFileControls.getOffsetHeight() + 20);
        editor.asElement().getStyle().setHeight(height, PX);
        editor.getEditor().resize();
    }

    @Override
    public void setPresenter(final LogFilePresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void loading() {
        int top = loading.getOffsetHeight() + editor.asElement().getOffsetHeight() / 2;
        loading.getStyle().setTop(-1 * top, PX);
        editorContainer.getClassList().add(logFileLoading);
    }

    @Override
    public void show(final LogFile logFile, final String content) {
        editorContainer.getClassList().remove(logFileLoading);
        header.setTextContent(logFile.getFilename());
        searchInput.setValue("");
        status.setTextContent("");
        editor.getEditor().getSession().setValue(content);
        int lines = editor.getEditor().getSession().getLength();
        editor.getEditor().gotoLine(lines, 0, false);
    }

    @EventHandler(element = "searchInput", on = keyup)
    void onSearchInput(KeyboardEvent event) {
        Elements.setVisible(clear, !Strings.isNullOrEmpty(searchInput.getValue()));
        if (event.getKeyCode() == KeyCode.ENTER) {
            onSearch();
        }
    }

    @EventHandler(element = "clear", on = click)
    void onClear() {
        searchInput.setValue("");
        searchInput.focus();
        Elements.setVisible(clear, false);
    }

    @EventHandler(element = "search", on = click)
    void onSearch() {
        Browser.getWindow().alert(Names.NYI);
    }

    @EventHandler(element = "previous", on = click)
    void onPrevious() {
        Browser.getWindow().alert(Names.NYI);
    }

    @EventHandler(element = "next", on = click)
    void onNext() {
        Browser.getWindow().alert(Names.NYI);
    }

    @EventHandler(element = "refresh", on = click)
    void onRefresh() {
        presenter.load();
    }

    @EventHandler(element = "download", on = click)
    void onDownload() {
        presenter.download();
    }

    @EventHandler(element = "external", on = click)
    void onExternal() {
        Browser.getWindow().alert(Names.NYI);
    }
}
