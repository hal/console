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
package org.jboss.hal.client.deployment;

import org.jboss.hal.dmr.ModelNode;

import static org.jboss.hal.dmr.ModelDescriptionConstants.DISABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ENABLED;

/**
 * An assigned deployment used in domain mode.
 *
 * @author Harald Pehl
 */
public class Assignment extends Content {

    private final String serverGroup;
    private Deployment deployment; // might be null if there's no reference server available

    public Assignment(final String serverGroup, final ModelNode node) {
        super(node);
        this.serverGroup = serverGroup;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof Assignment)) { return false; }
        if (!super.equals(o)) { return false; }

        Assignment that = (Assignment) o;
        //noinspection SimplifiableIfStatement
        if (!serverGroup.equals(that.serverGroup)) { return false; }
        return getName().equals(that.getName());

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + serverGroup.hashCode();
        result = 31 * result + getName().hashCode();
        return result;
    }

    public boolean isEnabled() {
        ModelNode enabled = get(ENABLED);
        //noinspection SimplifiableConditionalExpression
        return enabled.isDefined() ? enabled.asBoolean() : false;
    }

    public String getServerGroup() {
        return serverGroup;
    }

    public Deployment getDeployment() {
        return deployment;
    }

    public boolean hasDeployment() {
        return deployment != null;
    }

    public void setDeployment(final Deployment deployment) {
        this.deployment = deployment;
    }

    @Override
    public String toString() {
        return "Assignment{" + getName() + "@" + serverGroup + ", " + (isEnabled() ? ENABLED : DISABLED) + "}";
    }
}
