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
package org.jboss.hal.processor.mbui.basic;

import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.processor.mbui.MbuiTestPresenter;
import org.jboss.hal.spi.MbuiView;

@MbuiView
public abstract class HandlebarsView extends MbuiViewImpl<MbuiTestPresenter> implements MbuiTestPresenter.MyView {

    public static HandlebarsView create(final MbuiContext mbuiContext) {
        return null;
    }

    HandlebarsView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }
}
