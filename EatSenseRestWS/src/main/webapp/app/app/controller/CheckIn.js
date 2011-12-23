Ext.define('EatSense.controller.CheckIn', {
    extend: 'Ext.app.Controller',
    config: {
        profile: Ext.os.deviceType.toLowerCase()
    },
    
	views : [
		'Main',
		'Dashboard',
		'MenuOverview',
		'Checkinconfirmation',
		'CheckinWithOthers'
	],
	stores : [
	'CheckIn',
	'User'
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
        	ref: 'userlist',
        	selector: '#checkinDlg2Userlist'
        }
    ],
    init: function() {
    	console.log('initialized CheckInController');
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
            }
        });
    	 
    	 var models = {};
    	 this.models = models;
    },
    /**
     * CheckIn Process
     * Step 1: barcode is scanned and send to server
     */    
    checkInIntent: function(options) {
    	console.log('CheckIn Controller -> checkIn');
    	var barcode = this.getSearchfield().getValue();
    	var that = this;
    	Ext.ModelManager.getModel('EatSense.model.CheckIn').load(barcode, {
    		synchronous: true,
    	    success: function(model) {
    	    	console.log("CheckInIntent Status: " + model.get('status'));
    	    	console.log("checkInIntent Restaurant: " + model.get('restaurantName'));    	    	
    	    	that.checkInConfirm({model:model});
    	    }
    	});
   },
   /**
    * CheckIn Process
    * Step 2: User gets asked if he wants to check in. He can then choose a nickname used during his checkIn.
    * @param options
    */
   checkInConfirm: function(options) {
	   console.log("CheckIn Controller -> checkInConfirm");
	   
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
	   var nickname = this.getNickname().getValue();
	   this.models.activeCheckIn.data.nickname = nickname;
	 //checkIn(String userId, String nickname)
	   var svrResponse = ""; 
	   this.models.activeCheckIn.save(
			   {
			   	    success: function(response) {
			   	     if(response.data.status == 'YOUARENOTALONE') {
			   			 //others are checked in at the same spot, present a list and ask if user wants to check in with another user
			   	    	 var userId = response.data.userId;
			   	    	 that.showCheckinWithOthers({userId : userId});
			   	       
			   	    /*	 
			   	    	Ext.Ajax.request({
			   	    	    url: '/restaurant/spot/users/',
			   	    	    method: 'GET',
			   	    	    params: {
			   	    	        userId: response.data.userId
			   	    	    },
			   	    	    success: function(response){
			   	    	    	var userList = Ext.decode(response.responseText);
			   	    	    	linkToUser({
			   	    	    		userId :  userId,
			   	    	    		userList : userList
			   	    	    	});
			   	    	    }
			   	    	});
			   			   */
			   		   } else {
			   			   //show menu
			   			   that.showMenu();
			   		   }
			   	    }
			   }	   
	   );
	  
	   
	 
	   //show Menu
   },
 
   cancelCheckIn: function(options) {
	   console.log("CheckIn Controller -> cancelCheckIn");
	   options.model.destroy();	   
   },
   
   showCheckinWithOthers: function(options) {
	   var checkinwithothersDlg = this.getCheckinwithothers(), main = this.getMain();
	   
	    var userListStore = Ext.create('Ext.data.Store', {
	   			   model: 'User',
	   			   proxy: {
	   				   type: 'rest',
	   				   url : '/restaurant/spot/users?userId'+options.userId,
	   				   reader: {
	   					   type: 'json'
	   			   		},
	   			   }
	   		   });
	     //set list content in view	  
	  	 getUserList().store = userListStore; 
	  	   
	  	 getUserList().store.load({
	   	       scope   : this,
	   	       callback: function(records, operation, success) {
	   	    	   //show view
	   	    	   main.setActiveItem(checkinwithothersDlg);
	   	       }
	   	 });
   },
   linkToUser: function(options) {
	   var menu = this.getMenuoverview(), main = this.getMain();
	   
		main.setActiveItem(menu);
		
   },
	showMenu : function() {
		console.log("CheckIn Controller -> showMenu");
		var menu = this.getMenuoverview(), main = this.getMain();
		main.setActiveItem(menu);
	}
});

