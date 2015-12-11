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

import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasName;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import elemental.dom.Element;

/**
 * @author Harald Pehl
 */
public interface FormItem<T> extends HasEnabled, Focusable, HasName, HasValue<T>, HasText /* for expression support */ {

    Element asElement(Form.State state);

    void clearValue();

    /**
     * @return if this form item has no value.
     */
    boolean isEmpty();

    void setId(String id);

    void identifyAs(String id, String... additionalIds);

    void addValidationHandler(FormItemValidation<T> validationHandler);

    boolean validate();

    void clearError();

    void showError(String message);

    boolean isExpressionValue();

    void setExpressionValue(String expressionValue);

    String getExpressionValue();

    boolean supportsExpressions();

    void addResolveExpressionHandler(ResolveExpressionEvent.Handler handler);

    void resetMetaData();

    String getLabel();

    void setLabel(String label);

    boolean isRequired();

    void setRequired(boolean required);

    boolean isModified();

    void setModified(boolean modified);

    boolean isUndefined();

    void setUndefined(boolean undefined);

    boolean isExpressionAllowed();

    void setExpressionAllowed(boolean expressionAllowed);

    boolean isRestricted();

    void setRestricted(boolean restricted);
}
