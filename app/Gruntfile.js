/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
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
        pkg: grunt.file.readJSON('../npm/package.json'),
        config: {
            bower: 'bower_components',
            less: 'src/main/less',
            public: 'src/main/resources/org/jboss/hal/public'
        },

        clean: {
            public: [
                '<%= config.public %>/css/**',
                '<%= config.public %>/fonts/**',
                '<%= config.public %>/img/**',
                '<%= config.public %>/js/*.js',
                '<%= config.public %>/js/*.swf',
                '!<%= config.public %>/js/mode-logfile.js',
                '!<%= config.public %>/js/theme-logfile.js',
                '!<%= config.public %>/img/icon-*.png'
            ]
        },

        copy: {
            resources: {
                files: [
                    {
                        expand: true,
                        cwd: '<%= config.bower %>/patternfly/dist/fonts',
                        src: '*',
                        dest: '<%= config.public %>/fonts'
                    },
                    {
                        expand: true,
                        cwd: '<%= config.bower %>/patternfly/dist/img',
                        src: '*',
                        dest: '<%= config.public %>/img'
                    },
                    {
                        expand: true,
                        cwd: '<%= config.bower %>/jstree/dist/themes/default',
                        src: ['*.gif', '*.png'],
                        dest: '<%= config.public %>/img'
                    },
                    {
                        expand: true,
                        cwd: '<%= config.bower %>/zeroclipboard/dist',
                        src: 'ZeroClipboard.swf',
                        dest: '<%= config.public %>/js'
                    }
                ]
            }
        },

        concat: {
            /*
             * Order
             * 1) jQuery
             * 2) Bootstrap + Components
             * 3) C3 / D3
             * 4) Datatables
             * 5) Other JS libs (in no specific order)
             * 6) PatternFly
             */
            dev: {
                src: [
                    '<%= config.bower %>/jquery/dist/jquery.js',
                    '<%= config.bower %>/bootstrap/dist/js/bootstrap.js',
                    '<%= config.bower %>/bootstrap-select/dist/js/bootstrap-select.js',
                    '<%= config.bower %>/bootstrap-switch/dist/js/bootstrap-switch.js',
                    '<%= config.bower %>/c3/c3.js',
                    '<%= config.bower %>/d3/d3.js',
                    '<%= config.bower %>/datatables.net/js/jquery.dataTables.js',
                    '<%= config.bower %>/datatables.net-buttons/js/dataTables.buttons.js',
                    '<%= config.bower %>/datatables.net-keytable/js/dataTables.keyTable.js',
                    '<%= config.bower %>/datatables.net-select/js/dataTables.select.js',
                    '<%= config.bower %>/ace-builds/src-noconflict/ace.js',
                    '<%= config.bower %>/ace-builds/src-noconflict/ext-modelist.js',
                    '<%= config.bower %>/ace-builds/src-noconflict/mode-css.js',
                    '<%= config.bower %>/ace-builds/src-noconflict/mode-html.js',
                    '<%= config.bower %>/ace-builds/src-noconflict/mode-java.js',
                    '<%= config.bower %>/ace-builds/src-noconflict/mode-javascript.js',
                    '<%= config.bower %>/ace-builds/src-noconflict/mode-json.js',
                    '<%= config.bower %>/ace-builds/src-noconflict/mode-jsp.js',
                    '<%= config.bower %>/ace-builds/src-noconflict/mode-less.js',
                    '<%= config.bower %>/ace-builds/src-noconflict/mode-markdown.js',
                    '<%= config.bower %>/ace-builds/src-noconflict/mode-php.js',
                    '<%= config.bower %>/ace-builds/src-noconflict/mode-properties.js',
                    '<%= config.bower %>/ace-builds/src-noconflict/mode-python.js',
                    '<%= config.bower %>/ace-builds/src-noconflict/mode-ruby.js',
                    '<%= config.bower %>/ace-builds/src-noconflict/mode-sh.js',
                    '<%= config.bower %>/ace-builds/src-noconflict/mode-sql.js',
                    '<%= config.bower %>/ace-builds/src-noconflict/mode-text.js',
                    '<%= config.bower %>/ace-builds/src-noconflict/mode-typescript.js',
                    '<%= config.bower %>/ace-builds/src-noconflict/mode-xml.js',
                    '<%= config.bower %>/jstree/dist/jstree.js',
                    '<%= config.bower %>/tagmanager/tagmanager.js',
                    '<%= config.bower %>/typeahead.js/dist/typeahead.bundle.js',
                    '<%= config.bower %>/zeroclipboard/dist/ZeroClipboard.js',
                    '<%= config.bower %>/patternfly/dist/js/patternfly.js'
                ],
                dest: '<%= config.public %>/js/external.js'
            },
            prod: {
                options: {
                    banner: '/*!\n' +
                    ' * External JS files for <%= pkg.name %> <%= pkg.version %>\n' +
                    ' * Build date: <%= grunt.template.today("yyyy-mm-dd HH:MM:ss") %>\n' +
                    ' */\n\n',
                    stripBanners: true
                },
                src: [
                    '<%= config.bower %>/jquery/dist/jquery.min.js',
                    '<%= config.bower %>/bootstrap/dist/js/bootstrap.min.js',
                    '<%= config.bower %>/bootstrap-select/dist/js/bootstrap-select.min.js',
                    '<%= config.bower %>/bootstrap-switch/dist/js/bootstrap-switch.min.js',
                    '<%= config.bower %>/c3/c3.min.js',
                    '<%= config.bower %>/d3/d3.min.js',
                    '<%= config.bower %>/datatables.net/js/jquery.dataTables.min.js',
                    '<%= config.bower %>/datatables.net-buttons/js/dataTables.buttons.min.js',
                    '<%= config.bower %>/datatables.net-keytable/js/dataTables.keyTable.min.js',
                    '<%= config.bower %>/datatables.net-select/js/dataTables.select.min.js',
                    '<%= config.bower %>/ace-builds/src-min-noconflict/ace.js',
                    '<%= config.bower %>/ace-builds/src-min-noconflict/ext-modelist.js',
                    '<%= config.bower %>/ace-builds/src-min-noconflict/mode-css.js',
                    '<%= config.bower %>/ace-builds/src-min-noconflict/mode-html.js',
                    '<%= config.bower %>/ace-builds/src-min-noconflict/mode-java.js',
                    '<%= config.bower %>/ace-builds/src-min-noconflict/mode-javascript.js',
                    '<%= config.bower %>/ace-builds/src-min-noconflict/mode-json.js',
                    '<%= config.bower %>/ace-builds/src-min-noconflict/mode-jsp.js',
                    '<%= config.bower %>/ace-builds/src-min-noconflict/mode-less.js',
                    '<%= config.bower %>/ace-builds/src-min-noconflict/mode-markdown.js',
                    '<%= config.bower %>/ace-builds/src-min-noconflict/mode-php.js',
                    '<%= config.bower %>/ace-builds/src-min-noconflict/mode-properties.js',
                    '<%= config.bower %>/ace-builds/src-min-noconflict/mode-python.js',
                    '<%= config.bower %>/ace-builds/src-min-noconflict/mode-ruby.js',
                    '<%= config.bower %>/ace-builds/src-min-noconflict/mode-sh.js',
                    '<%= config.bower %>/ace-builds/src-min-noconflict/mode-sql.js',
                    '<%= config.bower %>/ace-builds/src-min-noconflict/mode-text.js',
                    '<%= config.bower %>/ace-builds/src-min-noconflict/mode-typescript.js',
                    '<%= config.bower %>/ace-builds/src-min-noconflict/mode-xml.js',
                    '<%= config.bower %>/jstree/dist/jstree.min.js',
                    '<%= config.bower %>/tagmanager/tagmanager.js',
                    '<%= config.bower %>/typeahead.js/dist/typeahead.bundle.min.js',
                    '<%= config.bower %>/zeroclipboard/dist/ZeroClipboard.min.js',
                    '<%= config.bower %>/patternfly/dist/js/patternfly.min.js'
                ],
                dest: '<%= config.public %>/js/external.min.js'
            }
        },

        less: {
            target: {
                options: {
                    banner: '/*\n' +
                    ' * Generated CSS file for <%= pkg.name %> <%= pkg.version %>\n' +
                    ' * Build date: <%= grunt.template.today("yyyy-mm-dd HH:MM:ss") %>\n' +
                    ' */\n\n',
                    paths: '<%= config.less %>',
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
        }
    });

    grunt.registerTask('dev', [
        'clean',
        'copy',
        'concat:dev',
        'less',
        'postcss'
    ]);

    grunt.registerTask('prod', [
        'clean',
        'copy',
        'concat:prod',
        'less',
        'postcss',
        'cssmin'
    ]);

    grunt.registerTask('default', ['dev']);
};