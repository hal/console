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
package org.jboss.hal.meta.description;

import java.util.List;
import java.util.function.Consumer;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

final class SegmentProcessor {

    static void process(List<String[]> segments, Consumer<String[]> consumer) {
        if (segments != null) {
            int index = 0;
            int length = segments.size();
            for (String[] segment : segments) {
                String key = segment[0];
                String value = segment[1];
                if (key != null && value != null) {
                    switch (key) {
                        case HOST:
                            if (length > 1 && index == 0) {
                                value = "*";
                            }
                            break;

                        case PROFILE:
                        case SERVER_GROUP:
                            if (index == 0) {
                                value = "*";
                            }
                            break;

                        case SERVER:
                        case SERVER_CONFIG:
                            if (index == 1) {
                                value = "*";
                            }
                            break;
                        default:
                            break;
                    }
                }
                consumer.accept(new String[]{key, value});
                index++;
            }
        }
    }

    private SegmentProcessor() {
    }
}
