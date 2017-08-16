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
package org.jboss.hal.client.patching.wizard;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.gwt.safehtml.shared.SafeHtml;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.InputType;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATCHING;
import static org.jboss.hal.resources.CSS.formHorizontal;
import static org.jboss.hal.resources.CSS.radio;

public class CheckRunningServersStep extends WizardStep<PatchContext, PatchState>  {

    private final HTMLElement root;
    private Boolean restartServers;
    private ServerActions serverActions;
    private List<Property> servers;
    private String host;

    public CheckRunningServersStep(final Resources resources, final ServerActions serverActions, List<Property> servers, String host) {
        super(resources.messages().patchStopAllServersTitle());
        this.serverActions = serverActions;
        this.servers = servers;
        this.host = host;

        List<String> serversList = new ArrayList<>();
        servers.forEach(p -> serversList.add(p.getName()));

        String serverStr = Joiner.on(", ").join(serversList);
        SafeHtml description = resources.messages().patchStopAllServersQuestion(serverStr, host);

        /*
        root = div()
                .add(div().innerHtml(description))
                .add(div().css(blankSlatePf)
                        .add(button("Yes, stop all servers").css(btn, btnLg, btnPrimary)
                                .on(click, event -> restartServers())))
                .asElement();
*/
        String radioName = Ids.build(HOST, PATCHING, "choose-restart");
        root = div().css(formHorizontal)
                .add(p().innerHtml(description))
                .add(div().css(radio)
                        .add(label()
                                .add(input(InputType.radio)
                                        .id(Ids.build(HOST, PATCHING, "ok"))
                                        .attr(UIConstants.NAME, radioName)
                                        .on(click, e -> {
                                            restartServers = true;
                                            //restartServersDialog.getButton(PRIMARY_POSITION).disabled = false;
                                        })
                                        .asElement())
                                .add(span().innerHtml(resources.messages().patchStopServersDialogMessage1()))))
                .add(div().css(radio)
                        .add(label()
                                .add(input(InputType.radio)
                                        .id(Ids.build(HOST, PATCHING, "no"))
                                        .attr(UIConstants.NAME, radioName)
                                        .on(click, e -> {
                                            restartServers = false;
                                            //restartServersDialog.getButton(PRIMARY_POSITION).disabled = false;
                                        })
                                        .asElement())
                                .add(span().innerHtml(resources.messages().patchStopServersDialogMessage2()))))
                .asElement();

    }

    @Override
    public HTMLElement asElement() {
        return root;
    }

    @Override
    public void reset(final PatchContext context) {
        restartServers = null;
    }

    @Override
    protected boolean onNext(final PatchContext context) {
        context.restartServers = restartServers != null && restartServers;
        context.servers = servers;
        // only navigates to next screen if the user selected one of the radio options
        return restartServers != null;
    }
}
