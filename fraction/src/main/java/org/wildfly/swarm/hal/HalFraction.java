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
package org.wildfly.swarm.hal;

import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configurable;

import static org.wildfly.swarm.hal.HalProperties.DEFAULT_CONTEXT;

@Configurable("swarm.hal")
public class HalFraction implements Fraction<HalFraction> {

    @AttributeDocumentation("Web context path of the console. Should start with a slash e.g. '/hal'.")
    private Defaultable<String> context = Defaultable.string(DEFAULT_CONTEXT);

    public HalFraction() {
        this.context.set(DEFAULT_CONTEXT);
    }

    public String context() {
        return context.get();
    }
}
