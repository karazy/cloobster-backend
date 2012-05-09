Ext.define('EatSense.store.CheckIn', {
    extend  : 'Ext.data.Store',    
    requires: ['EatSense.model.CheckIn'],
    config : {
    	model   : 'EatSense.model.CheckIn'
    }
});