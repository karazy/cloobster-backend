Ext.define('EatSense.model.Spot', {
	extend : 'Ext.data.Model',
	requires: ['EatSense.model.PaymentMethod'],
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
		}, {
			name: 'groupTag',
			type: 'string'
		}],
		 associations: [{
	            type: 'hasMany',
	            model: 'EatSense.model.PaymentMethod',
	            primaryKey: 'id',
	           // foreignKey: 'parent_id',
	            name: 'payments',
	        //    autoLoad: true,
	            associationKey: 'payments' // read child data from child_groups
	        }
	       ],
		
		proxy : {
			type : 'rest',
			url : '/spots',
			reader : {
				type : 'json',
			}
		}
	}
});