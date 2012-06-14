'use strict';

/* Cloobster namespace. Create if not exists.*/
var CloobsterAdmin =  {};

// Declare app level module which depends on filters, and services
CloobsterAdmin.module = angular.module('CloobsterAdmin', []).
  config(['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {
  	$locationProvider.hashPrefix = '!';
  	$routeProvider.when('/main', {template: 'partials/main.html'});
    $routeProvider.otherwise({redirectTo: '/main'});
 }]);

CloobsterAdmin.module.directive('importAlert', function(){
    return {
      restrict: 'A',
      replace: true,
      transclude: false,
      scope: { alert:'accessor' },
      template: '<div class="alert alert-block" ng-class="alert().type" ng-show="alert().show">'+
	  				'<h4 class="alert-heading" ng-bind="alert().title">Error!</h4>'+
	  				'<span ng-bind="alert().message"></span>'+
	  				'<p><button type="button" class="btn" ng-click="continue()" ng-bind="alert().buttonText"></button></p>'+
				'</div>',
      // The linking function will add behavior to the template
      link: function(scope, element, attrs) {
      	scope.continue = function() {
      		scope.alert.show = false;
      		scope.alert().continueFn();
      	}
      }
    }
  });