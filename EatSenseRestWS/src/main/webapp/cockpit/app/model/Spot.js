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
		}, { //shows 
			name: 'status',
			type: 'string'
		}, { //time when first person checked in
			name: 'checkInTime',
			type: 'date'			
		}, { //value of all orders
			name: 'currentTotal',
			type: 'number'
		}],
		proxy : {
			type : 'rest',
			url : '/spots/'
		}
	}
});