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
package org.jboss.hal.client.runtime.host;

import java.util.Map;

import elemental.dom.Element;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.FormValidation;
import org.jboss.hal.ballroom.form.StateMachine;
import org.jboss.hal.dmr.ModelNode;

/**
 * @author Harald Pehl
 */
public class HostForm<T extends ModelNode> implements Form<T> {

    private final Form<T> form;

    public HostForm(final Form<T> form) {
        this.form = form;
    }

    @Override
    public boolean isUndefined() {return form.isUndefined();}

    @Override
    public boolean isTransient() {return form.isTransient();}

    @Override
    public void view(final T model) {form.view(model);}

    @Override
    public void clear() {form.clear();}

    @Override
    public void edit(final T model) {form.edit(model);}

    @Override
    public boolean save() {return form.save();}

    @Override
    public void setSaveCallback(final SaveCallback<T> saveCallback) {form.setSaveCallback(saveCallback);}

    @Override
    public void cancel() {form.cancel();}

    @Override
    public void setCancelCallback(final CancelCallback<T> cancelCallback) {form.setCancelCallback(cancelCallback);}

    @Override
    public void setPrepareReset(final PrepareReset<T> prepareReset) {form.setPrepareReset(prepareReset);}

    @Override
    public void reset() {form.reset();}

    @Override
    public void setPrepareRemove(final PrepareRemove<T> removeCallback) {form.setPrepareRemove(removeCallback);}

    @Override
    public void remove() {form.remove();}

    @Override
    public String getId() {return form.getId();}

    @Override
    public T getModel() {return form.getModel();}

    @Override
    public StateMachine getStateMachine() {return form.getStateMachine();}

    @Override
    public <F> FormItem<F> getFormItem(final String name) {return form.getFormItem(name);}

    @Override
    public Iterable<FormItem> getFormItems() {return form.getFormItems();}

    @Override
    public Iterable<FormItem> getBoundFormItems() {return form.getBoundFormItems();}

    @Override
    public Map<String, Object> getUpdatedModel() {
        return form.getUpdatedModel();
    }

    @Override
    public void addFormValidation(final FormValidation<T> formValidation) {form.addFormValidation(formValidation);}

    @Override
    public Element asElement() {return form.asElement();}

    @Override
    public void attach() {form.attach();}

    @Override
    public void detach() {form.detach();}
}
