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
package org.jboss.hal.core.mvp;

import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.ViewImpl;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.gwt.elemento.core.IsElement;

/** Adapter between GWTPs views which are based on widgets and HAL views which are based on elements. */
public interface HalView extends View, IsElement, HasElements {

    /**
     * This method should be called <em>after</em> the view's elements are attached to the DOM. Typically this method
     * is called from {@link HalPresenter#onReveal()}.
     * <p>
     * Do <em>not</em> use {@link ViewImpl#onAttach()} to initialize PatternFly components. This works for widgets
     * only, but not for elements!
     */
    void attach();

    /**
     * Counterpart to {@link #attach()}. Implement this method if you need to remove stuff which was setup in {@link
     * #attach()}. The default implementation does nothing.
     */
    default void detach() {
    }
}
