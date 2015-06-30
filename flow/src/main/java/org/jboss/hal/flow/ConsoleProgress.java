/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.hal.flow;

/**
 * Progress implementation for the dev mode which prints the progress to {@code System.out}.
 *
 * @author Harald Pehl
 */
public final class ConsoleProgress implements Progress {

    private final String id;
    private int max;
    private int current;

    public ConsoleProgress(final String id) {this.id = id;}

    @Override
    public void reset() {
        reset(0);
    }

    @Override
    public void reset(final int max) {
        this.current = 0;
        this.max = max;
        System.out.println("progress#" + id + ".reset(" + max + ")");
    }

    @Override
    public void tick() {
        current++;
        if (max == 0) {
            System.out.println("progress#" + id + ".tick(" + current + ")");
        } else {
            System.out.println("progress#" + id + ".tick(" + current + " / " + max + ")");
        }
    }

    @Override
    public void finish() { System.out.println("progress#" + id + ".finish()"); }
}
