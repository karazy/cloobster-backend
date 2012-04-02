/**
 * A bill.
 */
Ext.define('EatSense.model.Bill', {
	extend : 'Ext.data.Model',
	config : {
		idProperty : 'id',
		fields : [ {
			name : 'id',
			type : 'string'
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
		} ],
		proxy: {
	 		type: 'rest',
	 		enablePagingParams: false,
	 		url : '/c/businesses/{pathId}/bills',
	 		reader: {
	 			type: 'json'
	 		}
	 	}
	}
});