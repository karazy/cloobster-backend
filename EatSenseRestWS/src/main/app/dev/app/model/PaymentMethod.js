Ext.define('EatSense.model.PaymentMethod', {
	extend : 'Ext.data.Model',
	config : {
		fields : [ 
		{
			name : 'name',
			type : 'string'
		},
		{
			name: 'order',
			type: 'number'
		}]	
	}
});