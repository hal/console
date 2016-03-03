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
import com.google.web.bindery.event.shared.EventBus;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.DivElement;
import elemental.html.InputElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.InputType;
import org.jboss.hal.ballroom.IdBuilder;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Messages;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.jboss.gwt.elemento.core.EventType.click;

/**
 * @author Harald Pehl
 */
class NewSingletonWizard extends Wizard<NewSingletonWizard.SingletonContext, NewSingletonWizard.SingletonState> {

    enum SingletonState {CHOOSE, CREATE}


    static class SingletonContext {

        final Node<Context> parent;
        final List<String> children;
        String singleton;
        ModelNode modelNode;

        SingletonContext(final Node<Context> parent, final List<String> children) {
            this.parent = parent;
            this.children = children;
        }
    }


    private static class ChooseSingletonStep extends WizardStep<SingletonContext, SingletonState> {

        private final Element root;

        ChooseSingletonStep(final NewSingletonWizard wizard) {
            super(wizard, CONSTANTS.chooseSingleton());

            Elements.Builder builder = new Elements.Builder().div();
            SortedSet<String> singletons = new TreeSet<>(wizard.getContext().parent.data.getSingletons());
            SortedSet<String> existing = new TreeSet<>(wizard.getContext().children);
            singletons.removeAll(existing);

            for (String singleton : singletons) {
                // @formatter:off
                builder.div().css(CSS.radio)
                    .label()
                        .input(InputType.radio)
                            .attr("name", "singleton") //NON-NLS
                            .attr("value", singleton)
                            .on(click, event ->
                                    wizard.getContext().singleton = ((InputElement) event.getTarget()).getValue())
                        .span().textContent(singleton).end()
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
            InputElement firstRadio = (InputElement) asElement().querySelector("input[type=radio]"); //NON-NLS
            firstRadio.setChecked(true);
            context.singleton = firstRadio.getValue();
        }
    }


    private static class CreateSingletonStep extends WizardStep<SingletonContext, SingletonState> {

        private static final Logger logger = LoggerFactory.getLogger(CreateSingletonStep.class);

        private final MetadataProvider metadataProvider;
        private final Capabilities capabilities;
        private final DivElement root;
        private final EventBus eventBus;
        private final Resources resources;

        CreateSingletonStep(final NewSingletonWizard wizard, final Capabilities capabilities, final EventBus eventBus,
                final Resources resources) {
            super(wizard, MESSAGES.addResourceTitle(wizard.getContext().parent.text));
            this.capabilities = capabilities;
            this.eventBus = eventBus;
            this.resources = resources;
            this.metadataProvider = wizard.metadataProvider;
            this.root = Browser.getDocument().createDivElement();
        }

        @Override
        public Element asElement() {
            return root;
        }

        @Override
        protected void onShow(final SingletonContext context) {
            Elements.removeChildrenFrom(root);
            setTitle(wizard.getContext().parent.text + "=" + wizard.getContext().singleton);
            ResourceAddress singletonAddress = wizard.getContext().parent.data.getAddress().getParent()
                    .add(wizard.getContext().parent.text, wizard.getContext().singleton);
            metadataProvider.getMetadata(wizard.getContext().parent, singletonAddress,
                    new MetadataProvider.MetadataCallback() {
                        @Override
                        public void onError(final Throwable error) {
                            //noinspection HardCodedStringLiteral
                            logger.error("Error while processing metadata for {}: {}", singletonAddress,
                                    error.getMessage());
                            MessageEvent.fire(eventBus,
                                    Message.error(resources.constants().metadataError(), error.getMessage()));
                        }

                        @Override
                        public void onMetadata(final SecurityContext securityContext,
                                final ResourceDescription description) {

                            String id = IdBuilder.build(id(), "form");
                            Form<ModelNode> form = new ModelNodeForm.Builder<>(id, securityContext, description,
                                    capabilities)
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

    private final MetadataProvider metadataProvider;

    NewSingletonWizard(final EventBus eventBus, final MetadataProvider metadataProvider, final Resources resources,
            final Node<Context> parent, List<String> children, FinishCallback<SingletonContext> finishCallback) {
        super(IdBuilder.build(parent.id, "add", "singleton"),
                MESSAGES.addResourceTitle(parent.text),
                new SingletonContext(parent, children),
                finishCallback);
        this.metadataProvider = metadataProvider;
        addStep(SingletonState.CHOOSE, new ChooseSingletonStep(this));
        addStep(SingletonState.CREATE,
                new CreateSingletonStep(this, metadataProvider.capabilities, eventBus, resources));
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
