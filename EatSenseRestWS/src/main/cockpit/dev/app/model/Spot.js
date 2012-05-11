Ext.define('EatSense.model.Spot', {
	extend : 'Ext.data.Model',
	// requires: ['EatSense.model.PaymentMethod'],
	config : {
		idProperty : 'id',
		fields : [ 
		{
			name: 'id'
		},
		{
			name : 'barcode',
			type : 'string'
		}, {
			name : 'business',
			type : 'string'
		}, {
			name : 'businessId',
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
			enablePagingParams: false,
			url : '/b/businesses/{pathId}/spots/'
		}
	}
});