// Angular-CLI build configuration
// This file lists all the node_modules files that will be used in a build
// Also see https://github.com/angular/angular-cli/wiki/3rd-party-libs

/* global require, module */

var Angular2App = require('angular-cli/lib/broccoli/angular2-app');

module.exports = function(defaults) {
  return new Angular2App(defaults, {
    vendorNpmFiles: [
      'systemjs/dist/system-polyfills.js',
      'systemjs/dist/system.src.js',
      'zone.js/dist/**/*.+(js|js.map)',
      'es6-shim/es6-shim.js',
      'reflect-metadata/**/*.+(ts|js|js.map)',
      'rxjs/**/*.+(js|js.map)',
      '@angular/**/*.+(js|js.map)',
      '@angular2-material/**/*',
      '@angular2-material/**/**/*',
      'ng2-bootstrap/**/*.js',
      'ng2-table/**/*.js',
      'moment/**/*.js',
      'primeui/**/*.*',
      'primeng/**/*.js',
      'jquery.inputmask/**/*.js',
      'core-js/client/*.js',
      'ng2-nvd3/**/*',
      'd3/**/*',
      'nvd3/**/*'
    ]
  });
};
