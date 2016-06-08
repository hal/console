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

import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.collect.Ordering;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.core.mvp.FullscreenPresenter;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.PatternFlyView;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.macro.Macro;
import org.jboss.hal.dmr.macro.Macros;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static java.util.stream.Collectors.toList;

/**
 * @author Harald Pehl
 */
public class MacroEditorPresenter
        extends FullscreenPresenter<MacroEditorPresenter.MyView, MacroEditorPresenter.MyProxy> {

    // @formatter:off
    @ProxyStandard
    @NameToken(NameTokens.MACRO_EDITOR)
    public interface MyProxy extends ProxyPlace<MacroEditorPresenter> {}

    public interface MyView extends PatternFlyView, HasPresenter<MacroEditorPresenter> {
        void empty();
        void setMacros(Iterable<Macro> macros);
        void selectMacro(final Macro macro);
        void enableMacro(Macro macro);
        void disableMacro(Macro macro);
    }
    // @formatter:on


    public final static String MACRO_PARAM = "macro";

    private final Dispatcher dispatcher;
    private final Macros macros;
    private final Provider<Progress> progress;
    private final Resources resources;
    private Macro macro;

    @Inject
    public MacroEditorPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final Dispatcher dispatcher,
            final Macros macros,
            @Footer Provider<Progress> progress,
            final Resources resources) {
        super(eventBus, view, proxy, resources.constants().macroEditor());
        this.dispatcher = dispatcher;
        this.macros = macros;
        this.progress = progress;
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
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
        load();
    }

    private void load() {
        if (macros.isEmpty()) {
            getView().empty();
        } else {
            List<Macro> ordered = Ordering.natural().onResultOf(Macro::getName)
                    .immutableSortedCopy(this.macros.getMacros());
            getView().setMacros(ordered);
            if (macro != null) {
                getView().selectMacro(macro);
            } else {
                getView().selectMacro(ordered.get(0));
            }
        }
    }

    void play(Macro macro) {
        List<MacroOperationFunction> functions = macro.getOperations().stream()
                .map(operation -> new MacroOperationFunction(dispatcher, operation)).collect(toList());
        Outcome<FunctionContext> outcome = new Outcome<FunctionContext>() {
            @Override
            public void onFailure(final FunctionContext context) {
                getView().enableMacro(macro);
                MessageEvent
                        .fire(getEventBus(),
                                Message.error(resources.constants().macroPlaybackError(), context.getErrorMessage()));
            }

            @Override
            public void onSuccess(final FunctionContext context) {
                getView().enableMacro(macro);
                MessageEvent.fire(getEventBus(), Message.success(resources.constants().macroPlaybackSuccessful()));
            }
        };
        getView().disableMacro(macro);
        new Async<FunctionContext>(progress.get())
                .waterfall(new FunctionContext(), outcome, functions.toArray(new Function[functions.size()]));
    }

    void rename(Macro macro) {

    }

    void remove(Macro macro) {
        macros.remove(macro);
        load();
    }
}
