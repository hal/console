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
package org.jboss.hal.meta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.hal.db.Document;
import org.jboss.hal.db.PouchDB;
import org.jboss.hal.dmr.ResourceAddress;
import rx.Single;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/** Abstract database which uses the specified statement context to resolve address templates. */
public abstract class AbstractDatabase<T> implements Database<T> {

    private StatementContext statementContext;
    private final String type;

    protected AbstractDatabase(StatementContext statementContext, String type) {
        this.statementContext = statementContext;
        this.type = type;
    }

    @Override
    public Map<ResourceAddress, AddressTemplate> addressLookup(Set<AddressTemplate> templates) {
        return templates.stream().collect(toMap(template -> template.resolve(statementContext), identity()));
    }

    @Override
    public Single<Map<ResourceAddress, T>> getAll(Set<AddressTemplate> templates) {
        Map<String, ResourceAddress> idToAddress = templates.stream()
                .map(template -> template.resolve(statementContext))
                .collect(toMap(address -> address.toString(), identity()));

        return Single.create(em -> database().getAll(idToAddress.keySet())
                .then(documents -> {
                    Map<ResourceAddress, T> metadata = new HashMap<>();
                    for (Map.Entry<String, Document> entry : documents.entrySet()) {
                        String id = entry.getKey();
                        Document document = entry.getValue();
                        metadata.put(idToAddress.get(id), asMetadata(document));
                    }
                    em.onSuccess(metadata);
                    return null;
                })
                .catch_(failure -> {
                    em.onError(new RuntimeException(String.valueOf(failure)));
                    return null;
                }));
    }

    @Override
    public Single<Set<String>> putAll(Map<ResourceAddress, T> metadata) {
        List<Document> documents = metadata.entrySet().stream()
                .map(entry -> asDocument(entry.getKey(), entry.getValue()))
                .collect(toList());
        return Single.create(em -> database().putAll(documents)
                .then(ids -> {
                    em.onSuccess(ids);
                    return null;
                })
                .catch_(failure -> {
                    em.onError(new RuntimeException(String.valueOf(failure)));
                    return null;
                }));
    }

    protected abstract T asMetadata(Document document);

    protected abstract Document asDocument(ResourceAddress address, T metadata);

    protected abstract PouchDB database();
}
