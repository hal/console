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
package org.jboss.hal.core;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Harald Pehl
 */
public class ProfileSelectionEvent extends GwtEvent<ProfileSelectionEvent.ProfileSelectionHandler> {

    public interface ProfileSelectionHandler extends EventHandler {

        void onProfileSelected(ProfileSelectionEvent event);
    }


    private static final Type<ProfileSelectionHandler> TYPE = new Type<>();

    public static Type<ProfileSelectionHandler> getType() {
        return TYPE;
    }

    private final String profile;

    public ProfileSelectionEvent(final String profile) {this.profile = profile;}

    public String getProfile() {
        return profile;
    }

    @Override
    protected void dispatch(ProfileSelectionHandler handler) {
        handler.onProfileSelected(this);
    }

    @Override
    public Type<ProfileSelectionHandler> getAssociatedType() {
        return TYPE;
    }
}
