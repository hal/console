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
package org.jboss.hal.ballroom.typeahead;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.UIConstants.OBJECT;

/**
 * Mapping for the Bloodhound class from typeahead.js
 *
 * @author Harald Pehl
 * @see <a href="https://github.com/twitter/typeahead.js/blob/master/doc/bloodhound.md#api">https://github.com/twitter/typeahead.js/blob/master/doc/bloodhound.md#api</a>
 */
@JsType(isNative = true, namespace = GLOBAL)
public class Bloodhound {

    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    public static class Options {

        public DataTokenizer datumTokenizer;
        public QueryTokenizer queryTokenizer;
        public Identifier identify;
        public int sufficient;
        public Comparator sorter;
        public RemoteOptions remote;
    }


    @JsConstructor
    public Bloodhound(Options options) {}

    public native void search(String query, SyncCallback syncCallback, AsyncCallback asyncCallback);

    public native void clearRemoteCache();
}
