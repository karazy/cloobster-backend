Ext.define('EatSense.controller.CheckIn', {
    extend: 'Ext.app.Controller',
    config: {
        profile: Ext.os.deviceType.toLowerCase()
    },
    
	views : [
		'Main',
		'Dashboard'
	],
	refs: [
        {
            ref       : 'main',
            selector  : 'main',
            xtype     : 'main',
            autoCreate: true
        }
    ],
    init: function() {
    	console.log('initialized CheckInController');
    	this.getMainView().create();
    	this.control({
    		'#checkInBtn' : {
    			click : this.checkIn
    		}
    	
    	});
    },
        
    checkIn: function(options) {
    	console.log('checkInBtn clicked');
   }	
});