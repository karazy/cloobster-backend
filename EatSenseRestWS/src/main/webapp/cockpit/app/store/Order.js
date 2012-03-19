Ext.define('EatSense.store.Order', {
	extend: 'Ext.data.Store',
	config: {
		model: 'EatSense.model.Order',
		storeId: 'orderStore',
	}			
});