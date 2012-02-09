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
		}, {
			name: 'product_id',
			type: 'string'
		},
		{
			name: 'product'
		}
		],

//		associations : {
//			type : 'hasOne',
//			model : 'EatSense.model.Product'
//		},
//		hasOne: {
//			model : 'EatSense.model.Product',
//			name: 'product'
//		},
		proxy : {
			type : 'rest',
			url : globalConf.serviceUrl + '/restaurant/order/',
			reader : {
				type : 'json',
			}
		}
	},
	
	calculate: function() {
		var _amount = parseFloat(this.get('amount')), _basePrice = this.get('product').calculate();
		return Math.round(_basePrice*_amount*100)/100;
	}
	

});