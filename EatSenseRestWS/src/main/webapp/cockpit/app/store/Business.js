Ext.define('EatSense.store.Business', {
	extend: 'Ext.data.Store',
	requires : [ 'EatSense.model.Business' ],
	config : {
		storeId : 'businessStore',
		model : 'EatSense.model.Business'
	}
});