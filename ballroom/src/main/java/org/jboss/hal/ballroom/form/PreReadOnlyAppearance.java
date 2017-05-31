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
package org.jboss.hal.ballroom.form;

import java.util.EnumSet;

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;

import static org.jboss.gwt.elemento.core.Elements.pre;
import static org.jboss.hal.ballroom.form.Decoration.RESTRICTED;
import static org.jboss.hal.resources.CSS.formControlStatic;
import static org.jboss.hal.resources.CSS.wrap;

/**
 * @author Harald Pehl
 */
class PreReadOnlyAppearance<T> extends ReadOnlyAppearance<T> {

    PreReadOnlyAppearance() {
        super(EnumSet.of(RESTRICTED));

        HTMLElement parent = (HTMLElement) valueContainer.parentNode;
        Elements.removeChildrenFrom(parent);

        valueElement = pre().css(formControlStatic, wrap).asElement();
        parent.appendChild(valueElement);
    }

    @Override
    protected String name() {
        return "PreReadOnlyAppearance";
    }

    @Override
    protected <C> void safeApply(final Decoration decoration, final C context) {
        if (decoration == RESTRICTED) {
            valueElement.textContent = "";
            valueElement.textContent = CONSTANTS.restricted();
        }
    }
}
