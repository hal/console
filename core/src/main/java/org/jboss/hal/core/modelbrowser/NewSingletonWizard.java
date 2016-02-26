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
package org.jboss.hal.core.modelbrowser;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.DivElement;
import elemental.html.InputElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.InputType;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.IdBuilder;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Messages;

import javax.inject.Provider;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.util.Collections.singleton;
import static org.jboss.gwt.elemento.core.EventType.click;

/**
 * @author Harald Pehl
 */
class NewSingletonWizard extends Wizard<NewSingletonWizard.SingletonContext, NewSingletonWizard.SingletonState> {

    enum SingletonState {CHOOSE, CREATE}


    static class SingletonContext {

        final AddressTemplate parentTemplate;
        final Node<Context> parentNode;
        final List<String> children;
        String singleton;
        ModelNode modelNode;

        SingletonContext(final AddressTemplate parentTemplate, final Node<Context> parentNode,
                final List<String> children) {
            this.parentTemplate = parentTemplate;
            this.parentNode = parentNode;
            this.children = children;
        }
    }


    private static class ChooseSingletonStep extends WizardStep<SingletonContext, SingletonState> {

        private final Element root;

        ChooseSingletonStep(final Wizard<SingletonContext, SingletonState> wizard) {
            super(wizard, CONSTANTS.chooseSingleton());

            Elements.Builder builder = new Elements.Builder().div();
            SortedSet<String> singletons = new TreeSet<>(wizard.getContext().parentNode.data.getSingletons());
            SortedSet<String> existing = new TreeSet<>(wizard.getContext().children);
            singletons.removeAll(existing);

            for (String singleton : singletons) {
                // @formatter:off
                builder.div().css(CSS.radio)
                    .label()
                        .input(InputType.radio)
                            .attr("name", "singleton")
                            .attr("value", singleton) //NON-NLS
                            .on(click, event ->
                                    wizard.getContext().singleton = ((InputElement) event.getTarget()).getValue())
                        .span().innerText(singleton).end()
                    .end()
                .end();
                // @formatter:on
            }
            this.root = builder.end().build();
        }

        @Override
        public Element asElement() {
            return root;
        }

        @Override
        protected void onShow(final SingletonContext context) {
            InputElement firstRadio = (InputElement) asElement().querySelector("input[type=radio]");
            firstRadio.setChecked(true);
            context.singleton = firstRadio.getValue();
        }
    }


    private static class CreateSingletonStep extends WizardStep<SingletonContext, SingletonState> {

        private final NewSingletonWizard wizard;
        private final DivElement root;

        CreateSingletonStep(final NewSingletonWizard wizard) {
            super(wizard, MESSAGES.addResourceTitle(wizard.getContext().parentNode.text));
            this.wizard = wizard;
            this.root = Browser.getDocument().createDivElement();
        }

        @Override
        public Element asElement() {
            return root;
        }

        @Override
        protected void onShow(final SingletonContext context) {
            Elements.removeChildrenFrom(root);
            Provider<Progress> progressProvider = () -> Progress.NOOP;
            AddressTemplate template = wizard.getContext().parentTemplate.replaceWildcards(context.singleton);
            wizard.modelBrowser.metadataProcessor.process(Ids.MODEL_BROWSER, singleton(template), progressProvider,
                    new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(final Throwable throwable) {
                            // TODO Error handling
                        }

                        @Override
                        public void onSuccess(final Void aVoid) {
                            String id = IdBuilder.build(id(), "form");
                            SecurityContext securityContext = wizard.modelBrowser.securityFramework.lookup(template);
                            ResourceDescription description = wizard.modelBrowser.resourceDescriptions.lookup(template);
                            Form<ModelNode> form = new ModelNodeForm.Builder<>(id,
                                    securityContext, description)
                                    .createResource()
                                    .onSave((f, changedValues) -> wizard.getContext().modelNode = f.getModel())
                                    .build();
                            root.appendChild(form.asElement());
                            PatternFly.initComponents();
                            form.attach();
                            form.add(new ModelNode());
                        }
                    });
        }
    }


    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final Messages MESSAGES = GWT.create(Messages.class);

    private final ModelBrowser modelBrowser;

    NewSingletonWizard(final ModelBrowser modelBrowser, final AddressTemplate parentTemplate,
            final Node<Context> parentNode, List<String> children, FinishCallback<SingletonContext> finishCallback) {
        super(IdBuilder.build(parentNode.id, "add", "singleton"), MESSAGES.addResourceTitle(parentNode.text),
                new SingletonContext(parentTemplate, parentNode, children), finishCallback);
        this.modelBrowser = modelBrowser;
        addStep(SingletonState.CHOOSE, new ChooseSingletonStep(this));
        addStep(SingletonState.CREATE, new CreateSingletonStep(this));
    }

    @Override
    protected SingletonState back(final SingletonState singletonState) {
        return singletonState == SingletonState.CREATE ? SingletonState.CHOOSE : null;
    }

    @Override
    protected SingletonState next(final SingletonState singletonState) {
        return singletonState == SingletonState.CHOOSE ? SingletonState.CREATE : null;
    }
}
