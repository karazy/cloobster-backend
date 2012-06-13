/** @module CloobsterAdmin */
'use strict';

/**
* 	@name CloobsterAdmin.Navigation 
*	@requires $location
*
* 	Checks the active location path.
* 	@constructor
*/
CloobsterAdmin.Navigation = function($scope, $location) {
	var location;

	/**
	*
	*/
	$scope.getActive = function(path) {

		location = $location.path();
		return (location.indexOf(path) === 0) ? "active" : "";
	}

};
CloobsterAdmin.Navigation.$inject = ['$scope', '$location'];

/**
* 	@name CloobsterAdmin.Import
*	@requires $http
*
* 	Manages data import.
* 	@constructor
*/
CloobsterAdmin.Import = function($scope, $http, $anchorScroll) {
	
	function showAlert( type, title, message, buttonText, continueFn) {
		$scope.importAlert.type = type;
		$scope.importAlert.show = true;
		$scope.importAlert.message = message;
		$scope.importAlert.title = title;
		$scope.importAlert.buttonText = buttonText;
		$scope.importAlert.continueFn =  function() {
			dismissAlert();
			continueFn();
		};
	}

	function dismissAlert() {
		$scope.importAlert = { show: false, type: "alert-error", message: "", title: "", buttonText:"Action", continueFn: dismissAlert};
	}

	function setError(message) {
		showAlert("alert-error", "Error!", message, "Try again", resetForm);
	}

	function setProgress( progress ) {
		$scope.importProgressStyle = { width: progress };
	}

	function importError(data, status) {
		var message = (data.message) ?
			data.message
			: "An unknown error occured, check the server and your connection.";
		setError(message);
	}

	function importSuccess() {
		setProgress("100%");
		showAlert("alert-success", "Done!", "Business import done.", "Import another", resetForm);
	}

	function resetForm() {
		$scope.jsonData = "";
		$scope.importProgress = false;
		setProgress("0%");
	}

	$scope.import = function() {
		var dto;
		$scope.importProgress = true;
		try {
			dto = angular.fromJson($scope.jsonData);	
		}
		catch(err) {
			setProgress("30%");
			setError("JSON parsing error: " + err.message);
			return;
		}
		$http.post($scope.importUrl, dto).success(importSuccess)
		.error(importError);
	}

	dismissAlert();
	setProgress("0%");
	resetForm();

	$anchorScroll();
}
CloobsterAdmin.Import.$inject = ['$scope', '$http', '$anchorScroll'];

CloobsterAdmin.Functions = function($scope, $http, $anchorScroll) {

}

CloobsterAdmin.Functions.$inject = ['$scope', '$http', '$anchorScroll'];