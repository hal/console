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
package org.jboss.hal.client.skeleton;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Window;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.HasPresenter;

import javax.inject.Inject;

import static org.jboss.hal.resources.Names.NYI;

/**
 * @author Harald Pehl
 */
public class FooterPresenter extends PresenterWidget<FooterPresenter.MyView> {

    // @formatter:off
    public interface MyView extends View, HasPresenter<FooterPresenter> {
        void update(Environment environment);
    }
    // @formatter:on


    private final Environment environment;

    @Inject
    public FooterPresenter(final EventBus eventBus,
            final MyView view,
            final Environment environment) {
        super(eventBus, view);
        this.environment = environment;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
        getView().update(environment);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        Scheduler.get().scheduleDeferred(PatternFly::initComponents);
    }

    public void onShowVersion() {
        Window.alert(NYI);
    }

    public void onModelBrowser() {
        Window.alert(NYI);
    }

    public void onExpressionResolver() {
        Window.alert(NYI);
    }

    public void onSettings() {
        Window.alert(NYI);
    }
}
