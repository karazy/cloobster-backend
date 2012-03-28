Ext.define('EatSense.model.Menu', {
	extend: 'Ext.data.Model',
	requires: ['EatSense.model.Product','EatSense.model.Choice','EatSense.model.Option','EatSense.data.proxy.CustomRestProxy'],
	config : {
		idProperty: 'id',
		fields: [
		    {name: 'id', type: 'string'},
			{name: 'title', type: 'string'}
		],
		proxy: {
		   type: 'rest',
		   url : '/c/businesses/{pathId}/menus',
		   reader: {
			   type: 'json'
	   		}
	 	},
		hasMany: {model: 'EatSense.model.Product', name: 'products'}
	}
});