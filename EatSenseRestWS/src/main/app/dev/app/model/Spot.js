Ext.define('EatSense.model.Spot', {
	extend : 'Ext.data.Model',
	requires: ['EatSense.model.PaymentMethod'],
	config : {
		idProperty : 'barcode',
		fields : [ {
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
		} ],
		 associations: [{
	            type: 'hasMany',
	            model: 'EatSense.model.PaymentMethod',
	            primaryKey: 'id',
	            name: 'payments',
	            //autoLoad: true,
	            associationKey: 'payments' // read child data from child_groups
	        }],
		proxy : {
			type : 'rest',
			url : '/spots/',
			enablePagingParams: false,
			reader : {
				type : 'json',
			}
		}
	}
});