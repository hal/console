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

import java.util.function.Supplier;

import com.google.gwt.core.client.GWT;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Messages;
import org.jboss.hal.spi.Callback;

import static org.jboss.hal.resources.CSS.failSafeForm;

/**
 * An element which uses the dispatcher and an operation to check whether a resource exists. If it exists the specified
 * form is displayed in view mode, otherwise an empty state element is displayed which can be used to create the
 * resource.
 * <p>
 * Useful to wrap forms for singleton resources which might not exist by default.
 *
 * @author Harald Pehl
 */
public class FailSafeForm<T extends ModelNode> implements IsElement, Attachable {

    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final Messages MESSAGES = GWT.create(Messages.class);

    private final Dispatcher dispatcher;
    private final EmptyState emptyState;
    private final Form<T> form;
    private final Supplier<Operation> readOperation;
    private final Element root;

    public FailSafeForm(final Dispatcher dispatcher, final Supplier<Operation> readOperation, final Form<T> form,
            final Callback addAction) {
        this(dispatcher, readOperation, form, new EmptyState.Builder(CONSTANTS.noResource())
                .description(MESSAGES.noResource())
                .primaryAction(CONSTANTS.add(), addAction)
                .build());
    }

    public FailSafeForm(final Dispatcher dispatcher, final Supplier<Operation> readOperation, final Form<T> form,
            final EmptyState emptyState) {
        this.dispatcher = dispatcher;
        this.emptyState = emptyState;
        this.form = form;
        this.readOperation = readOperation;
        this.root = new Elements.Builder().div().css(failSafeForm).add(emptyState).add(form).end().build();

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
        if (readOperation.get() != null) {
            dispatcher.execute(readOperation.get(),
                    result -> formMode(),
                    (op, failure) -> emptyStateMode());
        } else {
            emptyStateMode();
        }
    }

    @Override
    public void detach() {
        form.detach();
    }

    public void clear() {
        formMode();
        form.clear();
    }

    public void view(T model) {
        if (readOperation.get() != null) {
            dispatcher.execute(readOperation.get(),
                    result -> {
                        formMode();
                        form.view(model);
                    },
                    (op, failure) -> emptyStateMode());
        } else {
            emptyStateMode();
        }
    }

    public <F> FormItem<F> getFormItem(final String name) {return form.getFormItem(name);}

    private void emptyStateMode() {
        Elements.setVisible(emptyState.asElement(), true);
        Elements.setVisible(form.asElement(), false);
    }

    private void formMode() {
        Elements.setVisible(emptyState.asElement(), false);
        Elements.setVisible(form.asElement(), true);
    }
}
