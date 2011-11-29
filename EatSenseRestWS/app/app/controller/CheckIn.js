Ext.define('EatSense.controller.CheckIn', {
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
	'CheckIn'
	],
	refs: [
        {
            ref       : 'main',
            selector  : 'mainview',
            xtype     : 'mainview',
            autoCreate: true
        },
        {
        	ref: 'searchfield',
        	selector : 'dashboard textfield'
        	
        }, 
        {
        	ref: 'menuoverview',
        	selector: 'menuoverview'
        }
    ],
    init: function() {
    	console.log('initialized CheckInController');
    	this.getMainView().create();
    	 this.control({
            '#checkInBtn': {
                tap: this.checkInIntent
            }
        });
    },
        
    checkInIntent: function(options) {
    	console.log('CheckIn Controller -> checkIn');
    	var barcode = this.getSearchfield().getValue();
    	var that = this;
    	Ext.ModelManager.getModel('EatSense.model.CheckIn').load(barcode, {
    		synchronous: true,
    	    success: function(model) {
    	    	console.log("CheckInIntent Status: " + model.get('status'));
    	    	console.log("checkInIntent Restaurant: " + model.get('restaurantName'));
    	    	Ext.Msg.confirm("CheckIn", "Bei "
						+ model.get('restaurantName')
						+ " einchecken?", function(status) {
					if (status == 'yes') {
						that.checkIn({model:model});
					} else {
						that.cancelCheckIn({model:model});
					}
				});
    	    }
    	});
   },
   
   checkIn: function(options) {
	   console.log("CheckIn Controller -> checkIn");
	   var that = this;
	   options.model.save({
   	    success: function(model) {
   	    	that.showMenu();
   	    }
   	});	   
   },
   
   cancelCheckIn: function(options) {
	   console.log("CheckIn Controller -> cancelCheckIn");
	   options.model.destroy();	   
   },
 
	showMenu : function() {
		console.log("CheckIn Controller -> showMenu");
		var menu = this.getMenuoverview(), main = this.getMain();
		main.setActiveItem(menu);
	}
});

