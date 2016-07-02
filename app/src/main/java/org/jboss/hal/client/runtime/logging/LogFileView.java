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

import javax.inject.Inject;

import elemental.dom.Element;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.editor.AceEditor;
import org.jboss.hal.ballroom.editor.Options;
import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.resources.Ids;

import static elemental.css.CSSStyleDeclaration.Unit.PX;
import static org.jboss.hal.resources.CSS.lead;

/**
 * @author Harald Pehl
 */
public class LogFileView extends PatternFlyViewImpl implements LogFilePresenter.MyView {

    private static final String HEADER_ELEMENT = "headerElement";
    private static final String LEAD_ELEMENT = "leadElement";
    private final Element headerElement;
    private final Element leadElement;
    private final AceEditor editor;
    private LogFilePresenter presenter;

    @Inject
    public LogFileView() {
        Options editorOptions = new Options();
        editorOptions.readOnly = true;
        editorOptions.showPrintMargin = false;
        editor = new AceEditor(Ids.LOG_FILE_COLUMN, editorOptions);

        // @formatter:off
        LayoutBuilder layoutBuilder = new LayoutBuilder()
            .row()
                .column()
                    .h(1).rememberAs(HEADER_ELEMENT).end()
                    .p().rememberAs(LEAD_ELEMENT).css(lead).end()
                    .add(editor)
                .end()
            .end();
        // @formatter:on

        Element root = layoutBuilder.build();
        headerElement = layoutBuilder.referenceFor(HEADER_ELEMENT);
        leadElement = layoutBuilder.referenceFor(LEAD_ELEMENT);
        registerAttachable(editor);
        initElement(root);
    }

    @Override
    public void setPresenter(final LogFilePresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showContent(final LogFile logFile, final String content) {
        headerElement.setTextContent(logFile.getFilename());
        leadElement.setTextContent(logFile.getFormattedLastModifiedDate() + ", " + logFile.getFormattedSize());
        editor.getEditor().getSession().setValue(content);
        editor.asElement().getStyle().setHeight(200, PX);
        editor.getEditor().resize();
    }
}
