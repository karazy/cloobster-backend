/**
 * A bill.
 * Gets 
 */
Ext.define('EatSense.model.Bill', {
	extend : 'Ext.data.Model',
	config : {
		idProperty : 'id',
		fields : [ {
			name : 'id',
			type : 'string'
		}, {
			name : 'billnumber',
			type : 'string'
		}, {
			name : 'paymentMethod',
			type : 'string'
		}, {
			name : 'totalPrice,',
			type : 'number'
		} ]
	}
});