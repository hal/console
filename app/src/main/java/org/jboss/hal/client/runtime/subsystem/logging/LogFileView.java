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
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import org.jboss.gwt.elemento.template.DataElement;
import org.jboss.gwt.elemento.template.Templated;
import org.jboss.hal.ballroom.Clipboard;
import org.jboss.hal.ballroom.Format;
import org.jboss.hal.ballroom.Search;
import org.jboss.hal.ballroom.Tooltip;
import org.jboss.hal.ballroom.editor.AceEditor;
import org.jboss.hal.ballroom.editor.Options;
import org.jboss.hal.ballroom.form.SwitchBridge;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.ballroom.Skeleton;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;

import static elemental2.dom.DomGlobal.document;
import static elemental2.dom.DomGlobal.setTimeout;
import static elemental2.dom.DomGlobal.window;
import static java.lang.Math.max;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.Skeleton.MARGIN_BIG;
import static org.jboss.hal.resources.CSS.logFileLoading;
import static org.jboss.hal.resources.CSS.px;

/**
 * @author Harald Pehl
 */
@Templated("LogFileView.html#root")
public abstract class LogFileView extends HalViewImpl implements LogFilePresenter.MyView {

    // @formatter:off
    public static LogFileView create(final Environment environment, final StatementContext statementContext,
            final LogFiles logFiles, final Resources resources) {
        return new Templated_LogFileView(environment, statementContext, logFiles, resources);
    }

    public abstract Environment environment();
    public abstract StatementContext statementContext();
    public abstract LogFiles logFiles();
    public abstract Resources resources();
    // @formatter:on


    private static final int MIN_HEIGHT = 70;

    private Search search;
    private AceEditor editor;
    private LogFilePresenter presenter;

    @DataElement HTMLElement header;
    @DataElement HTMLElement logFileControls;
    @DataElement HTMLElement status;
    @DataElement HTMLInputElement tailMode;
    @DataElement HTMLElement copyToClipboard;
    @DataElement HTMLElement download;
    @DataElement HTMLElement editorContainer;
    @DataElement HTMLElement editorPlaceholder;
    @DataElement HTMLElement loading;
    @DataElement HTMLElement refresh;


    // ------------------------------------------------------ init & ui

    @DataElement("search")
    Search setupSearch() {
        search = new Search.Builder(Ids.LOG_FILE_SEARCH, query -> editor.getEditor().find(query))
                .onPrevious(query -> editor.getEditor().findPrevious())
                .onNext(query -> editor.getEditor().findNext())
                .build();
        return search;
    }

    @PostConstruct
    void init() {
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

        bind(refresh, click, event -> presenter.reloadFile());
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
            setTimeout((o) -> tooltip.hide(), 1000);
        }
    }

    @Override
    public void attach() {
        super.attach();

        SwitchBridge.Api.element(tailMode).onChange((event, state) -> presenter.toggleTailMode(state));

        editor.getEditor().$blockScrolling = 1;
        editor.getEditor().setTheme("ace/theme/logfile"); //NON-NLS
        editor.getEditor().getSession().setMode("ace/mode/logfile"); //NON-NLS

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

    private void adjustEditorHeight() {
        int height = Skeleton.applicationHeight();
        height -= 2 * MARGIN_BIG;
        height -= (header.offsetHeight + logFileControls.offsetHeight + 20);
        height = max(height, MIN_HEIGHT);
        editor.asElement().style.height = CSS.height(px(height));
        editor.getEditor().resize();
    }

    @Override
    public void setPresenter(final LogFilePresenter presenter) {
        this.presenter = presenter;
    }


    // ------------------------------------------------------ API

    @Override
    public void loading() {
        status.textContent = resources().constants().loadingPleaseWait();
        status.title = resources().constants().loadingPleaseWait();
        int top = (int) (loading.offsetHeight + editor.asElement().offsetHeight / 2);
        loading.style.top = String.valueOf(-1 * top) + "px"; //NON-NLS
        editorContainer.classList.add(logFileLoading);
    }

    @Override
    public void show(final LogFile logFile, int lines, final String content) {
        statusUpdate(lines);
        StringBuilder builder = new StringBuilder();
        if (presenter.isExternal()) {
            if (!environment().isStandalone()) {
                builder.append(statementContext().selectedHost())
                        .append(" / ")
                        .append(statementContext().selectedServer())
                        .append(" / ");
            } else {
                builder.append(Server.STANDALONE.getName())
                        .append(" / ");
            }
        }
        builder.append(logFile.getFilename());
        header.textContent = builder.toString();
        download.setAttribute(UIConstants.DOWNLOAD, logFile.getFilename());
        download.setAttribute(UIConstants.HREF, logFiles().downloadUrl(logFile.getFilename()));

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
        HTMLElement lineElement = (HTMLElement) document.querySelector(
                "#" + Ids.LOG_FILE_EDITOR + " .ace_text-layer .ace_line"); //NON-NLS
        if (lineElement != null) {
            lineHeight = (int) lineElement.offsetHeight;
        }
        return (int) (editor.asElement().offsetHeight / lineHeight);
    }

    private void statusUpdate(int lines) {
        String statusText = lines < LogFiles.LINES
                ? resources().messages().logFileFullStatus(lines, Format.time(new Date()))
                : resources().messages().logFilePartStatus(lines, Format.time(new Date()));
        status.textContent = statusText;
        status.title = statusText;
        editorContainer.classList.remove(logFileLoading);
        search.clear();
    }
}
