Ext.define('EatSense.model.Order', {
	extend : 'Ext.data.Model',
	config : {
		idProperty : 'id',
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
		}
	},

	calculate : function() {
		var _amount = parseFloat(this.get('amount')), _basePrice = this.getProduct().calculate();
		return Math.round(_basePrice * _amount * 100) / 100;
	},
	
	getRawJsonData: function() {
		var rawJson = {};
		
		rawJson.id = (this.phantom === true) ? this.get('genuineId') : this.get('id');
		rawJson.status = this.get('status');
		rawJson.amount = this.get('amount');
		rawJson.comment = this.get('comment');
		rawJson.orderTime = this.get('orderTime');
		
		rawJson.product = this.getProduct().getRawJsonData();
		
		return rawJson;
	}

});