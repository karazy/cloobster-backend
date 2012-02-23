Ext.define('EatSense.model.Spot', {
	extend : 'Ext.data.Model',
	config : {
		idProperty : 'brcode',
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
		} ],
		proxy : {
			type : 'rest',
			url : '/spots/',
			reader : {
				type : 'json',
			}
		}
	}
});