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
package org.jboss.hal.core.modelbrowser;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import elemental.dom.Element;
import elemental.html.InputElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.InputType;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.EventType.click;

/**
 * @author Harald Pehl
 */
class ChooseSingletonStep extends WizardStep<SingletonContext, SingletonState> {

    private final Element root;
    private final InputElement firstRadio;

    ChooseSingletonStep(final Node<Context> parent, final List<String> children, final Resources resources) {
        super(Ids.MODEL_BROWSER_CHOOSE_SINGLETON_STEP, resources.constants().chooseSingleton());

        Elements.Builder builder = new Elements.Builder().div();
        SortedSet<String> singletons = new TreeSet<>(parent.data.getSingletons());
        SortedSet<String> existing = new TreeSet<>(children);
        singletons.removeAll(existing);

        for (String singleton : singletons) {
            // @formatter:off
            builder.div().css(CSS.radio)
                .label()
                    .input(InputType.radio)
                        .attr("name", "singleton") //NON-NLS
                        .attr("value", singleton)
                        .on(click, event ->
                                wizard().getContext().singleton = ((InputElement) event.getTarget()).getValue())
                    .span().textContent(singleton).end()
                .end()
            .end();
            // @formatter:on
        }
        this.root = builder.end().build();

        firstRadio = (InputElement) root.querySelector("input[type=radio]"); //NON-NLS
        firstRadio.setChecked(true);
    }

    @Override
    public void reset(final SingletonContext context) {
        context.singleton = firstRadio.getValue();
    }

    @Override
    public Element asElement() {
        return root;
    }
}
