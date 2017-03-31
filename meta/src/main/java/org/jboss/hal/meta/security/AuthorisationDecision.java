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

import java.util.Optional;
import java.util.Set;

import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.hal.meta.security.Permission.EXECUTABLE;
import static org.jboss.hal.meta.security.Permission.READABLE;
import static org.jboss.hal.meta.security.Permission.WRITABLE;
import static org.jboss.hal.meta.security.Target.ATTRIBUTE;
import static org.jboss.hal.meta.security.Target.OPERATION;

/**
 * Class to decide whether a single or a set of constraints are allowed according to a given security context. The
 * security context must be provided by a {@link SecurityContextResolver}.
 * <p>
 * This class can operate in two modes:
 * <dl>
 * <dt>strict</dt>
 * <dd>{@code isAllowed()} returns {@code true} if the security context was resolved and the constraint is valid.</dd>
 * <dt>lenient</dt>
 * <dd>{@code isAllowed()} returns {@code true} if no security was resolved. Otherwise the constraint must be
 * valid.</dd>
 * </dl>
 * <p>
 * To hide or disable UI elements, use one of the following strategies:
 * <dl>
 * <dt>Eager filtering</dt>
 * <dd>If the security context is <strong>available</strong> when the UI elements are created, filter the elements
 * based on the outcome of {@code isAllowed()}. Add only allowed elements to the DOM.</dd>
 * <dt>Late hiding</dt>
 * <dd>If the security context is <strong>not</strong> available when the UI elements are created, store the
 * constraints as {@code data-constraint} attribute. Later when you have access to the security context
 * post-process the elements using one of the {@code processElements()} method from {@link ElementGuard}.</dd>
 * </dl>
 *
 * @author Harald Pehl
 */
public class AuthorisationDecision {

    public static AuthorisationDecision strict(final MetadataRegistry metadataRegistry) {
        return new AuthorisationDecision(true, constraint -> {
            if (metadataRegistry.contains(constraint.getTemplate())) {
                return Optional.of(metadataRegistry.lookup(constraint.getTemplate()).getSecurityContext());
            }
            return Optional.empty();
        });
    }

    public static AuthorisationDecision strict(final Metadata metadata) {
        return new AuthorisationDecision(true, constraint -> Optional.of(metadata.getSecurityContext()));
    }

    public static AuthorisationDecision strict(final SecurityContext securityContext) {
        return new AuthorisationDecision(true, constraint -> Optional.of(securityContext));
    }

    public static AuthorisationDecision strict(final SecurityContextResolver resolver) {
        return new AuthorisationDecision(true, resolver);
    }

    public static AuthorisationDecision lenient(final MetadataRegistry metadataRegistry) {
        return new AuthorisationDecision(false, constraint -> {
            if (metadataRegistry.contains(constraint.getTemplate())) {
                return Optional.of(metadataRegistry.lookup(constraint.getTemplate()).getSecurityContext());
            }
            return Optional.empty();
        });

    }

    public static AuthorisationDecision lenient(final Metadata metadata) {
        return new AuthorisationDecision(false, constraint -> Optional.of(metadata.getSecurityContext()));
    }

    public static AuthorisationDecision lenient(final SecurityContext securityContext) {
        return new AuthorisationDecision(false, constraint -> Optional.of(securityContext));
    }

    public static AuthorisationDecision lenient(final SecurityContextResolver resolver) {
        return new AuthorisationDecision(false, resolver);
    }

    @NonNls private static final Logger logger = LoggerFactory.getLogger(AuthorisationDecision.class);

    private final boolean strict;
    private final SecurityContextResolver resolver;

    private AuthorisationDecision(final boolean strict, final SecurityContextResolver resolver) {
        this.strict = strict;
        this.resolver = resolver;
    }

    public boolean isAllowed(Set<Constraint> constraints) {
        for (Constraint constraint : constraints) {
            if (!isAllowed(constraint)) {
                return false;
            }
        }
        return true;
    }

    public boolean isAllowed(Constraint constraint) {
        boolean allowed = !strict;
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
                }
            }
        } else {
            logger.debug("No security context found for {}", constraint);
        }
        if (!allowed) {
            logger.debug("{} is not allowed", constraint);
        }
        return allowed;
    }
}
