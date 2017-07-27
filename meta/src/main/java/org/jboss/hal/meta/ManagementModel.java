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
package org.jboss.hal.meta;

import org.jboss.hal.config.semver.Version;
import org.jboss.hal.dmr.ModelNode;

import static org.jboss.hal.dmr.ModelDescriptionConstants.MANAGEMENT_MAJOR_VERSION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MANAGEMENT_MICRO_VERSION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MANAGEMENT_MINOR_VERSION;

/** Static code related to management model versions and support for various operations and attributes */
public class ManagementModel {

    private static final Version V_2_0_0 = Version.forIntegers(2, 0, 0); // WildFly 8
    private static final Version V_3_0_0 = Version.forIntegers(3, 0, 0); // WildFly 9
    private static final Version V_5_0_0 = Version.forIntegers(5, 0, 0); // WildFly 11

    public static Version parseVersion(ModelNode modelNode) {
        if (modelNode.hasDefined(MANAGEMENT_MAJOR_VERSION) &&
                modelNode.hasDefined(MANAGEMENT_MINOR_VERSION) &&
                modelNode.hasDefined(MANAGEMENT_MICRO_VERSION)) {
            int major = modelNode.get(MANAGEMENT_MAJOR_VERSION).asInt();
            int minor = modelNode.get(MANAGEMENT_MINOR_VERSION).asInt();
            int micro = modelNode.get(MANAGEMENT_MICRO_VERSION).asInt();
            return Version.forIntegers(major, minor, micro);
        }
        return Version.UNDEFINED;
    }


    // ------------------------------------------------------ supports methods (A-Z)

    /**
     * Checks support for the capabilities registry.
     *
     * @return {@code true} if the provided version isn't {@linkplain Version#UNDEFINED undefined} and greater than or
     * equal {@code 5.0.0}
     */
    public static boolean supportsCapabilitiesRegistry(Version version) {
        return ensureVersion(version, V_5_0_0);
    }

    /**
     * Checks support for the resource {@code /subsystem=ejb3/application-security-domain=*}.
     *
     * @return {@code true} if the provided version isn't {@linkplain Version#UNDEFINED undefined} and greater than or
     * equal {@code 5.0.0}
     */
    public static boolean supportsEjbApplicationSecurityDomain(Version version) {
        return ensureVersion(version, V_5_0_0);
    }

    /**
     * Check support for {@code :explode} operation on deployment resources.
     *
     * @return {@code true} if the provided version isn't {@linkplain Version#UNDEFINED undefined} and greater than or
     * equal {@code 5.0.0}
     */
    public static boolean supportsExplodeDeployment(Version version) {
        return ensureVersion(version, V_5_0_0);
    }

    /**
     * Check support for {@code :list-log-files} operation.
     *
     * @return {@code true} if the provided version isn't {@linkplain Version#UNDEFINED undefined} and greater than or
     * equal {@code 2.0.0}
     */
    public static boolean supportsListLogFiles(Version version) {
        return ensureVersion(version, V_2_0_0);
    }

    /**
     * Check support for {@code :read-content} operation for deployments.
     *
     * @return {@code true} if the provided version isn't {@linkplain Version#UNDEFINED undefined} and greater than or
     * equal {@code 5.0.0}
     */
    public static boolean supportsReadContentFromDeployment(Version version) {
        return ensureVersion(version, V_5_0_0);
    }

    /**
     * Check support for suspend operation and related attributes.
     *
     * @return {@code true} if the provided version isn't {@linkplain Version#UNDEFINED undefined} and greater than or
     * equal {@code 3.0.0}
     */
    public static boolean supportsSuspend(Version version) {
        return ensureVersion(version, V_3_0_0);
    }


    // ------------------------------------------------------ helper methods

    private static boolean ensureVersion(Version existing, Version expected) {
        return existing != Version.UNDEFINED && existing.greaterThanOrEqualTo(expected);
    }
}
