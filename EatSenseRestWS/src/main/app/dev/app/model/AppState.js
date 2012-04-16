/**
 * Contains all information to restore application state, such as which orders
 * are in the cart. Furthermore used to store user settings.
 * 
 */
Ext.define('EatSense.model.AppState', {
	extend : 'Ext.data.Model',
	config : {
		fields : [ {
			name : 'id'
		}, {
			name : 'nickname',
			type : 'string'
		}, {
			name : 'checkInId',
			type : 'string'
		} ],
		associations : [ {
			type : 'hasMany',
			model : 'EatSense.model.Order',
			primaryKey : 'id',
			name : 'cartOrders',
			autoLoad : true,
			//associationKey : 'choices' // read child data from child_groups
		} ]
	}
});