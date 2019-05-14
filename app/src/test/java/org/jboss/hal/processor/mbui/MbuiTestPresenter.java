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
package org.jboss.hal.processor.mbui;

import java.util.Map;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;

@SuppressWarnings("unused")
public abstract class MbuiTestPresenter extends MbuiPresenter<MbuiTestPresenter.MyView, MbuiTestPresenter.MyProxy> {

    public interface MyProxy extends ProxyPlace<MbuiTestPresenter> {}

    public interface MyView extends MbuiView<MbuiTestPresenter> {}

    protected MbuiTestPresenter(final EventBus eventBus,
            final MyView view, final MyProxy myProxy, final Finder finder) {
        super(eventBus, view, myProxy, finder);
    }

    public void saveForm(Form<ModelNode> form, Map<String, Object> changedValues) {}

    public void resetForm(Form<ModelNode> form) {}

    public void saveNamedForm(Form<NamedNode> form, Map<String, Object> changedValues) {}

    @Override
    public void reload() {}
}

