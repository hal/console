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
package org.jboss.hal.client.tools;

import java.util.List;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import elemental.client.Browser;
import org.jboss.hal.ballroom.HasTitle;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.extension.Extension;
import org.jboss.hal.core.extension.ExtensionRegistry;
import org.jboss.hal.core.extension.ExtensionStorage;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.ApplicationPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static org.jboss.hal.dmr.ModelDescriptionConstants.SCRIPT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STYLES;

/**
 * @author Harald Pehl
 */
public class ExtensionPresenter extends ApplicationPresenter<ExtensionPresenter.MyView, ExtensionPresenter.MyProxy>
        implements HasTitle {

    // @formatter:off
    @ProxyStandard
    @NameToken(NameTokens.EXTENSIONS)
    public interface MyProxy extends ProxyPlace<ExtensionPresenter> {}

    public interface MyView extends HalView, HasPresenter<ExtensionPresenter> {
        void update(List<Extension> extensions);
    }
    // @formatter:on


    static final ExtensionResources RESOURCES = GWT.create(ExtensionResources.class);

    private final ExtensionStorage extensionStorage;
    private final ExtensionRegistry extensionRegistry;
    private final Resources resources;

    @Inject
    public ExtensionPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy myProxy,
            final ExtensionStorage extensionStorage,
            final ExtensionRegistry extensionRegistry,
            final Resources resources) {
        super(eventBus, view, myProxy);
        this.extensionStorage = extensionStorage;
        this.extensionRegistry = extensionRegistry;
        this.resources = resources;
    }

    @Override
    public String getTitle() {
        return Names.EXTENSIONS;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    protected void onReset() {
        super.onReset();
        refresh();
    }

    private void refresh() {
        // TODO Load server side extensions from /core-service=management/console-extension=*
        getView().update(extensionStorage.list());
    }

    void addExtension() {
        Metadata metadata = Metadata.staticDescription(RESOURCES.extension());
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.EXTENSION_ADD, metadata)
                .include(SCRIPT, STYLES)
                .addOnly()
                .unsorted()
                .build();
        AddResourceDialog dialog = new AddResourceDialog(Names.EXTENSIONS, form, (name, model) -> {
            if (model != null) {
                Extension extension = new Extension(model);
                extensionRegistry.ping(extension, status -> {
                    if (status >= 200 && status < 400) {
                        extensionRegistry.inject(extension);
                        extensionStorage.save(extension);
                        MessageEvent.fire(getEventBus(),
                                Message.success(resources.messages().addSingleResourceSuccess(Names.EXTENSION)));
                        refresh();
                    } else {
                        MessageEvent.fire(getEventBus(),
                                Message.error(resources.messages().addExtensionError(),
                                        "GET " + extension.getScript() + ": " + status)); //NON-NLS
                    }
                });
            }
        });
        dialog.show();
    }

    void removeExtension(final Extension extension) {
        DialogFactory.showConfirmation(resources.messages().removeConfirmationTitle(Names.EXTENSION),
                resources.messages().removeExtensionQuestion(), () -> {
                    extensionStorage.remove(extension);
                    Message message = Message.success(resources.messages().removeExtensionSuccess(),
                            resources.constants().reload(), () -> Browser.getWindow().getLocation().reload(), true);
                    MessageEvent.fire(getEventBus(), message);
                    refresh();
                });
    }
}
