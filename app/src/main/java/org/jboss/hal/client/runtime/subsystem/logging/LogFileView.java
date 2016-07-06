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
package org.jboss.hal.client.runtime.subsystem.logging;

import java.util.Date;
import javax.annotation.PostConstruct;

import com.google.common.base.Strings;
import elemental.client.Browser;
import elemental.dom.Document;
import elemental.dom.Element;
import elemental.events.KeyboardEvent;
import elemental.events.KeyboardEvent.KeyCode;
import elemental.html.InputElement;
import org.jboss.gwt.elemento.core.DataElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.EventHandler;
import org.jboss.gwt.elemento.core.Templated;
import org.jboss.hal.ballroom.Clipboard;
import org.jboss.hal.ballroom.Format;
import org.jboss.hal.ballroom.Tooltip;
import org.jboss.hal.ballroom.editor.AceEditor;
import org.jboss.hal.ballroom.editor.Options;
import org.jboss.hal.ballroom.form.SwitchBridge;
import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.core.ui.Skeleton;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;

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
    public static LogFileView create(final StatementContext statementContext, final LogFiles logFiles,
            final Resources resources) {
        return new Templated_LogFileView(statementContext, logFiles, resources);
    }

    public abstract StatementContext statementContext();
    public abstract LogFiles logFiles();
    public abstract Resources resources();
    // @formatter:on


    private static final int MIN_HEIGHT = 70;

    private AceEditor editor;
    private LogFilePresenter presenter;

    @DataElement Element header;
    @DataElement Element logFileControls;
    @DataElement InputElement searchInput;
    @DataElement Element clearSearch;
    @DataElement Element status;
    @DataElement Element tailMode;
    @DataElement Element copyToClipboard;
    @DataElement Element download;
    @DataElement Element external;
    @DataElement Element editorContainer;
    @DataElement Element editorPlaceholder;
    @DataElement Element loading;


    // ------------------------------------------------------ init & ui

    @PostConstruct
    void init() {
        Elements.setVisible(clearSearch, false);

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

        SwitchBridge.Bridge.element(tailMode).onChange((event, state) -> presenter.toggleTailMode(state));

        editor.getEditor().$blockScrolling = 1;
        editor.getEditor().setTheme("ace/theme/logfile"); //NON-NLS
        editor.getEditor().getSession().setMode("ace/mode/logfile"); //NON-NLS

        Browser.getWindow().setOnresize(event -> adjustHeight());
        adjustHeight();
    }

    private void adjustHeight() {
        int height = presenter.isExternal() ? Browser.getWindow().getInnerHeight() : Skeleton.applicationHeight();
        height -= 2 * MARGIN_BIG;
        height -= (header.getOffsetHeight() + logFileControls.getOffsetHeight() + 20);
        height = max(height, MIN_HEIGHT);
        editor.asElement().getStyle().setHeight(height, PX);
        editor.getEditor().resize();
    }

    @Override
    public void setPresenter(final LogFilePresenter presenter) {
        this.presenter = presenter;
    }


    // ------------------------------------------------------ API

    @Override
    public void externalMode() {
        Document document = Browser.getDocument();
        Element body = document.getBody();
        Element element = document.querySelector("body > nav.navbar"); //NON-NLS
        if (element != null) {
            body.removeChild(element);
        }
        element = document.querySelector("body > footer.footer"); //NON-NLS
        if (element != null) {
            body.removeChild(element);
        }
        body.getStyle().setPadding(0, PX);
        Elements.setVisible(external, false);
    }

    @Override
    public void loading() {
        status.setTextContent(resources().constants().loadingPleaseWait());
        status.setTitle(resources().constants().loadingPleaseWait());
        int top = loading.getOffsetHeight() + editor.asElement().getOffsetHeight() / 2;
        loading.getStyle().setTop(-1 * top, PX);
        editorContainer.getClassList().add(logFileLoading);
    }

    @Override
    public void show(final LogFile logFile, int lines, final String content) {
        statusUpdate(lines);
        StringBuilder builder = new StringBuilder();
        if (presenter.isExternal()) {
            builder.append(statementContext().selectedHost())
                    .append(" / ")
                    .append(statementContext().selectedServer())
                    .append(" / ");
        }
        builder.append(logFile.getFilename());
        header.setTextContent(builder.toString());
        download.setAttribute(UIConstants.DOWNLOAD, logFile.getFilename());
        download.setAttribute(UIConstants.HREF, logFiles().downloadUrl(logFile.getFilename()));
        external.setAttribute(UIConstants.HREF, logFiles().externalUrl(logFile.getFilename()));
        external.setAttribute(UIConstants.TARGET, logFiles().target(logFile.getFilename()));

        editor.getEditor().getSession().setValue(content);
        editor.getEditor().gotoLine(lines, 0, false);
    }

    @Override
    public void refresh(final int lines, final String content) {
        statusUpdate(lines);
        editor.getEditor().getSession().setValue(content);
        editor.getEditor().gotoLine(lines, 0, false);
    }

    @Override
    public int visibleLines() {
        int lineHeight = 15;
        Element lineElement = Browser.getDocument()
                .querySelector("#" + Ids.LOG_FILE_EDITOR + " .ace_text-layer .ace_line"); //NON-NLS
        if (lineElement != null) {
            lineHeight = lineElement.getOffsetHeight();
        }
        return editor.asElement().getOffsetHeight() / lineHeight;
    }

    private void statusUpdate(int lines) {
        status.setTextContent(resources().messages().logFileStatus(lines, Format.time(new Date())));
        status.setTitle(resources().messages().logFileStatus(lines, Format.time(new Date())));
        editorContainer.getClassList().remove(logFileLoading);
        searchInput.setValue("");
    }


    // ------------------------------------------------------ event handler

    @EventHandler(element = "searchInput", on = keyup)
    void onSearchInput(KeyboardEvent event) {
        Elements.setVisible(clearSearch, !Strings.isNullOrEmpty(searchInput.getValue()));
        if (event.getKeyCode() == KeyCode.ENTER) {
            onSearch();
        }
    }

    @EventHandler(element = "clearSearch", on = click)
    void onClear() {
        searchInput.setValue("");
        searchInput.focus();
        Elements.setVisible(clearSearch, false);
    }

    @EventHandler(element = "search", on = click)
    void onSearch() {
        editor.getEditor().find(searchInput.getValue());
    }

    @EventHandler(element = "previous", on = click)
    void onPrevious() {
        editor.getEditor().findPrevious();
    }

    @EventHandler(element = "next", on = click)
    void onNext() {
        editor.getEditor().findNext();
    }

    @EventHandler(element = "refresh", on = click)
    void onRefresh() {
        presenter.refresh();
    }
}
