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
package org.jboss.hal.client.bootstrap.functions;

import javax.inject.Inject;

import elemental.client.Browser;
import elemental.html.ScriptElement;
import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.meta.AddressTemplate;

/**
 * @author Harald Pehl
 */
public class ReadExtensions implements BootstrapFunction {

    private final CrudOperations crud;

    @Inject
    public ReadExtensions(final CrudOperations crud) {this.crud = crud;}

    @Override
    public void execute(final Control<FunctionContext> control) {
        // TODO Decide where to store extensions in the management model
        crud.readChildren(AddressTemplate.of("/core-service=management/service=console"), "extension",
                extensions -> extensions.forEach(
                        extension -> injectScript(extension.getValue().get("script").asString())));
    }

    @Override
    public String name() {
        return "Bootstrap[ReadExtensions]";
    }

    private void injectScript(String script) {
        // TODO Should there be any checks before we inject the script? Is that even possible?
        ScriptElement scriptElement = Browser.getDocument().createScriptElement();
        scriptElement.setType("text/javascript"); //NON-NLS
        scriptElement.setAsync(true);
        scriptElement.setSrc(script);
        Browser.getDocument().getHead().appendChild(scriptElement);
    }
}
