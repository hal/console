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
package org.jboss.hal.processor;

import com.google.auto.service.AutoService;
import org.jboss.auto.AbstractProcessor;
import org.jboss.gwt.circuit.meta.Store;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.jboss.hal.processor.TemplateNames.*;

/**
 * Processor which automates initialization of stores and their adapters. The processor generates the following
 * artifacts:
 * <ol>
 * <li>GIN bindings which binds each store and its adapter to scope singleton</li>
 * </ol>
 * <p/>
 * The processor makes some assumptions about the stores and their adapters:
 * <ul>
 * <li>They belong to the same package</li>
 * <li>The adapter's name follows the pattern {@code &lt;StoreName&gt;Adapter}</li>
 * </ul>
 *
 * @author Harald Pehl
 */
@AutoService(Processor.class)
@SuppressWarnings("HardCodedStringLiteral")
@SupportedAnnotationTypes("org.jboss.gwt.circuit.meta.Store")
public class StoreInitProcessor extends AbstractProcessor {

    private static final String ADAPTER_SUFFIX = "Adapter";

    private static final String STORE_MODULE_TEMPLATE = "StoreModule.ftl";
    private static final String STORE_MODULE_PACKAGE = GIN_PACKAGE;
    private static final String STORE_MODULE_CLASS = "StoreModule";

    private final Set<StoreInfo> storeInfos;

    public StoreInitProcessor() {
        super(StoreInitProcessor.class, TemplateNames.TEMPLATES);
        storeInfos = new HashSet<>();
    }

    @Override
    protected boolean onProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element e : roundEnv.getElementsAnnotatedWith(Store.class)) {
            TypeElement storeElement = (TypeElement) e;
            PackageElement packageElement = (PackageElement) storeElement.getEnclosingElement();
            String packageName = packageElement.getQualifiedName().toString();
            String storeDelegate = storeElement.getSimpleName().toString();
            String storeAdapter = storeDelegate + ADAPTER_SUFFIX;
            StoreInfo storeInfo = new StoreInfo(packageName, storeDelegate, storeAdapter);
            storeInfos.add(storeInfo);
            debug("Discovered store / store adapter [%s]", storeInfo);
        }

        // Don't generate files in onLastRound, since the generated GIN module
        // needs to be picked up by the GinModuleProcessor!
        if (!storeInfos.isEmpty()) {
            debug("Generating code for store module");
            code(STORE_MODULE_TEMPLATE, STORE_MODULE_PACKAGE, STORE_MODULE_CLASS,
                    () -> {
                        Map<String, Object> context = new HashMap<>();
                        context.put(GENERATED_WITH, StoreInitProcessor.class.getName());
                        context.put(PACKAGE_NAME, STORE_MODULE_PACKAGE);
                        context.put(CLASS_NAME, STORE_MODULE_CLASS);
                        context.put("storeInfos", storeInfos);
                        return context;
                    });

            info("Successfully generated store initialization class [%s].", STORE_MODULE_CLASS);
            storeInfos.clear();
        }
        return false;
    }


    public static final class StoreInfo {
        private final String packageName;
        private final String storeDelegate;
        private final String storeAdapter;

        StoreInfo(String packageName, String storeDelegate, String storeAdapter) {
            this.packageName = packageName;
            this.storeDelegate = storeDelegate;
            this.storeAdapter = storeAdapter;
        }

        @Override
        @SuppressWarnings("SimplifiableIfStatement")
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof StoreInfo)) return false;

            StoreInfo storeInfo = (StoreInfo) o;

            if (!packageName.equals(storeInfo.packageName)) return false;
            if (!storeDelegate.equals(storeInfo.storeDelegate)) return false;
            return storeAdapter.equals(storeInfo.storeAdapter);

        }

        @Override
        public int hashCode() {
            int result = packageName.hashCode();
            result = 31 * result + storeDelegate.hashCode();
            result = 31 * result + storeAdapter.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return storeDelegate + " / " + storeAdapter;
        }

        public String getPackageName() {
            return packageName;
        }

        public String getStoreAdapter() {
            return storeAdapter;
        }

        public String getStoreDelegate() {
            return storeDelegate;
        }
    }
}
