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
import java.util.List;
import java.util.Map;

import com.google.common.base.Stopwatch;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.ResourceDescriptionDatabase;
import org.jboss.hal.meta.description.ResourceDescriptionDocument;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextDatabase;
import org.jboss.hal.meta.security.SecurityContextDocument;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

class UpdateDatabase {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(UpdateDatabase.class);

    private final ResourceDescriptionDatabase resourceDescriptionDatabase;
    private final SecurityContextDatabase securityContextDatabase;

    UpdateDatabase(ResourceDescriptionDatabase resourceDescriptionDatabase,
            SecurityContextDatabase securityContextDatabase) {
        this.resourceDescriptionDatabase = resourceDescriptionDatabase;
        this.securityContextDatabase = securityContextDatabase;
    }

    void update(List<RrdResult> rrdResults) {
        Stopwatch pdWatch = Stopwatch.createStarted();
        List<ResourceDescriptionDocument> resourceDescriptionDocuments = new ArrayList<>();
        List<SecurityContextDocument> securityContextDocuments = new ArrayList<>();
        for (RrdResult rrdResult : rrdResults) {
            for (Map.Entry<ResourceAddress, ResourceDescription> entry : rrdResult.resourceDescriptions.entrySet()) {
                resourceDescriptionDocuments.add(new ResourceDescriptionDocument(entry.getKey(), entry.getValue()));
            }
            for (Map.Entry<ResourceAddress, SecurityContext> entry : rrdResult.securityContexts.entrySet()) {
                securityContextDocuments.add(new SecurityContextDocument(entry.getKey(), entry.getValue()));
            }
        }
        logger.debug("Prepared documents for database update in {} ms", pdWatch.stop().elapsed(MILLISECONDS));

        Stopwatch rdWatch = Stopwatch.createStarted();
        resourceDescriptionDatabase.addAll(resourceDescriptionDocuments).subscribe(ids -> {
            logger.debug("Updated resource description database in {} ms", rdWatch.stop().elapsed(MILLISECONDS));
        });
        Stopwatch scWatch = Stopwatch.createStarted();
        securityContextDatabase.addAll(securityContextDocuments).subscribe(ids -> {
            logger.debug("Updated security context database in {} ms", scWatch.stop().elapsed(MILLISECONDS));
        });
    }
}
