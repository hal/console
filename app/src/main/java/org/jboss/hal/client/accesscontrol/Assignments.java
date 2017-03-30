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
package org.jboss.hal.client.accesscontrol;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import org.jboss.hal.config.Role;

/**
 * @author Harald Pehl
 */
class Assignments implements Iterable<Assignment> {

    // @formatter:off
    static final Comparator<Assignment> EXCLUDES_FIRST = (a1, a2) -> Boolean.compare(a1.isInclude(), a2.isInclude());
    static final Comparator<Assignment> STANDARD_FIRST = (a1, a2) -> a1.getRole().getType().compareTo(a2.getRole().getType());
    static final Comparator<Assignment> USERS_FIRST = (a1, a2) -> a1.getPrincipal().getType().compareTo(a2.getPrincipal().getType());
    static final Comparator<Assignment> BY_ROLE_NAME = (a1, a2) -> a1.getRole().getName().compareTo(a2.getRole().getName());
    static final Comparator<Assignment> BY_PRINCIPAL_NAME = (a1, a2) -> a1.getPrincipal().getName().compareTo(a2.getPrincipal().getName());
    // @formatter:on

    private final Set<Assignment> assignments;

    Assignments() {assignments = new HashSet<>();}

    boolean add(final Assignment assignment) {return assignments.add(assignment);}

    void clear() {assignments.clear();}

    Stream<Assignment> byPrincipal(Principal principal) {
        return assignments.stream().filter(assignment -> assignment.getPrincipal().equals(principal));
    }

    Stream<Assignment> includes(Principal principal) {
        return assignments.stream()
                .filter(assignment -> assignment.getPrincipal().equals(principal) && assignment.isInclude());
    }

    Stream<Assignment> excludes(Principal principal) {
        return assignments.stream()
                .filter(assignment -> assignment.getPrincipal().equals(principal) && !assignment.isInclude());
    }

    Stream<Assignment> byRole(Role role) {
        return assignments.stream().filter(assignment -> assignment.getRole().equals(role));
    }

    Stream<Assignment> includes(Role role) {
        return assignments.stream().filter(assignment -> assignment.getRole().equals(role) && assignment.isInclude());
    }

    Stream<Assignment> excludes(Role role) {
        return assignments.stream().filter(assignment -> assignment.getRole().equals(role) && !assignment.isInclude());
    }

    @Override
    public Iterator<Assignment> iterator() {
        return assignments.iterator();
    }
}
