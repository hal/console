/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
module.exports = function (grunt) {
    'use strict';

    // load all grunt tasks
    require('matchdep').filterDev('grunt-*').forEach(grunt.loadNpmTasks);

    // Project configuration.
    grunt.initConfig({
        theme: grunt.option('theme') || 'hal',
        config: {
            devmodeTarget: 'target/gwt/devmode/war/hal',
            esdoc: {
                source: 'target/generated-resources/esdoc',
                destination: 'target/esdoc',
                input: 'src/esdoc'
            },
            js: 'src/js',
            less: 'src/less',
            node: 'node_modules',
            public: 'src/main/resources/org/jboss/hal/public',
            themeDir: '../themes/<%= theme %>/src/main/resources/org/jboss/hal/theme/<%= theme %>',
            version: '3.2.10',
        },

        clean: {
            public: [
                '<%= config.public %>/css/**',
                '<%= config.public %>/fonts/**',
                '<%= config.public %>/img/**',
                '<%= config.public %>/js/**'
            ]
        },

        copy: {
            resources: {
                files: [
                    {
                        expand: true,
                        cwd: '<%= config.node %>/ace-builds/src-min-noconflict',
                        src: ['mode-*.js', 'theme-*.js', 'worker-*.js'],
                        dest: '<%= config.public %>/js'
                    },
                    {
                        expand: true,
                        cwd: '<%= config.node %>/jstree/dist/themes/default',
                        src: ['*.gif', '*.png'],
                        dest: '<%= config.public %>/img'
                    },
                    {
                        expand: true,
                        cwd: '<%= config.node %>/patternfly/dist/fonts',
                        src: '*',
                        dest: '<%= config.public %>/fonts'
                    },
                    {
                        expand: true,
                        cwd: '<%= config.node %>/pouchdb/dist',
                        src: ['pouchdb.js', 'pouchdb.min.js'],
                        dest: '<%= config.public %>/js'
                    },
                    {
                        expand: true,
                        cwd: '<%= config.node %>/zeroclipboard/dist',
                        src: 'ZeroClipboard.swf',
                        dest: '<%= config.public %>/js'
                    },
                    {
                        expand: true,
                        cwd: '<%= config.js %>',
                        src: '*.js',
                        dest: '<%= config.public %>/js'
                    },
                    {
                        expand: true,
                        cwd: '<%= config.themeDir %>',
                        src: ['apple-touch-icon.png', 'favicon.ico'],
                        dest: '<%= config.public %>'
                    }
                ]
            },
            css: {
                files: [
                    {
                        expand: true,
                        cwd: '<%= config.public %>/css',
                        src: 'hal.css',
                        dest: '<%= config.devmodeTarget %>/css'
                    }
                ]
            }
        },

        concat: {
            polyfill: {
                options: {
                    banner: '/*!\n' +
                        ' * Polyfill JS files for IE 11 and below. HAL <%= config.version %>\n' +
                        ' * Build date: <%= grunt.template.today("yyyy-mm-dd HH:MM:ss") %>\n' +
                        ' */\n\n',
                    stripBanners: true
                },
                nonull: true,
                filter: function (filepath) {
                    if (!grunt.file.exists(filepath)) {
                        grunt.fail.fatal('Grunt error. Could not find: ' + filepath);
                    } else {
                        return true;
                    }
                },
                src: [
                    '<%= config.node %>/promise-polyfill/dist/polyfill.min.js',
                    '<%= config.node %>/whatwg-fetch/dist/fetch.umd.js'
                ],
                dest: '<%= config.public %>/js/polyfill.min.js'
            },
            externalDev: {
                /*
                 * Order
                 * 1) jQuery
                 * 2) Bootstrap + Components
                 * 3) C3 / D3
                 * 4) Datatables
                 * 5) Other JS libs (in no specific order)
                 * 6) PatternFly
                 */
                src: [
                    '<%= config.node %>/jquery/dist/jquery.js',
                    '<%= config.node %>/bootstrap/dist/js/bootstrap.js',
                    '<%= config.node %>/bootstrap-select/dist/js/bootstrap-select.js',
                    '<%= config.node %>/bootstrap-switch/dist/js/bootstrap-switch.js',
                    '<%= config.node %>/c3/c3.js',
                    '<%= config.node %>/d3/dist//d3.js',
                    '<%= config.node %>/datatables.net/js/jquery.dataTables.js',
                    '<%= config.node %>/datatables.net-buttons/js/dataTables.buttons.js',
                    '<%= config.node %>/datatables.net-keytable/js/dataTables.keyTable.js',
                    '<%= config.node %>/datatables.net-select/js/dataTables.select.js',
                    '<%= config.node %>/ace-builds/src-noconflict/ace.js',
                    '<%= config.node %>/ace-builds/src-noconflict/ext-modelist.js',
                    '<%= config.node %>/google-code-prettify/src/prettify.js',
                    '<%= config.js %>/auto-complete.js',
                    '<%= config.node %>/js-cookie/src/js.cookie.js',
                    '<%= config.node %>/jstree/dist/jstree.js',
                    '<%= config.node %>/pouchdb/dist/pouchdb.js',
                    '<%= config.js %>/tagmanager.js',
                    '<%= config.node %>/zeroclipboard/dist/ZeroClipboard.js',
                    '<%= config.node %>/patternfly/dist/js/patternfly.js'
                ],
                dest: '<%= config.public %>/js/external.js'
            },
            externalProd: {
                options: {
                    banner: '/*!\n' +
                        ' * External JS files for HAL <%= config.version %>\n' +
                        ' * Build date: <%= grunt.template.today("yyyy-mm-dd HH:MM:ss") %>\n' +
                        ' */\n\n',
                    stripBanners: true
                },
                nonull: true,
                filter: function (filepath) {
                    if (!grunt.file.exists(filepath)) {
                        grunt.fail.fatal('Grunt error. Could not find: ' + filepath);
                    } else {
                        return true;
                    }
                },
                src: [
                    '<%= config.node %>/jquery/dist/jquery.min.js',
                    '<%= config.node %>/bootstrap/dist/js/bootstrap.min.js',
                    '<%= config.node %>/bootstrap-select/dist/js/bootstrap-select.min.js',
                    '<%= config.node %>/bootstrap-switch/dist/js/bootstrap-switch.min.js',
                    '<%= config.node %>/c3/c3.min.js',
                    '<%= config.node %>/d3/dist/d3.min.js',
                    '<%= config.node %>/datatables.net/js/jquery.dataTables.js',
                    '<%= config.node %>/datatables.net-buttons/js/dataTables.buttons.min.js',
                    '<%= config.node %>/datatables.net-keytable/js/dataTables.keyTable.min.js',
                    '<%= config.node %>/datatables.net-select/js/dataTables.select.min.js',
                    '<%= config.node %>/ace-builds/src-min-noconflict/ace.js',
                    '<%= config.node %>/ace-builds/src-min-noconflict/ext-modelist.js',
                    '<%= config.node %>/google-code-prettify/bin/prettify.min.js',
                    '<%= config.js %>/auto-complete.min.js',
                    '<%= config.node %>/js-cookie/src/js.cookie.js',
                    '<%= config.node %>/jstree/dist/jstree.min.js',
                    '<%= config.node %>/pouchdb/dist/pouchdb.min.js',
                    '<%= config.js %>/tagmanager.js',
                    '<%= config.node %>/zeroclipboard/dist/ZeroClipboard.min.js',
                    '<%= config.node %>/patternfly/dist/js/patternfly.min.js'
                ],
                dest: '<%= config.public %>/js/external.min.js'
            }
        },

        less: {
            target: {
                options: {
                    banner: '/*\n' +
                        ' * Generated CSS file for HAL <%= config.version %>\n' +
                        ' * Build date: <%= grunt.template.today("yyyy-mm-dd HH:MM:ss") %>\n' +
                        ' */\n\n',
                    paths: ['<%= config.less %>', '../themes/<%= theme %>/src/main/less'],
                    strictMath: true
                },
                src: '<%= config.less %>/hal.less',
                dest: '<%= config.public %>/css/hal.css'
            }
        },

        postcss: {
            target: {
                options: {
                    processors: [
                        require('pixrem')(),
                        require('autoprefixer')({browsers: ['last 3 versions', 'ie 9']})
                    ]
                },
                files: [{
                    expand: true,
                    cwd: '<%= config.public %>/css',
                    src: 'hal.css',
                    dest: '<%= config.public %>/css'
                }]
            }
        },

        cssmin: {
            target: {
                files: [{
                    expand: true,
                    cwd: '<%= config.public %>/css',
                    src: ['*.css', '!*.min.css'],
                    dest: '<%= config.public %>/css',
                    ext: '.min.css'
                }]
            }
        },

        watch: {
            less: {
                files: ['<%= config.less %>/*.less'],
                tasks: ['less']
            }
        },

        esdoc: {
            dist: {
                options: {
                    source: '<%= config.esdoc.source %>',
                    excludes: [],
                    destination: '<%= config.esdoc.destination %>',
                    access: ['public'],
                    index: '<%= config.esdoc.input %>/README.md',
                    title: 'HAL JavaScript API',
                    styles: ['<%= config.esdoc.input %>/style.css'],
                    manual: {
                        example: ['<%= config.esdoc.input %>/manual/example.md']
                    },
                    coverage: false,
                    unexportIdentifier: true,
                    undocumentIdentifier: true,
                    experimentalProposal: {
                        "classProperties": true,
                        "objectRestSpread": true
                    }
                }
            }
        }
    });

    grunt.registerTask('css', [
        'less',
        'postcss',
        'copy:css'
    ]);

    grunt.registerTask('dev', [
        'clean',
        'copy:resources',
        'concat:polyfill',
        'concat:externalDev',
        'less',
        'postcss'
    ]);

    grunt.registerTask('prod', [
        'clean',
        'copy:resources',
        'concat:polyfill',
        'concat:externalProd',
        'less',
        'postcss',
        'cssmin'
    ]);

    grunt.registerTask('default', ['dev']);
};
