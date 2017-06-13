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

package org.jboss.hal.client.configuration.subsystem.elytron;

import java.util.Map;
import java.util.Set;

import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.spi.Callback;

/**
 * Constains the standard methods to be used from the ResourceView class, it must be implemented in the present classes.
 *
 * @author Claudio Miranda <claudio@redhat.com>
 */
public interface ElytronPresenter {


    void saveForm(final String title, final String name, final AddressTemplate template,
            final Map<String, Object> changedValues, final Metadata metadata);

    void saveComplexForm(final String title, final String name, String complexAttributeName,
            final AddressTemplate template,
            final Map<String, Object> changedValues, final Metadata metadata);

    void resetComplexAttribute(String type, String name, AddressTemplate template, Set<String> attributes,
            Metadata metadata, Callback callback);

    void reload();
}
