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
package org.jboss.hal.core.expression;

import java.util.Map;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import elemental.dom.Element;
import elemental.events.KeyboardEvent;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.StaticItem;
import org.jboss.hal.ballroom.form.ValidationResult;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EXPRESSION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.resources.CSS.formControl;

/**
 * @author Harald Pehl
 */
public class ExpressionDialog {

    private static final ExpressionResources RESOURCES = GWT.create(ExpressionResources.class);

    private final ExpressionResolver expressionResolver;
    private final boolean standalone;
    private final Resources resources;
    private final Alert alert;
    private final Form<ModelNode> form;
    private final StaticItem standaloneValue;
    private final Dialog dialog;

    public ExpressionDialog(final ExpressionResolver expressionResolver, final Environment environment,
            final Resources resources) {
        this.expressionResolver = expressionResolver;
        this.standalone = environment.isStandalone();
        this.resources = resources;

        alert = new Alert(Icons.ERROR, SafeHtmlUtils.EMPTY_SAFE_HTML);
        Elements.setVisible(alert.asElement(), false);

        Metadata metadata = Metadata.staticDescription(RESOURCES.expression());
        ModelNodeForm.Builder<ModelNode> builder = new ModelNodeForm.Builder<>(Ids.RESOLVE_EXPRESSION_FORM, metadata)
                .addOnly()
                .onSave((f, changedValues) -> resolve(f.getModel()));
        if (standalone) {
            standaloneValue = new StaticItem(VALUE, resources.constants().resolvedValue());
            builder.unboundFormItem(standaloneValue);
        } else {
            standaloneValue = null;
        }
        form = builder.build();

        FormItem<String> expressionItem = form.getFormItem(EXPRESSION);
        expressionItem.setExpressionAllowed(false);
        expressionItem.addValidationHandler(value -> {
            try {
                Expression.of(value);
                return ValidationResult.OK;
            } catch (IllegalArgumentException e) {
                return ValidationResult.invalid(resources.constants().invalidExpression());
            }
        });
        Element inputElement = expressionItem.asElement(EDITING).querySelector("." + formControl);
        if (inputElement != null) {
            inputElement.setOnkeydown(evt -> {
                KeyboardEvent keyboardEvent = (KeyboardEvent) evt;
                if (keyboardEvent.getKeyCode() == KeyboardEvent.KeyCode.ENTER) {
                    // this would normally happen in the on change handler of the text box item,
                    // which is too late for
                    expressionItem.setUndefined(Strings.isNullOrEmpty(expressionItem.getValue()));
                    form.save();
                }
            });
        }

        dialog = new Dialog.Builder(resources.constants().resolveExpression())
                .add(alert.asElement())
                .add(form.asElement())
                .primary(resources.constants().resolve(), () -> {
                    if (!form.save()) { // calls the save handler from above upon successful validation
                        clearValue();
                    }
                    return false; // keep the dialog open
                })
                .secondary(resources.constants().close(), () -> true)
                .size(Dialog.Size.MEDIUM)
                .closeOnEsc(true)
                .closeIcon(true)
                .build();
        dialog.registerAttachable(form);
    }

    public void show() {
        dialog.show();
        form.add(new ModelNode());
    }

    void showAndResolve(String expression) {
        dialog.show();
        form.add(new ModelNode());
        FormItem<String> expressionItem = form.getFormItem(EXPRESSION);
        expressionItem.setValue(expression);
        expressionItem.setUndefined(false);
        form.save();
    }

    private void resolve(ModelNode modelNode) {
        String value = modelNode.get(EXPRESSION).asString();
        try {
            Expression expression = Expression.of(value);
            expressionResolver.resolve(expression, new AsyncCallback<Map<String, String>>() {
                @Override
                public void onSuccess(final Map<String, String> result) {
                    String v = result.getOrDefault(Server.STANDALONE.getName(), Names.NOT_AVAILABLE);
                    standaloneValue.setValue(v);
                    Elements.setVisible(alert.asElement(), false);
                }

                @Override
                public void onFailure(final Throwable caught) {
                    clearValue();
                    alert.setText(resources.messages().expressionError(value));
                    Elements.setVisible(alert.asElement(), true);
                }
            });

        } catch (IllegalArgumentException e) {
            form.getFormItem(EXPRESSION).showError(resources.constants().invalidExpression());
        }
    }

    private void clearValue() {
        if (standalone) {
            standaloneValue.clearValue();
        }
    }
}
