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

import elemental.dom.Element;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public abstract class RuntimePreview<T> extends PreviewContent<T> {

    protected final String ALERT_CONTAINER = "alert-container-element";
    protected static final String ALERT_ICON = "alert-icon-element";
    protected static final String ALERT_TEXT = "alert-text-element";
    protected static final String RELOAD_LINK = "reload-link";
    protected static final String RESTART_LINK = "restart-link";

    protected  final Resources resources;
    protected Element alertContainer;
    protected Element alertIcon;
    protected Element alertText;

    protected RuntimePreview(final String header, final String lead, final Resources resources) {
        super(header, lead);
        this.resources = resources;
    }

    protected void adminOnly(String type, String name) {
        alertContainer.setClassName(alert + " " + alertInfo);
        alertIcon.setClassName(Icons.STOPPED);
        alertText.setInnerHTML(resources.messages().adminOnly(type, name).asString());
    }

    protected void starting(String type, String name) {
        alertContainer.setClassName(alert + " " + alertInfo);
        alertIcon.setClassName(Icons.DISABLED);
        alertText.setInnerHTML(resources.messages().restartPending(type, name).asString());
    }

    protected void suspending(String type, String name) {
        alertContainer.setClassName(alert + " " + alertWarning);
        alertIcon.setClassName(Icons.WARNING);
        alertText.setInnerHTML(resources.messages().suspending(type, name).asString());
    }

    protected void needsReload(String type, String name) {
        warning();
        alertText.setInnerHTML(resources.messages().needsReload(type, name).asString());
    }

    protected void needsRestart(String type, String name) {
        warning();
        alertText.setInnerHTML(resources.messages().needsRestart(type, name).asString());
    }

    protected void running(String type, String name) {
        alertContainer.setClassName(alert + " " + alertSuccess);
        alertIcon.setClassName(Icons.OK);
        alertText.setInnerHTML(resources.messages().running(type, name).asString());
    }

    protected void timeout(String type, String name ) {
        error();
        alertText.setInnerHTML(resources.messages().timeout(type, name).asString());
    }

    protected void undefined(String type, String name) {
        error();
        alertText.setInnerHTML(resources.messages().unknownState(type, name).asString());
    }

    private void warning() {
        alertContainer.setClassName(alert + " " + alertWarning);
        alertIcon.setClassName(Icons.WARNING);
    }

    private void error() {
        alertContainer.setClassName(alert + " " + alertDanger);
        alertIcon.setClassName(Icons.ERROR);
    }
}
