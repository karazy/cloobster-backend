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
		this.getSpotsview().getStore().removeAll();
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
		

		//just for dev purpose!
		// //create a store loading all available HUP Spots. 

		// var spotStore = Ext.create('Ext.data.Store', {
		// 	model: 'EatSense.model.Spot',
		// 	storeId: 'spotStore',
		// 	proxy: {
		// 		type: 'rest',
		// 		url: 'restaurants/1/spots',
		// 		reader: {
		// 			type: 'json'
		// 		}
		// 	},
		// 	data: [
		// 	{barcode: 'hup001', restaurant: 'Heidi und Paul', restaurantId: 1, name: 'Tisch 1'},
		// 	{barcode: 'hup002', restaurant: 'Heidi und Paul', restaurantId: 1, name: 'Tisch 2'},
		// 	{barcode: 'hup003', restaurant: 'Heidi und Paul', restaurantId: 1, name: 'Tisch 3'},
		// 	{barcode: 'hup004', restaurant: 'Heidi und Paul', restaurantId: 1, name: 'Tisch 4'},
		// 	{barcode: 'hup005', restaurant: 'Heidi und Paul', restaurantId: 1, name: 'Tisch 5'}
		// 	]
		// });

		// this.getSpotlist().setStore(spotStore);
		// this.getSpotlist().refresh();
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