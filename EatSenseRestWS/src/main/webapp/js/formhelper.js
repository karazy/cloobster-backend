
/**
* Validates a form with jQuery.
* Tests all inputs and textarea with calss required or email.
*/
function validateForm() {
	var nameValue = $('#entry_0').val(),
		emailValue = $('#entry_1').val(),
		textValue = $('#entry_2').val(),
		phoneValue = $('#entry_3').val(),
		fieldsToValidate = $('input.required, textarea.required'),
		emailFieldsToValidate = $('input.email'),
		valid = true;

		fieldsToValidate.each(function(index, element) {
			if($.trim($(element).val()).length == 0) {
				valid = false;
				$("#"+element.id + "_error").show();
			} else {
				$("#"+element.id + "_error").hide();
			}
		});	

		emailFieldsToValidate.each(function(index, element) {
			if(!validateEmail($(element).val())) {
				valid = false;
				$("#"+element.id + "_error").show();
			} else {
				$("#"+element.id + "_error").hide();
			}
		});	

		if(valid) {
			return true;
		} else {
			return false;
		}

}

/**
* Checks if given string is a valid email.
*/
function validateEmail(email) { 
	var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(email);
} 

$(document).ready(function() {
	$('form').submit(function(e) {
		return validateForm();
	});
});