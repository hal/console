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

import java.util.List;
import java.util.Set;

import org.jboss.hal.db.Document;
import org.jboss.hal.db.PouchDB;
import org.jboss.hal.dmr.ResourceAddress;
import rx.Single;

/** Abstract database which uses the specified statement context to resolve the address template. */
public abstract class AbstractDatabase<T extends Document> implements Database<T> {

    private StatementContext statementContext;
    private final String type;

    protected AbstractDatabase(StatementContext statementContext, String type) {
        this.statementContext = statementContext;
        this.type = type;
    }

    @Override
    public Single<T> lookup(AddressTemplate template) {
        ResourceAddress address = template.resolve(statementContext);
        return Single.create(em -> database().get(address.toString())
                .then(document -> {
                    em.onSuccess(document);
                    return null;
                })
                .catch_(failure -> {
                    em.onError(new RuntimeException(String.valueOf(failure)));
                    return null;
                }));
    }

    @Override
    public Single<Set<String>> addAll(List<T> documents) {
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

    protected abstract PouchDB<T> database();
}
