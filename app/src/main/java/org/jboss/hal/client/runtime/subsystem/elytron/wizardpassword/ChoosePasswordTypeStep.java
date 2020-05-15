/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.runtime.subsystem.elytron.wizardpassword;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.InputType;
import org.jboss.gwt.elemento.core.builder.HtmlContentBuilder;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SET_PASSWORD;
import static org.jboss.hal.resources.CSS.formHorizontal;
import static org.jboss.hal.resources.CSS.marginTopLarge;
import static org.jboss.hal.resources.CSS.radio;

public class ChoosePasswordTypeStep extends WizardStep<PasswordContext, PasswordState> {

    private final HTMLElement root;
    private Resources resources;

    ChoosePasswordTypeStep(Resources resources) {
        super(resources.constants().chooseIdentityPasswordTitle());
        this.resources = resources;

        HtmlContentBuilder<HTMLDivElement> builder;
        builder = div().css(formHorizontal)
                .add(p().css(marginTopLarge).innerHtml(resources.messages().setIdentityPasswordQuestion()));

        for (PasswordContext.PasswordType type : PasswordContext.PasswordType.values()) {
            builder.add(div().css(radio)
                    .add(label()
                            .add(input(InputType.radio)
                                    .id(type.name())
                                    .attr(UIConstants.NAME, SET_PASSWORD)
                                    .on(click, e -> wizard().getContext().type = type).element())
                            .add(span().textContent(getDescription(type)))));
        }
        root = builder.element();
    }

    private String getDescription(PasswordContext.PasswordType type) {
        String description = null;
        switch (type) {
            // BCRYPT, CLEAR, DIGEST, OTP, SALTED_SIMPLE_DIGEST, SCRAM_DIGEST, SIMPLE_DIGEST
            case BCRYPT:
                description = resources.messages().identityPasswordBcrypt();
                break;
            case CLEAR:
                description = resources.messages().identityPasswordClear();
                break;
            case DIGEST:
                description = resources.messages().identityPasswordDigest();
                break;
            case OTP:
                description = resources.messages().identityPasswordOtp();
                break;
            case SALTED_SIMPLE_DIGEST:
                description = resources.messages().identityPasswordSaltedSimpleDigest();
                break;
            case SCRAM_DIGEST:
                description = resources.messages().identityPasswordScramDigest();
                break;
            case SIMPLE_DIGEST:
                description = resources.messages().identityPasswordSimpleDigest();
                break;
            default:
                break;
        }
        return description;
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public void reset(PasswordContext context) {
        context.type = null;
    }

    @Override
    protected boolean onNext(PasswordContext context) {
        return context.type != null;
    }
}
