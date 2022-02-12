/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.bootstrap.tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.jboss.hal.config.Environment;
import org.jboss.hal.config.Settings;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.resources.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Completable;

import static com.google.common.base.Strings.emptyToNull;
import static elemental2.dom.DomGlobal.document;

public class SetTitle implements BootstrapTask {

    private static final Logger logger = LoggerFactory.getLogger(SetTitle.class);
    private static final String NAME_PLACEHOLDER = "%n";
    private static final String ORGANIZATION_PLACEHOLDER = "%o";

    private final Settings settings;
    private final Map<String, Supplier<String>> data;

    @Inject
    public SetTitle(Environment environment, Settings settings) {
        this.settings = settings;
        this.data = new HashMap<>();

        data.put(NAME_PLACEHOLDER, environment::getName);
        data.put(ORGANIZATION_PLACEHOLDER, environment::getOrganization);
    }

    @Override
    public Completable call(FlowContext context) {
        String title = settings.get(Settings.Key.TITLE).value();
        if (emptyToNull(title) != null) {
            for (Map.Entry<String, Supplier<String>> entry : data.entrySet()) {
                if (title.contains(entry.getKey())) {
                    String value = entry.getValue().get();
                    if (emptyToNull(value) != null) {
                        title = title.replace(entry.getKey(), value);
                    } else {
                        logger.error("Value for placeholder '{}' in custom title is undefined. " +
                                "Fall back to built in title.", entry.getKey());
                        title = Names.BROWSER_FALLBACK_TITLE;
                        break;
                    }
                }
            }
            document.title = title;
        }
        return Completable.complete();
    }
}
