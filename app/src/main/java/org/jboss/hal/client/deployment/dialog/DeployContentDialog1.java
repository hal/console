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
import java.util.Set;

import com.google.gwt.safehtml.shared.SafeHtml;
import elemental.dom.Element;
import elemental.html.InputElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.form.SwitchBridge;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.ballroom.table.OptionsBuilder;
import org.jboss.hal.client.deployment.Content;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toList;
import static org.jboss.gwt.elemento.core.InputType.checkbox;
import static org.jboss.hal.ballroom.table.RefreshMode.RESET;
import static org.jboss.hal.resources.CSS.marginTopLarge;

/**
 * Dialog used to deploy and undeploy content to one or more server groups.
 *
 * @author Harald Pehl
 */
public class DeployContentDialog1 {

    @FunctionalInterface
    public interface DeployCallback {

        void deploy(Content content, List<String> serverGroups, boolean enable);
    }


    @FunctionalInterface
    public interface UndeployCallback {

        void undeploy(Content content, List<String> serverGroups);
    }


    private static class ServerGroup {

        final String serverGroup;

        ServerGroup(final String serverGroup) {
            this.serverGroup = serverGroup;
        }
    }


    private static final String ENABLE_CONTAINER = "enableContainer";
    private static final String ENABLE = "enable";

    private final Content content;
    private final List<ServerGroup> serverGroups;
    private final DeployCallback deployCallback;
    private final UndeployCallback undeployCallback;
    private final Alert noServerGroupSelected;
    private final DataTable<ServerGroup> table;
    private final Element enableContainer;
    private final InputElement enable;
    private final Dialog dialog;

    public DeployContentDialog1(final Content content, final Set<String> serverGroupsWithoutContent,
            final Resources resources, final DeployCallback callback) {
        this(content, serverGroupsWithoutContent, resources, callback, null);
    }

    public DeployContentDialog1(final Content content, final Set<String> serverGroupsWithContent,
            final Resources resources, final UndeployCallback callback) {
        this(content, serverGroupsWithContent, resources, null, callback);
    }

    private DeployContentDialog1(final Content content, final Set<String> serverGroups, final Resources resources,
            final DeployCallback deployCallback, final UndeployCallback undeployCallback) {
        this.content = content;
        //noinspection Convert2MethodRef - do not replace w/ method reference. GWT compiler will blow up
        this.serverGroups = serverGroups.stream()
                .sorted(naturalOrder())
                .map((serverGroup) -> new ServerGroup(serverGroup))
                .collect(toList());
        this.deployCallback = deployCallback;
        this.undeployCallback = undeployCallback;

        noServerGroupSelected = new Alert(Icons.ERROR, resources.messages().noServerGroupSelected());

        Options<ServerGroup> options = new OptionsBuilder<ServerGroup>()
                .checkboxColumn()
                .column(Names.SERVER_GROUP, (cell, type, row, meta) -> row.serverGroup)
                .keys(false)
                .paging(false)
                .searching(false)
                .multiselect()
                .build();
        table = new DataTable<>(Ids.SERVER_GROUP_DEPLOYMENT_TABLE, options);

        SafeHtml description = deployCallback != null ? resources.messages()
                .chooseServerGroupsToDeploy(content.getName()) : resources.messages()
                .chooseServerGroupsToUndeploy(content.getName());
        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .div().add(noServerGroupSelected).end()
            .p().innerHtml(description).end()
            .add(table)
            .div().css(marginTopLarge).rememberAs(ENABLE_CONTAINER)
                .input(checkbox).rememberAs(ENABLE).id(Ids.SERVER_GROUP_DEPLOYMENT_ENABLE)
                .label().css(CSS.marginLeft5)
                    .attr("for", Ids.SERVER_GROUP_DEPLOYMENT_ENABLE)
                    .textContent(resources.constants().enableDeployment())
                .end()
            .end();
        // @formatter:on
        enable = builder.referenceFor(ENABLE);
        enableContainer = builder.referenceFor(ENABLE_CONTAINER);

        String title = deployCallback != null ? resources.constants().deployContent() : resources.constants()
                .undeployContent();
        String primary = deployCallback != null ? resources.constants().deploy() : resources.constants().undeploy();
        dialog = new Dialog.Builder(title)
                .add(builder.elements())
                .primary(primary, this::finish)
                .cancel()
                .closeIcon(true)
                .closeOnEsc(true)
                .build();
        dialog.registerAttachable(table);
    }

    private boolean finish() {
        boolean hasSelection = table.api().hasSelection();
        Elements.setVisible(noServerGroupSelected.asElement(), !hasSelection);
        if (hasSelection) {
            List<String> serverGroups = table.api().selectedRows().stream()
                    .map(usg -> usg.serverGroup)
                    .collect(toList());
            if (deployCallback != null) {
                deployCallback.deploy(content, serverGroups, SwitchBridge.Bridge.element(enable).getValue());
            } else if (undeployCallback != null) {
                undeployCallback.undeploy(content, serverGroups);
            }
        }
        return hasSelection;
    }

    public void show() {
        dialog.show();
        Elements.setVisible(noServerGroupSelected.asElement(), false);
        Elements.setVisible(enableContainer, deployCallback != null);
        table.update(serverGroups);
        SwitchBridge.Bridge.element(enable).setValue(false);
    }
}
