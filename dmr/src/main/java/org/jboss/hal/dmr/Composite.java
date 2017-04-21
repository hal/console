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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.joining;
import static org.jboss.hal.dmr.ModelDescriptionConstants.COMPOSITE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OPERATION_HEADERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STEPS;

/**
 * @author Harald Pehl
 */
@JsType
public class Composite extends Operation implements Iterable<Operation> {

    private List<Operation> operations;

    public Composite() {
        super(COMPOSITE, ResourceAddress.root(), new ModelNode(), new ModelNode(), emptySet());
        this.operations = new ArrayList<>();
    }

    @JsIgnore
    public Composite(Operation first, Operation... rest) {
        this();
        add(first);
        if (rest != null) {
            for (Operation operation : rest) {
                add(operation);
            }
        }
    }

    @JsIgnore
    public Composite(List<Operation> operations) {
        this();
        operations.forEach(this::add);
    }

    @JsMethod(name = "addOperation")
    public Composite add(Operation operation) {
        operations.add(operation);
        get(STEPS).add(operation);
        return this;
    }

    @JsIgnore
    public Composite addHeader(String name, String value) {
        get(OPERATION_HEADERS).get(name).set(value);
        return this;
    }

    @JsIgnore
    public Composite addHeader(String name, boolean value) {
        get(OPERATION_HEADERS).get(name).set(value);
        return this;
    }

    @Override
    @JsIgnore
    public Iterator<Operation> iterator() {
        return operations.iterator();
    }

    public boolean isEmpty() {return operations.isEmpty();}

    public int size() {return operations.size();}

    @JsIgnore
    public Composite runAs(final Set<String> runAs) {
        List<Operation> runAsOperations = operations.stream()
                .map(operation -> operation.runAs(runAs))
                .collect(Collectors.toList());
        return new Composite(runAsOperations);
    }

    @Override
    public String toString() {
        return "Composite(" + operations.size() + ")";
    }

    public String asCli() {
        return operations.stream().map(Operation::asCli).collect(joining("\n"));
    }
}
