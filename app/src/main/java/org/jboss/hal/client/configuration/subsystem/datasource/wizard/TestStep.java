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
package org.jboss.hal.client.configuration.subsystem.datasource.wizard;

import com.google.gwt.safehtml.shared.SafeHtml;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.client.configuration.subsystem.datasource.DataSourceColumn;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.blankSlatePf;
import static org.jboss.hal.resources.CSS.btn;
import static org.jboss.hal.resources.CSS.btnLg;
import static org.jboss.hal.resources.CSS.btnPrimary;

/**
 * @author Harald Pehl
 */
public class TestStep extends WizardStep<Context, State> {

    private final Element root;

    public TestStep(final DataSourceColumn column, final Resources resources, final boolean xa,
            final boolean standalone) {
        super(Ids.DATA_SOURCE_TEST_STEP, resources.constants().testConnection());

        String testConnection = resources.constants().testConnection();
        SafeHtml description = standalone
                ? resources.messages().testConnectionStandalone(testConnection)
                : resources.messages().testConnectionDomain(testConnection);
        // @formatter:off
        root = new Elements.Builder()
            .div()
                .div().innerHtml(description).end()
                .div().css(blankSlatePf)
                    .button(resources.constants().testConnection()).css(btn, btnLg, btnPrimary)
                        .on(click, event -> column.testConnectionFromWizard(wizard()))
                    .end()
                .end()
            .end()
        .build();
        // @formatter:on
    }

    @Override
    public Element asElement() {
        return root;
    }
}
