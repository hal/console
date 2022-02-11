/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.core.finder;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class FinderContextEvent extends GwtEvent<FinderContextEvent.FinderContextHandler> {

    private static final Type<FinderContextHandler> TYPE = new Type<>();

    public static Type<FinderContextHandler> getType() {
        return TYPE;
    }

    private final FinderContext finderContext;

    public FinderContextEvent(final FinderContext finderContext) {
        this.finderContext = finderContext;
    }

    public FinderContext getFinderContext() {
        return finderContext;
    }

    @Override
    protected void dispatch(FinderContextHandler handler) {
        handler.onFinderContext(this);
    }

    @Override
    public Type<FinderContextHandler> getAssociatedType() {
        return TYPE;
    }

    public interface FinderContextHandler extends EventHandler {

        void onFinderContext(FinderContextEvent event);
    }
}
