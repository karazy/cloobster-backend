Ext.define('EatSense.store.AppState', {
	extend : 'Ext.data.Store',
	requires : [ 'EatSense.model.Account' ],
	config : {
		storeId : 'appStateStore',
		model : 'EatSense.model.Account',
		autoSync: true,
		proxy : {
			type : 'localstorage',
		}
	}
});