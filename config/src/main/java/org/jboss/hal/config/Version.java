/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.config;

import java.util.Iterator;

import com.google.common.base.Splitter;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

/**
 * Version identifier for bundles and packages.
 *
 * <p>
 * Version identifiers have four components.
 * <ol>
 * <li>Major version. A non-negative integer.</li>
 * <li>Minor version. A non-negative integer.</li>
 * <li>Micro version. A non-negative integer.</li>
 * <li>Qualifier. A text string. See <code>Version(String)</code> for the format of the qualifier string.</li>
 * </ol>
 *
 * <p>
 * <code>Version</code> objects are immutable.
 */
@JsType(namespace = "hal.config")
public class Version implements Comparable {

    private static final String SEPARATOR = ".";

    /**
     * The empty version "0.0.0". Equivalent to calling <code>new Version(0,0,0)</code>.
     */
    @JsIgnore public static final Version EMPTY_VERSION = new Version(0, 0, 0);

    /**
     * Parses a version identifier from the specified string.
     *
     * <p>
     * See <code>Version(String)</code> for the format of the version string.
     *
     * @param version String representation of the version identifier. Leading and trailing whitespace will be ignored.
     *
     * @return A <code>Version</code> object representing the version identifier. If <code>version</code> is <code>null</code>
     *         or the empty string then <code>EMPTY_VERSION</code> will be returned.
     *
     * @throws IllegalArgumentException If <code>version</code> is improperly formatted.
     */
    public static Version parseVersion(String version) {
        if (version == null) {
            return EMPTY_VERSION;
        }

        version = version.trim();
        if (version.length() == 0) {
            return EMPTY_VERSION;
        }

        return new Version(version);
    }

    private final int major;
    private final int minor;
    private final int micro;
    private final String qualifier;

    /**
     * Creates a version identifier from the specified numerical components.
     *
     * <p>
     * The qualifier is set to the empty string.
     *
     * @param major Major component of the version identifier.
     * @param minor Minor component of the version identifier.
     * @param micro Micro component of the version identifier.
     *
     * @throws IllegalArgumentException If the numerical components are negative.
     */
    @JsIgnore
    public Version(int major, int minor, int micro) {
        this(major, minor, micro, null);
    }

    /**
     * Creates a version identifier from the specifed components.
     *
     * @param major Major component of the version identifier.
     * @param minor Minor component of the version identifier.
     * @param micro Micro component of the version identifier.
     * @param qualifier Qualifier component of the version identifier. If <code>null</code> is specified, then the qualifier
     *        will be set to the empty string.
     *
     * @throws IllegalArgumentException If the numerical components are negative or the qualifier string is invalid.
     */
    @JsIgnore
    public Version(int major, int minor, int micro, String qualifier) {
        if (qualifier == null) {
            qualifier = ""; //$NON-NLS-1$
        }

        this.major = major;
        this.minor = minor;
        this.micro = micro;
        this.qualifier = qualifier;
        validate();
    }

