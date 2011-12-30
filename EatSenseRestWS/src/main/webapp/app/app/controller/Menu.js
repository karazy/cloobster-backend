Ext.define('EatSense.controller.Menu', {
    extend: 'Ext.app.Controller',
    config: {
        profile: Ext.os.deviceType.toLowerCase()
    },
    
	views : [
		'Main',
		'Dashboard',
		'MenuOverview'
	],
	stores : [
	'Menu'
	],
	refs: [
        {
            ref       : 'main',
            selector  : 'main',
            xtype     : 'main'
        },
        {
        	ref: 'menulist',
        	selector : '#menulist'        	
        }
    ],
    init: function() {
    	console.log('initialized MenuController');
    	 this.control({
//    		 '#menulist': {
//             	select: this.showProductlist
//             }
        });
    	 
    	 //store retrieved models
    	 var models = {};
    	 this.models = models;
    },
        	
});

