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
package org.jboss.hal.core.finder;

/**
 * Handler which will be invoked when selecting one of the items in the breadcrumb dropdown.
 * <p>
 * When the handler navigates to another place which changes the finder path, the handler must call {@link
 * Finder#updatePath(FinderPath)} with the updated finder path!
 *
 * @author Harald Pehl
 */
@FunctionalInterface
public interface BreadcrumbItemHandler<T> {

    void execute(T item, FinderContext context);
}
