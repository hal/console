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