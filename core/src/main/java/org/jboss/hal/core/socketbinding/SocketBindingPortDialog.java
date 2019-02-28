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
package org.jboss.hal.core.socketbinding;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.StaticItem;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.hal.dmr.ModelDescriptionConstants.EXPRESSION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PORT;

public class SocketBindingPortDialog {

    private static final SocketBindingResource RESOURCES = GWT.create(SocketBindingResource.class);
    static Logger _log = LoggerFactory.getLogger("org.jboss");

    private final SocketBindingResolver socketBindingResolver;
    private final boolean standalone;
    private final Resources resources;
    private final Alert alert;
    private final Form<ModelNode> form;
    private final StaticItem portItem;
    private final Dialog dialog;

    public SocketBindingPortDialog(SocketBindingResolver socketBindingResolver, Environment environment,
            Resources resources) {
        this.socketBindingResolver = socketBindingResolver;
        this.standalone = environment.isStandalone();
        this.resources = resources;

        alert = new Alert(Icons.ERROR, SafeHtmlUtils.EMPTY_SAFE_HTML);
        noAlert();

        portItem = new StaticItem(PORT, "Port");
        Metadata metadata = Metadata.empty();
        ModelNodeForm.Builder<ModelNode> builder = new ModelNodeForm.Builder<>(Ids.RESOLVE_EXPRESSION_FORM, metadata)
                .unboundFormItem(new NameItem(), 0)
                .unboundFormItem(portItem)
                .readOnly();
        form = builder.build();

        dialog = new Dialog.Builder(resources.constants().resolveSocketBinding())
                .add(alert.element())
                .add(form.element())
                .primary(resources.constants().close(), () -> true)
                .size(Dialog.Size.SMALL)
                .build();
        dialog.registerAttachable(form);
    }

    void showAndResolve(String socketBindingName) {
        _log.info(" SocketBindingDialog showAndResolve: {}", socketBindingName);
        dialog.show();
        form.view(new ModelNode());
        form.getFormItem(NAME).setValue(socketBindingName);

        try {
            socketBindingResolver.resolve(socketBindingName, new AsyncCallback<String>() {
                @Override
                public void onSuccess(String port) {
                    portItem.setValue(port);
                    noAlert();
                }

                @Override
                public void onFailure(Throwable caught) {
                    clearValue();
                    error(resources.messages().expressionError(socketBindingName));
                }
            });

        } catch (IllegalArgumentException e) {
            form.getFormItem(EXPRESSION).showError(resources.constants().invalidExpression());
        }
    }

    private void clearValue() {
        portItem.clearValue();
    }

    private void noAlert() {
        Elements.setVisible(alert.element(), false);
    }

    private void error(SafeHtml message) {
        alert.setIcon(Icons.ERROR);
        alert.setText(message);
        Elements.setVisible(alert.element(), true);
    }
}
