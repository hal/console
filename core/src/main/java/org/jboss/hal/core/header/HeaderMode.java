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
package org.jboss.hal.core.header;

import org.jboss.hal.dmr.ResourceAddress;

import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.dispatch.annotation.Optional;
import com.gwtplatform.dispatch.annotation.Order;

/** Payload for the {@link HeaderModeEvent}. The event is fired by presenters to change the state of the header. */
@GenEvent
public class HeaderMode {

    @Order(1) PresenterType presenterType;
    @Order(2)
    @Optional String token;
    @Order(3)
    @Optional String title;
    @Order(4)
    @Optional ResourceAddress expertModeAddress;
    @Order(5)
    @Optional boolean backToNormalMode;
    @Order(6)
    @Optional boolean supportsExternal;
    @Order(7)
    @Optional boolean refresh;
}
