Ext.define('EatSense.controller.CheckIn', {
    extend: 'Ext.app.Controller',
    config: {
        profile: Ext.os.deviceType.toLowerCase()
    },
    
	views : [
		'Main',
		'Dashboard'
	],
	stores : [
	'CheckIn'
	],
	refs: [
        {
            ref       : 'main',
            selector  : 'main',
            xtype     : 'main',
            autoCreate: true
        },
        {
        	ref: 'searchfield',
        	selector : 'dashboard textfield'
        	
        }
    ],
    init: function() {
    	console.log('initialized CheckInController');
    	this.getMainView().create();
    	 this.control({
            '#checkInBtn': {
                tap: this.checkIn
            }
        });
    },
        
    checkIn: function(options) {
    	console.log('checkInBtn clicked');
    	var barcode = this.getSearchfield().getValue();
    	Ext.ModelManager.getModel('EatSense.model.CheckIn').load(barcode, {
    	    success: function(model) {
    	    	console.log("CheckIn Status: " + model.get('status'));
    	    	console.log("CheckIn Restaurant: " + model.get('restaurantName'));
    	    	Ext.Msg.confirm("CheckIn", "Bei "+ model.get('restaurantName') +" einchecken?", Ext.emptyFn);
    	    }
    	});
   }	
});

