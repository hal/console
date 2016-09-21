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
package org.jboss.hal.client.deployment.dialog;

import java.util.List;

import elemental.html.InputElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.form.SwitchBridge;
import org.jboss.hal.ballroom.table.Column;
import org.jboss.hal.ballroom.table.Column.RenderCallback;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.ballroom.table.OptionsBuilder;
import org.jboss.hal.client.deployment.Content;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.jboss.gwt.elemento.core.InputType.checkbox;
import static org.jboss.hal.ballroom.table.Api.RefreshMode.RESET;

/**
 * Dialog used to deploy one or several one content items to one server group.
 *
 * @author Harald Pehl
 */
public class DeployContentDialog2 {

    @FunctionalInterface
    public interface DeployCallback {

        void deploy(String serverGroup, List<Content> content, boolean enable);
    }


    private static final String ENABLE = "enable";

    private final String serverGroup;
    private final List<Content> content;
    private final DeployCallback deployCallback;
    private final Alert noContentSelected;
    private final DataTable<Content> table;
    private final InputElement enable;
    private final Dialog dialog;

    public DeployContentDialog2(final String serverGroup, final List<Content> content, final Resources resources,
            final DeployCallback deployCallback) {
        this.serverGroup = serverGroup;
        this.content = content.stream()
                .sorted(comparing(Content::getName))
                .collect(toList());
        this.deployCallback = deployCallback;

        noContentSelected = new Alert(Icons.ERROR, resources.messages().noContentSelected());

        Options<Content> options = new OptionsBuilder<Content>()
                .checkboxColumn()
                .column(resources.constants().content(), new RenderCallback<Content, String>() {
                    @Override
                    public String render(final String cell, final String type, final Content row,
                            final Column.Meta meta) {
                        return row.getName();
                    }
                })
                .keys(false)
                .paging(false)
                .searching(false)
                .multiselect()
                .build();
        table = new DataTable<>(Ids.SERVER_GROUP_DEPLOYMENT_TABLE, options);

        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .div().add(noContentSelected).end()
            .p().innerHtml(resources.messages().chooseContentToDeploy(serverGroup)).end()
            .add(table)
            .div()
                .input(checkbox).rememberAs(ENABLE).id(Ids.SERVER_GROUP_DEPLOYMENT_ENABLE)
                .label().css(CSS.marginLeft4)
                    .attr("for", Ids.SERVER_GROUP_DEPLOYMENT_ENABLE)
                    .textContent(resources.constants().enableDeployment())
                .end()
            .end();
        // @formatter:on
        enable = builder.referenceFor(ENABLE);

        dialog = new Dialog.Builder(resources.constants().deployContent())
                .add(builder.elements())
                .primary(resources.constants().deploy(), this::finish)
                .cancel()
                .closeIcon(true)
                .closeOnEsc(true)
                .build();
        dialog.registerAttachable(table);
    }

    private boolean finish() {
        boolean hasSelection = table.api().hasSelection();
        Elements.setVisible(noContentSelected.asElement(), !hasSelection);
        if (hasSelection) {
            List<Content> content = table.api().selectedRows();
            deployCallback.deploy(serverGroup, content, SwitchBridge.Bridge.element(enable).getValue());
        }
        return hasSelection;
    }

    public void show() {
        dialog.show();
        Elements.setVisible(noContentSelected.asElement(), false);
        table.api().clear().add(content).refresh(RESET);
        SwitchBridge.Bridge.element(enable).setValue(false);
    }
}
