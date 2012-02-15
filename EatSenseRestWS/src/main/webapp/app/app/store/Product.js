Ext.define('EatSense.store.Product', {
    extend  : 'Ext.data.Store',
    requires: ['EatSense.model.Product'],
    config : {
    	model   : 'EatSense.model.Product'
    }
});