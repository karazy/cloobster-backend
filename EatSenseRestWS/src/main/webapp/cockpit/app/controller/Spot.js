Ext.define('EatSense.controller.Spot', {
	extend: 'Ext.app.Controller',
	config: {
		refs: {
			spotitem: '#spotcard dataview spotitem'
		}
	},

	init: function() {
		Ext.Logger.info('initializing Spot Controller');
		console.log('initializing Spot Controller');
		 this.control({
		 	spotitem: {
		 		tab: function() {
		 			alert('tab');
		 		},
		 		itemtab: function() {
		 			alert('itemtab');
		 		},
		 		select: function() {
		 			alert('select');
		 		}
		 	}

		 });
		
	},

	loadSpots: function() {
		console.log('loadSpots');

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
	}

})