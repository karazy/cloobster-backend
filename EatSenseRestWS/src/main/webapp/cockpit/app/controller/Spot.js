Ext.define('EatSense.controller.Spot', {
	extend: 'Ext.app.Controller',
	requires: ['EatSense.view.Main'],
	config: {
		refs: {
			spotitem: 'spotitem button',
			spotsview: '#spotsview'
		}
	},

	init: function() {
		Ext.Logger.info('initializing Spot Controller');
		console.log('initializing Spot Controller');
		 this.control({
		 	spotitem: {
		 		tap:  this.showSpotDetails
		 	}
		 });
		
	},
	/**
	*	Loads all businesses this user account is assigned to.
	*
	*/
	loadBusinesses: function() {
		console.log('loadBusinesses');
		var businessStore = Ext.StoreManager.lookup('businessStore'),
		loginCtr = this.getApplication().getController('Login');

		businessStore.load({
			params: {
				pathId: loginCtr.getAccount().get('login')
			},
			 callback: function(records, operation, success) {
			 	if(success) {			 		
			 	}				
			 },
			 scope: this
		});
	},
	/**
	*
	*
	*/
	loadSpots: function() {
		console.log('loadSpots');
		var loginCtr = this.getApplication().getController('Login'),
		account = loginCtr.getAccount();
		//TODO workaround
		//this.getSpotsview().getStore().removeAll();
		this.getSpotsview().getStore().load({
			 params: {
			 	pathId : 1,
			 },
			 callback: function(records, operation, success) {
			 	if(success) {
			 		this.getSpotsview().refresh();	 		
			 	}				
			 },
			 scope: this
		});	
	},
	/**
	*	Gets called when user taps on a spot. Shows whats going on at a particular spot.
	*   Like orders, payment requests ...
	*
	*/
	showSpotDetails: function(button, eventObj, eOpts) {
		console.log('showSpotDetails for ');
	}

})