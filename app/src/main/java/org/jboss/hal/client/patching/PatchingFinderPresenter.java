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
package org.jboss.hal.client.patching;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.mvp.FinderPresenter;
import org.jboss.hal.core.mvp.FinderView;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

/**
 * @author Claudio Miranda <claudio@redhat.com>
 */
public class PatchingFinderPresenter extends FinderPresenter<PatchingFinderPresenter.MyView, PatchingFinderPresenter.MyProxy> {

    // @formatter:off
    @ProxyStandard
    @NameToken(NameTokens.PATCHING)
    public interface MyProxy extends ProxyPlace<PatchingFinderPresenter> {}

    public interface MyView extends FinderView {}
    // @formatter:on

    private final Environment environment;

    @Inject
    public PatchingFinderPresenter(
            final EventBus eventBus,
            final PatchingFinderPresenter.MyView view,
            final PatchingFinderPresenter.MyProxy proxy,
            final Finder finder,
            final Resources resources,
            final Environment environment) {
        super(eventBus, view, proxy, finder, resources);
        this.environment = environment;
    }

    @Override
    protected String initialColumn() {
        return environment.isStandalone() ? Ids.PATCHING : Ids.PATCHING_DOMAIN;
    }

    @Override
    protected PreviewContent initialPreview() {
        return new PreviewContent(Names.PATCHING, resources.previews().patching());
    }
}
