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
package org.jboss.hal.processor.mbui;

/**
 * @author Harald Pehl
 */
public class MetadataInfo {

    private static int counter = 0;

    private final String name;
    private final String template;

    MetadataInfo(final String template) {
        this.name = "metadata" + counter; //NON-NLS
        this.template = template;
        counter++;
    }

    public String getName() {
        return name;
    }

    public String getTemplate() {
        return template;
    }
}
