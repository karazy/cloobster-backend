Ext.define('EatSense.store.AppState', {
	extend : 'Ext.data.Store',
	requires : [ 'EatSense.model.Account' ],
	config : {
		storeId : 'cockpitStateStore',
		model : 'EatSense.model.Account',
		proxy : {
			type : 'localstorage',
			id: 'eatSense_cockpit_store'
		}
	}
});