/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.runtime.host;

import java.util.List;

import org.jboss.elemento.Elements;
import org.jboss.hal.ballroom.Format;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.client.runtime.RuntimePreview;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttribute;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.core.runtime.host.HostActions;
import org.jboss.hal.core.runtime.host.HostPreviewAttributes;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;

import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.li;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.Elements.ul;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.client.runtime.host.HostColumn.hostTemplate;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHANNEL_VERSIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DISCONNECTED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST_STATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LAST_CONNECTED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MANAGEMENT_MAJOR_VERSION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MANAGEMENT_MICRO_VERSION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MANAGEMENT_MINOR_VERSION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MANAGEMENT_VERSION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PRODUCT_INFO;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PRODUCT_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PRODUCT_VERSION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELEASE_CODENAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELEASE_VERSION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELOAD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESTART;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RUNNING_MODE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUMMARY;
import static org.jboss.hal.meta.security.Constraint.executable;
import static org.jboss.hal.resources.CSS.alertLink;
import static org.jboss.hal.resources.CSS.clickable;
import static org.jboss.hal.resources.CSS.hidden;
import static org.jboss.hal.resources.CSS.listGroup;
import static org.jboss.hal.resources.CSS.listGroupItem;
import static org.jboss.hal.resources.CSS.value;
import static org.jboss.hal.resources.UIConstants.CONSTRAINT;

public class HostPreview extends RuntimePreview<Host> {

    private static final String ID_HEADER_CHANNEL_VERSIONS = "h2-channel-versions";
    private static final String ID_CHANNEL_VERSIONS = "channel-versions";

    private final Dispatcher dispatcher;
    private final HTMLElement reloadLink;
    private final HTMLElement restartLink;
    private final PreviewAttributes<Host> attributes;
    private final HostActions hostActions;
    private final Resources resources;
    private final LabelBuilder labelBuilder;
    private final HTMLElement headerChannelVersions = h(2, new LabelBuilder().label(CHANNEL_VERSIONS)).id(
            ID_HEADER_CHANNEL_VERSIONS).element();
    private final HTMLElement ulChannelVersions = ul().id(ID_CHANNEL_VERSIONS).css(listGroup).element();

    public HostPreview(Dispatcher dispatcher, HostActions hostActions, Host host, Resources resources) {
        super(host.getName(), host.isDomainController() ? Names.DOMAIN_CONTROLLER : Names.HOST_CONTROLLER, resources);
        this.dispatcher = dispatcher;
        this.hostActions = hostActions;
        this.resources = resources;
        this.labelBuilder = new LabelBuilder();

        previewBuilder()
                .add(alertContainer = div()
                        .add(alertIcon = span().element())
                        .add(alertText = span().element())
                        .add(span().textContent(" "))
                        .add(reloadLink = a().css(clickable, alertLink)
                                .on(click, event -> hostActions.reload(host))
                                .data(CONSTRAINT, executable(hostTemplate(host), RELOAD).data())
                                .textContent(resources.constants().reload()).element())
                        .add(restartLink = a().css(clickable, alertLink)
                                .on(click, event -> hostActions.restart(host))
                                .data(CONSTRAINT, executable(hostTemplate(host), RESTART).data())
                                .textContent(resources.constants().restart()).element())
                        .element());

        attributes = new PreviewAttributes<>(host,
                asList(RELEASE_CODENAME, RELEASE_VERSION, PRODUCT_NAME, PRODUCT_VERSION,
                        HOST_STATE, RUNNING_MODE))
                .append(model -> new PreviewAttribute(
                        labelBuilder.label(MANAGEMENT_VERSION),
                        String.join(".", model.get(MANAGEMENT_MAJOR_VERSION).asString(),
                                model.get(MANAGEMENT_MINOR_VERSION).asString(),
                                model.get(MANAGEMENT_MICRO_VERSION).asString())))
                .append(model -> new PreviewAttribute(labelBuilder.label(LAST_CONNECTED),
                        model.getLastConnected() != null
                                ? Format.mediumDateTime(model.getLastConnected())
                                : Names.NOT_AVAILABLE))
                .append(model -> new PreviewAttribute(labelBuilder.label(DISCONNECTED),
                        model.getLastConnected() != null
                                ? Format.mediumDateTime(model.getDisconnected())
                                : Names.NOT_AVAILABLE));
        previewBuilder().addAll(attributes);

        setVisible(headerChannelVersions, false);
        setVisible(ulChannelVersions, false);
        previewBuilder().add(headerChannelVersions);
        previewBuilder().add(ulChannelVersions);

        update(host);
    }

    @Override
    public void update(Host host) {
        if (!host.isConnected()) {
            disconnected(resources.messages().hostDisconnected(host.getName()));
        } else if (hostActions.isPending(host)) {
            pending(resources.messages().hostPending(host.getName()));
        } else if (host.isAdminMode()) {
            adminOnly(resources.messages().hostAdminMode(host.getName()));
        } else if (host.isBooting() || host.isStarting()) {
            starting(resources.messages().hostStarting(host.getName()));
        } else if (host.needsReload()) {
            needsReload(resources.messages().hostNeedsReload(host.getName()));
        } else if (host.needsRestart()) {
            needsRestart(resources.messages().hostNeedsRestart(host.getName()));
        } else if (host.isRunning()) {
            running(resources.messages().hostRunning(host.getName()));
        } else {
            unknown(resources.messages().hostUndefined(host.getName()));
        }

        // Do not simply hide the links but add the hidden CSS class.
        // Important when constraints for the links are processed later.
        Elements.toggle(reloadLink, hidden, !host.needsReload());
        Elements.toggle(restartLink, hidden, !host.needsRestart());

        HostPreviewAttributes.refresh(host, attributes, hostActions);

        if (host.isRunning() || host.needsRestart() || host.needsReload()) {
            dispatcher.execute(new Operation.Builder(host.getAddress(), PRODUCT_INFO).build()).then(result -> {
                boolean showChannelVersions = false;
                if (result.isDefined()) {
                    ModelNode summary = findSummary(result.asList(), host);
                    if (summary.hasDefined(CHANNEL_VERSIONS)) {
                        Elements.removeChildrenFrom(ulChannelVersions);
                        summary.get(CHANNEL_VERSIONS)
                                .asList()
                                .forEach(cv -> ulChannelVersions.appendChild(li().css(listGroupItem)
                                        .add(span().css(value).style("margin-left:0")
                                                .textContent(cv.asString()).element())
                                        .element()));
                        showChannelVersions = true;
                    }
                }
                setVisible(headerChannelVersions, showChannelVersions);
                setVisible(ulChannelVersions, showChannelVersions);
                return null;
            });
        }
    }

    private ModelNode findSummary(List<ModelNode> summaries, Host host) {
        for (ModelNode modelNode : summaries) {
            if (modelNode.hasDefined(ModelDescriptionConstants.SUMMARY)) {
                ModelNode summary = modelNode.get(SUMMARY);
                if (host.getName().equals(summary.get("node-name").asString())) {
                    return summary;
                }
            }
        }
        return new ModelNode();
    }
}
