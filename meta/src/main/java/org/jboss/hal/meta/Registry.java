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

import org.jboss.hal.dmr.model.ResourceAddress;

/**
 * A registry for meta data such as resource descriptions or security context information. The registry has the
 * following contract for adding and resolving meta data:
 * <ol>
 * <li>The meta data is added using a concrete {@link ResourceAddress}</li>
 * <li>The meta data needs to be resolved using a generic {@link AddressTemplate}</li>
 * </ol>
 * <p>
 * It's up to the concrete implementation how to resolve the generic address template and lookup the associated meta
 * data using the concrete resource address. The recommendation is to use a {@link StatementContext} together with
 * {@link AddressTemplate#resolve(StatementContext, String...)} to turn the template into a concrete resource address.
 *
 * @author Harald Pehl
 */
interface Registry<T> {

    T lookup(final AddressTemplate template) throws MissingMetadataException;

    boolean contains(final AddressTemplate template);

    void add(final ResourceAddress address, final T metadata);
}
