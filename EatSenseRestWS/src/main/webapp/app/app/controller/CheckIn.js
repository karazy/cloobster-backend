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
    	        	settingsBt: 'dashboard button[action=settings]',
    	        	settingsBackBt: 'settings button[action=back]',
    	        	nicknameTogglefield: 'checkinconfirmation togglefield[name=nicknameToggle]',
    	        	nicknameSettingsField: 'settings #nicknameSetting',
    	        	settingsview: 'settings',
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
    	        	regenerateNicknameBt : '#regenerateNicknameBt',
    	        	menuTab: '#menutab',
    	        	cartTab: '#carttab'    	        		
    	        },
    	        /**
    	    	  * Contains information to resume application state after the app was closed.
    	    	  */
    	 appState : Ext.create('EatSense.model.AppState', {id: '1'})
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
            	tap: this.showDashboard
            },
            regenerateNicknameBt: {
            	tap: this.generateNickname
            },
            settingsBt: {
            	tap: this.showSettings
            },
            settingsBackBt: {
            	tap: this.showDashboard            	
            },
            nicknameSettingsField: {            	
            	change: this.saveNickname
            }
        });
    	 
    	 var models = {};
    	 this.models = models;

    	 
    	 this.on('statusChanged', this.handleStatusChange, this);
    	 
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
    	        			 me.models.activeSpot = record;
    	        			 me.checkInConfirm({model:record, deviceId : deviceId}); 	        	    	
     	        	    },
     	        	    failure: function(record, operation) {     	        	    	
     	        	    	if(operation.getError() != null && operation.getError().status != null && operation.getError().status == 404) {
     	        	    		Ext.Msg.alert(i18nPlugin.translate('errorTitle'), i18nPlugin.translate('checkInErrorBarcode'), Ext.emptyFn);
     	        	    	} else {
     	        	    		Ext.Msg.alert(i18nPlugin.translate('errorTitle'), i18nPlugin.translate('errorMsg'), Ext.emptyFn);
     	        	    	}     	        	    	
     	        	    },
     	        	    callback: function() {
     	        	    	me.getDashboard().showLoadScreen(false);
     	        	    	button.enable();
     	        	    }
    	        	});
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
    		 
 
        //setup store for products	 
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
   	
		 
  		//setup store for orders
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
		 
		 //setup store for bills
		 var BillType = Ext.ModelManager.getModel('EatSense.model.Bill');
		 if(BillType.getProxy() == null) {
			 console.log('create bill store');
			 var billStore =	 Ext.create('Ext.data.Store', {
	 			   model: 'EatSense.model.Bill',
	 			   storeId: 'billStore',
	 			   proxy: {
	 				   type: 'rest',
	 				  enablePagingParams: false,
	 				   url : '/restaurants/'+restaurantId+'/bills',
	 				   reader: {
	 					   type: 'json'
	 			   		}
	 			   }
	 		 });
			 BillType.setProxy(billStore.getProxy());	 
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
    	if(this.getProfile() == 'desktop' || !window.plugins || !window.plugins.barcodeScanner) {
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
	   var checkInDialog = this.getCheckinconfirmation(), 
		main = this.getMain(),
		checkIn = Ext.create('EatSense.model.CheckIn');
	   
	   //this.getCheckInDlg1Label1().setHtml(i18nPlugin.translate('checkInStep1Label1', options.model.get('name'), options.model.get('restaurant')));
		
			
	   	 if(this.getAppState().get('nickname') != null && Ext.String.trim(this.getAppState().get('nickname')) != '') {
	   		 this.getNickname().setValue(this.getAppState().get('nickname'));
	   	 } else {
	   		this.generateNickname();
	   	 }
		
		checkIn.set('spotId', options.model.get('barcode'));
		checkIn.set('restaurantName', options.model.get('restaurant'));
		checkIn.set('restaurantId', options.model.get('restaurantId'));
		checkIn.set('spot', options.model.get('name'));
		checkIn.set('status','INTENT');
		
		if(options.deviceId) {
			//store device uuid
			checkIn.set('deviceId',options.deviceId);
		}			
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
	   var me = this,
	   nickname = Ext.String.trim(this.getNickname().getValue()),
	   error,
	   nicknameToggle = this.getNicknameTogglefield();
	    
	 //get CheckIn Object and save it.	   
	   if(nickname.length < 3) {
		   Ext.Msg.alert(i18nPlugin.translate('errorTitle'), i18nPlugin.translate('checkInErrorNickname',3,25), Ext.emptyFn);
	   } else {		   
		   this.models.activeCheckIn.set('nickname',nickname);		  	   
			this.models.activeCheckIn.save(
					   {
					   	    success: function(response) {
					   	    console.log("CheckIn Controller -> checkIn success");
					   	    //currently disabled, will be enabled when linking to users actually makes sense
//					   	     me.showCheckinWithOthers();					   	    
					   	     me.showMenu();
					   	     me.getAppState().set('checkInId', response.get('userId'));
					   	     
					   	     //save nickname in settings
							   if(nicknameToggle.getValue() == 1) {
								   me.getAppState().set('nickname', nickname);
								   nicknameToggle.reset();
							   }						   	     	
					   	    },
					   	    failure: function(response, operation) {
					   	    	console.log('checkIn failure');					   	    	
				    	    	if(operation.getError() != null && operation.getError().status != null && operation.getError().status == 500) {
				    	    		try {
				    	    			//TODO Bug in error message handling in some browsers
										error = Ext.JSON.decode(operation.getError().statusText);
										Ext.Msg.alert(i18nPlugin.translate('errorTitle'), i18nPlugin.translate(error.errorKey,error.substitutions), Ext.emptyFn);
									} catch (e) {
										Ext.Msg.alert(i18nPlugin.translate('errorTitle'), i18nPlugin.translate('checkInErrorNicknameExists'), Ext.emptyFn);
									}
				    	    		
				    	    	} else {
				    	    		Ext.Msg.alert(i18nPlugin.translate('errorTitle'), i18nPlugin.translate('errorMsg'), Ext.emptyFn);
				    	    	}
					   	    }
					   }	   
			   );
	   }
   },
   /**
    * CheckIn Process
    * Step 2 alt: cancel process
    */
   showDashboard: function(options) {
	   console.log("CheckIn Controller -> showDashboard");
	   var dashboardView = this.getDashboard(),
	   main = this.getMain(),
	   nicknameToggle = this.getNicknameTogglefield();
	   
	   this.models.activeCheckIn = null;
	   	   
	   main.switchAnim('right');
	   main.setActiveItem(dashboardView);
	   nicknameToggle.reset();
		
	   //ensure that main is only added once to viewport
	   if(main.getParent() !== Ext.Viewport) {
		   Ext.Viewport.add(main);
	   }
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
			  	   main.switchAnim('left');
				  	if(records.length > 0) {
				  		main.setActiveItem(checkinwithothersDlg);
				  	} else {
				  		this.showMenu();
				  	}
	  	     }
	  	 });	  		  	
   },
   /**
    * CheckIn Process
    * Step 4-I: Link user to a chosen person 
    * @param dataview
    * @param record
    */
   linkToUser: function(dataview, record) {
	   console.log("CheckIn Controller -> linkToUser");
	   var checkIn = this.models.activeCheckIn,
	   me = this;	   
	   checkIn.set('linkedCheckInId', record.get('userId'));
	   
	   checkIn.save({
		  scope: this,
		  success: function(record, operation) {
			  me.showMenu();
		  },
		   failure: function(record, operation) {
   	    	if(operation.getError() != null && operation.getError().status != null && operation.getError().status == 500) {
   	    		var error = Ext.JSON.decode(response.statusText);
   	    		Ext.Msg.alert(i18nPlugin.translate('errorTitle'), i18nPlugin.translate(error.errorKey,error.substitutions), Ext.emptyFn);
   	    	} else {
   	    		Ext.Msg.alert(i18nPlugin.translate('errorTitle'), i18nPlugin.translate('errorMsg'), Ext.emptyFn);
   	    	}
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
			    	 }
			     }
			 });
			 
			 menu.hideBackButton();
	    	 main.switchAnim('left');
	    	 main.setActiveItem(lounge);
		 }
	},
	/**
	 * Show settings screen.
	 * 
	 */
	showSettings: function() {
		console.log('CheckIn Controller -> showSettings');
		var main = this.getMain(),
		settings = this.getSettingsview();
		
		this.getNicknameSettingsField().setValue(this.getAppState().get('nickname'));
		
		main.switchAnim('left');
   	 	main.setActiveItem(settings);
	},
	/**
	 * Saves the application state in local store.
	 */
	saveNickname: function(component, newData, oldValue, eOpts) {
		console.log('CheckIn Controller -> saveNickname '+newData);
		this.getAppState().set('nickname', newData);
	},
	/**
	 * Makes an ajax call to the server, retrieves a random nickname
	 * and sets the nickname field.
	 * 
	 * ®return
	 * 		the nickname
	 */
	generateNickname : function(callback) {
		Ext.Ajax.request({
    	    url: '/nicknames',
    	    method: 'GET',
    	    scope: this,
    	    params: {
    	        random: ""
    	    },
    	    success: function(response){
    	    	this.getNickname().setValue(response.responseText);
    	    }
    	});		
	},
	/**
	 * This method is called from launch function during application start 
	 * when an existing checkin was found. This could happen when a user exits
	 * the application during a checkin and restarts.
	 * The method makes sure that all relevant information is restored like products in cart,
	 * or the active spot.
	 * 
	 * @param checkin
	 * 		Restored checkin
	 */
	restoreState: function(checkIn) {
		var main = this.getMain(),
		orderCtr = this.getApplication().getController('Order');
		
		this.models.activeCheckIn = checkIn;
		
	
		
		//load active spot
		EatSense.model.Spot.load(checkIn.get('spotId'), {
		scope: this,
   		 success: function(record, operation) {
   			 this.models.activeSpot = record;   			 
   			 this.showMenu();
   			    			
   			Ext.Viewport.add(main);
   			
   			//after spot information is restored and stores are initialized load orders
   			
   			this.models.activeCheckIn.orders().load({
   				scope: this,
   				params: {
   					'status': Karazy.constants.Order.CART,
   					'checkInId': this.models.activeCheckIn.getId()
   				},
   				callback: function(records, operation, success) {
   					if(success == true) {
   						orderCtr.refreshCart();
   					}
   				}						
   			});
    	    },
    	    failure: function(record, operation) {
    	    	//TODO show error message that  restoring data failed
    	    	if(operation.getError() != null && operation.getError().status != null && operation.getError().status == 404) {
    	    		Ext.Msg.alert(i18nPlugin.translate('errorTitle'), i18nPlugin.translate('checkInErrorBarcode'), Ext.emptyFn);
    	    	} else {
    	    		Ext.Msg.alert(i18nPlugin.translate('errorTitle'), i18nPlugin.translate('errorMsg'), Ext.emptyFn);
    	    	}     	        	    	
    	    },
    	    callback: function() {
    	    	
    	    }
		});		
		
		
	},	

	/**
	 * This method handle status changes. It checks if valid transsions are made.
	 * E. g. You cannot directly switch from PAYMENT_REQUEST to INTENT.
	 * It enables or disbales certain functionalities depending on the status.
	 * Always use this method to change the application status. 
	 * @param status
	 */
	handleStatusChange: function(status) {
		console.log('CheckIn Controller -> handleStatusChange' + ' new status '+status);
		//TODO check status transsions
				
		if(status == Karazy.constants.PAYMENT_REQUEST) {
			this.getMenuTab().disable();
			this.getCartTab().disable();
			
			this.models.activeCheckIn.set('status', status);
		} else if (status == Karazy.constants.COMPLETE) {
			this.getMenuTab().enable();
			this.getCartTab().enable();
			this.getAppState().set('checkInId', null);
			this.getLoungeview().setActiveItem(this.getMenuTab());
			
			this.showDashboard();
			
		}
		
				
		
	}

});

