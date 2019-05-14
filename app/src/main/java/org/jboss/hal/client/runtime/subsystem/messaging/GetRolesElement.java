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
package org.jboss.hal.client.runtime.subsystem.messaging;

import elemental2.dom.CSSProperties.MarginBottomUnionType;
import elemental2.dom.CSSProperties.WidthUnionType;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.gwt.elemento.core.Key;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.button;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.input;
import static org.jboss.gwt.elemento.core.Elements.label;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.gwt.elemento.core.EventType.keyup;
import static org.jboss.gwt.elemento.core.InputType.text;
import static org.jboss.hal.resources.CSS.*;

class GetRolesElement implements IsElement<HTMLElement>, HasPresenter<ServerPresenter> {

    private HTMLInputElement addressMatch;
    private HTMLElement root;
    private ServerPresenter presenter;

    GetRolesElement(Resources resources) {
        String addressMatchId = Ids.uniqueId();
        root = div().css(marginBottomLarge)
                .apply(e -> {
                    e.style.display = "flex";
                    e.style.alignItems = "center";
                })
                .add(label().css(marginRightLarge)
                        .textContent(resources.constants().enterAddressMatch())
                        .apply(e -> {
                            e.htmlFor = addressMatchId;
                            e.style.marginBottom = MarginBottomUnionType.of(0);
                        }))
                .add(addressMatch = input(text).css(formControl, marginRightLarge)
                        .id(addressMatchId)
                        .apply(e -> {
                            e.placeholder = resources.constants().addressMatch();
                            e.style.width = WidthUnionType.of("inherit");
                            e.style.flex = "1 1 auto";
                        })
                        .on(keyup, event -> {
                            if (Key.Enter.match(event)) {
                                presenter.getRoles(addressMatch.value);
                            }
                        })
                        .get())
                .add(button().css(btn, btnHal, btnDefault)
                        .textContent(resources.constants().getRoles())
                        .on(click, event -> presenter.getRoles(addressMatch.value)))
                .get();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public void setPresenter(ServerPresenter presenter) {
        this.presenter = presenter;
    }

    void clear() {
        addressMatch.value = "";
    }
}
