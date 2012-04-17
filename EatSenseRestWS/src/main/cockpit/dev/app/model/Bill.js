/**
 * A bill.
 */
Ext.define('EatSense.model.Bill', {
	extend : 'Ext.data.Model',
	config : {
		idProperty : 'id',
		fields : [ {
			name : 'id'
		}, {
			name : 'billnumber',
			type : 'string'
		}, {
			name : 'paymentMethod',
			type : 'string'
		}, {
			name : 'total',
			type : 'number',
			defaultValue: 0
		}, {
			name : 'time',
			type : 'date',
			dateFormat : 'time'
		}, {
			name: 'cleared',
			type: 'boolean'
		}, {
			name: 'checkInId'
		} ],
		associations : {
			type : 'hasOne',
			model : 'EatSense.model.PaymentMethod',
		},
		proxy: {
	 		type: 'rest',
	 		enablePagingParams: false,
	 		url : '/b/businesses/{pathId}/bills',
	 		reader: {
	 			type: 'json'
	 		}
	 	}
	}
});