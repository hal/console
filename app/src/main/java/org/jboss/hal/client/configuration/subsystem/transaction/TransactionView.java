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
package org.jboss.hal.client.configuration.subsystem.transaction;

import java.util.Collections;

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.form.RequiredByValidation;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static org.jboss.hal.dmr.ModelDescriptionConstants.PROCESS_ID_SOCKET_BINDING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROCESS_ID_SOCKET_MAX_PORTS;

@MbuiView
@SuppressWarnings({ "DuplicateStringLiteralInspection", "HardCodedStringLiteral", "WeakerAccess" })
public abstract class TransactionView extends MbuiViewImpl<TransactionPresenter>
        implements TransactionPresenter.MyView {

    // ------------------------------------------------------ initialization

    public static TransactionView create(final MbuiContext mbuiContext) {
        return new Mbui_TransactionView(mbuiContext);
    }

    @MbuiElement("tx-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("tx-attributes-form") Form<ModelNode> attributesForm;
    @MbuiElement("tx-process-form") Form<ModelNode> processForm;
    @MbuiElement("tx-recovery-form") Form<ModelNode> recoveryForm;
    @MbuiElement("tx-path-form") Form<ModelNode> pathForm;
    @MbuiElement("tx-jdbc-form") Form<ModelNode> jdbcForm;

    TransactionView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @Override
    public void setPresenter(final TransactionPresenter presenter) {
        super.setPresenter(presenter);

        // process-id-socket-max-ports requires process-id-socket-binding, but as process-id-socket-binding is a required
        // attribute, the "requires" validation handler in ModelNodeForm only elects them for validation if it is
        // a non-required attribute
        FormItem<String> socketBindingItem = processForm.getFormItem(PROCESS_ID_SOCKET_BINDING);
        socketBindingItem.addValidationHandler(
                new RequiredByValidation<>(socketBindingItem, Collections.singletonList(PROCESS_ID_SOCKET_MAX_PORTS),
                        ((ModelNodeForm) processForm), mbuiContext.resources().constants(),
                        mbuiContext.resources().messages()));
    }

    @Override
    public void updateConfiguration(final ModelNode configuration) {
        attributesForm.view(configuration);
        processForm.view(configuration);
        recoveryForm.view(configuration);
        pathForm.view(configuration);
        jdbcForm.view(configuration);
    }
}
