Ext.define('EatSense.store.Request', {
	extend: 'Ext.data.Store',
	config: {
		model: 'EatSense.model.Request',
		storeId: 'requestStore',
	}			
});