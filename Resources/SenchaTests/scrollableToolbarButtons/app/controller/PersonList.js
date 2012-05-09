Ext.define('ListTest.controller.PersonList', {
    extend: 'Ext.app.Controller',
    config: {
        profile: Ext.os.deviceType.toLowerCase()
    },
    
	views : [
		'ListWrapper',
		'PersonList',
		'Detail'
	],
	stores : [
		'Person'
	],
	refs: [
		{
            ref       : 'listwrapper',
            selector  : 'listwrapper',
            xtype     : 'listwrapper',
            autoCreate: true
        },
        {
            ref       : 'personlist',
            selector  : '#thelist'
        },
        {
            ref       : 'detail',
            selector  : 'detail'
        }
    ],
    init: function() {
    	console.log('initialized controller');
    	this.getListWrapperView().create();
    	this.getPersonlist().setStore(this.getPersonStore());
    	//this.getPersonlist().setStore(this.getPersonStore());
    	
    	 this.control({
    		 '#thelist': {
             	select: this.showDetail
             },
             '#backToList' : {
            	tap: this.backToList 
             }
        });
    },

    showDetail: function(dataview, record) {
    	console.log("show detail");

    },

	backToList : function() {				
		console.log("back to list");
		  	 
	}
     	
});

