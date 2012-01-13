Ext.define('EatSense.model.CheckIn', {
	extend: 'Ext.data.Model',
	requires: ['EatSense.model.Error'],
	idProperty: 'userId',
	fields: [
		{name: 'status', type: 'string'},
		{name: 'restaurantName', type: 'string'},
		{name: 'restaurantId', type: 'string'},
		{name: 'spot', type: 'string'},
		{name: 'userId', type: 'string'},
		{name: 'nickname', type: 'string'}
	],
	proxy: {
		type: 'rest',
		url: '/restaurant/spot/', 
		//appendId: false,
		reader: {
			type: 'json',
		}
	},
	associations : {
        type : "hasOne",
        model : "EatSense.model.Error"
    }
});