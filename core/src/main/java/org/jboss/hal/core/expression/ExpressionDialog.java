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
package org.jboss.hal.core.expression;

import java.util.Map;

import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.Key;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.StaticItem;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;

import elemental2.dom.HTMLInputElement;

import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.keydown;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EXPRESSION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.resources.CSS.formControl;

public class ExpressionDialog {

    private static final ExpressionResources RESOURCES = GWT.create(ExpressionResources.class);

    private final ExpressionResolver expressionResolver;
    private final boolean standalone;
    private final Resources resources;
    private final Alert alert;
    private final Form<ModelNode> form;
    private final StaticItem resolvedValue;
    private final Dialog dialog;

    public ExpressionDialog(ExpressionResolver expressionResolver, Environment environment,
            Resources resources) {
        this.expressionResolver = expressionResolver;
        this.standalone = environment.isStandalone();
        this.resources = resources;

        alert = new Alert(Icons.ERROR, SafeHtmlUtils.EMPTY_SAFE_HTML);
        noAlert();

        Metadata metadata = Metadata.staticDescription(RESOURCES.expression());
        ModelNodeForm.Builder<ModelNode> builder = new ModelNodeForm.Builder<>(Ids.RESOLVE_EXPRESSION_FORM, metadata)
                .addOnly()
                .customFormItem(EXPRESSION, attributeDescription -> new ExpressionItem(resources))
                .onSave((f, changedValues) -> resolve(f.getModel()));
        resolvedValue = new StaticItem(VALUE, resources.constants().resolvedValue());
        builder.unboundFormItem(resolvedValue);
        form = builder.build();

        FormItem<String> expressionItem = form.getFormItem(EXPRESSION);
        HTMLInputElement inputElement = (HTMLInputElement) expressionItem.element(EDITING)
                .querySelector("." + formControl);
        if (inputElement != null) {
            bind(inputElement, keydown, event -> {
                if (Key.fromEvent(event) == Key.Enter) {
                    // this would normally happen in the on change handler of the text box item,
                    // which is too late
                    String value = inputElement.value;
                    expressionItem.setValue(value);
                    expressionItem.setModified(true);
                    expressionItem.setUndefined(Strings.isNullOrEmpty(value));
                    form.save();
                }
            });
        }

        dialog = new Dialog.Builder(resources.constants().resolveExpression())
                .add(alert.element())
                .add(form.element())
                .primary(resources.constants().resolve(), () -> {
                    if (!form.save()) { // calls the save handler from above upon successful validation
                        clearValue();
                    }
                    return false; // keep the dialog open
                })
                .secondary(resources.constants().close(), () -> true)
                .size(Dialog.Size.MEDIUM)
                .build();
        dialog.registerAttachable(form);
    }

    public void show() {
        dialog.show();
        form.edit(new ModelNode());
    }

    void showAndResolve(String expression) {
        dialog.show();
        form.edit(new ModelNode());
        FormItem<String> expressionItem = form.getFormItem(EXPRESSION);
        expressionItem.setValue(expression);
        expressionItem.setModified(true);
        expressionItem.setUndefined(false);
        form.save();
    }

    private void resolve(ModelNode modelNode) {
        String value = modelNode.get(EXPRESSION).asString();
        try {
            Expression expression = Expression.of(value);
            expressionResolver.resolve(expression, new AsyncCallback<Map<String, String>>() {
                @Override
                public void onSuccess(Map<String, String> result) {
                    if (result.isEmpty()) {
                        warning(resources.messages().expressionWarning(value));
                    } else {
                        String rv = standalone
                                ? result.getOrDefault(Server.STANDALONE.getName(), Names.NOT_AVAILABLE)
                                : Joiner.on(", ").withKeyValueSeparator(" \u21D2 ").join(result);
                        resolvedValue.setValue(rv);
                        noAlert();
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    clearValue();
                    error(resources.messages().expressionError(value));
                }
            });

        } catch (IllegalArgumentException e) {
            form.getFormItem(EXPRESSION).showError(resources.constants().invalidExpression());
        }
    }

    private void clearValue() {
        resolvedValue.clearValue();
    }

    private void noAlert() {
        Elements.setVisible(alert.element(), false);
    }

    private void error(SafeHtml message) {
        alert.setIcon(Icons.ERROR);
        alert.setText(message);
        Elements.setVisible(alert.element(), true);
    }

    private void warning(SafeHtml message) {
        alert.setIcon(Icons.WARNING);
        alert.setText(message);
        Elements.setVisible(alert.element(), true);
    }
}
