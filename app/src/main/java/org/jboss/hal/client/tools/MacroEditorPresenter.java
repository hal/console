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
package org.jboss.hal.client.tools;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.ballroom.HasTitle;
import org.jboss.hal.core.mvp.ApplicationPresenter;
import org.jboss.hal.core.mvp.PatternFlyView;
import org.jboss.hal.dmr.macro.Macro;
import org.jboss.hal.dmr.macro.Macros;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Resources;

import javax.inject.Inject;

/**
 * @author Harald Pehl
 */
public class MacroEditorPresenter
        extends ApplicationPresenter<MacroEditorPresenter.MyView, MacroEditorPresenter.MyProxy>
        implements HasTitle {

    // @formatter:off
    @ProxyStandard
    @NameToken(NameTokens.MACRO_EDITOR)
    public interface MyProxy extends ProxyPlace<MacroEditorPresenter> {}

    public interface MyView extends PatternFlyView {
        void setMacros(Macros macros);
        void selectMacro(final Macro macro);
    }
    // @formatter:on


    public final static String MACRO_PARAM = "macro";

    private final Macros macros;
    private final Resources resources;
    private Macro macro;

    @Inject
    public MacroEditorPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
            final Macros macros, final Resources resources) {
        super(eventBus, view, proxy);
        this.macros = macros;
        this.resources = resources;
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        String name = request.getParameter(MACRO_PARAM, null);
        if (name != null) {
            macro = macros.get(name);
        }
    }

    @Override
    protected void onReset() {
        super.onReset();
        getView().setMacros(macros);
        if (macro != null) {
            getView().selectMacro(macro);
        }
    }

    @Override
    public String getTitle() {
        return resources.constants().macroEditor();
    }
}
