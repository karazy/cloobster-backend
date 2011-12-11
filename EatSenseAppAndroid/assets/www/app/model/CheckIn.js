Ext.define('EatSense.model.CheckIn', {
	extend: 'Ext.data.Model',
	idProperty: 'userId',
	fields: [
		{name: 'status', type: 'string'},
		{name: 'restaurantName', type: 'string'},
		{name: 'spot', type: 'string'},
		{name: 'userId', type: 'string'}
	],
	proxy: {
		type: 'rest',
		//http://2.karazy-eatsense.appspot.com
		url: 'http://2.karazy-eatsense.appspot.com/restaurant/spot/', 
		reader: {
			type: 'json',
		}
	}
});