Ext.define('EatSense.model.CheckIn', {
	extend : 'Ext.data.Model',
	requires : [ 'EatSense.model.Error' ],
	config : {
		idProperty : 'userId',
		fields : [ {
			name : 'status',
			type : 'string'
		}, {
			name : 'businessName',
			type : 'string'
		}, {
			name : 'businessId',
			type : 'string'
		}, {
			name : 'spot',
			type : 'string'
		}, {
			name : 'spotId',
			type : 'string'
		}, {
			name : 'userId',
			type : 'string'
		},
		{
			name : 'linkedCheckInId',
			type : 'string'
		},
		{
			name : 'nickname',
			type : 'string'
		}, {
			name : 'deviceId',
			type : 'string'
		} ],
		proxy : {
			type : 'rest',
			url : '/c/checkins/',
			enablePagingParams: false,
			reader : {
				type : 'json',
			}
		},
		hasMany : {
			model : 'EatSense.model.Order',
			name : 'orders'
		}
	}

});