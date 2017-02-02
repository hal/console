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
package org.jboss.hal.client.bootstrap;

import javax.annotation.PostConstruct;

import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.DataElement;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.gwt.elemento.core.Templated;
import org.jboss.hal.resources.Constants;

import static org.jboss.hal.resources.CSS.bootstrapError;

@Templated
public abstract class BootstrapFailed implements IsElement {

    // @formatter:off
    public static BootstrapFailed create(String error, Constants constants) {
        return new Templated_BootstrapFailed(error, constants);
    }

    public abstract String error();
    public abstract Constants constants();
    // @formatter:on

    @DataElement Element errorHolder;

    @PostConstruct
    void init() {
        Browser.getDocument().getDocumentElement().getClassList().add(bootstrapError);
        errorHolder.setInnerText(error());
    }
}
