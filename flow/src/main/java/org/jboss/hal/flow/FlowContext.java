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
package org.jboss.hal.flow;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;

/**
 * General purpose context to be used inside a {@linkplain Flow flow} and {@linkplain Task tasks}. Provides a
 * {@linkplain Progress progress indicator}, a stack and a map for sharing data between tasks.
 */
public class FlowContext {

    private final Stack<Object> stack;
    private final Map<String, Object> data;
    private SafeHtml failure;
    final Progress progress;

    public FlowContext() {
        this(Progress.NOOP);
    }

    public FlowContext(Progress progress) {
        this.progress = progress;
        this.stack = new Stack<>();
        this.data = new HashMap<>();
        this.failure = null;
    }

    /**
     * Pushes the value om top of the context stack.
     */
    public <T> FlowContext push(T value) {
        stack.push(value);
        return this;
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

    @SuppressWarnings("unchecked")
    public <T> T pop(T defaultValue) {
        return emptyStack() ? defaultValue : (T) stack.pop();
    }

    /**
     * @return {@code true} if the context stack is empty, {@code false} otherwise.
     */
    public boolean emptyStack() {
        return stack.empty();
    }

    /**
     * Stores the value under the given key in the context map.
     */
    public <T> FlowContext set(String key, T value) {
        data.put(key, value);
        return this;
    }

    /**
     * @return the object for the given key from the context map or {@code null} if no such key was found.
     */
    public <T> T get(String key) {
        return get(key, null);
    }

    /**
     * @return the object for the given key from the context map or {@code null} if no such key was found.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        return (T) data.getOrDefault(key, defaultValue);
    }

    /**
     * @return the object for the given key from the context map or {@code null} if no such key was found.
     */
    public Set<String> keys() {
        return data.keySet();
    }

    public FlowContext failure(SafeHtml failure) {
        this.failure = failure;
        return this;
    }

    public boolean hasFailure() {
        return failure != null;
    }

    public void showFailure(EventBus eventBus) {
        if (hasFailure()) {
            MessageEvent.fire(eventBus, Message.error(failure));
        }
    }

    @Override
    public String toString() {
        return "FlowContext {stack: " + stack + ", map: " + data + "}";
    }
}
