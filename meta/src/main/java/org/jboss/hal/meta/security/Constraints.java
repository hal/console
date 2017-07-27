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
package org.jboss.hal.meta.security;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.base.Splitter;
import org.jetbrains.annotations.NotNull;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.jboss.hal.meta.security.Constraints.Operator.AND;
import static org.jboss.hal.meta.security.Constraints.Operator.OR;

/** A set of {@linkplain Constraint constraints} with an operator. */
public class Constraints implements Iterable<Constraint> {

    public enum Operator {
        AND("&"), OR("|");


        private final String operator;

        Operator(final String operator) {
            this.operator = operator;
        }

        public String operator() {
            return operator;
        }
    }


    // ------------------------------------------------------ factory methods

    public static Constraints single(Constraint constraint) {
        return new Constraints(newHashSet(constraint), AND);
    }

    public static Constraints and(Constraint first, Constraint... rest) {
        HashSet<Constraint> constraints = new HashSet<>();
        constraints.add(first);
        if (rest != null) {
            constraints.addAll(asList(rest));
        }
        return new Constraints(constraints, AND);
    }

    public static Constraints and(Iterable<Constraint> constraints) {
        return new Constraints(newHashSet(constraints), AND);
    }

    public static Constraints or(Constraint first, Constraint... rest) {
        HashSet<Constraint> constraints = new HashSet<>();
        constraints.add(first);
        if (rest != null) {
            constraints.addAll(asList(rest));
        }
        return new Constraints(constraints, OR);
    }

    public static Constraints or(Iterable<Constraint> constraints) {
        return new Constraints(newHashSet(constraints), OR);
    }

    /**
     * Creates an empty instance using {@link Operator#AND} as operator.
     */
    public static Constraints empty() {
        return new Constraints(new HashSet<>(), AND);
    }


    // ------------------------------------------------------ parse

    public static Constraints parse(String input) {
        if (input != null) {
            Operator operator;
            if (input.contains(AND.operator)) {
                operator = AND;
            } else if (input.contains(OR.operator)) {
                operator = OR;
            } else {
                operator = AND;
            }
            Iterable<String> values = Splitter.on(operator.operator)
                    .omitEmptyStrings()
                    .trimResults()
                    .split(input);
            Set<Constraint> constraints = new HashSet<>();
            for (String value : values) {
                try {
                    constraints.add(Constraint.parse(value));
                } catch (IllegalArgumentException ignored) {}
            }
            return new Constraints(constraints, operator);

        } else {
            return empty();
        }
    }


    // ------------------------------------------------------ instance

    private final Set<Constraint> constraints;
    private final Operator operator;

    private Constraints(final Set<Constraint> constraints,
            final Operator operator) {
        this.constraints = constraints;
        this.operator = operator;
    }

    @NotNull
    @Override
    public Iterator<Constraint> iterator() {
        return constraints.iterator();
    }

    public int size() {return constraints.size();}

    public boolean isEmpty() {return constraints.isEmpty();}

    public boolean contains(final Object o) {return constraints.contains(o);}

    public Set<Constraint> getConstraints() {
        return constraints;
    }

    public Operator getOperator() {
        return operator;
    }
}
