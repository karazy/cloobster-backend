Ext.define('EatSense.store.Bill', {
	extend : 'Ext.data.Store',
	requires : [ 'EatSense.model.Bill' ],
	config : {
		storeId : 'billStore',
		model : 'EatSense.model.Bill'
	}
});