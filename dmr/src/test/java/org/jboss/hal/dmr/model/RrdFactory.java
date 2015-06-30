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
package org.jboss.hal.dmr.model;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.ResourceDescription;

import java.util.List;

import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;

/**
 * @author Harald Pehl
 */
class RrdFactory {

    // /subsystem=mail/mail-session=*:read-resource-description(recursive=true)
    final static String MAIL_SESSIONS =
            "bwAAAAMAB291dGNvbWVzAAdzdWNjZXNzAAZyZXN1bHRsAAAAAW8AAAADAAdhZGRyZXNzbAAAAAJw\n" +
            "AAlzdWJzeXN0ZW1zAARtYWlscAAMbWFpbC1zZXNzaW9ucwABKgAHb3V0Y29tZXMAB3N1Y2Nlc3MA\n" +
            "BnJlc3VsdG8AAAAGAAtkZXNjcmlwdGlvbnMAF01haWwgc2Vzc2lvbiBkZWZpbml0aW9uABJhY2Nl\n" +
            "c3MtY29uc3RyYWludHNvAAAAAQALYXBwbGljYXRpb25vAAAAAQAMbWFpbC1zZXNzaW9ubwAAAAEA\n" +
            "BHR5cGVzAARtYWlsAAphdHRyaWJ1dGVzbwAAAAMABWRlYnVnbwAAAAgABHR5cGV0WgALZGVzY3Jp\n" +
            "cHRpb25zABpFbmFibGVzIEphdmFNYWlsIGRlYnVnZ2luZwATZXhwcmVzc2lvbnMtYWxsb3dlZFoB\n" +
            "AAhuaWxsYWJsZVoBAAdkZWZhdWx0WgAAC2FjY2Vzcy10eXBlcwAKcmVhZC13cml0ZQAHc3RvcmFn\n" +
            "ZXMADWNvbmZpZ3VyYXRpb24AEHJlc3RhcnQtcmVxdWlyZWRzAAxhbGwtc2VydmljZXMABGZyb21v\n" +
            "AAAACQAEdHlwZXRzAAtkZXNjcmlwdGlvbnMAQkZyb20gYWRkcmVzcyB0aGF0IGlzIHVzZWQgYXMg\n" +
            "ZGVmYXVsdCBmcm9tLCBpZiBub3Qgc2V0IHdoZW4gc2VuZGluZwATZXhwcmVzc2lvbnMtYWxsb3dl\n" +
            "ZFoBAAhuaWxsYWJsZVoBAAptaW4tbGVuZ3RoSgAAAAAAAAABAAptYXgtbGVuZ3RoSgAAAAB/////\n" +
            "AAthY2Nlc3MtdHlwZXMACnJlYWQtd3JpdGUAB3N0b3JhZ2VzAA1jb25maWd1cmF0aW9uABByZXN0\n" +
            "YXJ0LXJlcXVpcmVkcwAMYWxsLXNlcnZpY2VzAAlqbmRpLW5hbWVvAAAACQAEdHlwZXRzAAtkZXNj\n" +
            "cmlwdGlvbnMAL0pOREkgbmFtZSB0byB3aGVyZSBtYWlsIHNlc3Npb24gc2hvdWxkIGJlIGJvdW5k\n" +
            "ABNleHByZXNzaW9ucy1hbGxvd2VkWgEACG5pbGxhYmxlWgAACm1pbi1sZW5ndGhKAAAAAAAAAAEA\n" +
            "Cm1heC1sZW5ndGhKAAAAAH////8AC2FjY2Vzcy10eXBlcwAKcmVhZC13cml0ZQAHc3RvcmFnZXMA\n" +
            "DWNvbmZpZ3VyYXRpb24AEHJlc3RhcnQtcmVxdWlyZWRzAAxhbGwtc2VydmljZXMACm9wZXJhdGlv\n" +
            "bnN1AA1ub3RpZmljYXRpb25zdQAIY2hpbGRyZW5vAAAAAgAGY3VzdG9tbwAAAAIAC2Rlc2NyaXB0\n" +
            "aW9ucwAgQ3VzdG9tIG1haWwgc2VydmVyIGNvbmZpZ3VyYXRpb24AEW1vZGVsLWRlc2NyaXB0aW9u\n" +
            "bwAAAAEAASpvAAAABgALZGVzY3JpcHRpb25zABNNYWlsIHNlc3Npb24gc2VydmVyABJhY2Nlc3Mt\n" +
            "Y29uc3RyYWludHNvAAAAAQALYXBwbGljYXRpb25vAAAAAQAMbWFpbC1zZXNzaW9ubwAAAAEABHR5\n" +
            "cGVzAARtYWlsAAphdHRyaWJ1dGVzbwAAAAYAG291dGJvdW5kLXNvY2tldC1iaW5kaW5nLXJlZm8A\n" +
            "AAAKAAR0eXBldHMAC2Rlc2NyaXB0aW9ucwAmT3V0Ym91bmQgU29ja2V0IGJpbmRpbmcgdG8gbWFp\n" +
            "bCBzZXJ2ZXIAE2V4cHJlc3Npb25zLWFsbG93ZWRaAQAIbmlsbGFibGVaAQAKbWluLWxlbmd0aEoA\n" +
            "AAAAAAAAAQAKbWF4LWxlbmd0aEoAAAAAf////wASYWNjZXNzLWNvbnN0cmFpbnRzbwAAAAEACXNl\n" +
            "bnNpdGl2ZW8AAAABABJzb2NrZXQtYmluZGluZy1yZWZvAAAAAQAEdHlwZXMABGNvcmUAC2FjY2Vz\n" +
            "cy10eXBlcwAKcmVhZC13cml0ZQAHc3RvcmFnZXMADWNvbmZpZ3VyYXRpb24AEHJlc3RhcnQtcmVx\n" +
            "dWlyZWRzAAxhbGwtc2VydmljZXMACHBhc3N3b3JkbwAAAAoABHR5cGV0cwALZGVzY3JpcHRpb25z\n" +
            "ACJQYXNzd29yZCB0byBhdXRoZW50aWNhdGUgb24gc2VydmVyABNleHByZXNzaW9ucy1hbGxvd2Vk\n" +
            "WgEACG5pbGxhYmxlWgEACm1pbi1sZW5ndGhKAAAAAAAAAAEACm1heC1sZW5ndGhKAAAAAH////8A\n" +
            "EmFjY2Vzcy1jb25zdHJhaW50c28AAAABAAlzZW5zaXRpdmVvAAAAAgAKY3JlZGVudGlhbG8AAAAB\n" +
            "AAR0eXBlcwAEY29yZQAUbWFpbC1zZXJ2ZXItc2VjdXJpdHlvAAAAAQAEdHlwZXMABG1haWwAC2Fj\n" +
            "Y2Vzcy10eXBlcwAKcmVhZC13cml0ZQAHc3RvcmFnZXMADWNvbmZpZ3VyYXRpb24AEHJlc3RhcnQt\n" +
            "cmVxdWlyZWRzAAxhbGwtc2VydmljZXMACnByb3BlcnRpZXNvAAAACAAEdHlwZXRvAAtkZXNjcmlw\n" +
            "dGlvbnMAE0phdmFNYWlsIHByb3BlcnRpZXMAE2V4cHJlc3Npb25zLWFsbG93ZWRaAQAIbmlsbGFi\n" +
            "bGVaAQAKdmFsdWUtdHlwZXRzAAthY2Nlc3MtdHlwZXMACnJlYWQtd3JpdGUAB3N0b3JhZ2VzAA1j\n" +
            "b25maWd1cmF0aW9uABByZXN0YXJ0LXJlcXVpcmVkcwALbm8tc2VydmljZXMAA3NzbG8AAAAJAAR0\n" +
            "eXBldFoAC2Rlc2NyaXB0aW9ucwAYRG9lcyBzZXJ2ZXIgcmVxdWlyZSBTU0w/ABNleHByZXNzaW9u\n" +
            "cy1hbGxvd2VkWgEACG5pbGxhYmxlWgEAB2RlZmF1bHRaAAASYWNjZXNzLWNvbnN0cmFpbnRzbwAA\n" +
            "AAEACXNlbnNpdGl2ZW8AAAABABRtYWlsLXNlcnZlci1zZWN1cml0eW8AAAABAAR0eXBlcwAEbWFp\n" +
            "bAALYWNjZXNzLXR5cGVzAApyZWFkLXdyaXRlAAdzdG9yYWdlcwANY29uZmlndXJhdGlvbgAQcmVz\n" +
            "dGFydC1yZXF1aXJlZHMADGFsbC1zZXJ2aWNlcwADdGxzbwAAAAkABHR5cGV0WgALZGVzY3JpcHRp\n" +
            "b25zABhEb2VzIHNlcnZlciByZXF1aXJlIFRMUz8AE2V4cHJlc3Npb25zLWFsbG93ZWRaAQAIbmls\n" +
            "bGFibGVaAQAHZGVmYXVsdFoAABJhY2Nlc3MtY29uc3RyYWludHNvAAAAAQAJc2Vuc2l0aXZlbwAA\n" +
            "AAEAFG1haWwtc2VydmVyLXNlY3VyaXR5bwAAAAEABHR5cGVzAARtYWlsAAthY2Nlc3MtdHlwZXMA\n" +
            "CnJlYWQtd3JpdGUAB3N0b3JhZ2VzAA1jb25maWd1cmF0aW9uABByZXN0YXJ0LXJlcXVpcmVkcwAM\n" +
            "YWxsLXNlcnZpY2VzAAh1c2VybmFtZW8AAAAKAAR0eXBldHMAC2Rlc2NyaXB0aW9ucwAiVXNlcm5h\n" +
            "bWUgdG8gYXV0aGVudGljYXRlIG9uIHNlcnZlcgATZXhwcmVzc2lvbnMtYWxsb3dlZFoBAAhuaWxs\n" +
            "YWJsZVoBAAptaW4tbGVuZ3RoSgAAAAAAAAABAAptYXgtbGVuZ3RoSgAAAAB/////ABJhY2Nlc3Mt\n" +
            "Y29uc3RyYWludHNvAAAAAQAJc2Vuc2l0aXZlbwAAAAIACmNyZWRlbnRpYWxvAAAAAQAEdHlwZXMA\n" +
            "BGNvcmUAFG1haWwtc2VydmVyLXNlY3VyaXR5bwAAAAEABHR5cGVzAARtYWlsAAthY2Nlc3MtdHlw\n" +
            "ZXMACnJlYWQtd3JpdGUAB3N0b3JhZ2VzAA1jb25maWd1cmF0aW9uABByZXN0YXJ0LXJlcXVpcmVk\n" +
            "cwAMYWxsLXNlcnZpY2VzAApvcGVyYXRpb25zdQANbm90aWZpY2F0aW9uc3UACGNoaWxkcmVubwAA\n" +
            "AAAABnNlcnZlcm8AAAACAAtkZXNjcmlwdGlvbnMAE01haWwgc2Vzc2lvbiBzZXJ2ZXIAEW1vZGVs\n" +
            "LWRlc2NyaXB0aW9ubwAAAAMABHBvcDNvAAAABgALZGVzY3JpcHRpb25zABNNYWlsIHNlc3Npb24g\n" +
            "c2VydmVyABJhY2Nlc3MtY29uc3RyYWludHNvAAAAAQALYXBwbGljYXRpb25vAAAAAQAMbWFpbC1z\n" +
            "ZXNzaW9ubwAAAAEABHR5cGVzAARtYWlsAAphdHRyaWJ1dGVzbwAAAAUAG291dGJvdW5kLXNvY2tl\n" +
            "dC1iaW5kaW5nLXJlZm8AAAAKAAR0eXBldHMAC2Rlc2NyaXB0aW9ucwAmT3V0Ym91bmQgU29ja2V0\n" +
            "IGJpbmRpbmcgdG8gbWFpbCBzZXJ2ZXIAE2V4cHJlc3Npb25zLWFsbG93ZWRaAQAIbmlsbGFibGVa\n" +
            "AAAKbWluLWxlbmd0aEoAAAAAAAAAAQAKbWF4LWxlbmd0aEoAAAAAf////wASYWNjZXNzLWNvbnN0\n" +
            "cmFpbnRzbwAAAAEACXNlbnNpdGl2ZW8AAAABABJzb2NrZXQtYmluZGluZy1yZWZvAAAAAQAEdHlw\n" +
            "ZXMABGNvcmUAC2FjY2Vzcy10eXBlcwAKcmVhZC13cml0ZQAHc3RvcmFnZXMADWNvbmZpZ3VyYXRp\n" +
            "b24AEHJlc3RhcnQtcmVxdWlyZWRzAAxhbGwtc2VydmljZXMACHBhc3N3b3JkbwAAAAoABHR5cGV0\n" +
            "cwALZGVzY3JpcHRpb25zACJQYXNzd29yZCB0byBhdXRoZW50aWNhdGUgb24gc2VydmVyABNleHBy\n" +
            "ZXNzaW9ucy1hbGxvd2VkWgEACG5pbGxhYmxlWgEACm1pbi1sZW5ndGhKAAAAAAAAAAEACm1heC1s\n" +
            "ZW5ndGhKAAAAAH////8AEmFjY2Vzcy1jb25zdHJhaW50c28AAAABAAlzZW5zaXRpdmVvAAAAAgAK\n" +
            "Y3JlZGVudGlhbG8AAAABAAR0eXBlcwAEY29yZQAUbWFpbC1zZXJ2ZXItc2VjdXJpdHlvAAAAAQAE\n" +
            "dHlwZXMABG1haWwAC2FjY2Vzcy10eXBlcwAKcmVhZC13cml0ZQAHc3RvcmFnZXMADWNvbmZpZ3Vy\n" +
            "YXRpb24AEHJlc3RhcnQtcmVxdWlyZWRzAAxhbGwtc2VydmljZXMAA3NzbG8AAAAJAAR0eXBldFoA\n" +
            "C2Rlc2NyaXB0aW9ucwAYRG9lcyBzZXJ2ZXIgcmVxdWlyZSBTU0w/ABNleHByZXNzaW9ucy1hbGxv\n" +
            "d2VkWgEACG5pbGxhYmxlWgEAB2RlZmF1bHRaAAASYWNjZXNzLWNvbnN0cmFpbnRzbwAAAAEACXNl\n" +
            "bnNpdGl2ZW8AAAABABRtYWlsLXNlcnZlci1zZWN1cml0eW8AAAABAAR0eXBlcwAEbWFpbAALYWNj\n" +
            "ZXNzLXR5cGVzAApyZWFkLXdyaXRlAAdzdG9yYWdlcwANY29uZmlndXJhdGlvbgAQcmVzdGFydC1y\n" +
            "ZXF1aXJlZHMADGFsbC1zZXJ2aWNlcwADdGxzbwAAAAkABHR5cGV0WgALZGVzY3JpcHRpb25zABhE\n" +
            "b2VzIHNlcnZlciByZXF1aXJlIFRMUz8AE2V4cHJlc3Npb25zLWFsbG93ZWRaAQAIbmlsbGFibGVa\n" +
            "AQAHZGVmYXVsdFoAABJhY2Nlc3MtY29uc3RyYWludHNvAAAAAQAJc2Vuc2l0aXZlbwAAAAEAFG1h\n" +
            "aWwtc2VydmVyLXNlY3VyaXR5bwAAAAEABHR5cGVzAARtYWlsAAthY2Nlc3MtdHlwZXMACnJlYWQt\n" +
            "d3JpdGUAB3N0b3JhZ2VzAA1jb25maWd1cmF0aW9uABByZXN0YXJ0LXJlcXVpcmVkcwAMYWxsLXNl\n" +
            "cnZpY2VzAAh1c2VybmFtZW8AAAAKAAR0eXBldHMAC2Rlc2NyaXB0aW9ucwAiVXNlcm5hbWUgdG8g\n" +
            "YXV0aGVudGljYXRlIG9uIHNlcnZlcgATZXhwcmVzc2lvbnMtYWxsb3dlZFoBAAhuaWxsYWJsZVoB\n" +
            "AAptaW4tbGVuZ3RoSgAAAAAAAAABAAptYXgtbGVuZ3RoSgAAAAB/////ABJhY2Nlc3MtY29uc3Ry\n" +
            "YWludHNvAAAAAQAJc2Vuc2l0aXZlbwAAAAIACmNyZWRlbnRpYWxvAAAAAQAEdHlwZXMABGNvcmUA\n" +
            "FG1haWwtc2VydmVyLXNlY3VyaXR5bwAAAAEABHR5cGVzAARtYWlsAAthY2Nlc3MtdHlwZXMACnJl\n" +
            "YWQtd3JpdGUAB3N0b3JhZ2VzAA1jb25maWd1cmF0aW9uABByZXN0YXJ0LXJlcXVpcmVkcwAMYWxs\n" +
            "LXNlcnZpY2VzAApvcGVyYXRpb25zdQANbm90aWZpY2F0aW9uc3UACGNoaWxkcmVubwAAAAAABGlt\n" +
            "YXBvAAAABgALZGVzY3JpcHRpb25zABNNYWlsIHNlc3Npb24gc2VydmVyABJhY2Nlc3MtY29uc3Ry\n" +
            "YWludHNvAAAAAQALYXBwbGljYXRpb25vAAAAAQAMbWFpbC1zZXNzaW9ubwAAAAEABHR5cGVzAARt\n" +
            "YWlsAAphdHRyaWJ1dGVzbwAAAAUAG291dGJvdW5kLXNvY2tldC1iaW5kaW5nLXJlZm8AAAAKAAR0\n" +
            "eXBldHMAC2Rlc2NyaXB0aW9ucwAmT3V0Ym91bmQgU29ja2V0IGJpbmRpbmcgdG8gbWFpbCBzZXJ2\n" +
            "ZXIAE2V4cHJlc3Npb25zLWFsbG93ZWRaAQAIbmlsbGFibGVaAAAKbWluLWxlbmd0aEoAAAAAAAAA\n" +
            "AQAKbWF4LWxlbmd0aEoAAAAAf////wASYWNjZXNzLWNvbnN0cmFpbnRzbwAAAAEACXNlbnNpdGl2\n" +
            "ZW8AAAABABJzb2NrZXQtYmluZGluZy1yZWZvAAAAAQAEdHlwZXMABGNvcmUAC2FjY2Vzcy10eXBl\n" +
            "cwAKcmVhZC13cml0ZQAHc3RvcmFnZXMADWNvbmZpZ3VyYXRpb24AEHJlc3RhcnQtcmVxdWlyZWRz\n" +
            "AAxhbGwtc2VydmljZXMACHBhc3N3b3JkbwAAAAoABHR5cGV0cwALZGVzY3JpcHRpb25zACJQYXNz\n" +
            "d29yZCB0byBhdXRoZW50aWNhdGUgb24gc2VydmVyABNleHByZXNzaW9ucy1hbGxvd2VkWgEACG5p\n" +
            "bGxhYmxlWgEACm1pbi1sZW5ndGhKAAAAAAAAAAEACm1heC1sZW5ndGhKAAAAAH////8AEmFjY2Vz\n" +
            "cy1jb25zdHJhaW50c28AAAABAAlzZW5zaXRpdmVvAAAAAgAKY3JlZGVudGlhbG8AAAABAAR0eXBl\n" +
            "cwAEY29yZQAUbWFpbC1zZXJ2ZXItc2VjdXJpdHlvAAAAAQAEdHlwZXMABG1haWwAC2FjY2Vzcy10\n" +
            "eXBlcwAKcmVhZC13cml0ZQAHc3RvcmFnZXMADWNvbmZpZ3VyYXRpb24AEHJlc3RhcnQtcmVxdWly\n" +
            "ZWRzAAxhbGwtc2VydmljZXMAA3NzbG8AAAAJAAR0eXBldFoAC2Rlc2NyaXB0aW9ucwAYRG9lcyBz\n" +
            "ZXJ2ZXIgcmVxdWlyZSBTU0w/ABNleHByZXNzaW9ucy1hbGxvd2VkWgEACG5pbGxhYmxlWgEAB2Rl\n" +
            "ZmF1bHRaAAASYWNjZXNzLWNvbnN0cmFpbnRzbwAAAAEACXNlbnNpdGl2ZW8AAAABABRtYWlsLXNl\n" +
            "cnZlci1zZWN1cml0eW8AAAABAAR0eXBlcwAEbWFpbAALYWNjZXNzLXR5cGVzAApyZWFkLXdyaXRl\n" +
            "AAdzdG9yYWdlcwANY29uZmlndXJhdGlvbgAQcmVzdGFydC1yZXF1aXJlZHMADGFsbC1zZXJ2aWNl\n" +
            "cwADdGxzbwAAAAkABHR5cGV0WgALZGVzY3JpcHRpb25zABhEb2VzIHNlcnZlciByZXF1aXJlIFRM\n" +
            "Uz8AE2V4cHJlc3Npb25zLWFsbG93ZWRaAQAIbmlsbGFibGVaAQAHZGVmYXVsdFoAABJhY2Nlc3Mt\n" +
            "Y29uc3RyYWludHNvAAAAAQAJc2Vuc2l0aXZlbwAAAAEAFG1haWwtc2VydmVyLXNlY3VyaXR5bwAA\n" +
            "AAEABHR5cGVzAARtYWlsAAthY2Nlc3MtdHlwZXMACnJlYWQtd3JpdGUAB3N0b3JhZ2VzAA1jb25m\n" +
            "aWd1cmF0aW9uABByZXN0YXJ0LXJlcXVpcmVkcwAMYWxsLXNlcnZpY2VzAAh1c2VybmFtZW8AAAAK\n" +
            "AAR0eXBldHMAC2Rlc2NyaXB0aW9ucwAiVXNlcm5hbWUgdG8gYXV0aGVudGljYXRlIG9uIHNlcnZl\n" +
            "cgATZXhwcmVzc2lvbnMtYWxsb3dlZFoBAAhuaWxsYWJsZVoBAAptaW4tbGVuZ3RoSgAAAAAAAAAB\n" +
            "AAptYXgtbGVuZ3RoSgAAAAB/////ABJhY2Nlc3MtY29uc3RyYWludHNvAAAAAQAJc2Vuc2l0aXZl\n" +
            "bwAAAAIACmNyZWRlbnRpYWxvAAAAAQAEdHlwZXMABGNvcmUAFG1haWwtc2VydmVyLXNlY3VyaXR5\n" +
            "bwAAAAEABHR5cGVzAARtYWlsAAthY2Nlc3MtdHlwZXMACnJlYWQtd3JpdGUAB3N0b3JhZ2VzAA1j\n" +
            "b25maWd1cmF0aW9uABByZXN0YXJ0LXJlcXVpcmVkcwAMYWxsLXNlcnZpY2VzAApvcGVyYXRpb25z\n" +
            "dQANbm90aWZpY2F0aW9uc3UACGNoaWxkcmVubwAAAAAABHNtdHBvAAAABgALZGVzY3JpcHRpb25z\n" +
            "ABNNYWlsIHNlc3Npb24gc2VydmVyABJhY2Nlc3MtY29uc3RyYWludHNvAAAAAQALYXBwbGljYXRp\n" +
            "b25vAAAAAQAMbWFpbC1zZXNzaW9ubwAAAAEABHR5cGVzAARtYWlsAAphdHRyaWJ1dGVzbwAAAAUA\n" +
            "G291dGJvdW5kLXNvY2tldC1iaW5kaW5nLXJlZm8AAAAKAAR0eXBldHMAC2Rlc2NyaXB0aW9ucwAm\n" +
            "T3V0Ym91bmQgU29ja2V0IGJpbmRpbmcgdG8gbWFpbCBzZXJ2ZXIAE2V4cHJlc3Npb25zLWFsbG93\n" +
            "ZWRaAQAIbmlsbGFibGVaAAAKbWluLWxlbmd0aEoAAAAAAAAAAQAKbWF4LWxlbmd0aEoAAAAAf///\n" +
            "/wASYWNjZXNzLWNvbnN0cmFpbnRzbwAAAAEACXNlbnNpdGl2ZW8AAAABABJzb2NrZXQtYmluZGlu\n" +
            "Zy1yZWZvAAAAAQAEdHlwZXMABGNvcmUAC2FjY2Vzcy10eXBlcwAKcmVhZC13cml0ZQAHc3RvcmFn\n" +
            "ZXMADWNvbmZpZ3VyYXRpb24AEHJlc3RhcnQtcmVxdWlyZWRzAAxhbGwtc2VydmljZXMACHBhc3N3\n" +
            "b3JkbwAAAAoABHR5cGV0cwALZGVzY3JpcHRpb25zACJQYXNzd29yZCB0byBhdXRoZW50aWNhdGUg\n" +
            "b24gc2VydmVyABNleHByZXNzaW9ucy1hbGxvd2VkWgEACG5pbGxhYmxlWgEACm1pbi1sZW5ndGhK\n" +
            "AAAAAAAAAAEACm1heC1sZW5ndGhKAAAAAH////8AEmFjY2Vzcy1jb25zdHJhaW50c28AAAABAAlz\n" +
            "ZW5zaXRpdmVvAAAAAgAKY3JlZGVudGlhbG8AAAABAAR0eXBlcwAEY29yZQAUbWFpbC1zZXJ2ZXIt\n" +
            "c2VjdXJpdHlvAAAAAQAEdHlwZXMABG1haWwAC2FjY2Vzcy10eXBlcwAKcmVhZC13cml0ZQAHc3Rv\n" +
            "cmFnZXMADWNvbmZpZ3VyYXRpb24AEHJlc3RhcnQtcmVxdWlyZWRzAAxhbGwtc2VydmljZXMAA3Nz\n" +
            "bG8AAAAJAAR0eXBldFoAC2Rlc2NyaXB0aW9ucwAYRG9lcyBzZXJ2ZXIgcmVxdWlyZSBTU0w/ABNl\n" +
            "eHByZXNzaW9ucy1hbGxvd2VkWgEACG5pbGxhYmxlWgEAB2RlZmF1bHRaAAASYWNjZXNzLWNvbnN0\n" +
            "cmFpbnRzbwAAAAEACXNlbnNpdGl2ZW8AAAABABRtYWlsLXNlcnZlci1zZWN1cml0eW8AAAABAAR0\n" +
            "eXBlcwAEbWFpbAALYWNjZXNzLXR5cGVzAApyZWFkLXdyaXRlAAdzdG9yYWdlcwANY29uZmlndXJh\n" +
            "dGlvbgAQcmVzdGFydC1yZXF1aXJlZHMADGFsbC1zZXJ2aWNlcwADdGxzbwAAAAkABHR5cGV0WgAL\n" +
            "ZGVzY3JpcHRpb25zABhEb2VzIHNlcnZlciByZXF1aXJlIFRMUz8AE2V4cHJlc3Npb25zLWFsbG93\n" +
            "ZWRaAQAIbmlsbGFibGVaAQAHZGVmYXVsdFoAABJhY2Nlc3MtY29uc3RyYWludHNvAAAAAQAJc2Vu\n" +
            "c2l0aXZlbwAAAAEAFG1haWwtc2VydmVyLXNlY3VyaXR5bwAAAAEABHR5cGVzAARtYWlsAAthY2Nl\n" +
            "c3MtdHlwZXMACnJlYWQtd3JpdGUAB3N0b3JhZ2VzAA1jb25maWd1cmF0aW9uABByZXN0YXJ0LXJl\n" +
            "cXVpcmVkcwAMYWxsLXNlcnZpY2VzAAh1c2VybmFtZW8AAAAKAAR0eXBldHMAC2Rlc2NyaXB0aW9u\n" +
            "cwAiVXNlcm5hbWUgdG8gYXV0aGVudGljYXRlIG9uIHNlcnZlcgATZXhwcmVzc2lvbnMtYWxsb3dl\n" +
            "ZFoBAAhuaWxsYWJsZVoBAAptaW4tbGVuZ3RoSgAAAAAAAAABAAptYXgtbGVuZ3RoSgAAAAB/////\n" +
            "ABJhY2Nlc3MtY29uc3RyYWludHNvAAAAAQAJc2Vuc2l0aXZlbwAAAAIACmNyZWRlbnRpYWxvAAAA\n" +
            "AQAEdHlwZXMABGNvcmUAFG1haWwtc2VydmVyLXNlY3VyaXR5bwAAAAEABHR5cGVzAARtYWlsAAth\n" +
            "Y2Nlc3MtdHlwZXMACnJlYWQtd3JpdGUAB3N0b3JhZ2VzAA1jb25maWd1cmF0aW9uABByZXN0YXJ0\n" +
            "LXJlcXVpcmVkcwAMYWxsLXNlcnZpY2VzAApvcGVyYXRpb25zdQANbm90aWZpY2F0aW9uc3UACGNo\n" +
            "aWxkcmVubwAAAAAAEHJlc3BvbnNlLWhlYWRlcnNvAAAAAQANcHJvY2Vzcy1zdGF0ZXMAD3JlbG9h\n" +
            "ZC1yZXF1aXJlZA==";

    static ResourceDescription mailSessions() {
        ModelNode node = ModelNode.fromBase64(MAIL_SESSIONS);
        List<ModelNode> nodes = node.get(RESULT).asList();
        return new ResourceDescription((nodes.get(0).get(RESULT)));
    }
}
