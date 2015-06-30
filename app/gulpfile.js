/*!
 * gulp
 * $ npm install gulp-LESS gulp-autoprefixer gulp-minify-css gulp-rename gulp-copy del --save-dev
 */

var gulp = require('gulp'),
    less = require('gulp-less'),
    autoprefixer = require('gulp-autoprefixer'),
    minifycss = require('gulp-minify-css'),
    rename = require('gulp-rename'),
    copy = require('gulp-copy'),
    del = require('del');

gulp.task('styles', function () {
    return gulp.src('src/main/less/hal.less')
        .pipe(less())
        .pipe(autoprefixer({browsers: ['last 2 versions'], cascade: false}))
        .pipe(rename({suffix: '.min'}))
        .pipe(minifycss())
        .pipe(gulp.dest('src/main/webapp/css'));
});

gulp.task('dependencies', function () {
    // Web Components
    gulp.src('bower_components/polymer/*.html')
        .pipe(copy('src/main/webapp', {prefix: 1}));
    gulp.src('bower_components/webcomponentsjs/*.min.js')
        .pipe(copy('src/main/webapp', {prefix: 1}));

    // Patternfly
    gulp.src('bower_components/patternfly/dist/css/*.min.css')
        .pipe(copy('src/main/webapp', {prefix: 1}));
    gulp.src('bower_components/patternfly/dist/fonts/*')
        .pipe(copy('src/main/webapp', {prefix: 1}));
    gulp.src('bower_components/patternfly/dist/img/*')
        .pipe(copy('src/main/webapp', {prefix: 1}));
    gulp.src('bower_components/patternfly/dist/js/*.min.js')
        .pipe(copy('src/main/webapp', {prefix: 1}));

    // JQuery
    gulp.src('bower_components/patternfly/components/jquery/jquery.min.js')
        .pipe(copy('src/main/webapp', {prefix: 1}));

    // Bootstrap
    gulp.src('bower_components/patternfly/components/bootstrap/dist/js/bootstrap.min.js')
        .pipe(copy('src/main/webapp', {prefix: 1}));
    gulp.src('bower_components/patternfly/components/bootstrap-select/bootstrap-select.min.js')
        .pipe(copy('src/main/webapp', {prefix: 1}));

    // Fonts
    gulp.src('bower_components/patternfly/components/bootstrap/dist/fonts/*')
        .pipe(copy('src/main/webapp', {prefix: 1}));
    gulp.src('bower_components/patternfly/components/font-awesome/fonts/*')
        .pipe(copy('src/main/webapp', {prefix: 1}));
});

// Clean
gulp.task('clean', function (cb) {
    del(['src/main/webapp/polymer/**',
        'src/main/webapp/webcomponentsjs/**',
        'src/main/webapp/patternfly/**',
        'src/main/webapp/css/**'], cb)
});

// Default task
gulp.task('default', ['styles', 'dependencies']);

// Watch
gulp.task('watch', function () {
    // Watch .less files
    gulp.watch('src/main/less/**/*.less', ['styles']);
});
