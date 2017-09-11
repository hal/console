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
import org.jboss.hal.ballroom.HasTitle;
import org.jboss.hal.core.mvp.ApplicationPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.macro.Macro;
import org.jboss.hal.dmr.macro.Macros;
import org.jboss.hal.flow.Flow;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Outcome;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static elemental2.dom.DomGlobal.alert;
import static java.util.stream.Collectors.toList;

public class MacroEditorPresenter
        extends ApplicationPresenter<MacroEditorPresenter.MyView, MacroEditorPresenter.MyProxy>
        implements HasTitle {

    // @formatter:off
    @ProxyStandard
    @NameToken(NameTokens.MACRO_EDITOR)
    public interface MyProxy extends ProxyPlace<MacroEditorPresenter> {}

    public interface MyView extends HalView, HasPresenter<MacroEditorPresenter> {
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
    public MacroEditorPresenter(EventBus eventBus,
            MyView view,
            MyProxy proxy,
            Dispatcher dispatcher,
            Macros macros,
            @Footer Provider<Progress> progress,
            Resources resources) {
        super(eventBus, view, proxy);
        this.dispatcher = dispatcher;
        this.macros = macros;
        this.progress = progress;
        this.resources = resources;
    }

    @Override
    public String getTitle() {
        return resources.constants().macroEditor();
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
        List<MacroOperationStep> steps = macro.getOperations().stream()
                .map(operation -> new MacroOperationStep(dispatcher, operation)).collect(toList());
        getView().disableMacro(macro);
        Flow.series(progress.get(), new FlowContext(), steps)
                .subscribe(new Outcome<FlowContext>() {
                    @Override
                    public void onError(FlowContext context, Throwable error) {
                        getView().enableMacro(macro);
                        MessageEvent.fire(getEventBus(), Message.error(resources.messages().macroPlaybackError(),
                                error.getMessage()));
                    }

                    @Override
                    public void onSuccess(FlowContext context) {
                        getView().enableMacro(macro);
                        MessageEvent.fire(getEventBus(),
                                Message.success(resources.messages().macroPlaybackSuccessful()));
                    }
                });
    }

    void rename(Macro macro) {
        alert(Names.NYI);
    }

    void remove(Macro macro) {
        macros.remove(macro);
        load();
    }
}
