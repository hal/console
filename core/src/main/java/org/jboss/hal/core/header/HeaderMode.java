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
package org.jboss.hal.core.header;

import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.dispatch.annotation.Optional;
import com.gwtplatform.dispatch.annotation.Order;
import org.jboss.hal.dmr.model.ResourceAddress;

/**
 * @author Harald Pehl
 */
@GenEvent
public class HeaderMode {

    @Order(1) PresenterType presenterType;
    @Order(2) @Optional String token;
    @Order(3) @Optional String title;
    @Order(4) @Optional boolean external;
    @Order(5) @Optional ResourceAddress expertMode;
    @Order(6) @Optional boolean normalMode;
}
