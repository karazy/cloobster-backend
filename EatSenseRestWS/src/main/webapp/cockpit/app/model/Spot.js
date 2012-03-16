Ext.define('EatSense.model.Spot', {
	extend : 'Ext.data.Model',
	// requires: ['EatSense.model.PaymentMethod'],
	config : {
		idProperty : 'barcode',
		fields : [ 
		{
			name: 'id'
		},
		{
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
		}, { 
			name: 'checkInCount',
			type: 'number'			
		}, { //value of all orders
			name: 'currentTotal',
			type: 'number'
		}],
		proxy : {
			type : 'rest',
			url : '/restaurants/{pathId}/spots/'
		}
	}
});