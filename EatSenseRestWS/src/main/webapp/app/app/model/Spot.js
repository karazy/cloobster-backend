Ext.define('EatSense.model.Spot', {
	extend : 'Ext.data.Model',
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
		} ],
		hasMany : {
			model : 'EatSense.model.PaymentMethod',
			name : 'payments'
		},
		proxy : {
			type : 'rest',
			url : '/spots/',
			reader : {
				type : 'json',
			}
		}
	}
});