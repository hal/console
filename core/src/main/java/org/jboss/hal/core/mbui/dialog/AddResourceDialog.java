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
package org.jboss.hal.core.mbui.dialog;

import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.gwt.core.client.GWT;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.Dialog.Size;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.resources.Constants;

import java.util.List;

import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

/**
 * @author Harald Pehl
 */
public class AddResourceDialog<T extends ModelNode> {

    @FunctionalInterface
    @SuppressWarnings("WeakerAccess")
    public interface Callback<T> {

        void onAdd(final String name, final T model);
    }


    private static final Constants CONSTANTS = GWT.create(Constants.class);

    private Form<T> form;
    private Dialog dialog;

    /**
     * Creates an add resource dialog with a form which contains an unbound name item plus all request properties from
     * the add operation. Clicking on the add button will call the specified callback.
     */
    public AddResourceDialog(final String id, final String title,
            final SecurityContext securityContext,
            final ResourceDescription resourceDescription,
            final Callback<T> callback) {

        TextBoxItem nameItem = new TextBoxItem(NAME, CONSTANTS.name());
        nameItem.setRequired(true);
        nameItem.setExpressionAllowed(false);

        List<String> properties = Ordering.natural().sortedCopy(
                Iterables.transform(resourceDescription.getRequestProperties(), Property::getName));

        ModelNodeForm.Builder<T> formBuilder = new ModelNodeForm.Builder<T>(id, securityContext, resourceDescription)
                .createResource()
                .unsorted()
                .unboundFormItem(nameItem, 0)
                .onSave((f, changedValues) -> {
                    String name = String.valueOf(changedValues.remove(NAME));
                    callback.onAdd(name, form.getModel());
                });
        formBuilder.include(properties);

        init(title, formBuilder.build());
    }

    /**
     * Uses an existing form for the dialog, please make sure the form has an item called {@link
     * org.jboss.hal.dmr.ModelDescriptionConstants#NAME}. If the form has a save callback it's overridden by this
     * constructor.
     */
    public AddResourceDialog(final String title, final Form<T> form, final Callback<T> callback) {
        form.setSaveCallback((f, changedValues) -> {
            String name = String.valueOf(changedValues.remove(NAME));
            callback.onAdd(name, form.getModel());
        });
        init(title, form);
    }

    private void init(final String title, final Form<T> form) {
        this.form = form;
        this.dialog = new Dialog.Builder(title)
                .add(form.asElement())
                .primary(CONSTANTS.add(), () -> {
                    form.save();
                    return true;
                })
                .secondary(CONSTANTS.cancel(), () -> true)
                .size(Size.MEDIUM)
                .closeOnEsc(true)
                .build();
        this.dialog.registerAttachable(form);
    }

    public void show() {
        // show first (which attaches everything), then call form.add()
        dialog.show();
        //noinspection unchecked
        form.add((T) new ModelNode());
    }
}
