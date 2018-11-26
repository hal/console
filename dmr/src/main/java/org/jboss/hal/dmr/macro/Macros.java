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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import elemental2.webstorage.Storage;
import elemental2.webstorage.WebStorageWindow;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.resources.Ids;

import static elemental2.dom.DomGlobal.window;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/** Repository for macros. */
public class Macros {

    private final Map<String, Macro> macros;
    private final Storage storage;
    private Macro current;
    private MacroOptions options;

    public Macros() {
        storage = WebStorageWindow.of(window).localStorage;
        macros = load();
    }

    private Map<String, Macro> load() {
        Map<String, Macro> macros = new HashMap<>();
        if (storage != null) {
            for (int i = 0; i < storage.getLength(); i++) {
                String key = storage.key(i);
                if (key.startsWith(Ids.MACRO_STORAGE)) {
                    Macro macro = deserialize(storage.getItem(key));
                    macros.put(macro.getName(), macro);
                }
            }
        }
        current = null;
        options = null;
        return macros;
    }

    private Macro deserialize(String base64) {
        ModelNode modelNode = ModelNode.fromBase64(base64);
        String name = modelNode.get(NAME).asString();
        String description = modelNode.get(DESCRIPTION).asString();
        Macro macro = new Macro(name, description);

        List<ModelNode> operations = modelNode.get(ModelDescriptionConstants.OPERATIONS).asList();
        for (ModelNode operation : operations) {

            if (COMPOSITE.equals(operation.get(OP).asString())) {
                List<ModelNode> steps = operation.get(STEPS).asList();
                List<Operation> macroOperations = new ArrayList<>(steps.size());
                for (ModelNode step : steps) {
                    macroOperations.add(new Operation(step));
                }
                macro.addOperation(new Composite(macroOperations));

            } else {
                macro.addOperation(new Operation(operation));
            }
        }

        macro.seal();
        return macro;
    }

    private String serialize(Macro macro) {
        ModelNode modelNode = new ModelNode();
        modelNode.get(NAME).set(macro.getName());
        if (macro.getDescription() != null) {
            modelNode.get(DESCRIPTION).set(macro.getDescription());
        }
        for (Operation operation : macro.getOperations()) {
            modelNode.get(OPERATIONS).add(operation);
        }
        return modelNode.toBase64String();
    }

    public void remove(Macro macro) {
        macros.remove(macro.getName());
        if (storage != null) {
            storage.removeItem(macro.getId());
        }

    }

    public int size() {
        return macros.size();
    }

    public boolean isEmpty() {
        return macros.isEmpty();
    }

    public boolean contains(Macro macro) {
        return macros.containsKey(macro.getName());
    }

    public Macro get(String name) {
        return macros.get(name);
    }

    public List<Macro> getMacros() {
        return Collections.unmodifiableList(new ArrayList<>(macros.values()));
    }

    public void startRecording(Macro macro, MacroOptions options) {
        this.current = macro;
        this.options = options;
    }

    public void stopRecording() {
        if (current != null) {
            current.seal();
            macros.put(current.getName(), current);
            if (storage != null) {
                storage.setItem(current.getId(), serialize(current));
            }

            current = null;
            options = null;
        }
    }

    public Macro current() {
        return current;
    }

    public MacroOptions currentOptions() {
        return options;
    }
}
