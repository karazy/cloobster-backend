Ext.define('EatSense.store.User', {
    extend  : 'Ext.data.Store',    
    requires: ['EatSense.model.User'],
    config : {
    	model   : 'EatSense.model.User'
    }
});