/**
* Specifically handles actions relevant for Android.
*/
Ext.define('EatSense.controller.Android', {
	extend: 'Ext.app.Controller',
	config: {
		//Array of functions to execute when back button event is triggered
		androidBackHandler : null
	},
	launch: function() {

	},
	addBackHandler: function(handler) {
		if(Karazy.util.isFunction(handler) && Karazy.util.isArray(this.getAndroidBackHandler())) {
			console.log('Android Controller -> addBackHandler');
			this.getAndroidBackHandler().push(handler);	
		} else {
			console.log('handler is not of type function');
		}
	},
	removeLastBackHandler: function() {		
		if(Karazy.util.isArray(this.getAndroidBackHandler())) {
			console.log('Android Controller -> removeLastBackHandler');
			this.getAndroidBackHandler().pop();
		}		
	},
	executeBackHandler: function() {
		var handler;
		
		if(Karazy.util.isArray(this.getAndroidBackHandler()) &&  this.getAndroidBackHandler().length > 0) {
			console.log('Android Controller -> executeBackHandler');
			handler = this.getAndroidBackHandler().pop();
			handler();
		}
	}
});