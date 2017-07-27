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
package org.jboss.hal.client.configuration.subsystem.transaction;

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral", "WeakerAccess"})
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

        // set the process fields as not required, because uuid and socket-binding are mutually exclusive.
        processForm.getBoundFormItems().forEach(formItem -> formItem.setRequired(false));

        // --------------- form validation for the general attributes
        attributesForm.addFormValidation(presenter.getAttributesFormValidation());

        // --------------- form validation for the process attributes
        processForm.addFormValidation(presenter.getProcessFormValidation());

        // --------------- form validation for the jdbc attributes
        jdbcForm.addFormValidation(presenter.getJdbcFormValidation());
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
