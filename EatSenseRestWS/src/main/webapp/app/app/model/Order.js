Ext.define('EatSense.model.Order', {
	extend : 'Ext.data.Model',
	config : {
		idProperty : 'id',
		fields : [ {
			name : 'id',
			type : 'string'
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
			type : 'date',
			dateFormat: 'time'
		} ],
		associations : {
			type : 'hasOne',
			model : 'EatSense.model.Product',
		},
		proxy: {
			type: 'rest',
			enablePagingParams: false,
			url : '/c/businesses/{pathId}/orders',
			reader: {
				type: 'json'
		   	}
	 	}
	},

	calculate : function() {
		var _amount = parseFloat(this.get('amount'));
		return this.getProduct().calculate(_amount);
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