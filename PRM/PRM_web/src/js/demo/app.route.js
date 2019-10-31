(function() {
  'use strict';

  // routes
  angular
    .module('material-lite')
    .config(['$routeProvider', routeProvider])
    .run(['$route', routeRunner]);

  function routeProvider($routeProvider) {

    $routeProvider.when('/', {
      templateUrl: 'tpl/demo/dashboard.html'

    }).when('/personas/:idPersona/timeline', {
        templateUrl: 'tpl/demo/timeline.html',
        controller: 'PersonsTimelineController'
        }
    ).when('/:folder/:tpl', {
        templateUrl: function(attr){
          return 'tpl/demo/' + attr.folder + '/' + attr.tpl + '.html';
        }

    }).when('/:tpl', {
      templateUrl: function(attr){
        return 'tpl/demo/' + attr.tpl + '.html';
      }

    }).otherwise({ redirectTo: '/' });
  }

  function routeRunner($route) {
    // $route.reload();
  }

})();
