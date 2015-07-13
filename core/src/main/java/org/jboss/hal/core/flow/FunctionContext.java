/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.hal.core.flow;

import com.google.common.base.Function;
import org.jboss.hal.dmr.dispatch.DispatchException;

import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * General purpose context to be used for functions inside a flow. Provides a stack and a map for sharing data between
 * function calls.
 *
 * @author Harald Pehl
 */
public class FunctionContext {

    private final Stack<Object> stack;
    private final Map<String, Object> data;
    private Throwable error;
    private String errorMessage;

    public FunctionContext() {
        stack = new Stack<>();
        data = new HashMap<>();
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
     *
     * @throws EmptyStackException if this context stack is empty.
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

    /**
     * @return the object for the given key from the context map or the result of the provided function if no such key
     * was found.
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, Function<String, T> provideDefault) {
        T value = (T) data.get(key);
        if (value == null) {
            value = provideDefault.apply(key);
        }
        return value;
    }

    /**
     * Sets the error <em>and</em> error message.
     */
    public void setError(final Throwable error) {
        this.error = error;
        this.errorMessage = error.getMessage();
    }

    public Throwable getError() {
        return error;
    }

    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean hasError() {
        return error != null || errorMessage != null;
    }

    /**
     * @return {@code true} if the stored error is an instance of {@link org.jboss.hal.dmr.dispatch.DispatchException}
     * and its status code is 403.
     */
    public boolean isForbidden() {
        return (error instanceof DispatchException && ((DispatchException) error).getStatusCode() == 403);
    }

    @Override
    public String toString() {
        return "FunctionContext {stack: " + stack + ", map: " + data + "}";
    }
}
