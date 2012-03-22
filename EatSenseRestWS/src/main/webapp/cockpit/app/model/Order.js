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
		}, {
			name: 'checkInId'
		}],

		associations : {
			type : 'hasOne',
			model : 'EatSense.model.Product',
		},

		proxy : {
			type : 'rest',
			url : '/b/businesses/{pathId}/orders/'
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
		rawJson.checkInId = this.get('checkInId');
		
		rawJson.product = this.getProduct().getRawJsonData();
		
		return rawJson;
	}
});