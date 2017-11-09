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

import com.google.common.collect.Lists;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

class UpdateDatabase {

    private static final int RD_BUCKET_SIZE = 5;
    private static final int SC_BUCKET_SIZE = 15;
    private static final long RD_INTERVAL = 500; // ms
    private static final long SC_INTERVAL = 500; // ms
    @NonNls private static final Logger logger = LoggerFactory.getLogger(UpdateDatabase.class);

    private final WorkerChannel workerChannel;

    UpdateDatabase(WorkerChannel workerChannel) {
        this.workerChannel = workerChannel;
    }

    @SuppressWarnings("unchecked")
    public void post(LookupContext context) {
        if (context.updateDatabase()) {
            List<Map<ResourceAddress, ResourceDescription>> rdBuckets = partition(context.toResourceDescriptionDatabase,
                    RD_BUCKET_SIZE);
            List<Map<ResourceAddress, SecurityContext>> scBuckets = partition(context.toSecurityContextDatabase,
                    SC_BUCKET_SIZE);

            Observable rdObservable = Observable.interval(RD_INTERVAL, RD_INTERVAL, MILLISECONDS)
                    .take(rdBuckets.size())
                    .doOnNext(index -> {
                        if (index < rdBuckets.size()) {
                            Map<ResourceAddress, ResourceDescription> metadata = rdBuckets.get(index.intValue());
                            for (Map.Entry<ResourceAddress, ResourceDescription> entry : metadata.entrySet()) {
                                workerChannel.postResourceDescription(entry.getKey(), entry.getValue());
                            }
                        }
                    })
                    .onErrorResumeNext(error -> {
                        logger.error("Unable to post resource description: {}", error.getMessage());
                        return Observable.empty();
                    });
            Observable scObservable = Observable.interval(SC_INTERVAL, SC_INTERVAL, MILLISECONDS)
                    .take(scBuckets.size())
                    .doOnNext(index -> {
                        if (index < scBuckets.size()) {
                            Map<ResourceAddress, SecurityContext> metadata = scBuckets.get(index.intValue());
                            for (Map.Entry<ResourceAddress, SecurityContext> entry : metadata.entrySet()) {
                                workerChannel.postSecurityContext(entry.getKey(), entry.getValue());
                            }
                        }
                    })
                    .onErrorResumeNext(error -> {
                        logger.error("Unable to post security context: {}", error.getMessage());
                        return Observable.empty();
                    });
            Observable.concat(rdObservable, scObservable).subscribe();
        }
    }

    private <T> List<Map<ResourceAddress, T>> partition(Map<ResourceAddress, T> metadata, int size) {
        List<Map<ResourceAddress, T>> buckets = new ArrayList<>();
        List<List<ResourceAddress>> partitions = Lists.partition(new ArrayList<>(metadata.keySet()), size);
        for (List<ResourceAddress> keys : partitions) {
            Map<ResourceAddress, T> bucket = new HashMap<>();
            for (ResourceAddress key : keys) {
                bucket.put(key, metadata.get(key));
            }
            buckets.add(bucket);
        }
        return buckets;
    }
}
