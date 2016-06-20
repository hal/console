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
package org.jboss.hal.client.configuration.subsystem.transactions;

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

/**
 * @author Claudio Miranda
 */
@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral", "WeakerAccess"})
public abstract class TransactionView extends MbuiViewImpl<TransactionPresenter> implements TransactionPresenter.MyView {

    // ------------------------------------------------------ initialization

    public static TransactionView create(final MbuiContext mbuiContext) {
        return new Mbui_TransactionView(mbuiContext);
    }

    @MbuiElement("transaction-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("attributes-form") Form<ModelNode> attributesForm;
    @MbuiElement("process-form") Form<ModelNode> processForm;
    @MbuiElement("recovery-form") Form<ModelNode> recoveryForm;
    @MbuiElement("path-form") Form<ModelNode> pathForm;
    @MbuiElement("jdbc-form") Form<ModelNode> jdbcForm;

    TransactionView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }


    @Override
    public void updateConfiguration(final ModelNode configuration) {
        attributesForm.view(configuration);
        processForm.view(configuration);
        recoveryForm.view(configuration);
        pathForm.view(configuration);
        jdbcForm.view(configuration);
    }

    // ------------------------------------------------------ view / mbui contract

    @Override
    public VerticalNavigation getVerticalNavigation() {
        return navigation;
    }
}
