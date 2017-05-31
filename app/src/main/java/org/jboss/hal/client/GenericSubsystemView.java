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
package org.jboss.hal.client;

import javax.inject.Inject;

import org.jboss.hal.core.modelbrowser.ModelBrowser;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ResourceAddress;

/**
 * @author Harald Pehl
 */
public class GenericSubsystemView extends HalViewImpl implements GenericSubsystemPresenter.MyView {

    private final ModelBrowser modelBrowser;

    @Inject
    public GenericSubsystemView(ModelBrowser modelBrowser) {
        this.modelBrowser = modelBrowser;
        initElement(modelBrowser);
    }

    @Override
    public void setRoot(final ResourceAddress root) {
        modelBrowser.setRoot(root, false);
    }
}
