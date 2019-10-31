"use strict";

// SystemJS configuration file, see links for more information
// https://github.com/systemjs/systemjs
// https://github.com/systemjs/systemjs/blob/master/docs/config-api.md

/***********************************************************************************************
 * User Configuration.
 **********************************************************************************************/
/** Map relative paths to URLs. */
const map: any = {
  '@angular2-material': 'vendor/@angular2-material',
  'ng2-bootstrap': 'vendor/ng2-bootstrap',
  'ng2-table': 'vendor/ng2-table',
  'moment': 'vendor/moment',
  'primeui': 'vendor/primeui',
  'primeng': 'vendor/primeng',
  'ng2-nvd3': 'vendor/ng2-nvd3/build/lib/ng2-nvd3.js'
};

/** User packages configuration. */
const packages: any = {
  'rxjs'                             : {main: 'Rx'},
  '@angular/core'                    : {main: 'bundles/core.umd.min.js'},
  '@angular/common'                  : {main: 'bundles/common.umd.min.js'},
  '@angular/compiler'                : {main: 'bundles/compiler.umd.min.js'},
  '@angular/forms'                   : {main: 'bundles/forms.umd.min.js'},
  '@angular/platform-browser'        : {main: 'bundles/platform-browser.umd.min.js'},
  '@angular/platform-browser-dynamic': {main: 'bundles/platform-browser-dynamic.umd.min.js'},
  '@angular/http'                    : {main: 'bundles/http.umd.min.js'},
  '@angular/router'                  : {main: 'bundles/router.umd.min.js'},
  'ng2-bootstrap': {
    format: 'cjs',
    defaultExtension: 'js',
    main: 'ng2-bootstrap.js'

  },
  'ng2-table': {
    format: 'cjs',
    defaultExtension: 'js',
    main: 'ng2-table.js'

  },
  'moment': {
    format: 'cjs',
    defaultExtension: 'js',
    main: 'moment.js'
  },

  'primeng': {
    defaultExtension: 'js'
  },

  'ng2-nvd3': {
    defaultExtension: 'js'
  }
};

// put the names of any of your Material components here
const materialPkgs:string[] = [
  'core',
  'card',
  'button',
  'sidenav',
  'toolbar',
  'checkbox',
  'grid-list',
  'icon',
  'input',
  'list',
  'menu',
  'tabs',
  'tooltip',
  'progress-bar',
  'progress-circle',
  'radio',
  'slide-toggle',
  'icon'
];


materialPkgs.forEach((pkg) => {
  packages[`@angular2-material/${pkg}`] = {main: `${pkg}.umd.js`};
});


var ngPackageNames = [
  'common',
  'compiler',
  'core',
  'forms',
  'http',
  'platform-browser',
  'platform-browser-dynamic',
  'router',
  'router-deprecated',
  'upgrade',
];

// Individual files (~300 requests):
function packIndex(pkgName) {
  packages['@angular/'+pkgName] = { main: 'index.js', defaultExtension: 'js' };
}
// Bundled (~40 requests):
function packUmd(pkgName) {
  packages['@angular/'+pkgName] = { main: 'bundles/' + pkgName + '.umd.js', defaultExtension: 'js' };
}
// Most environments should use UMD; some (Karma) need the individual index files
var setPackageConfig = System.packageWithIndex ? packIndex : packUmd;
// Add package entries for angular packages
ngPackageNames.forEach(setPackageConfig);

////////////////////////////////////////////////////////////////////////////////////////////////
/***********************************************************************************************
 * Everything underneath this line is managed by the CLI.
 **********************************************************************************************/
const barrels: string[] = [

  // App specific barrels.
  'app',
  'views',
  'app/shared',
  /** @cli-barrel */
];

const cliSystemConfigPackages: any = {};
barrels.forEach((barrelName: string) => {
  cliSystemConfigPackages[barrelName] = { main: 'index' };
});

/** Type declaration for ambient System. */
declare var System: any;

// Apply the CLI SystemJS configuration.
System.config({
  map: {
    '@angular': 'vendor/@angular',
    'rxjs': 'vendor/rxjs',
    'main': 'main.js'
  },
  packages: cliSystemConfigPackages
});

// Apply the user's configuration.
System.config({ map, packages });
