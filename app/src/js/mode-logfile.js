/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
ace.define("ace/mode/logfile_highlight_rules", ["require", "exports", "module", "ace/lib/oop", "ace/mode/text_highlight_rules"], function (require, exports, module) {
    "use strict";

    var oop = require("../lib/oop");
    var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;

    var LogFileHighlightRules = function () {

        this.$rules = {
            "start": [
                // timestamp
                {
                    token: "timestamp",
                    regex: "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3}"
                },
                // category
                {
                    token: "category",
                    regex: "\\[[a-z\\.]+\\]"
                },
                // levels
                {
                    token: "level.error",
                    regex: "ERROR"
                },
                {
                    token: "level.warn",
                    regex: "WARN"
                },
                {
                    token: "level.info",
                    regex: "INFO"
                },
                {
                    token: "level.debug",
                    regex: "DEBUG"
                },
                // exceptions
                {
                    token: "exception",
                    regex: "\\w*Exception"
                },
                {
                    token: "exception",
                    regex: "\\w*Error"
                }
            ]
        };

    };

    oop.inherits(LogFileHighlightRules, TextHighlightRules);
    exports.LogFileHighlightRules = LogFileHighlightRules;
});


ace.define("ace/mode/logfile", ["require", "exports", "module", "ace/lib/oop", "ace/mode/text", "ace/mode/properties_highlight_rules"], function (require, exports, module) {
    "use strict";

    var oop = require("../lib/oop");
    var TextMode = require("./text").Mode;
    var LogFileHighlightRules = require("./logfile_highlight_rules").LogFileHighlightRules;

    var Mode = function () {
        this.HighlightRules = LogFileHighlightRules;
    };
    oop.inherits(Mode, TextMode);

    (function () {
        this.$id = "ace/mode/logfile";
    }).call(Mode.prototype);

    exports.Mode = Mode;
});
