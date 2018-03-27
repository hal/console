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
package org.jboss.hal.client.management.sslwizard;

import com.google.gwt.safehtml.shared.SafeHtml;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.InputType;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.Elements.label;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.*;

public class DefineStrategyStep extends WizardStep<EnableSSLContext, EnableSSLState> {

    private final HTMLElement root;
    private Boolean mutual;
    private EnableSSLContext.Strategy strategy;
    private HTMLDivElement errorMsg;

    DefineStrategyStep(final Resources resources) {
        super(resources.constants().enableSSLManagementInitialSetup());

        SafeHtml description = resources.messages().enableSSLDescription();

        errorMsg = div().css(alert, alertDanger)
                .add(span().css(pfIcon(errorCircleO)))
                .add(span().textContent(resources.constants().enableSSLManagementErrorMsg()))
                .asElement();

        String radioMutualName = "mutual";
        String radioStrategyName = "key-store-strategy";
        root = div().css(formHorizontal)
                .add(errorMsg)
                .add(p().innerHtml(description))

                // asks the user to set the mutual client authentication
                .add(p().css(marginTopLarge).innerHtml(resources.messages().enableSSLMutualQuestion()))
                .add(div().css(radio)
                        .add(label()
                                .add(input(InputType.radio)
                                        .id("choose-mutual-yes")
                                        .attr(UIConstants.NAME, radioMutualName)
                                        .on(click, e -> mutual = true)
                                        .asElement())
                                .add(span().textContent(resources.constants().yes()))))
                .add(div().css(radio)
                        .add(label()
                                .add(input(InputType.radio)
                                        .id("choose-mutual-no")
                                        .attr(UIConstants.NAME, radioMutualName)
                                        .on(click, e -> mutual = false)
                                        .asElement())
                                .add(span().textContent(resources.constants().no()))))

                // asks the user to choose the key-store strategy
                .add(p().css(marginTopLarge).innerHtml(resources.messages().enableSSLStrategyQuestion()))
                .add(div().css(radio)
                        .add(label()
                                .add(input(InputType.radio)
                                        .id("strategy-create-all")
                                        .attr(UIConstants.NAME, radioStrategyName)
                                        .on(click, e -> strategy = EnableSSLContext.Strategy.KEYSTORE_CREATE)
                                        .asElement())
                                .add(span().innerHtml(resources.messages().enableSSLStrategyQuestionCreateAll()))))
                .add(div().css(radio)
                        .add(label()
                                .add(input(InputType.radio)
                                        .id("strategy-create-key-store")
                                        .attr(UIConstants.NAME, radioStrategyName)
                                        .on(click, e -> strategy = EnableSSLContext.Strategy.KEYSTORE_FILE_EXISTS)
                                        .asElement())
                                .add(span().innerHtml(resources.messages().enableSSLStrategyQuestionCreateKeyStore()))))
                .add(div().css(radio)
                        .add(label()
                                .add(input(InputType.radio)
                                        .id("strategy-reuse-key-store")
                                        .attr(UIConstants.NAME, radioStrategyName)
                                        .on(click, e -> strategy = EnableSSLContext.Strategy.KEYSTORE_RESOURCE_EXISTS)
                                        .asElement())
                                .add(span().innerHtml(resources.messages().enableSSLStrategyQuestionReuseKeyStore()))))
                .asElement();

        Elements.setVisible(errorMsg, false);
    }

    @Override
    public HTMLElement asElement() {
        return root;
    }

    @Override
    public void reset(final EnableSSLContext context) {
        mutual = null;
        strategy = null;
    }

    @Override
    protected boolean onNext(final EnableSSLContext context) {
        context.mutualAuthentication = mutual;
        context.strategy = strategy;
        boolean valid = strategy != null && mutual != null;
        Elements.setVisible(errorMsg, !valid);
        return valid;
    }
}
