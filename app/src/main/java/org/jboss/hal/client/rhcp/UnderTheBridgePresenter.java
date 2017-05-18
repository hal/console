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
package org.jboss.hal.client.rhcp;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelNode;

@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection", "SpellCheckingInspection"})
public class UnderTheBridgePresenter
        extends ApplicationFinderPresenter<UnderTheBridgePresenter.MyView, UnderTheBridgePresenter.MyProxy> {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken("utb")
    public interface MyProxy extends ProxyPlace<UnderTheBridgePresenter> {}

    public interface MyView extends HalView, HasPresenter<UnderTheBridgePresenter> {
        void show(ModelNode model);
    }
    // @formatter:on

    private ModelNode model;

    @Inject
    public UnderTheBridgePresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
            final Finder finder) {
        super(eventBus, view, proxy, finder);
        model = new ModelNode();
        model.get("string-required").set("Foo");
        model.get("int-required").set(1);
        model.get("long-required").set(2L);
        model.get("boolean-required").set(true);
        model.get("single-select-required").set("sit");
        model.get("multi-select-required").add("Zombie");
        model.get("multi-select-required").add("inferno");
        model.get("multi-select-required").add("cerebro");
        model.get("list-required").add("foo");
        model.get("list-required").add("bar");
        model.get("properties-required").set("foo", "bar");
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    protected void reload() {
        getView().show(model);
    }

    @Override
    public FinderPath finderPath() {
        return new FinderPath()
                .append("rhcp-color", "red", "Color", "Red")
                .append("rhcp-temperature", "hot", "Temperature", "Hot")
                .append("rhcp-vegetable", "chili", "Vegetable", "Chili")
                .append("rhcp-spice", "peppers", "Spice", "Peppers")
                .append("rhcp-decade", "1990-1999", "Decade", "1990 - 1999")
                .append("rhcp-album", "blood-sugar-sex-magik", "Album", "Blood Sugar Sex Magik")
                .append("rhcp-track", "under-the-bridge", "Track", "Under the Bridge");
    }

    void saveModel(final ModelNode model) {
        this.model = model;
        getView().show(model);
    }
}
