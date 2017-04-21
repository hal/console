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
package org.jboss.hal.dmr;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * @author Harald Pehl
 */
@JsType
public class CompositeResult implements Iterable<ModelNode> {

    private final LinkedHashMap<String, ModelNode> steps;

    @JsIgnore
    public CompositeResult(ModelNode steps) {
        this.steps = new LinkedHashMap<>();
        if (steps.isDefined()) {
            for (Property property : steps.asPropertyList()) {
                this.steps.put(property.getName(), property.getValue());
            }
        }
    }

    /**
     * @param index zero-based!
     *
     * @return the related step result
     */
    @JsIgnore
    public ModelNode step(int index) {
        return step("step-" + (index + 1)); //NON-NLS
    }

    /**
     * @param step Step as "step-n" (one-based!)
     *
     * @return the related step result
     */
    public ModelNode step(String step) {
        if (steps.containsKey(step)) {
            return steps.get(step);
        }
        return new ModelNode();
    }

    @Override
    @JsIgnore
    public Iterator<ModelNode> iterator() {
        return steps.values().iterator();
    }

    @JsProperty(name = "size")
    public int size() {return steps.size();}

    @JsProperty
    public boolean isEmpty() {return steps.isEmpty();}

    @JsIgnore
    public Stream<ModelNode> stream() {
        return StreamSupport.stream(spliterator(), false);
    }


    // ------------------------------------------------------ JS methods

    @JsProperty(name = "steps")
    public ModelNode[] jsSteps() {
        return steps.values().toArray(new ModelNode[steps.size()]);
    }
}
