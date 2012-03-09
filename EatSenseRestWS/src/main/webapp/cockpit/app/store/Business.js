Ext.define('EatSense.model.Business', {
	extend: 'Ext.data.Store',
	requires : [ 'EatSense.model.Business' ],
	config : {
		storeId : 'myBusinessStore',
		model : 'EatSense.model.Business'
	}
});