// noinspection JSValidateTypes

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

// Keep the order (unless you know what you're doing)

// JQuery
// https://stackoverflow.com/a/47984928
const jquery = require("jquery");
window.$ = window.jQuery = jquery;
window.Cookies = require("js-cookie");

// Bootstrap
require("bootstrap");
require("bootstrap-select");
require("bootstrap-switch");

// Datatables
// https://datatables.net/download/npm
require("datatables.net")(window, $);
require("datatables.net-select")(window, $);
require("datatables.net-buttons")(window, $);
require("datatables.net-keytable")(window, $);

// Ace Editor
require("ace-builds");
require("ace-builds/src-noconflict/ext-modelist");
require("ace-builds/src-noconflict/mode-css");
require("ace-builds/src-noconflict/mode-html");
require("ace-builds/src-noconflict/mode-java");
require("ace-builds/src-noconflict/mode-javascript");
require("ace-builds/src-noconflict/mode-json");
require("ace-builds/src-noconflict/mode-json5");
require("ace-builds/src-noconflict/mode-jsp");
require("ace-builds/src-noconflict/mode-php");
require("ace-builds/src-noconflict/mode-properties");
require("ace-builds/src-noconflict/mode-sh");
require("ace-builds/src-noconflict/mode-sql");
require("ace-builds/src-noconflict/mode-svg");
require("ace-builds/src-noconflict/mode-text");
require("ace-builds/src-noconflict/mode-toml");
require("ace-builds/src-noconflict/mode-typescript");
require("ace-builds/src-noconflict/mode-xml");
require("ace-builds/src-noconflict/mode-yaml");
require("./mode-logfile");
require("./theme-logfile");

// Misc (in no particular order)
require("jstree");
require("google-code-prettify/src/prettify");
require("patternfly/dist/js/patternfly");
require("./autocomplete");
require("./tagmanager");
window.c3 = require("c3");
window.d3 = require("d3");
window.PouchDB = require("pouchdb-browser").default;
window.ClipboardJS = require("clipboard");

// TODO Web worker
window.metadataChannel = new Worker(new URL("./worker.js", import.meta.url), {type: "module"});

window.keycloakReady = import("keycloak-js/lib/keycloak").then(
  (module) => (window.KeycloakInstance = module.default)
);