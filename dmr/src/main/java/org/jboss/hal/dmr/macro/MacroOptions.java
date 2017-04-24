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
package org.jboss.hal.dmr.macro;

import com.google.gwt.core.client.GWT;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;

/**
 * @author Harald Pehl
 */
public class MacroOptions extends NamedNode {

    public static final MacroOptionsResources RESOURCES = GWT.create(MacroOptionsResources.class);
    public static final String OMIT_READ_OPERATIONS = "omit-read-operations";
    public static final String OPEN_IN_EDITOR = "open-in-editor";

    public MacroOptions() {
        super("", new ModelNode());
        get(OMIT_READ_OPERATIONS).set(false);
        get(OPEN_IN_EDITOR).set(false);
    }

    public boolean omitReadOperations() {
        return get(OMIT_READ_OPERATIONS).asBoolean(false);
    }

    public boolean openInEditor() {
        return get(OPEN_IN_EDITOR).asBoolean(false);
    }
}
