/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.core.mvp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.resources.Icons;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.gwtplatform.mvp.client.ViewImpl;

import elemental2.dom.HTMLElement;

import static org.jboss.gwt.elemento.core.Elements.code;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.hal.resources.CSS.marginTopLarge;

/**
 * View which initializes JavaScript based PatternFly components like select picker, tooltips or data tables in its
 * {@link #attach()} method.
 */
public abstract class HalViewImpl extends ViewImpl implements HalView {

    private final List<Attachable> attachables;
    private HTMLElement element;
    private Iterable<HTMLElement> elements = Collections.emptyList();
    private boolean attached;

    protected HalViewImpl() {
        attachables = new ArrayList<>();
        attached = false;

        // noinspection HardCodedStringLiteral
        element = div().css(marginTopLarge)
                .add(new Alert(Icons.ERROR, SafeHtmlUtils.fromString("View not initialized")).element())
                .add(p()
                        .add(span().textContent("The view is not initialized. Did you forget to call "))
                        .add(code().textContent("initElement(Element)"))
                        .add(span().textContent(" / "))
                        .add(code().textContent("initElements(Iterable<Element>)"))
                        .add(span().textContent("?")))
                .element();
    }

    protected void initElement(IsElement element) {
        initElement(element.element());
    }

    protected void initElement(HTMLElement element) {
        this.element = element;
    }

    protected void initElements(Iterable<HTMLElement> elements) {
        this.elements = elements;
    }

    @Override
    public HTMLElement element() {
        return element;
    }

    @Override
    public Iterator<HTMLElement> iterator() {
        return elements.iterator();
    }

    protected void registerAttachable(Attachable first, Attachable... rest) {
        attachables.add(first);
        if (rest != null) {
            Collections.addAll(attachables, rest);
        }
    }

    protected <A extends Attachable> void registerAttachables(Collection<A> attachables) {
        this.attachables.addAll(attachables);
    }

    @Override
    public void attach() {
        if (!attached) {
            PatternFly.initComponents();
            for (Attachable attachable : attachables) {
                attachable.attach();
            }
            attached = true;
        }
    }

    @Override
    public void detach() {
        if (attached) {
            for (Attachable attachable : attachables) {
                attachable.detach();
            }
            attached = false;
        }
    }
}
