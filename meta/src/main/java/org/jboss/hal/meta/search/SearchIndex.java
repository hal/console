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
package org.jboss.hal.meta.search;

import java.util.Set;

import org.jboss.hal.spi.Keywords;

public interface SearchIndex {

    /**
     * Returns the tokens for the given operation mode which are not excluded by {@link Keywords#exclude()}.
     *
     * @param standalone the execution mode
     *
     * @return a set of matching tokens
     */
    Set<String> getTokens(boolean standalone);

    Set<String> getResources(String token);

    Set<String> getKeywords(String token);
}
