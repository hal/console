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

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import org.jboss.hal.meta.AddressTemplate;

import static org.jboss.hal.meta.security.Target.ATTRIBUTE;
import static org.jboss.hal.meta.security.Target.OPERATION;

/** A constraint for an attribute or operation of a DMR resource. */
public class Constraint {

    public static Constraint executable(final AddressTemplate template, final String operation) {
        return new Constraint(template, operation, OPERATION, Permission.EXECUTABLE);
    }

    public static Constraint writable(final AddressTemplate template, final String attribute) {
        return new Constraint(template, attribute, ATTRIBUTE, Permission.WRITABLE);
    }

    @SuppressWarnings("DuplicateStringLiteralInspection")
    public static Constraint parse(String input) throws IllegalArgumentException {
        if (!CONSTRAINT_REGEX.test(input)) {
            throw new IllegalArgumentException("Invalid constraint: " + input);
        }
        MatchResult result = CONSTRAINT_REGEX.exec(input);
        if (result.getGroupCount() != 5) {
            throw new IllegalArgumentException("Invalid constraint: " + input);
        }
        return new Constraint(AddressTemplate.of(result.getGroup(2)), result.getGroup(4),
                Target.parse(result.getGroup(3)), Permission.valueOf(result.getGroup(1).toUpperCase()));
    }


    private static final RegExp CONSTRAINT_REGEX = RegExp.compile(
            "^(readable|writable|executable)\\(([\\w{}=*\\-\\/\\.]+)(:|@)([\\w\\-]+)\\)$"); //NON-NLS

    private final AddressTemplate template;
    private final Target target;
    private final String name;
    private final Permission permission;

    private Constraint(final AddressTemplate template, final String name, final Target target,
            final Permission permission) {
        this.template = template;
        this.target = target;
        this.name = name;
        this.permission = permission;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Constraint)) {
            return false;
        }

        Constraint that = (Constraint) o;
        if (!template.equals(that.template)) {
            return false;
        }
        if (target != that.target) {
            return false;
        }
        if (!name.equals(that.name)) {
            return false;
        }
        return permission == that.permission;
    }

    @Override
    public int hashCode() {
        int result = template.hashCode();
        result = 31 * result + target.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + permission.hashCode();
        return result;
    }

    @Override
    public String toString() {
        // Do NOT change the format, Constraint.parseSingle() relies on it!
        return permission.name().toLowerCase() + "(" + template + target.symbol() + name + ")";
    }

    public String data() {
        return toString();
    }

    public AddressTemplate getTemplate() {
        return template;
    }

    public Target getTarget() {
        return target;
    }

    public String getName() {
        return name;
    }

    public Permission getPermission() {
        return permission;
    }
}
