/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.client.bootstrap;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.jboss.hal.dmr.dispatch.DispatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Harald Pehl
 */
public class BootstrapContext {

    public static final String ERROR = "error";
    public static final String STATUS_CODE = "status-code";

    private final Map<String, Object> data;
    private String failure;
    private Throwable exception;

    public BootstrapContext() {
        this.data = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }

    public <T> void set(String key, T value) {
        data.put(key, value);
    }

    public void failed(String reaspon) {
        failure = reaspon;
    }

    public void failed(Throwable exception) {
        this.exception = exception;
    }

    public boolean hasError() {
        return failure != null || exception != null;
    }

    public String getFailure() {
        return failure;
    }

    public Throwable getException() {
        return exception;
    }

    public Multimap<String, String> getErrors() {
        ArrayListMultimap<String, String> multimap = ArrayListMultimap.create();
        if (failure != null) {
            multimap.put(ERROR, failure);
        }
        else if (exception != null) {
            multimap.put(ERROR, exception.getMessage());
            if (exception instanceof DispatchException) {
                multimap.put(STATUS_CODE, String.valueOf(((DispatchException) exception).getStatusCode()));
            }
        }
        return multimap;
    }
}
