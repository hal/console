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
package org.jboss.hal.core.mvp;

import org.jboss.hal.core.finder.FinderPath;

public interface HasFinderPath {

    /**
     * Application presenters need to provide information about their path in the finder. Normally this path is updated
     * automatically when navigating in the finder. However since application presenters can also be revealed using the
     * breadcrumb dropdown or by entering the URL directly this information is crucial to restore the path in the finder
     * context.
     * <p>
     * Please make sure that the IDs for selected items in the finder path match to the IDs returned by
     * {@link org.jboss.hal.core.finder.ItemDisplay#getId()}
     * <p>
     * If this method returns {@code null} the path in the finder context is not touched.
     */
    FinderPath finderPath();
}
