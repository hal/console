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

/**
 * Repository for macros.
 *
 * @author Harald Pehl
 */
public class Macros {

    private final Map<String, Macro> macros;
    private Macro current;
    private MacroOptions options;

    public Macros() {
        macros = new HashMap<>();
        current = null;
    }

    public void remove(final Macro macro) {macros.remove(macro.getName());}

    public int size() {return macros.size();}

    public boolean isEmpty() {return macros.isEmpty();}

    public boolean contains(final Macro macro) {return macros.containsKey(macro.getName());}

    public Macro get(String name) {
        return macros.get(name);
    }

    public List<Macro> getMacros() {
        return Collections.unmodifiableList(new ArrayList<>(macros.values()));
    }

    public void start(Macro macro, MacroOptions options) {
        this.current = macro;
        this.options = options;
    }

    public void stop() {
        current.seal();
        macros.put(current.getName(), current);
        current = null;
        options = null;
    }

    public Macro current() {
        return current;
    }

    public MacroOptions currentOptions() {
        return options;
    }
}
