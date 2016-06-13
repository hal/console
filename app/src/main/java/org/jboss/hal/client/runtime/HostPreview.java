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
package org.jboss.hal.client.runtime;

import com.google.common.base.Joiner;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
class HostPreview extends PreviewContent {

    @SuppressWarnings("HardCodedStringLiteral")
    HostPreview(final HostColumn hostColumn, final HostActions hostActions, final Host host,
            final Resources resources) {
        super(host.getName(), host.isDomainController() ? Names.DOMAIN_CONTROLLER : Names.HOST_CONTROLLER);

        previewBuilder().div();
        if (host.isAdminMode()) {
            previewBuilder().css(alert, alertInfo)
                    .span().css(Icons.DISABLED).end()
                    .span().innerHtml(resources.messages().adminOnly(host.getName())).end();

        } else if (host.isSuspending() || host.needsReload() || host.needsRestart()) {
            previewBuilder().css(alert, alertWarning)
                    .span().css(Icons.WARNING).end();

            if (host.isSuspending()) {
                previewBuilder().span().innerHtml(resources.messages().suspending(Names.HOST, host.getName())).end();

            } else if (host.needsReload()) {
                previewBuilder().span().innerHtml(resources.messages().needsReload(Names.HOST, host.getName())).end()
                        .span().textContent(" ").end()
                        .a().css(clickable, alertLink)
                        .on(click, event -> hostActions.reload(host, host.isDomainController(),
                                () -> {
                                    if (!host.isDomainController()) {
                                        hostColumn.startProgress(host);
                                    }
                                },
                                () -> {
                                    if (!host.isDomainController()) {
                                        hostColumn.endProgress(host);
                                    }
                                    hostColumn.refresh(RESTORE_SELECTION);
                                }))
                        .textContent(resources.constants().reload()).end();

            } else if (host.needsRestart()) {
                previewBuilder().span().innerHtml(resources.messages().needsRestart(Names.HOST, host.getName())).end()
                        .span().textContent(" ").end()
                        .a().css(clickable, alertLink)
                        .on(click, event -> hostActions.restart(host, host.isDomainController(),
                                () -> {
                                    if (!host.isDomainController()) {
                                        hostColumn.startProgress(host);
                                    }
                                },
                                () -> {
                                    if (!host.isDomainController()) {
                                        hostColumn.endProgress(host);
                                    }
                                    hostColumn.refresh(RESTORE_SELECTION);
                                }))
                        .textContent(resources.constants().restart()).end();
            }

        } else if (host.isRunning()) {
            previewBuilder().css(alert, alertSuccess)
                    .span().css(Icons.OK).end()
                    .span().innerHtml(resources.messages().running(Names.HOST, host.getName())).end();

        } else {
            previewBuilder().css(alert, alertDanger)
                    .span().css(Icons.ERROR).end()
                    .span().innerHtml(resources.messages().unknownState(Names.HOST, host.getName())).end();
        }
        previewBuilder().end(); // </div>

        PreviewAttributes<Host> attributes = new PreviewAttributes<>(host,
                asList("release-codename", "release-version", "product-name", "product-version", "host-state",
                        "running-mode"))
                .append(model -> {
                    return new String[]{
                            "Management Version",
                            Joiner.on('.').join(
                                    model.get("management-major-version"),
                                    model.get("management-minor-version"),
                                    model.get("management-micro-version"))
                    };
                })
                .end();
        previewBuilder().addAll(attributes);
    }
}
