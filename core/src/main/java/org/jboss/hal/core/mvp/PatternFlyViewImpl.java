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
package org.jboss.hal.core.mvp;

import com.gwtplatform.mvp.client.ViewImpl;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.PatternFly;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Harald Pehl
 */
public abstract class PatternFlyViewImpl extends ViewImpl implements PatternFlyView {

    private final List<Attachable> attachables;
    private boolean attached;

    protected PatternFlyViewImpl() {
        attachables = new ArrayList<>();
        attached = false;
    }

    protected void registerAttachable(Attachable first, Attachable... rest) {
        attachables.add(first);
        if (rest != null) {
            for (Attachable attachable : rest) {
                attachables.add(attachable);
            }
        }
    }

    @Override
    public void attach() {
        if (!attached) {
            PatternFly.initComponents();
            for (Attachable attachable : attachables) {
                attachable.attach();
            }
            attached = true;
        }
    }
}
