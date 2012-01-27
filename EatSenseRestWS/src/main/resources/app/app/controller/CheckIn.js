/**
 * Controller handles the checkin process.
 * This includes scanning of a barcode, chosing a nickname, checking in with others
 * and finally navigating to the menu.
 * 
 */
Ext.define('EatSense.controller.CheckIn', {
    extend: 'Ext.app.Controller',
    config: {
        profile: Ext.os.deviceType.toLowerCase()
    },
    
	views : [
		'Main',
		'Dashboard',		
		'Checkinconfirmation',
		'CheckinWithOthers',
		'MenuOverview'
	],
	stores : [
	'CheckIn',
	'User',
	'Menu'
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
        	ref: 'checkinconfirmation',
        	selector: 'checkinconfirmation'
        },
        {
        	ref: 'nickname',
        	selector : '#nicknameTf'
        },
        {
        	ref: 'menuoverview',
        	selector: 'menuoverview'
        }, 
        {
        	ref: 'checkinwithothers',
        	selector: 'checkinwithothers'
        },
        {
        	ref: 'dashboard',
        	selector: 'dashboard'
        },
        {
        	ref: 'userlist',
        	selector: '#checkinDlg2Userlist'
        },
        {
        	ref: 'checkInDlg1Label1',
        	selector: '#checkInDlg1Label1'
        },    
        {
        	ref: 'cancelCheckInBt',
        	selector: '#cancelCheckInBt'
        },
        {
        	ref: 'menulist',
        	selector: '#menulist'
        }
        
    ],
    init: function() {
    	console.log('initialized CheckInController');
    	//ONLY CREATE ONCE!!!
    	this.getMainView().create();
    	 this.control({
            '#checkInBtn': {
                tap: this.checkInIntent
            },
            '#confirmCheckInBt': {
            	tap: this.checkIn
            }, 
            '#checkinDlg2Userlist': {
            	select: this.linkToUser
            },
            '#checkinDlg2CancelBt': {
            	tap: this.showMenu
            },
            '#cancelCheckInBt': {
            	tap: this.cancelCheckIn
            }
            
        });
    	 
    	 var models = {};
    	 this.models = models;
    	 
    	 //private functions
    	 
    	 //called by checkInIntent. 
    	 this.doCheckInIntent = function(barcode, button) {
    	    	//validate barcode field
    	    	if(barcode.length == 0) {
    	    		this.getDashboard().setMask(false);
    	    		Ext.Msg.alert(i18nPlugin.translate('errorTitle'), i18nPlugin.translate('checkInErrorBarcode'), Ext.emptyFn);
    	    	} else {
    	        	var that = this;
    	        	Ext.ModelManager.getModel('EatSense.model.CheckIn').load(barcode, {
    	        	    success: function(model) {
    	        	    	console.log("CheckInIntent Status: " + model.get('status'));
    	        	    	console.log("checkInIntent Restaurant: " + model.get('restaurantName'));    	  
    	        	    	if(model.data.status == "INTENT") {
    	        	    		that.checkInConfirm({model:model});
    	        	    	} else if(model.data.status == "BARCODE_ERROR") {
    	        	    		Ext.Msg.alert(i18nPlugin.translate('errorTitle'), i18nPlugin.translate('checkInErrorBarcode'), Ext.emptyFn);
    	        	    	} else {
    	        	    		Ext.Msg.alert(i18nPlugin.translate('errorTitle'), i18nPlugin.translate('errorMsg'), Ext.emptyFn);
    	        	    	}    	        	    	
    	        	    },
    	        	    callback: function() {
    	        	    	that.getDashboard().setMask(false);
    	        	    	button.enable();
    	        	    }
    	        	    
    	        	});
    	    	}
    	 };
    },
    /**
     * CheckIn Process
     * Step 1: barcode is scanned and send to server
     */    
    checkInIntent: function(button) {
    	console.log('CheckIn Controller -> checkIn');
    	//disable button to prevent multiple checkins
    	button.disable();
    	var barcode, that = this;
    	if(this.getProfile() == 'desktop' || !window.plugins.barcodeScanner) {
    		barcode = Ext.String.trim(this.getSearchfield().getValue());    
    		this.getDashboard().setMask(true);
    		this.doCheckInIntent(barcode, button);
    	} else if(this.getProfile() == 'phone' || this.getProfile() == 'tablet') {
    			window.plugins.barcodeScanner.scan(function(result, barcode) {
    			barcode = result.text;
    			that.getDashboard().setMask(true);
    			that.doCheckInIntent(barcode, button);
    		}, function(error) {
    			Ext.Msg.alert("Scanning failed: " + error, Ext.emptyFn);
    		});
    	} else {
    		button.enable();
    	}
    	

   },
   /**
    * CheckIn Process
    * Step 2: User gets asked if he wants to check in. He can then choose a nickname used during his checkIn.
    * @param options
    */
   checkInConfirm: function(options) {
	   console.log("CheckIn Controller -> checkInConfirm");
	  
	   //'Do you want to check in at <strong>'+options.model.data.spot+' at '+options.model.data.restaurantName+'</strong>'	   
	   this.getCheckInDlg1Label1().setHtml(i18nPlugin.translate('checkInStep1Label1', options.model.data.spot, options.model.data.restaurantName));
		var checkInDialog = this.getCheckinconfirmation(), main = this.getMain();
		this.getNickname().setValue(options.model.data.nickname);
		this.models.activeCheckIn = options.model;
		main.setActiveItem(checkInDialog);	     
   },
   /**
    * CheckIn Process
    * Step 3: User confirmed his wish to check in
    * @param options
    */
   checkIn: function(){
	   var that = this;
	   //get CheckIn Object and save it. 
	   var nickname = Ext.String.trim(this.getNickname().getValue());
	   if(nickname.length < 3) {
		   Ext.Msg.alert(i18nPlugin.translate('errorTitle'), i18nPlugin.translate('checkInErrorNickname',3,25), Ext.emptyFn);
	   } else {
		   this.models.activeCheckIn.data.nickname = nickname;
			 //checkIn(String userId, String nickname)
			   this.models.activeCheckIn.save(
					   {
					   	    success: function(response) {
					   	     if(response.data.status == 'YOUARENOTALONE') {
					   			 //others are checked in at the same spot, present a list and ask if user wants to check in with another user
					   	    	 var userId = response.data.userId;
					   	    	 that.showCheckinWithOthers({userId : userId});
					   		   }
					   		   else if(response.data.status == 'CHECKEDIN') {
					   			   that.showMenu();
					   		   }
					   		   else if(response.data.status == 'VALIDATION_ERROR') {
					   			 Ext.Msg.alert(i18nPlugin.translate('errorTitle'), i18nPlugin.translate(response.raw.error.errorKey,response.raw.error.substitutions), Ext.emptyFn);
					   		   }
					   		   else {
					   			Ext.Msg.alert(i18nPlugin.translate('errorTitle'), i18nPlugin.translate('errorMsg'), Ext.emptyFn);
					   		   }
					   	    }
					   }	   
			   );
	   }

	  
	   
	 
	   //show Menu
   },
   /**
    * CheckIn Process
    * Step 2 alt: cancel process
    */
   cancelCheckIn: function(options) {
	   console.log("CheckIn Controller -> cancelCheckIn");
	   var dashboardView = this.getDashboard(), main = this.getMain();
	   this.models.activeCheckIn.destroy();	   
	   main.setActiveItem(dashboardView);
   },
   /**
    * CheckIn Process
    * Step 4: List other users located at this spot
    * @param options
    */
   showCheckinWithOthers: function(options) {
	   console.log("CheckIn Controller -> showCheckinWithOthers");
	   var checkinwithothersDlg = this.getCheckinwithothers(), main = this.getMain();
	   
	    var userListStore = Ext.create('Ext.data.Store', {
	   			   model: 'EatSense.model.User',
	   			   proxy: {
	   				   type: 'rest',
	   				   url : globalConf.serviceUrl+'/restaurant/spot/users?userId='+options.userId,
	   				   reader: {
	   					   type: 'json'
	   			   		}
	   			   }
	   		   });
	     //set list content in view	  
	  	 this.getUserlist().setStore(userListStore); 
	  	 this.getUserlist().getStore().load({
	  	     scope   : this,
	  	     callback: function(records, operation, success) {
	  	     //the operation object contains all of the details of the load operation
	  	     console.log(records);
	  	     }
	  	     });
	  	main.setActiveItem(checkinwithothersDlg);
   },
   /**
    * CheckIn Process
    * Step 4-I: Link user to a chosen person 
    * @param dataview
    * @param record
    */
   linkToUser: function(dataview, record) {
	   console.log("CheckIn Controller -> linkToUser");
	   
    	Ext.Ajax.request({
    	    url: globalConf.serviceUrl+'/restaurant/spot/users',
    	    method: 'POST',
    	    scope: this,
    	    params: {
    	        userId: this.models.activeCheckIn.data.userId,
    	        linkedUserId: record.data.userId
    	    },
    	    success: function(response){
    	    	this.showMenu();
    	    }
    	});
		
   },
   /**
    * CheckIn Process
    * Step 5: Show menu to user 
    * @param dataview
    * @param record
    */
	showMenu : function() {
		console.log("CheckIn Controller -> showMenu");
		 var menu = this.getMenuoverview(), main = this.getMain(), restaurantId = Ext.String.trim(this.models.activeCheckIn.data.restaurantId), that = this;
		 if(restaurantId.toString().length != 0) {
			 //load menudata and store it in MenuController
			 var menuListStore = Ext.create('Ext.data.Store', {
	 			   model: 'EatSense.model.Menu',
	 			   proxy: {
	 				   type: 'rest',
	 				   url : globalConf.serviceUrl+'/restaurant/'+restaurantId+'/menu',
	 				   reader: {
	 					   type: 'json'
	 			   		}
	 			   }
	 		 });
			 this.getMenulist().setStore(menuListStore);
			 this.getMenulist().getStore().load({
				 scope   : this,
			     callback: function(records, operation, success) {
			    	 if(success) {
			    	 that.getController('Menu').models.menudata = records;		
			    	 main.setActiveItem(menu);			    	 
			    	 }
			     }
			 });
		 }
	}
});
