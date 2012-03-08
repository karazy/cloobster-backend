Ext.define('EatSense.store.Account', {
	extend : 'Ext.data.Store',
	requires : [ 'EatSense.model.Account' ],
	config : {
		storeId : 'accountStore',
		model : 'EatSense.model.Account'
	}
});