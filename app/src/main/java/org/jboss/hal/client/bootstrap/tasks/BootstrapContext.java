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
package org.jboss.hal.client.bootstrap.tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

class BootstrapContext {

    private final Stack<Object> stack;
    private final Map<String, Object> data;

    BootstrapContext() {
        this.stack = new Stack<>();
        this.data = new HashMap<>();
    }

    <T> void push(T value) {
        stack.push(value);
    }

    <T> T pop() {
        return (T) stack.pop();
    }

    <T> void set(String key, T value) {
        data.put(key, value);
    }

    <T> T get(String key) {
        return (T) data.get(key);
    }
}