    /**
     * Created a version identifier from the specified string.
     *
     * <p>
     * Here is the grammar for version strings.
     *
     * <pre>
     * version ::= major('.'minor('.'micro('.'qualifier)?)?)?
     * major ::= digit+
     * minor ::= digit+
     * micro ::= digit+
     * qualifier ::= (alpha|digit|'_'|'-')+
     * digit ::= [0..9]
     * alpha ::= [a..zA..Z]
     * </pre>
     *
     * There must be no whitespace in version.
     *
     * @param version String representation of the version identifier.
     *
     * @throws IllegalArgumentException If <code>version</code> is improperly formatted.
     */
    @JsIgnore
    public Version(String version) {
        int major;
        int minor = 0;
        int micro = 0;
        String qualifier = "";

        try {
            Iterator<String> iterator = Splitter.on(SEPARATOR).split(version).iterator();
            major = Integer.parseInt(iterator.next());

            if (iterator.hasNext()) {
                minor = Integer.parseInt(iterator.next());

                if (iterator.hasNext()) {
                    String rest = iterator.next();
                    if (rest.contains("-")) {
                        micro = Integer.parseInt(rest.substring(0, rest.indexOf("-")));
                        qualifier = rest.substring(rest.indexOf("-") + 1);
                    } else {
                        micro = Integer.parseInt(rest);
                    }
                    if (iterator.hasNext()) {
                        qualifier = iterator.next();

                        if (iterator.hasNext()) {
                            throw new IllegalArgumentException("invalid format");
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid format");
        }

        this.major = major;
        this.minor = minor;
        this.micro = micro;
        this.qualifier = qualifier;
        validate();
    }

    /**
     * Called by the Version constructors to validate the version components.
     *
     * @throws IllegalArgumentException If the numerical components are negative or the qualifier string is invalid.
     */
    private void validate() {
        if (major < 0) {
            throw new IllegalArgumentException("negative major");
        }
        if (minor < 0) {
            throw new IllegalArgumentException("negative minor");
        }
        if (micro < 0) {
            throw new IllegalArgumentException("negative micro");
        }
        int length = qualifier.length();
        for (int i = 0; i < length; i++) {
            if ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-".indexOf(
                    qualifier.charAt(i)) == -1) {
                throw new IllegalArgumentException("invalid qualifier");
            }
        }
    }

    /**
     * Returns the major component of this version identifier.
     *
     * @return The major component.
     */
    public int getMajor() {
        return major;
    }

    /**
     * Returns the minor component of this version identifier.
     *
     * @return The minor component.
     */
    public int getMinor() {
        return minor;
    }

    /**
     * Returns the micro component of this version identifier.
     *
     * @return The micro component.
     */
    public int getMicro() {
        return micro;
    }

    /**
     * Returns the qualifier component of this version identifier.
     *
     * @return The qualifier component.
     */
    public String getQualifier() {
        return qualifier;
    }

    /**
     * Returns the string representation of this version identifier.
     *
     * <p>
     * The format of the version string will be <code>major.minor.micro</code> if qualifier is the empty string or
     * <code>major.minor.micro.qualifier</code> otherwise.
     *
     * @return The string representation of this version identifier.
     */
    @JsIgnore
    public String toString() {
        String base = major + SEPARATOR + minor + SEPARATOR + micro;
        if (qualifier.length() == 0) {
            return base;
        } else {
            return base + SEPARATOR + qualifier;
        }
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return An integer which is a hash code value for this object.
     */
    @JsIgnore
    public int hashCode() {
        return (major << 24) + (minor << 16) + (micro << 8) + qualifier.hashCode();
    }

    /**
     * Compares this <code>Version</code> object to another object.
     *
     * <p>
     * A version is considered to be <b>equal to </b> another version if the major, minor and micro components are equal and the
     * qualifier component is equal (using <code>String.equals</code>).
     *
     * @param object The <code>Version</code> object to be compared.
     *
     * @return <code>true</code> if <code>object</code> is a <code>Version</code> and is equal to this object;
     *         <code>false</code> otherwise.
     */
    @JsIgnore
    public boolean equals(Object object) {
        if (object == this) { // quicktest
            return true;
        }

        if (!(object instanceof Version)) {
            return false;
        }

        Version other = (Version) object;
        return (major == other.major) && (minor == other.minor)
                && (micro == other.micro) && qualifier.equals(other.qualifier);
    }

    /**
     * Compares this <code>Version</code> object to another object.
     *
     * <p>
     * A version is considered to be <b>less than </b> another version if its major component is less than the other version's
     * major component, or the major components are equal and its minor component is less than the other version's minor
     * component, or the major and minor components are equal and its micro component is less than the other version's micro
     * component, or the major, minor and micro components are equal and it's qualifier component is less than the other
     * version's qualifier component (using <code>String.compareTo</code>).
     *
     * <p>
     * A version is considered to be <b>equal to</b> another version if the major, minor and micro components are equal and the
     * qualifier component is equal (using <code>String.compareTo</code>).
     *
     * @param object The <code>Version</code> object to be compared.
     *
     * @return A negative integer, zero, or a positive integer if this object is less than, equal to, or greater than the
     *         specified <code>Version</code> object.
     *
     * @throws ClassCastException If the specified object is not a <code>Version</code>.
     */
    @JsIgnore
    public int compareTo(Object object) {
        if (object == this) { // quicktest
            return 0;
        }

        Version other = (Version) object;

        int result = major - other.major;
        if (result != 0) {
            return result;
        }

        result = minor - other.minor;
        if (result != 0) {
            return result;
        }

        result = micro - other.micro;
        if (result != 0) {
            return result;
        }

        return qualifier.compareTo(other.qualifier);
    }

    /**
     * Checks if this version is greater than the other version.
     *
     * @param other the other version to compare to
     *
     * @return {@code true} if this version is greater than the other version or {@code false} otherwise
     *
     * @see #compareTo(Version other)
     */
    @JsIgnore
    public boolean greaterThan(Version other) {
        return compareTo(other) > 0;
    }

    /**
     * Checks if this version is greater than or equal to the other version.
     *
     * @param other the other version to compare to
     *
     * @return {@code true} if this version is greater than or equal to the other version or {@code false} otherwise
     *
     * @see #compareTo(Version other)
     */
    @JsIgnore
    public boolean greaterThanOrEqualTo(Version other) {
        return compareTo(other) >= 0;
    }

    /**
     * Checks if this version is less than the other version.
     *
     * @param other the other version to compare to
     *
     * @return {@code true} if this version is less than the other version or {@code false} otherwise
     *
     * @see #compareTo(Version other)
     */
    @JsIgnore
    public boolean lessThan(Version other) {
        return compareTo(other) < 0;
    }

    /**
     * Checks if this version is less than or equal to the other version.
     *
     * @param other the other version to compare to
     *
     * @return {@code true} if this version is less than or equal to the other version or {@code false} otherwise
     *
     * @see #compareTo(Version other)
     */
    @JsIgnore
    public boolean lessThanOrEqualTo(Version other) {
        return compareTo(other) <= 0;
    }
}
