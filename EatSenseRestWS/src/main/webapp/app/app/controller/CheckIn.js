/**
 * Controller handles the checkin process.
 * This includes scanning of a barcode, chosing a nickname, checking in with others
 * and finally navigating to the menu.
 * 
 */
Ext.define('EatSense.controller.CheckIn', {
    extend: 'Ext.app.Controller',
    requires: ['EatSense.data.proxy.CustomRestProxy'],
    config: {
        profile: Ext.os.deviceType.toLowerCase(),
        
    	refs: 
    	        {
    	            main : 'mainview',
    	            searchfield : 'dashboard textfield',
    	            checkinconfirmation : 'checkinconfirmation',
    	        	nickname : '#nicknameTf',
    	        	menuoverview: 'menuoverview',    	   
    	        	checkinwithothers: 'checkinwithothers',
    	        	dashboard: 'dashboard',
    	        	userlist: '#checkinDlg2Userlist',
    	        	checkInDlg1Label1: '#checkInDlg1Label1',    	       
    	        	cancelCheckInBt: '#cancelCheckInBt',    	       
    	        	menulist: 'lounge #menulist',
    	        	menuview: 'menu',
    	        	loungeview : 'lounge',
    	        	myorderlist: '#myorderlist',
    	        	checkInBtn: '#checkInBtn',
    	        	confirmCheckInBt : '#confirmCheckInBt',
    	        	checkinDlg2Userlist: '#checkinDlg2Userlist',
    	        	checkinDlg2CancelBt : '#checkinDlg2CancelBt',
    	        	cancelCheckInBt : '#cancelCheckInBt',
    	        	regenerateNicknameBt : '#regenerateNicknameBt'
    	        }   
    },
    init: function() {
    	console.log('initialized CheckInController');
    	 this.control({
    		 checkInBtn: {
                tap: this.checkInIntent
            },
            confirmCheckInBt: {
            	tap: this.checkIn
            }, 
            checkinDlg2Userlist: {
            	select: this.linkToUser
            },
            checkinDlg2CancelBt: {
            	tap: this.showMenu
            },
            cancelCheckInBt: {
            	tap: this.cancelCheckIn
            },
            regenerateNicknameBt: {
            	tap: this.generateNickname
            }
        });
    	 
    	 var models = {};
    	 this.models = models;
    	 
    	 //private functions
    	 
    	 //called by checkInIntent. 
    	 this.doCheckInIntent = function(barcode, button, deviceId) {
    	    	//validate barcode field
    	    	if(barcode.length == 0) {
    	    		this.getDashboard().showLoadScreen(false);
    	    		button.enable();
    	    		Ext.Msg.alert(i18nPlugin.translate('errorTitle'), i18nPlugin.translate('checkInErrorBarcode'), Ext.emptyFn);
    	    	} else {
    	        	var me = this;
    	        	EatSense.model.Spot.load(barcode, {
    	        		 success: function(record, operation) {	  
    	        			 me.checkInConfirm({model:record, deviceId : deviceId});
//     	        	    	if(model.data.status == "INTENT") {
     	        	    		
//     	        	    	} else if(model.data.status == "BARCODE_ERROR") {
//     	        	    		Ext.Msg.alert(i18nPlugin.translate('errorTitle'), i18nPlugin.translate('checkInErrorBarcode'), Ext.emptyFn);
//     	        	    	} else {
//     	        	    		Ext.Msg.alert(i18nPlugin.translate('errorTitle'), i18nPlugin.translate('errorMsg'), Ext.emptyFn);
//     	        	    	}    	        	    	
     	        	    },
     	        	    failure: function(record, operation) {
     	        	    	Ext.Msg.alert(i18nPlugin.translate('errorTitle'), i18nPlugin.translate('checkInErrorBarcode'), Ext.emptyFn);
     	        	    },
     	        	    callback: function() {
     	        	    	me.getDashboard().showLoadScreen(false);
     	        	    	button.enable();
     	        	    }
    	        	});
    	        	
//    	        	Ext.ModelManager.getModel('EatSense.model.CheckIn').load(barcode, {
//    	        	    success: function(model) {
//    	        	    	console.log("CheckInIntent Status: " + model.get('status'));
//    	        	    	console.log("checkInIntent Restaurant: " + model.get('restaurantName'));    	  
//    	        	    	if(model.data.status == "INTENT") {
//    	        	    		me.checkInConfirm({model:model, deviceId : deviceId});
//    	        	    	} else if(model.data.status == "BARCODE_ERROR") {
//    	        	    		Ext.Msg.alert(i18nPlugin.translate('errorTitle'), i18nPlugin.translate('checkInErrorBarcode'), Ext.emptyFn);
//    	        	    	} else {
//    	        	    		Ext.Msg.alert(i18nPlugin.translate('errorTitle'), i18nPlugin.translate('errorMsg'), Ext.emptyFn);
//    	        	    	}    	        	    	
//    	        	    },
//    	        	    callback: function() {
//    	        	    	me.getDashboard().showLoadScreen(false);
//    	        	    	button.enable();
//    	        	    }
//    	        	    
//    	        	});
    	    	}
    	 };
    	 
    	 /*
    	  * Sets up all necessary store which depend on the restaurant id.
    	  * This method is called once after checkin was successful.
    	  */
    	 this.createStores = function(restaurantId) {
    		 console.log('create menu store');
    		 var menusStore =	 Ext.create('Ext.data.Store', {
	 			   model: 'EatSense.model.Menu',
	 			   storeId: 'menuStore',
	 			   proxy: {
	 				   type: 'rest',
	 				   url : '/restaurants/'+restaurantId+'/menus',
	 				   reader: {
	 					   type: 'json'
	 			   		}
	 			   }
	 		 });
    		 
    		 this.getMenulist().setStore(menusStore);
    		 
      
  		 
//  		 var _productProxy = Ext.create('Ext.data.proxy.Rest', {
//				   type: 'rest',
//				   url : '/restaurants/'+restaurantId+'/products',
//				   reader: {
//					   type: 'json'
//			   		}
//	 		 });
//  		 
  		 var ProductType = Ext.ModelManager.getModel('EatSense.model.Product');
  		 if(ProductType.getProxy() == null) {
  			console.log('create product store');
  	 		 var _productListStore =	 Ext.create('Ext.data.Store', {
	 			   model: 'EatSense.model.Product',
	 			   storeId: 'productStore',
	 			   proxy: {
	 				   type: 'rest',
	 				   url : '/restaurants/'+restaurantId+'/products',
	 				   reader: {
	 					   type: 'json'
	 			   		},
	 			   		writer: {
	 			   			type: 'json',
	 			   			writeAllFields: true
	 			   		}
	 			   }
	 		 });
  			 
  			ProductType.setProxy(_productListStore.getProxy());
  		 }
  		 
  		 
   		 
   	
		 
//		 var _productProxy = Ext.create('Ext.data.proxy.Rest', {
//			   type: 'rest',
//			   url : '/restaurants/'+restaurantId+'/products',
//			   reader: {
//				   type: 'json'
//		   		}
// 		 });
//		 
		 var OrderType = Ext.ModelManager.getModel('EatSense.model.Order');
		 if(OrderType.getProxy() == null) {
			 console.log('create order store');
			 var _orderListStore =	 Ext.create('Ext.data.Store', {
	 			   model: 'EatSense.model.Order',
	 			   storeId: 'orderStore',
	 			   filters: [
	 			             {property: "status", value: "PLACED"}
	 			   ],
	 			   proxy: {
	 				   type: 'rest',
	 				  enablePagingParams: false,
	 				   url : '/restaurants/'+restaurantId+'/orders',
	 				   reader: {
	 					   type: 'json'
	 			   		}
	 			   }
	 		 });
			 
			 OrderType.setProxy(_orderListStore.getProxy());
			 if(this.getMyorderlist() != null && this.getMyorderlist() !== 'undefined') {
				 this.getMyorderlist().setStore(_orderListStore);
			 } else {
				 console.log('Could not access myorderlist.');
			 }
			 
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
    	var barcode, that = this, deviceId;
    	if(this.getProfile() == 'desktop' || !window.plugins.barcodeScanner) {
    		barcode = Ext.String.trim(this.getSearchfield().getValue());    		
    		deviceId = '_browser'; //just for testing
    		this.getDashboard().showLoadScreen(true);
    		this.doCheckInIntent(barcode, button, deviceId);
    	} else if(this.getProfile() == 'phone' || this.getProfile() == 'tablet') {
    			window.plugins.barcodeScanner.scan(function(result, barcode) {
    			barcode = result.text;
    			that.getDashboard().showLoadScreen(true);
    			deviceId = device.uuid;
    			that.doCheckInIntent(barcode, button, deviceId);
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
	   this.getCheckInDlg1Label1().setHtml(i18nPlugin.translate('checkInStep1Label1', options.model.get('name'), options.model.get('restaurant')));
		var checkInDialog = this.getCheckinconfirmation(), 
		main = this.getMain(),
		checkIn = Ext.create('EatSense.model.CheckIn');
		this.generateNickname();
		
		checkIn.set('spotId', options.model.get('barcode'));
		checkIn.set('restaurant', options.model.get('restaurant'));
		checkIn.set('restaurantId', options.model.get('restaurantId'));
		checkIn.set('spot', options.model.get('name'));
		checkIn.set('status','INTENT');
		
		if(options.deviceId) {
			//store device uuid
			checkIn.set('deviceId',options.deviceId);
//			options.model.data.deviceId = options.deviceId;
		}			
//		this.models.activeCheckIn = options.model;
		this.models.activeCheckIn = checkIn;
		main.switchAnim('left');
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
		   this.models.activeCheckIn.set('nickname',nickname);
			 //checkIn(String userId, String nickname)
			this.models.activeCheckIn.save(
					   {
						   scope: this,
					   	    success: function(response) {
					   	     
					   	     this.models.activeCheckIn.set('userId', response.responseText);
					   	     that.showCheckinWithOthers();
//					   	     if(response.data.status == 'YOUARENOTALONE') {
//					   			 //others are checked in at the same spot, present a list and ask if user wants to check in with another user
//					   	    	 var userId = response.data.userId;
//					   	    	 that.showCheckinWithOthers({userId : userId});
//					   		   }
//					   		   else if(response.data.status == 'CHECKEDIN') {
//					   			   that.showMenu();
//					   		   }
//					   		   else if(response.data.status == 'VALIDATION_ERROR') {
//					   			 Ext.Msg.alert(i18nPlugin.translate('errorTitle'), i18nPlugin.translate(response.data.error.errorKey,response.data.error.substitutions), Ext.emptyFn);
//					   		   }
//					   		   else {
//					   			Ext.Msg.alert(i18nPlugin.translate('errorTitle'), i18nPlugin.translate('errorMsg'), Ext.emptyFn);
//					   		   }
					   	    },
					   	    failure: function(response) {
					   	     if(response.data.status == 'VALIDATION_ERROR') {
					   			 Ext.Msg.alert(i18nPlugin.translate('errorTitle'), i18nPlugin.translate(response.data.error.errorKey,response.data.error.substitutions), Ext.emptyFn);
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
	   //this.models.activeCheckIn.erase();
	   //TODO Workaorund in B1 because delete is not working	   
//		Ext.Ajax.request({
//    	    url: globalConf.serviceUrl+'/restaurant/spot/'+this.models.activeCheckIn.userId,
//    	    method: 'DELETE',
//    	    scope: this,
//    	    success: function(response){
//    	    	console.log('Canceled checkin.');
//    	    }
//    	});
	   this.models.activeCheckIn = null;
	   main.switchAnim('right');
	   main.setActiveItem(dashboardView);
   },
   /**
    * CheckIn Process
    * Step 4: List other users located at this spot
    * @param options
    */
   showCheckinWithOthers: function(options) {
	   console.log("CheckIn Controller -> showCheckinWithOthers");
	   var checkinwithothersDlg = this.getCheckinwithothers(), 
	   main = this.getMain(),
	   spotId = this.models.activeCheckIn.get('spotId'),
	   checkInId = this.models.activeCheckIn.get('userId');
	   
	    var userListStore = Ext.create('Ext.data.Store', {
	   			   model: 'EatSense.model.User',
	   			   proxy: {
	   				   type: 'rest',
	   				   url : globalConf.serviceUrl+'/checkins/?spotId='+spotId+'&checkInId='+checkInId,
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
	  	main.switchAnim('left');
	  	if(userListStore.getCount() > 0) {
	  		main.setActiveItem(checkinwithothersDlg);
	  	} else {
	  		this.showMenu();
	  	}
	  	
   },
   /**
    * CheckIn Process
    * Step 4-I: Link user to a chosen person 
    * @param dataview
    * @param record
    */
   linkToUser: function(dataview, record) {
	   console.log("CheckIn Controller -> linkToUser");
	   var checkIn = this.models.activeCheckIn;
	   
	   checkIn.set('linkedCheckInId', record.get('userId'));
	   
	   checkIn.save({
		  scope: this,
		  success: function(record, operation) {
			  this.showMenu();
		  },
		   failure: function(record, operation) {
			   
		   }
	   });
	   
//    	Ext.Ajax.request({
//    	    url: globalConf.serviceUrl+'/restaurants/spot/users',
//    	    method: 'POST',
//    	    scope: this,
//    	    params: {
//    	        userId: this.models.activeCheckIn.data.userId,
//    	        linkedUserId: record.data.userId
//    	    },
//    	    success: function(response){
//    	    	this.showMenu();
//    	    }
//    	});
		
   },
   /**
    * CheckIn Process
    * Step 5: Show menu to user 
    * @param dataview
    * @param record
    */
	showMenu : function() {
		console.log("CheckIn Controller -> showMenu");
		
		this.createStores(this.models.activeCheckIn.get('restaurantId'));
		
		 var menu = this.getMenuview(), 
		 lounge = this.getLoungeview(),
		 main = this.getMain(), 
		 restaurantId = Ext.String.trim(this.models.activeCheckIn.data.restaurantId), 
		 me = this; 
		 
		 if(restaurantId.toString().length != 0) {
			 this.getMenulist().getStore().load({
				 scope   : this,
				 params: {
					 includeProducts : true
				 },
			     callback: function(records, operation, success) {
			    	 if(success) {
				    	 me.getApplication().getController('Menu').models.menudata = records;		
				    	 menu.hideBackButton();
				    	 main.switchAnim('left');
				    	 main.setActiveItem(lounge);			    	 
			    	 }
			     }
			 });
		 }
	},
	/**
	 * Makes an ajax call to the server and retrieves a random nickname.
	 * Automatically sets the nickname field.
	 * ®return
	 * 		the nickname
	 */
	generateNickname : function() {
		Ext.Ajax.request({
    	    url: '/nicknames',
    	    method: 'GET',
    	    scope: this,
    	    params: {
    	        random: ""
    	    },
    	    success: function(response){
    	    	this.getNickname().setValue(response.responseText);
    	    	return response.responseText;
    	    }
    	});
		
	}
	
});

