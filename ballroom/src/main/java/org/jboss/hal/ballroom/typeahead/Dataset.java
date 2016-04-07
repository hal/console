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

import elemental.js.json.JsJsonObject;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.UIConstants.OBJECT;

/**
 * Mapping for the dataset options from typeahead.js
 *
 * @author Harald Pehl
 * @see <a href="https://github.com/twitter/typeahead.js/blob/master/doc/jquery_typeahead.md#options">https://github.com/twitter/typeahead.js/blob/master/doc/jquery_typeahead.md#options</a>
 */
@JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
public class Dataset {

    @JsFunction
    @FunctionalInterface
    public interface Source {

        void source(String query, SyncCallback syncCallback, AsyncCallback asyncCallback);
    }


    public Source source;
    public boolean async;
    public int limit;
    public Display display;
    public Templates templates;
}
