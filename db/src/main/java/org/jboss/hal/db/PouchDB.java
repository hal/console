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
package org.jboss.hal.db;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import elemental2.core.Array;
import elemental2.promise.Promise;
import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;

@JsType(isNative = true, namespace = GLOBAL)
public class PouchDB<T extends Document> {

    @JsConstructor
    public PouchDB(String name) {
    }


    // ------------------------------------------------------ get

    public native Promise<T> get(String id);

    native Promise<AllDocsResponse<T>> allDocs(AllDocsOptions options);

    @JsOverlay
    public final Promise<List<T>> getAll(Set<String> ids) {
        AllDocsOptions options = new AllDocsOptions();
        options.include_docs = true;
        options.keys = new Array<>();
        for (String id : ids) {
            options.keys.push(id);
        }

        return allDocs(options).then(response -> {
            List<T> documents = new ArrayList<>();
            for (int i = 0; i < response.rows.getLength(); i++) {
                documents.add(response.rows.getAt(i).doc);
            }
            return Promise.resolve(documents);
        });
    }


    // ------------------------------------------------------ put

    @JsOverlay
    public final Promise<String> put(T document) {
        return internalPut(document).then(response -> Promise.resolve(response.id));
    }

    @JsOverlay
    public final Promise<Set<String>> putAll(List<T> documents) {
        Array<T> docs = new Array<>();
        for (T document : documents) {
            docs.push(document);
        }
        return bulkDocs(docs).then(response -> {
            Set<String> ids = new HashSet<>();
            for (int i = 0; i < response.getLength(); i++) {
                BulkDocsSingleUnionType unionType = response.getAt(i);
                if (unionType.isSuccess()) {
                    ids.add(unionType.asSuccess().id);
                }
            }
            return Promise.resolve(ids);
        });
    }

    @JsMethod(name = "put")
    native Promise<PutResponse> internalPut(T document);

    @JsMethod
    native Promise<Array<BulkDocsSingleUnionType>> bulkDocs(Array<T> documents);
}
