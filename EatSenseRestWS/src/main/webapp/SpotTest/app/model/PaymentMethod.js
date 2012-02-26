Ext.define('EatSense.model.PaymentMethod', {
	extend : 'Ext.data.Model',
	config : {
		idProperty : 'id',
		fields : [ 
		           {
			name : 'id',
			type : 'string'
		}, 
		{
			name : 'name',
			type : 'string'
		}]	
	}
});