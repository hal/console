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
package org.jboss.hal.dmr.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jboss.hal.dmr.ModelNode;

import static java.util.stream.Collectors.joining;
import static org.jboss.hal.dmr.ModelDescriptionConstants.COMPOSITE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STEPS;

/**
 * @author Harald Pehl
 */
public class Composite extends Operation implements Iterable<Operation> {

    private List<Operation> operations;

    public Composite(Operation first, Operation... rest) {
        super(COMPOSITE, ResourceAddress.ROOT, new ModelNode(), null);
        this.operations = new ArrayList<>();
        this.operations.add(first);
        if (rest != null) {
            Collections.addAll(operations, rest);
        }
        addSteps();
    }

    public Composite(List<Operation> operations) {
        super(COMPOSITE, ResourceAddress.ROOT, new ModelNode(), null);
        this.operations = new ArrayList<>();
        this.operations.addAll(operations);
        addSteps();
    }

    private void addSteps() {
        for (Operation operation : operations) {
            get(STEPS).add(operation);
        }
    }

    @Override
    public Iterator<Operation> iterator() {
        return operations.iterator();
    }

    public boolean isEmpty() {return operations.isEmpty();}

    public int size() {return operations.size();}

    @Override
    public String toString() {
        return "Composite(" + operations.size() + ")";
    }

    public String asCli() {
        return operations.stream().map(Operation::asCli).collect(joining("\n"));
    }
}
