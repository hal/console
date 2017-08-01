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
package org.jboss.hal.client.runtime.subsystem.ejb;

import elemental2.dom.HTMLElement;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.resources.CSS;

import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;

public class EjbView extends HalViewImpl implements EjbPresenter.MyView {

    private final HTMLElement header;
    private final HTMLElement lead;
    private EjbPresenter presenter;

    public EjbView() {
        initElement(row()
                .add(column()
                        .add(header = h(1).asElement())
                        .add(lead = p().css(CSS.lead).asElement())));
    }

    @Override
    public void setPresenter(EjbPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(EjbNode ejb) {
        header.textContent = ejb.getName();
        lead.textContent = ejb.type.type;
    }
}
