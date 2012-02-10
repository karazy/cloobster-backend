Ext.define('EatSense.store.Menu', {
    extend  : 'Ext.data.Store',
    requires: ['EatSense.model.Menu'],
    storeId: 'menuStore',
    config : {
    	model   : 'EatSense.model.Menu'
    }
});