Ext.define('EatSense.store.AppState', {
	extend : 'Ext.data.Store',
	requires : [ 'EatSense.model.AppState' ],
	config : {
		storeId : 'appStateStore',
		model : 'EatSense.model.AppState',
		autoSync : true,
		proxy : {
			type : 'localstorage',
		},
		//we make sure that only one appState instance is used
		filters : [ {
			property : 'id',
			value : '1'
		} ]
	}
});