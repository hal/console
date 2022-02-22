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
const path = require("path");
const {Resolver} = require("@parcel/plugin");

/*
 * Resolves theme dependencies starting with 'theme:'. The current theme is taken from the
 * environment variable `THEME` with a fallback of "hal" if not defined.
 *
 * The resolver can handle relative and absolute dependencies:
 *
 * 1. Relative dependencies:
 *    The dependency is expected to be in `/themes/<current-theme>/<relative-to-importing-file>`
 *
 * 2. Absolute dependencies:
 *    The dependency is expected to be in `/themes/<current-theme>/<absolute-path>`
 */
module.exports = new Resolver({
    async resolve({specifier, dependency, logger}) {
        if (specifier.startsWith("theme:")) {
            let currentTheme = process.env.THEME || "hal";
            let themeDependency = specifier.substring("theme:".length);
            if (themeDependency.startsWith("/")) {
                themeDependency = themeDependency.substring(1);
                let filePath = path.join(process.cwd(), "../themes", currentTheme, themeDependency);
                logger.info({message: `Resolve ${specifier} to ${filePath}`})
                return {
                    filePath: filePath
                };
            } else {
                let themePath = path.join(process.cwd(), "../themes", currentTheme);
                let resolveFrom = path.dirname(dependency.resolveFrom);
                let relativePart = resolveFrom.substring(process.cwd().length + 1); // to remove the leading '/'
                let filePath = path.resolve(themePath, relativePart, themeDependency);
                logger.info({message: `Resolve ${specifier} to ${filePath}`})
                return {
                    filePath: filePath
                };
            }
        }
        return null;
    }
});
