Ext.define('EatSense.model.Order', {
	extend : 'Ext.data.Model',
	config : {
		idProperty : '',
		fields : [ {
			name : 'id',
			type : 'number'
		}, {
			name : 'status',
			type : 'string'
		}, {
			name : 'amount',
			type : 'number'
		}, {
			name : 'comment',
			type : 'string'
		}, {
			name : 'orderTime',
			type : 'date'
		} ],

		associations : {
			type : 'hasOne',
			model : 'EatSense.model.Product'
		},
		proxy : {
			type : 'rest',
			url : globalConf.serviceUrl + '/restaurant/order/',
			reader : {
				type : 'json',
			}
		}
	}

});