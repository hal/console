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
package org.jboss.hal.ballroom.typeahead;

import com.google.gwt.core.client.js.JsFunction;
import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.Names.OBJECT;

/**
 * Mapping for the Bloodhound class from typeahead.js
 *
 * @author Harald Pehl
 * @see <a href="https://github.com/twitter/typeahead.js/blob/master/doc/bloodhound.md#api">https://github.com/twitter/typeahead.js/blob/master/doc/bloodhound.md#api</a>
 */
@JsType(isNative = true)
public class Bloodhound<T> {

    @JsFunction
    @FunctionalInterface
    public interface DatumTokenizer<T> {

        String[] tokenize(T datum);
    }


    @JsFunction
    @FunctionalInterface
    public interface QueryTokenizer {

        String[] tokenize(String query);
    }


    @JsFunction
    @FunctionalInterface
    public interface Identifier<T> {

        String identify(T datum);
    }


    @JsFunction
    @FunctionalInterface
    public interface Comparator<T> {

        int compare(T datum1, T datum2);
    }


    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    public static class Options<T> {

        public DatumTokenizer<T> datumTokenizer;
        public QueryTokenizer queryTokenizer;
        public Identifier<T> identify;
        public Comparator<T> sorter;
        public RemoteOptions<T> remote;
    }


    private final Options<T> options;

    @JsConstructor
    public Bloodhound(Options<T> options) {
        this.options = options;
    }
}
