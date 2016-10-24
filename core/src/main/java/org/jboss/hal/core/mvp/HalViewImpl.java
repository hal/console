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
package org.jboss.hal.core.mvp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.gwtplatform.mvp.client.ViewImpl;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.PatternFly;

/**
 * View which initializes JavaScript based PatternFly components like select picker, tooltips or data tables in its
 * {@link #attach()} method.
 *
 * @author Harald Pehl
 */
public abstract class HalViewImpl extends ViewImpl implements HalView {

    private final List<Attachable> attachables;
    private Element element;
    private Iterable<Element> elements;
    private boolean attached;

    protected HalViewImpl() {
        attachables = new ArrayList<>();
        attached = false;
    }

    protected void initElement(IsElement element) {
        initElement(element.asElement());
    }

    protected void initElement(Element element) {
        this.element = element;
    }

    protected void initElements(HasElements elements) {
        initElements(elements.asElements());
    }

    protected void initElements(Iterable<Element> elements) {
        this.elements = elements;
    }

    @Override
    public Element asElement() {
        return element;
    }

    @Override
    public Iterable<Element> asElements() {
        return elements;
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
            attachables.forEach(Attachable::attach);
            attached = true;
        }
    }

    @Override
    public void detach() {
        if (attached) {
            attachables.forEach(Attachable::detach);
            attached = false;
        }
    }
}
