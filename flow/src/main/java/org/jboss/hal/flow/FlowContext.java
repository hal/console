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
package org.jboss.hal.flow;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * General purpose context to be used inside a flow. Provides a {@linkplain Progress progress indicator}, a stack and a
 * map for sharing data between tasks.
 */
public final class FlowContext {

    public final Progress progress;
    private final Stack<Object> stack;
    private final Map<String, Object> data;

    public FlowContext() {
        this(Progress.NOOP);
    }

    public FlowContext(Progress progress) {
        this.progress = progress;
        this.stack = new Stack<>();
        this.data = new HashMap<>();
    }

    /**
     * Pushes the value om top of the context stack.
     */
    public <T> void push(T value) {
        stack.push(value);
    }

    /**
     * Removes the object at the top of the context stack and returns that object.
     *
     * @return The object at the top of the context stack.
     */
    @SuppressWarnings("unchecked")
    public <T> T pop() {
        return (T) stack.pop();
    }

    /**
     * @return {@code true} if the context stack is empty, {@code false} otherwise.
     */
    public boolean emptyStack() {return stack.empty();}

    /**
     * Stores the value under the given key in the context map.
     */
    public <T> void set(String key, T value) {
        data.put(key, value);
    }

    /**
     * @return the object for the given key from the context map or {@code null} if no such key was found.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }

    @Override
    public String toString() {
        return "FlowContext {stack: " + stack + ", map: " + data + "}";
    }
}
