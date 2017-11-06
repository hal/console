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
import java.util.List;
import java.util.Map;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
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

    private static final int BUCKET_SIZE = 5;
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
        List<WriteBucket> buckets = new ArrayList<>();
        buckets.addAll(partition(resourceDescriptionDatabase, context.toResourceDescriptionDatabase, BUCKET_SIZE));
        buckets.addAll(partition(securityContextDatabase, context.toSecurityContextDatabase, BUCKET_SIZE));

        if (!buckets.isEmpty()) {
            Observable.interval(INTERVAL, INTERVAL, MILLISECONDS)
                    .take(buckets.size())
                    .subscribe(next -> buckets.get(next.intValue()).write(),
                            error -> logger.error("Unable to update database: {}", error.getMessage()));
        }
    }

    private <T> List<WriteBucket<T>> partition(Database<T> database, Map<ResourceAddress, T> metadata, int size) {
        List<WriteBucket<T>> buckets = new ArrayList<>();
        List<List<ResourceAddress>> partitions = Lists.partition(new ArrayList<>(metadata.keySet()), size);
        for (List<ResourceAddress> keys : partitions) {
            Map<ResourceAddress, T> chunk = new HashMap<>();
            for (ResourceAddress key : keys) {
                chunk.put(key, metadata.get(key));
            }
            buckets.add(new WriteBucket<>(database, chunk));
        }
        return buckets;
    }


    private static class WriteBucket<T> {

        private final Database<T> database;
        private final Map<ResourceAddress, T> metadata;

        private WriteBucket(Database<T> database, Map<ResourceAddress, T> metadata) {
            this.database = database;
            this.metadata = metadata;
        }

        private void write() {
            Stopwatch watch = Stopwatch.createStarted();
            database.putAll(metadata).subscribe(ids -> {
                logger.debug("Added {} {}s to the database in {} ms", ids.size(), database.type(),
                        watch.stop().elapsed(MILLISECONDS));
            });
        }
    }
}
