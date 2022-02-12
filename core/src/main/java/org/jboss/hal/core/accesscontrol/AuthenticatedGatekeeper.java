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
package org.jboss.hal.core.accesscontrol;

import javax.inject.Inject;

import org.jboss.hal.config.User;

import com.gwtplatform.mvp.client.annotations.DefaultGatekeeper;
import com.gwtplatform.mvp.client.proxy.Gatekeeper;

/** Default gatekeeper which makes sure the current user is {@linkplain User#isAuthenticated() authenticated}. */
@DefaultGatekeeper
public class AuthenticatedGatekeeper implements Gatekeeper {

    private final User user;

    @Inject
    public AuthenticatedGatekeeper(User user) {
        this.user = user;
    }

    @Override
    public boolean canReveal() {
        return user.isAuthenticated();
    }
}
