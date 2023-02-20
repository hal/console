/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.update;

import java.util.Date;

import org.jboss.elemento.HtmlContentBuilder;
import org.jboss.hal.ballroom.Format;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttribute;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLPreElement;

import static java.util.stream.Collectors.joining;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.pre;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HISTORY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.KIND;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REVISION;

public class HistoryPreview extends PreviewContent<NamedNode> {

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final HtmlContentBuilder<HTMLPreElement> pre;

    public HistoryPreview(final NamedNode history, final Dispatcher dispatcher, final StatementContext statementContext,
            final Resources resources) {
        super(history.getName());
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;

        PreviewAttributes<NamedNode> attributes = new PreviewAttributes<>(history);
        attributes.append(model -> {
            Date date = ModelNodeHelper.failSafeDate(model, DATE);
            return new PreviewAttribute(new LabelBuilder().label(DATE), Format.mediumDateTime(date));
        });
        attributes.append(KIND);
        previewBuilder().addAll(attributes);
        previewBuilder()
                .add(h(2).textContent(resources.constants().content()))
                .add(pre = pre());
    }

    @Override
    public void update(final NamedNode item) {
        Operation operation = new Operation.Builder(AddressTemplates.INSTALLER_TEMPLATE.resolve(statementContext), HISTORY)
                .param(REVISION, item.getName())
                .build();
        dispatcher.execute(operation, result -> {
            String details = result.asList().stream().map(ModelNode::asString).collect(joining("\n"));
            pre.textContent(details);
        });
    }
}
