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
package org.jboss.hal.ballroom.form;

/**
 * Predefined set of decorations which can be {@linkplain Appearance#apply(Decoration, Object) applied} and
 * {@linkplain Appearance#unapply(Decoration) unapplied} to an {@link Appearance} to modify its L&F.
 */
public enum Decoration {
    // The context used with the decoration is given as comment.
    DEFAULT, // String defaultValue
    DEPRECATED, // Deprecation deprecationInfo
    ENABLED, // null
    EXPRESSION, // ExpressionContext(String expressionValue, ExpressionCallback callback)
    HINT, // String hint
    INVALID, // String errorMessage
    REQUIRED, // null
    RESTRICTED, // null
    SENSITIVE, // null
    SUGGESTIONS, // SuggestHandler
}
