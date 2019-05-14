/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

import elemental2.core.JsArray;
import elemental2.promise.Promise;
import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;
import org.jboss.hal.db.AllDocsResponse.Row;

import static jsinterop.annotations.JsPackage.GLOBAL;

@JsType(isNative = true, namespace = GLOBAL)
public class PouchDB {

    @JsConstructor
    public PouchDB(String name) {
    }


    // ------------------------------------------------------ get

    public native Promise<Document> get(String id);

    /** Returns the documents for the specified ids. Only documents with existing IDs will be returned. */
    @JsOverlay
    public final Promise<List<Document>> getAll(Set<String> ids) {
        AllDocsOptions options = new AllDocsOptions();
        options.include_docs = true;
        options.keys = new JsArray<>();
        for (String id : ids) {
            options.keys.push(id);
        }

        return allDocs(options).then(response -> {
            List<Document> documents = new ArrayList<>();
            for (int i = 0; i < response.rows.getLength(); i++) {
                Row row = response.rows.getAt(i);
                if (!"not_found".equals(row.error)) {
                    documents.add(row.doc);
                }
            }
            return Promise.resolve(documents);
        });
    }

    /** Returns all documents whose ID starts with the specified ID. */
    @JsOverlay
    public final Promise<List<Document>> prefixSearch(String id) {
        AllDocsOptions options = new AllDocsOptions();
        options.include_docs = true;
        options.startkey = id;
        options.endkey = id + "\ufff0";

        return allDocs(options).then(response -> {
            List<Document> documents = new ArrayList<>();
            for (int i = 0; i < response.rows.getLength(); i++) {
                Row row = response.rows.getAt(i);
                if (!"not_found".equals(row.error)) {
                    documents.add(row.doc);
                }
            }
            return Promise.resolve(documents);
        });
    }

    native Promise<AllDocsResponse> allDocs(AllDocsOptions options);


    // ------------------------------------------------------ put

    @JsOverlay
    public final Promise<String> put(Document document) {
        return internalPut(document).then(response -> Promise.resolve(response.id));
    }

    @JsOverlay
    public final Promise<Set<String>> putAll(List<Document> documents) {
        JsArray<Document> docs = new JsArray<>();
        for (Document document : documents) {
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
    native Promise<PutResponse> internalPut(Document document);

    @JsMethod
    native Promise<JsArray<BulkDocsSingleUnionType>> bulkDocs(JsArray<Document> documents);
}
