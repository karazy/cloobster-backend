/**
* Reprensents a business like a Hotel or Restaurant.
*/
Ext.define('EatSense.model.Business', {
	extend: 'Ext.data.Model', 
		config: {
			fields: [
			{name: 'id'},
			{name: 'name'},
			{name: 'description'}
			],
			proxy: {
				type: 'rest',
				url: '/accounts/{pathId}/businesses'
			}
		}	
});