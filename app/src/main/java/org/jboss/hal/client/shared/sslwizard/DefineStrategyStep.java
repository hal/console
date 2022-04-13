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
package org.jboss.hal.client.shared.sslwizard;

import org.jboss.elemento.Elements;
import org.jboss.elemento.HtmlContentBuilder;
import org.jboss.elemento.InputType;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;

import com.google.gwt.safehtml.shared.SafeHtml;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.*;
import static org.jboss.elemento.Elements.label;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.resources.CSS.*;

public class DefineStrategyStep extends WizardStep<EnableSSLContext, EnableSSLState> {

    private final HTMLElement root;
    private Boolean mutual;
    private EnableSSLContext.Strategy strategy;
    private HTMLDivElement errorMsg;

    DefineStrategyStep(Resources resources, boolean standaloneMode, boolean undertowHttps) {
        super(resources.constants().enableSSLManagementInitialSetup());

        SafeHtml description = resources.messages().enableManagementSSLDescription();
        if (undertowHttps) {
            description = resources.messages().enableUndertowSSLDescription();
        }

        errorMsg = div().css(alert, alertDanger)
                .add(span().css(pfIcon(errorCircleO)))
                .add(span().textContent(resources.constants().enableSSLManagementErrorMsg())).element();

        String radioMutualName = "mutual";
        String radioStrategyName = "key-store-strategy";
        HtmlContentBuilder<HTMLDivElement> builder;
        builder = div().css(formHorizontal)
                .add(errorMsg)
                .add(p().innerHtml(description))

                // asks the user to set the mutual client authentication
                .add(p().css(marginTopLarge).innerHtml(resources.messages().enableSSLMutualQuestion()))
                .add(div().css(radio)
                        .add(label()
                                .add(input(InputType.radio)
                                        .id("choose-mutual-yes")
                                        .attr(UIConstants.NAME, radioMutualName)
                                        .on(click, e -> mutual = true).element())
                                .add(span().textContent(resources.constants().yes()))))
                .add(div().css(radio)
                        .add(label()
                                .add(input(InputType.radio)
                                        .id("choose-mutual-no")
                                        .attr(UIConstants.NAME, radioMutualName)
                                        .on(click, e -> mutual = false).element())
                                .add(span().textContent(resources.constants().no()))))

                // asks the user to choose the key-store strategy
                .add(p().css(marginTopLarge).innerHtml(resources.messages().enableSSLStrategyQuestion()));

        // the option to generate a self signed certificate is not available on domain mode and profile level
        // because the operation to generate the self signed certificate is only available
        // on a runtime resource (/host=any/server=any/subsysem=elytron/key-store=any:generate-key-pair)
        // and it would generate a different certificate for each key-store
        // The same problem applies to obtain a certificate from LetsEncrypt, this requires to call
        // key-store=foo:obtain-certificate on a running server, but calling this op on different servers
        // would obtain certificates with different checksums
        boolean generateSelfSigned = standaloneMode || !undertowHttps;
        if (generateSelfSigned) {
            builder.add(div().css(radio)
                    .add(label()
                            .add(input(InputType.radio)
                                    .id("strategy-create-all")
                                    .attr(UIConstants.NAME, radioStrategyName)
                                    .on(click, e -> strategy = EnableSSLContext.Strategy.KEYSTORE_CREATE).element())
                            .add(span().innerHtml(resources.messages().enableSSLStrategyQuestionCreateAll()))));

            builder.add(div().css(radio)
                    .add(label()
                            .add(input(InputType.radio)
                                    .id("strategy-obtain-from-letsencrypt")
                                    .attr(UIConstants.NAME, radioStrategyName)
                                    .on(click, e -> strategy = EnableSSLContext.Strategy.KEYSTORE_OBTAIN_LETSENCRYPT)
                                    .element())
                            .add(span().innerHtml(
                                    resources.messages().enableSSLStrategyQuestionObtainFromLetsEncrypt()))));
        }

        builder.add(div().css(radio)
                .add(label()
                        .add(input(InputType.radio)
                                .id("strategy-create-key-store")
                                .attr(UIConstants.NAME, radioStrategyName)
                                .on(click, e -> strategy = EnableSSLContext.Strategy.KEYSTORE_FILE_EXISTS).element())
                        .add(span().innerHtml(resources.messages().enableSSLStrategyQuestionCreateKeyStore()))));
        builder.add(div().css(radio)
                .add(label()
                        .add(input(InputType.radio)
                                .id("strategy-reuse-key-store")
                                .attr(UIConstants.NAME, radioStrategyName)
                                .on(click, e -> strategy = EnableSSLContext.Strategy.KEYSTORE_RESOURCE_EXISTS)
                                .element())
                        .add(span().innerHtml(resources.messages().enableSSLStrategyQuestionReuseKeyStore()))));

        root = builder.element();

        Elements.setVisible(errorMsg, false);
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public void reset(EnableSSLContext context) {
        mutual = null;
        strategy = null;
    }

    @Override
    protected boolean onNext(EnableSSLContext context) {
        context.mutualAuthentication = mutual;
        context.strategy = strategy;
        boolean valid = strategy != null && mutual != null;
        Elements.setVisible(errorMsg, !valid);
        return valid;
    }
}
