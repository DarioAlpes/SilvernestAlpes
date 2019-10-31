(function() {
  'use strict';

  angular
    .module('material-lite')

    .factory('iconService', function () {
        return {
            icon: function (tipo) {
                var icono = "settings";
                switch(tipo){
                  case "Feedback":
                  icono = "mode_comment";
                  break;
                  case "Purchase":
                  icono = "attach_money";
                  break;
                  case "Action":
                  icono = "directions_run";
                  break;
                  case "Visit":
                  icono = "gps_fixed";
                  break;
                  case "Order":
                  icono = "add_shopping_cart";
                  break;
                }
                return icono;
            }
        }
    })

    .controller('PersonsTimelineController', ['$http', '$scope','$routeParams', 'config', 'PlaceholderTextService', 'ngTableParams' ,'$filter', '$interval', 'iconService', PersonsTimelineController]);

  function PersonsTimelineController($http, $scope, $routeParams, config, PlaceholderTextService, ngTableParams, $filter, $interval, iconService) {

    // adding demo data


	var data = [];
	$scope.data = data;
  console.log(config);
  $scope.icon = iconService.icon;

    /* jshint newcap: false */
    $scope.tableParams = new ngTableParams({
      page: 1,            // show first page
      count: 10,
      sorting: {
        EPC: 'asc'     // initial sorting
      }
    }, {
      filterDelay: 50,
      total: data.length, // length of data
      getData: function ($defer, params) {
        var searchStr = params.filter().search;
        var mydata = [];

        if (searchStr) {
          searchStr = searchStr.toLowerCase();
          mydata = data.filter(function (item) {
            return item.EPC.toLowerCase().indexOf(searchStr) > -1 || item.SKU.toLowerCase().indexOf(searchStr) > -1;
          });

        } else {
          mydata = data;
        }

        mydata = params.sorting() ? $filter('orderBy')(mydata, params.orderBy()) : mydata;
        $defer.resolve(mydata.slice((params.page() - 1) * params.count(), params.page() * params.count()));
      }
    });

    $scope.recuperarTimeLine=function(){
       $http({method: "GET", url: "https://smartobjectssas.appspot.com/clients/"+config.idCliente+"/persons/"+$routeParams.idPersona+"/events/"}).then(function(response) {
		console.log(response);
		var data = response.data;
    for (var i=0; i< data.length; i++) {
      var str = data[i]['initial-time'];
      data[i]['initial-time']
      var d = new Date(str.substring(0, 4),str.substring(4, 6),str.substring(6, 8),str.substring(8, 10),str.substring(10, 12),str.substring(12, 14));
      data[i]['initial-time'] = d.setHours(d.getHours() - 5);
    }
    $scope.data = data;
		$scope.tableParams = new ngTableParams({
		  page: 1,            // show first page
		  count: 10,
		  sorting: {
			EPC: 'asc'     // initial sorting
		  }
		}, {
		  filterDelay: 50,
		  total: data.length, // length of data
		  getData: function ($defer, params) {
			var searchStr = params.filter().search;
			var mydata = [];
			if (searchStr) {
			  searchStr = searchStr.toLowerCase();
			  mydata = data.filter(function (item) {
				return item.EPC.toLowerCase().indexOf(searchStr) > -1 || item.SKU.toLowerCase().indexOf(searchStr) > -1;
			  });

			} else {
			  mydata = data;
			}

			mydata = params.sorting() ? $filter('orderBy')(mydata, params.orderBy()) : mydata;
			$defer.resolve(mydata.slice((params.page() - 1) * params.count(), params.page() * params.count()));
		  }
		});//$scope.myData = response.data.records;
    });
    };
//	$http.defaults.headers.common.Authorization = 'd07220752e57865db08c6e7e6741e00bc705ac0622b36451f260abdd2a992e39';
    $scope.recuperarTimeLine();

   var refreshTimeline = $interval($scope.recuperarTimeLine, 5000);


   // $scope.$on('$destroy', function() {
    //   if (angular.isDefined(refreshTimeline))
    //   {
    //       $interval.cancel(refreshTimeline);
    //       refreshTimeline=null;
    //   }
    //});


    $http({method: "GET", url: "https://smartobjectssas.appspot.com/clients/"+config.idCliente+"/persons/"+$routeParams.idPersona+"/"}).then(function(response) {
		console.log(response.data);
		var data = response.data;
		$scope.person = data;
		//$scope.myData = response.data.records;
	});

    var metrics = [{low:"No Consumo de Alcohol", high:"Consumo de Alcohol", value:"32"},{low:"No Deportista", high:"Deportista", value:"100"},{low:"Bajo Consumo de Comida", high:"Alto Consumo de Comida", value:"70"},{low:"Bajo Gasto", high:"Alto Gasto", value:"5"}];
    $scope.metrics = metrics;

  }


})();
