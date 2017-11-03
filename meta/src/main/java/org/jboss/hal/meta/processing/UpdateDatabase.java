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

import com.google.common.base.Stopwatch;
import org.jboss.hal.meta.description.ResourceDescriptionDatabase;
import org.jboss.hal.meta.security.SecurityContextDatabase;
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

    void update(LookupContext context) {
        Stopwatch rdWatch = Stopwatch.createStarted();
        resourceDescriptionDatabase.putAll(context.toResourceDescriptionDatabase).subscribe(ids -> {
            rdWatch.stop();
            logger.debug("Added resource descriptions for {} to database in {} ms", ids, rdWatch.elapsed(MILLISECONDS));
        });
        Stopwatch scWatch = Stopwatch.createStarted();
        securityContextDatabase.putAll(context.toSecurityContextDatabase).subscribe(ids -> {
            scWatch.stop();
            logger.debug("Added security contexts for {} to database in {} ms", ids, scWatch.elapsed(MILLISECONDS));
        });
    }
}
