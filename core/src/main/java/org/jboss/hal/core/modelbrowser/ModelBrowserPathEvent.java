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
package org.jboss.hal.core.modelbrowser;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

// No @GenEvent here due to naming conflicts
public class ModelBrowserPathEvent extends GwtEvent<ModelBrowserPathEvent.ModelBrowserPathHandler> {

    private static final Type<ModelBrowserPathHandler> TYPE = new Type<>();

    public static Type<ModelBrowserPathHandler> getType() {
        return TYPE;
    }

    private final ModelBrowserPath path;

    public ModelBrowserPathEvent(final ModelBrowserPath path) {
        this.path = path;
    }

    public ModelBrowserPath getPath() {
        return path;
    }

    @Override
    protected void dispatch(ModelBrowserPathHandler handler) {
        handler.onModelBrowserAddress(this);
    }

    @Override
    public Type<ModelBrowserPathHandler> getAssociatedType() {
        return TYPE;
    }


    public interface ModelBrowserPathHandler extends EventHandler {

        void onModelBrowserAddress(ModelBrowserPathEvent event);
    }
}
