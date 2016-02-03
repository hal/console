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
package org.jboss.hal.ballroom.form;

import org.jboss.hal.ballroom.form.InputElement.Context;
import org.jboss.hal.ballroom.form.SelectBoxBridge.Single;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.jboss.hal.resources.CSS.formControl;
import static org.jboss.hal.resources.CSS.selectpicker;

/**
 * @author Harald Pehl
 */
public class SingleSelectBoxItem extends AbstractFormItem<String> {

    private final boolean allowEmpty;
    private SingleSelectBoxElement selectBox;

    public SingleSelectBoxItem(final String name, final String label, List<String> options) {
        this(name, label, options, false);
    }

    public SingleSelectBoxItem(final String name, final String label, List<String> options, boolean allowEmpty) {
        super(name, label, null, new Context<>(allowEmpty));
        this.allowEmpty = allowEmpty;
        List<String> localOptions = options;
        if (allowEmpty && !options.isEmpty() && emptyToNull(options.get(0)) != null) {
            localOptions = new ArrayList<>(options);
            localOptions.add(0, "");
        }
        setOptions(localOptions);
    }

    @Override
    protected InputElement<String> newInputElement(Context<?> context) {
        Boolean allowEmpty = (Boolean) context.data();
        selectBox = new SingleSelectBoxElement(allowEmpty);
        selectBox.setClassName(formControl + " " + selectpicker);
        Single.element(selectBox.asElement()).onChange((event, index) -> {
            String value = getValue();
            setModified(true);
            setUndefined(isNullOrEmpty(value));
            signalChange(value);
        });
        return selectBox;
    }

    @Override
    public boolean supportsExpressions() {
        return false;
    }

    public void setOptions(List<String> options) {
        selectBox.setOptions(options);
        setUndefined(allowEmpty);
        if (!allowEmpty) {
            setModified(true);
        }
    }

    @Override
    public void setUndefined(final boolean undefined) {
        if (allowEmpty || !undefined) {
            // ok
            super.setUndefined(undefined);
        } else {
            // there's always a value and this form item can never get undefined!
            setUndefined(false);
        }
    }

    @Override
    void markDefaultValue(final boolean on, final String defaultValue) {
        super.markDefaultValue(on, defaultValue);
        Single.element(selectBox.asElement()).refresh();
    }


    static class SingleSelectBoxElement extends SelectBoxElement<String> {

        SingleSelectBoxElement(final boolean allowEmpty) {
            super(allowEmpty, false);
        }

        @Override
        public String getValue() {
            return Single.element(asElement()).getValue();
        }

        @Override
        public void setValue(final String value) {
            Single.element(asElement()).setValue(value);
        }

        @Override
        public void clearValue() {
            if (allowEmpty) {
                Single.element(asElement()).setValue("");
            }
        }

        @Override
        public String getText() {
            return getValue();
        }

        @Override
        public void setText(final String s) {
            setValue(s);
        }
    }
}
