Ext.define('EatSense.store.Request', {
	extend: 'Ext.data.Store',
	requires: ['EatSense.model.Request'],
	config: {
		storeId: 'requestStore',
		model: 'EatSense.model.Request'
	}
});