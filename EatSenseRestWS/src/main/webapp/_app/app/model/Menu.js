Ext.define('EatSense.model.Menu', {
	extend: 'Ext.data.Model',
	requires: ['EatSense.model.Product'],
	idProperty: 'id',
	fields: [
	    {name: 'id', type: 'string'},
		{name: 'title', type: 'string'}
	],
	proxy: {
		type: 'rest',
		url: '/restaurant/menu/', 
		reader: {
			type: 'json',
		} 
	},
	hasMany: {model: 'EatSense.model.Product', name: 'products'}
});