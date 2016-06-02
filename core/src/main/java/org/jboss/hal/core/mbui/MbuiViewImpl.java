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
package org.jboss.hal.core.mbui;

import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.meta.StatementContext;

/**
 * Base class for views generated using {@code @MbuiView}.
 *
 * @author Harald Pehl
 */
public abstract class MbuiViewImpl<P extends MbuiPresenter> extends PatternFlyViewImpl implements MbuiView<P> {

    protected final MbuiContext mbuiContext;
    protected P presenter;

    protected MbuiViewImpl(final MbuiContext mbuiContext) {
        this.mbuiContext = mbuiContext;
    }

    protected MbuiViewImpl(final MbuiContext mbuiContext, final StatementContext statementContext) {
        this.mbuiContext = mbuiContext;
        this.mbuiContext.updateStatementContext(statementContext);
    }

    @Override
    public void setPresenter(final P presenter) {
        this.presenter = presenter;
    }
}
