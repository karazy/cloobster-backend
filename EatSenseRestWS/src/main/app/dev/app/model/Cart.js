/**
* Represents a customers cart.
* This is just a representation for all orders issued by the customer.
* It can be used to apply mass updates to orders.	
*/
Ext.define('EatSense.model.Cart', {
	extend: 'Ext.data.Model',
	config: {
		fields:[
			{
				name: 'status',
				type: 'string'
			}
		],
		proxy: {
			type: 'rest',
			url: '/c/checkins/{checkInId}/cart',
			enablePagingParams: false,
		}
	}
});