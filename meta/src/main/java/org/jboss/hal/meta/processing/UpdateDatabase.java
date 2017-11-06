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
package org.jboss.hal.meta.processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Stopwatch;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.Database;
import org.jboss.hal.meta.description.ResourceDescriptionDatabase;
import org.jboss.hal.meta.security.SecurityContextDatabase;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

class UpdateDatabase {

    private static final int BUCKET_SIZE = 20;
    private static final long INTERVAL = 500; // ms
    @NonNls private static final Logger logger = LoggerFactory.getLogger(UpdateDatabase.class);

    private final ResourceDescriptionDatabase resourceDescriptionDatabase;
    private final SecurityContextDatabase securityContextDatabase;

    UpdateDatabase(ResourceDescriptionDatabase resourceDescriptionDatabase,
            SecurityContextDatabase securityContextDatabase) {
        this.resourceDescriptionDatabase = resourceDescriptionDatabase;
        this.securityContextDatabase = securityContextDatabase;
    }

    void update(LookupContext context) {
        LinkedList<WriteBucket> buckets = new LinkedList<>();
        if (!context.toResourceDescriptionDatabase.isEmpty()) {
            if (context.toResourceDescriptionDatabase.size() > BUCKET_SIZE) {
                buckets.addAll(partition(resourceDescriptionDatabase, "resource description",
                        context.toResourceDescriptionDatabase, BUCKET_SIZE));
            }
        }
        if (!context.toSecurityContextDatabase.isEmpty()) {
            if (context.toSecurityContextDatabase.size() > BUCKET_SIZE) {
                buckets.addAll(partition(securityContextDatabase, "security context",
                        context.toSecurityContextDatabase, BUCKET_SIZE));
            }
        }
        if (!buckets.isEmpty()) {
            Observable.interval(INTERVAL, MILLISECONDS)
                    .takeUntil(l -> !buckets.isEmpty())
                    .doOnEach(l -> buckets.removeFirst().write())
                    .subscribe();
        }
    }

    private <T> List<WriteBucket<T>> partition(Database<T> database, String type, Map<ResourceAddress, T> metadata,
            int size) {
        int index = 0;
        Map<ResourceAddress, T> chunk = new HashMap<>();
        List<WriteBucket<T>> buckets = new ArrayList<>();

        for (Iterator<ResourceAddress> iterator = metadata.keySet().iterator(); iterator.hasNext(); ) {
            ResourceAddress address = iterator.next();
            if (index < size) {
                chunk.put(address, metadata.get(address));
                index++;
            } else {
                buckets.add(new WriteBucket<>(database, type, chunk));
                index = 0;
                chunk = new HashMap<>();
            }
            iterator.remove();
        }
        return buckets;
    }


    private static class WriteBucket<T> {

        private final Database<T> database;
        private final String type;
        private final Map<ResourceAddress, T> metadata;

        private WriteBucket(Database<T> database, String type, Map<ResourceAddress, T> metadata) {
            this.database = database;
            this.type = type;
            this.metadata = metadata;
        }

        private void write() {
            Stopwatch watch = Stopwatch.createStarted();
            database.putAll(metadata).subscribe(ids -> {
                logger.debug("Added {} {} to resource description database in {} ms", ids.size(), type,
                        watch.stop().elapsed(MILLISECONDS));
            });
        }
    }
}
