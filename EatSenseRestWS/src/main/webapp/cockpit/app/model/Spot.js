Ext.define('EatSense.model.Spot', {
	extend : 'Ext.data.Model',
	// requires: ['EatSense.model.PaymentMethod'],
	config : {
		idProperty : 'barcode',
		fields : [ {
			name : 'barcode',
			type : 'string'
		}, {
			name : 'restaurant',
			type : 'string'
		}, {
			name : 'restaurantId',
			type : 'string'
		}, {
			name : 'name',
			type : 'string'
		}, { //shows status of orders
			name: 'status',
			type: 'string'
		}, { //time when first person checked in
			name: 'checkInTime',
			type: 'date'			
		}, { //value of all orders
			name: 'currentTotal',
			type: 'number'
		}],
		 // associations: [{
	  //           type: 'hasMany',
	  //           model: 'EatSense.model.PaymentMethod',
	  //           primaryKey: 'id',
	  //           name: 'payments',
	  //           //autoLoad: true,
	  //           associationKey: 'payments' // read child data from child_groups
	  //       }],
		proxy : {
			type : 'rest',
			url : '/spots/',
			reader : {
				type : 'json',
			}
		}
	}
});