var karazy;

//create karazy namespace
karazy = karazy || {};

karazy.Login = (function() {

	/**
	* Show/hide loginbox depending on state.
	*/
	function toggleLoginBox() {
		var existingToken = window.localStorage.getItem("accessToken");
		
		$(".login_failed").hide();
		
		//0. check if accessToken exists
		if(existingToken) {
			logMeIn(existingToken, true);
		}
		
		
		$('.login_box').slideToggle("slow", function() {
			 $(":input[name='login_user']").focus();
		});
		return false;
	}

	/**
	* Gather data from input fields and attempt a login try.
	* If successful redirect to frontend.
	*/
	function doLogin() {
		var username,
			password,
			save,
			valid = true;
			

		username = $(":input[name='login_user']").val();
		password = $(":input[name='login_pw']").val();
		save = $(":input[name='login_save']").is(":checked");
		
		$(".login_failed").hide();
		
		//1. check if empty
		if($.trim(username).length == 0) {
			valid = false;
			$(".login_user_error").show();
		} else {
			$(".login_user_error").hide();
		}

		if($.trim(password).length == 0) {
			valid = false;
			$(".login_pw_error").show();
		} else {
			$(".login_pw_error").hide();
		}

		if(!valid) {
			return;
		}

		//2. do ajax request
		$.ajax({
			url: "/accounts/tokens",
			type: "POST",
			dataType: "json",
			beforeSend: function(xhr) {
    			xhr.setRequestHeader("login", username);
    			xhr.setRequestHeader("password", password);
  			},
			success: function(data, status) {
				logMeIn(data.accessToken, save);
			},
			error: function(jqXHR, textStatus, errorThrown) {
				$(".login_failed").show();
			}
		});		
	}
	
	function logMeIn(accessToken, save) {
		//3. store access token
		window.localStorage.setItem("accessToken", accessToken);

		if(save == "save" || save == true) {
			window.localStorage.removeItem("accessTokenTransient");				
		} else {
			window.localStorage.setItem("accessTokenTransient", true);
		}
		//4. redirect to frontend on success
		window.location = "frontend/#/businesses";
	}

	$(document).ready(function() {
		$("#loginaction").click(function(e) {			
			karazy.Login.login();
		});

		$(".login_but").click(function(e) {
			karazy.Login.show();
		});
		
		$(":input[name='login_user'], :input[name='login_pw'], :input[name='login_save']").keypress(function(e) {
		    if(e.which == 13) {
		        doLogin();
		    }
		});
	});
	//public methods
	return {
		show: function() {
			toggleLoginBox();
		},

		login: function() {
			doLogin();
		}
	};

}());

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



