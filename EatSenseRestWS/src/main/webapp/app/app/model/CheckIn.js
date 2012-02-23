Ext.define('EatSense.model.CheckIn', {
	extend : 'Ext.data.Model',
	requires : [ 'EatSense.model.Error' ],
	config : {
		idProperty : 'userId',
		fields : [ {
			name : 'status',
			type : 'string'
		}, {
			name : 'restaurantName',
			type : 'string'
		}, {
			name : 'restaurantId',
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
			url : '/checkins/',
			reader : {
				type : 'json',
			}
		},
		// BUG (Sencha) doesn't work currently. Retest
		associations : {
			type : "hasOne",
			model : "EatSense.model.Error"
		},
		hasMany : {
			model : 'EatSense.model.Order',
			name : 'orders'
		}
	}

});