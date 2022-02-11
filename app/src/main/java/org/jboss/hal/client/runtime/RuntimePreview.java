/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.runtime;

import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Resources;

import com.google.gwt.safehtml.shared.SafeHtml;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.resources.CSS.*;

public abstract class RuntimePreview<T> extends PreviewContent<T> {

    private static final String SPACE = " ";

    protected final Resources resources;
    protected HTMLElement alertContainer;
    protected HTMLElement alertIcon;
    protected HTMLElement alertText;

    protected RuntimePreview(final String header, final String lead, final Resources resources) {
        super(header, lead);
        this.resources = resources;
    }

    protected void adminOnly(SafeHtml message) {
        alertContainer.className = alert + SPACE + alertInfo;
        alertIcon.className = Icons.LOCK;
        alertText.innerHTML = message.asString();
    }

    protected void starting(SafeHtml message) {
        alertContainer.className = alert + SPACE + alertInfo;
        alertIcon.className = Icons.PENDING;
        alertText.innerHTML = message.asString();
    }

    protected void pending(SafeHtml message) {
        alertContainer.className = alert + SPACE + alertInfo;
        alertIcon.className = Icons.PENDING;
        alertText.innerHTML = message.asString();
    }

    protected void suspended(SafeHtml message) {
        alertContainer.className = alert + SPACE + alertSuspended;
        alertIcon.className = Icons.PAUSED;
        alertText.innerHTML = message.asString();
    }

    protected void needsReload(SafeHtml message) {
        warning();
        alertText.innerHTML = message.asString();
    }

    protected void needsRestart(SafeHtml message) {
        warning();
        alertText.innerHTML = message.asString();
    }

    protected void running(SafeHtml message) {
        alertContainer.className = alert + SPACE + alertSuccess;
        alertIcon.className = Icons.OK;
        alertText.innerHTML = message.asString();
    }

    protected void unknown(SafeHtml message) {
        alertContainer.className = alert + SPACE + alertWarning;
        alertIcon.className = Icons.UNKNOWN;
        alertText.innerHTML = message.asString();
    }

    protected void disconnected(SafeHtml message) {
        alertContainer.className = alert + SPACE + alertInfo;
        alertIcon.className = Icons.DISCONNECTED;
        alertText.innerHTML = message.asString();
    }

    private void warning() {
        alertContainer.className = alert + SPACE + alertWarning;
        alertIcon.className = Icons.WARNING;
    }

    private void error() {
        alertContainer.className = alert + SPACE + alertDanger;
        alertIcon.className = Icons.ERROR;
    }

    protected void error(SafeHtml message) {
        error();
        alertText.innerHTML = message.asString();
    }
}
