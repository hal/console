/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.ballroom.dialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import elemental.client.Browser;
import elemental.html.ParagraphElement;
import org.jboss.hal.resources.Constants;

import static org.jboss.hal.ballroom.dialog.Dialog.Size.SMALL;

/**
 * @author Harald Pehl
 */
public final class DialogFactory {

    private static final Constants CONSTANTS = GWT.create(Constants.class);

    private DialogFactory() {}

    public static Dialog confirmation(String title, SafeHtml question, Dialog.Callback confirm) {
        ParagraphElement p = Browser.getDocument().createParagraphElement();
        p.setInnerHTML(question.asString());

        return new Dialog.Builder(title)
                .closeIcon(true)
                .closeOnEsc(true)
                .noYes(confirm)
                .size(SMALL)
                .primary(CONSTANTS.yes(), confirm)
                .add(p)
                .build();
    }
}
