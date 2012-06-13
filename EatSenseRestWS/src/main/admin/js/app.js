'use strict';

/* Cloobster namespace. Create if not exists.*/
var Cloobster =  {};

// Declare app level module which depends on filters, and services
Cloobster.module = angular.module('CloobsterAdmin', []).
  config(['$routeProvider', function($routeProvider) {
  	$routeProvider.when('/', {template: 'partials/home.html'});
    $routeProvider.otherwise({redirectTo: '/'});
 }]);
