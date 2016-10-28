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
package org.jboss.hal.core.mbui.form;

import com.google.gwt.core.client.GWT;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Messages;

/**
 * An element which uses the dispatcher and an operation to check whether a resource exists. If it exists the specified
 * form is displayed in view mode, otherwise the empty state element is displayed which can be used to create the
 * resource.
 *
 * @author Harald Pehl
 */
public class FailSafeModelNodeForm<T extends ModelNode> implements IsElement, Attachable {

    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final Messages MESSAGES = GWT.create(Messages.class);

    private final Dispatcher dispatcher;
    private final EmptyState emptyState;
    private final Form<T> form;
    private final Operation operation;
    private final Element root;

    public FailSafeModelNodeForm(final Dispatcher dispatcher, final Operation operation,
            final Form<T> form, final EmptyState.Action action) {
        this(dispatcher, operation, form, new EmptyState.Builder(CONSTANTS.noResource())
                .description(MESSAGES.noResource())
                .primaryAction(CONSTANTS.add(), action)
                .build());
    }

    public FailSafeModelNodeForm(final Dispatcher dispatcher, final Operation operation, final Form<T> form,
            final EmptyState emptyState) {
        this.dispatcher = dispatcher;
        this.emptyState = emptyState;
        this.form = form;
        this.operation = operation;
        this.root = new Elements.Builder().div().add(emptyState).add(form).end().build();

        Elements.setVisible(emptyState.asElement(), false);
        Elements.setVisible(form.asElement(), false);
    }

    @Override
    public Element asElement() {
        return root;
    }

    @Override
    public void attach() {
        form.attach();
        dispatcher.execute(operation,
                result -> formMode(),
                (op, failure) -> emptyStateMode());
    }

    @Override
    public void detach() {
        form.detach();
    }

    public void view(T model) {
        dispatcher.execute(operation,
                result -> {
                    formMode();
                    form.view(model);
                },
                (op, failure) -> emptyStateMode());
    }

    private void emptyStateMode() {
        Elements.setVisible(emptyState.asElement(), true);
        Elements.setVisible(form.asElement(), false);
    }

    private void formMode() {
        Elements.setVisible(emptyState.asElement(), false);
        Elements.setVisible(form.asElement(), true);
    }
}
