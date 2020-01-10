/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.meta.security;

import java.util.BitSet;
import java.util.Optional;

import org.jboss.hal.config.AccessControlProvider;
import org.jboss.hal.config.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.hal.meta.security.Constraints.Operator.AND;
import static org.jboss.hal.meta.security.Permission.EXECUTABLE;
import static org.jboss.hal.meta.security.Permission.READABLE;
import static org.jboss.hal.meta.security.Permission.WRITABLE;
import static org.jboss.hal.meta.security.Target.ATTRIBUTE;
import static org.jboss.hal.meta.security.Target.OPERATION;

/**
 * Class to decide whether a single or a set of constraints are allowed according to a given security context. The
 * security context must be provided by a {@link SecurityContextResolver}. {@code isAllowed()} returns {@code true} if
 * the security context was resolved and the constraint is valid.
 * <p>
 * To hide or disable UI elements, use one of the following strategies:
 * <dl>
 * <dt>Eager filtering</dt>
 * <dd>If the security context is <strong>available</strong> when the UI elements are created, filter the elements
 * based on the outcome of {@code isAllowed()}. Add only allowed elements to the DOM.</dd>
 * <dt>Late hiding</dt>
 * <dd>If the security context is <strong>not</strong> available when the UI elements are created, store the
 * constraints as {@code data-constraint} attributes. Later when you have access to the security context
 * post-process the elements using one of the {@code processElements()} method from {@link ElementGuard}.</dd>
 * </dl>
 * <p>
 * If WildFly uses {@link AccessControlProvider#SIMPLE}, {@code isAllowed()} will <strong>always</strong>
 * return {@code true}.
 */
public class AuthorisationDecision {

    // ------------------------------------------------------ factory methods

    public static AuthorisationDecision from(Environment environment,
            SecurityContextRegistry securityContextRegistry) {
        return new AuthorisationDecision(environment, constraint -> {
            if (securityContextRegistry.contains(constraint.getTemplate())) {
                return Optional.of(securityContextRegistry.lookup(constraint.getTemplate()));
            }
            return Optional.empty();
        });
    }

    public static AuthorisationDecision from(Environment environment, SecurityContext securityContext) {
        return new AuthorisationDecision(environment, constraint -> Optional.of(securityContext));
    }

    public static AuthorisationDecision from(Environment environment, SecurityContextResolver resolver) {
        return new AuthorisationDecision(environment, resolver);
    }


    // ------------------------------------------------------ instance

    private static final Logger logger = LoggerFactory.getLogger(AuthorisationDecision.class);

    private final Environment environment;
    private final SecurityContextResolver resolver;

    private AuthorisationDecision(Environment environment, SecurityContextResolver resolver) {
        this.environment = environment;
        this.resolver = resolver;
    }

    public boolean isAllowed(Constraints constraints) {
        if (environment.getAccessControlProvider() == AccessControlProvider.SIMPLE || constraints.isEmpty()) {
            return true;
        }

        int size = constraints.size();
        if (size == 1) {
            return isAllowed(constraints.iterator().next());
        } else {
            int index = 0;
            BitSet bits = new BitSet(size);
            for (Constraint constraint : constraints) {
                bits.set(index, isAllowed(constraint));
                index++;
            }
            int cardinality = bits.cardinality();
            return constraints.getOperator() == AND ? cardinality == size : cardinality > 0;
        }
    }

    public boolean isAllowed(Constraint constraint) {
        if (environment.getAccessControlProvider() == AccessControlProvider.SIMPLE) {
            return true;
        }
        boolean allowed = false;
        Optional<SecurityContext> optional = resolver.resolve(constraint);
        if (optional.isPresent()) {
            SecurityContext securityContext = optional.get();
            if (constraint.getTarget() == OPERATION) {
                switch (constraint.getPermission()) {
                    case EXECUTABLE:
                        allowed = securityContext.isExecutable(constraint.getName());
                        break;
                    case READABLE:
                    case WRITABLE:
                        logger.error("Unsupported permission in constraint {}. Only {} is allowed for target {}.",
                                constraint, EXECUTABLE.name().toLowerCase(), OPERATION.name().toLowerCase());
                        break;
                    default:
                        break;
                }

            } else if (constraint.getTarget() == ATTRIBUTE) {
                switch (constraint.getPermission()) {
                    case READABLE:
                        allowed = securityContext.isReadable(constraint.getName());
                        break;
                    case WRITABLE:
                        allowed = securityContext.isWritable(constraint.getName());
                        break;
                    case EXECUTABLE:
                        logger.error("Unsupported permission in constraint {}. Only ({}|{}) are allowed for target {}.",
                                constraint, READABLE.name().toLowerCase(), WRITABLE.name().toLowerCase(),
                                ATTRIBUTE.name().toLowerCase());
                        break;
                    default:
                        break;
                }
            }
        } else {
            logger.warn("No security context found for {}", constraint);
        }
        return allowed;
    }
}
