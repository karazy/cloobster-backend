/**
 * Controller handles the checkin process.
 * This includes scanning of a barcode, chosing a nickname, checking in with others
 * and finally navigating to the menu.
 * 
 */
Ext.define('EatSense.controller.CheckIn', {
    extend: 'Ext.app.Controller',
    requires: ['Ext.data.proxy.LocalStorage', 'EatSense.controller.Message', 'EatSense.view.About'],
    config: {
        profile: Ext.os.deviceType.toLowerCase(),
    	refs: {
            main : 'mainview',
            checkinconfirmation : 'checkinconfirmation',
        	nickname : 'checkinconfirmation #nicknameTf',
        	checkinwithothers: 'checkinwithothers',
        	dashboard: 'dashboard',
        	settingsBt: 'dashboard button[action=settings]',
          aboutBt: 'dashboard button[action=about]',
        	settingsBackBt: 'settings button[action=back]',
        	nicknameTogglefield: 'checkinconfirmation togglefield[action=toggle-nickname]',
        	nicknameSettingsField: 'settings #nicknameSetting',
        	settingsview: 'settings',
        	
        	checkInDlg1Label1: 'checkinconfirmation #checkInDlg1Label1',    	       
        	           	
        	checkInBtn: 'dashboard button[action=checkin]',
            //confirm checkn view
            cancelCheckInBt: 'checkinconfirmation button[action=cancel-checkin]',           
        	confirmCheckInBt : 'checkinconfirmation button[action=confirm-checkin]',
            regenerateNicknameBt : 'checkinconfirmation button[action=regenerate-nickname]',
            //checkIn w/ others
            userlist: '#checkinDlg2Userlist',
        	checkinDlg2Userlist: '#checkinDlg2Userlist',
        	checkinDlg2CancelBt : '#checkinDlg2CancelBt',
            //loungeview and tabs
            loungeview : 'lounge',
        	menuTab: 'menutab',
        	cartTab: 'carttab',
            settingsTab: 'settingstab',
            requestsTab: 'requeststab',
    	},
    	control: {
    		checkInBtn: {
                tap: 'checkInIntent'
            },
            confirmCheckInBt: {
            	tap: 'checkIn'
            }, 
            checkinDlg2Userlist: {
            	select: 'linkToUser'
            },
            checkinDlg2CancelBt: {
            	tap: 'showLounge'
            },
            cancelCheckInBt: {
            	tap: 'showDashboard'
            },
            regenerateNicknameBt: {
            	tap: 'generateNickname'
            },
            aboutBt: {
              tap: 'showAbout'
            },
            settingsBt: {
            	tap: 'showSettings'
            },
            settingsBackBt: {
            	tap: 'showDashboard'            	
            },
            nicknameSettingsField: {            	
            	change: 'saveNickname'
            }
    	},
        /**
      	* Contains information to resume application state after the app was closed.
      	*/
      	appState : Ext.create('EatSense.model.AppState', {id: '1'}),
        /**
        *   Active checkIn for this session. Used througout whole application
        */
        activeCheckIn: null,
        /**
        *   The spot the activeCheckIn is assigned to.
        */
        activeSpot: null
    },
    init: function() {
    	var messageCtr = this.getApplication().getController('Message');
    	 
    	this.on('statusChanged', this.handleStatusChange, this);
      this.getApplication().on('statusChanged', this.handleStatusChange, this);
    	messageCtr.on('eatSense.checkin', this.handleCheckInMessage, this);

    	 //private functions

        /*
        *   Resets default Ajax headers.
        */
        this.resetDefaultAjaxHeaders = function() {
            Ext.Ajax.setDefaultHeaders({});
        };
    	 
    	//called by checkInIntent. 
    	this.doCheckInIntent = function(barcode, button, deviceId) {    		 
    	    	//validate barcode field
    	    	if(barcode.length == 0) {
    	    		this.getDashboard().showLoadScreen(false);
    	    		button.enable();
    	    		Ext.Msg.alert(Karazy.i18n.translate('errorTitle'), Karazy.i18n.translate('checkInErrorBarcode'), Ext.emptyFn);
    	    	} else {
    	        	var me = this;
    	        	EatSense.model.Spot.load(barcode, {
    	        		 success: function(record, operation) {
    	        			 me.setActiveSpot(record);
    	        			 me.checkInConfirm({model:record, deviceId : deviceId}); 	        	    	
     	        	    },
     	        	    failure: function(record, operation) {
                            me.getApplication().handleServerError({
                                'error': operation.error,
                                'message': {
                                    404: Karazy.i18n.translate('checkInErrorBarcode')
                                } 
                            }); 
     	        	    },
     	        	    callback: function() {
     	        	    	me.getDashboard().showLoadScreen(false);
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
    	var barcode, that = this, deviceId;
    	if(this.getProfile() == 'desktop' || !window.plugins || !window.plugins.barcodeScanner) {
            Ext.Msg.show({
                title: Karazy.i18n.translate('barcodePromptTitle'),
                message: Karazy.i18n.translate('barcodePromptText'),
                buttons: [{
                    text: Karazy.i18n.translate('yes'),
                    itemId: 'yes',
                    ui: 'action'
                }, {
                    text: Karazy.i18n.translate('no'),
                    itemId: 'no',
                    ui: 'action'
                }],
                prompt : { maxlength : 20},
                scope: this,
                fn: function(btnId, value, opt) {
                    if(btnId=='yes') {
                        barcode = Ext.String.trim(value);    
                        deviceId = '_browser';
                        this.getDashboard().showLoadScreen(true);
                        this.doCheckInIntent(barcode, button, deviceId);
                    } else {
                        button.enable();
                    }
                }
            }); 
            // barcode = Ext.String.trim(this.getSearchfield().getValue());
    	} else if(this.getProfile() == 'phone' || this.getProfile() == 'tablet') {
    			window.plugins.barcodeScanner.scan(function(result) {
            if(!result.cancelled) {
              barcode = result.text;
              console.log('scanned ' + barcode);
              that.getDashboard().showLoadScreen(true);
              //FR 28.03.12 apple rejects apps which track device uuid
              // deviceId = device.uuid;
              that.doCheckInIntent(barcode, button, deviceId);
            } else {
              console.log('user cancelled scan');
              button.enable();
            }
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
	  var checkInDialog = this.getCheckinconfirmation(), 
		    main = this.getMain(),
        nicknameToggle = this.getNicknameTogglefield(),
		    checkIn = Ext.create('EatSense.model.CheckIn');		
			
	   	 if(this.getAppState().get('nickname') != null && Ext.String.trim(this.getAppState().get('nickname')) != '') {
	   		 this.getNickname().setValue(this.getAppState().get('nickname'));
         nicknameToggle.setValue(1);
	   	 } else {
	   		this.generateNickname();
	   	 }
		
		checkIn.set('spotId', options.model.get('barcode'));
		checkIn.set('businessName', options.model.get('business'));
		checkIn.set('businessId', options.model.get('businessId'));
		checkIn.set('spot', options.model.get('name'));
		checkIn.set('status','INTENT');
		
		if(options.deviceId) {
			//store device uuid
			checkIn.set('deviceId',options.deviceId);
		}			
		this.setActiveCheckIn(checkIn);
		
		main.switchAnim('left');
		main.setActiveItem(checkInDialog);	  			
   },
   /**
    * CheckIn Process
    * Step 3: User confirmed his wish to check in
    * @param options
    */
   checkIn: function(){
	   var     me = this,
	           nickname = Ext.String.trim(this.getNickname().getValue()),
	           nicknameToggle = this.getNicknameTogglefield(),
             messageCtr = this.getApplication().getController('Message'),
             checkInDialog = this.getCheckinconfirmation();

	 //get CheckIn Object and save it.	   
	   if(nickname.length < 3) {
		   Ext.Msg.alert(Karazy.i18n.translate('errorTitle'), Karazy.i18n.translate('checkInErrorNickname',3,25), Ext.emptyFn);
	   } else {		   
          checkInDialog.showLoadScreen(true);
		      this.getActiveCheckIn().set('nickname',nickname);		  	   
		      this.getActiveCheckIn().save(
					   {
					   	    success: function(response) {
                    checkInDialog.showLoadScreen(false);
  					   	    console.log("CheckIn Controller -> checkIn success");
  					   	    //currently disabled, will be enabled when linking to users actually makes sense
                    //me.showCheckinWithOthers();					   	    
  					   	     me.showLounge();
  					   	     me.getAppState().set('checkInId', response.get('userId'));

                    //Set default headers so that always checkInId is send
                    Ext.Ajax.setDefaultHeaders({
                        'checkInId': response.get('userId'),
                        'pathId' : me.getActiveCheckIn().get('businessId')
                    });
  					   	     
  					   	    //save nickname in settings
  							   if(nicknameToggle.getValue() == 1) {
  								   me.getAppState().set('nickname', nickname);
  								   nicknameToggle.reset();
  							   }
                   //open a channel for push messags
                   try {
                        messageCtr.openChannel(response.get('userId'));
                    } catch(e) {
                        console.log('could not open a channel ' + e);
                    }
					   	    },
					   	    failure: function(response, operation) {
                    checkInDialog.showLoadScreen(false);
                    me.getApplication().handleServerError({
                      'error': operation.error, 
                      'forceLogout':{403 : true}
                    }); 
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
	   var dashboardView = this.getDashboard(),
	       main = this.getMain(),
	       nicknameToggle = this.getNicknameTogglefield();
	   
	   this.setActiveCheckIn(null);
	   	   
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
	   //TODO out of order
	   var checkinwithothersDlg = this.getCheckinwithothers(), 
	   main = this.getMain(),
	   spotId = this.getActiveCheckIn().get('spotId'),
	   checkInId = this.getActiveCheckIn().get('userId');
	   
	    var userListStore = Ext.create('Ext.data.Store', {
	   			   model: 'EatSense.model.User',
	   			   proxy: {
	   				   type: 'rest',
	   				   url : Karazy.config.serviceUrl+'/checkins/?spotId='+spotId+'&checkInId='+checkInId,
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
				  		this.showLounge();
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
	   //TODO out of order
	   var checkIn = this.getActiveCheckIn(),
	   me = this;	   
	   checkIn.set('linkedCheckInId', record.get('userId'));
	   
	   checkIn.save({
		  scope: this,
		  success: function(record, operation) {
			  me.showLounge();
		  },
		   failure: function(record, operation) {
   	    	if(operation.getError() != null && operation.getError().status != null && operation.getError().status == 500) {
   	    		var error = Ext.JSON.decode(response.statusText);
   	    		Ext.Msg.alert(Karazy.i18n.translate('errorTitle'), Karazy.i18n.translate(error.errorKey,error.substitutions), Ext.emptyFn);
   	    	} else {
   	    		Ext.Msg.alert(Karazy.i18n.translate('errorTitle'), Karazy.i18n.translate('errorMsg'), Ext.emptyFn);
   	    	}
		   }
	   });
   },
   /**
    *
    * Show menu to user 
    */
	showLounge: function() {
    	var menuCtr = this.getApplication().getController('Menu'),
          requestCtr = this.getApplication().getController('Request'),
          androidCtr = this.getApplication().getController('Android');

        menuCtr.showMenu();
        requestCtr.refreshAccountLabel();
        androidCtr.setAndroidBackHandler(menuCtr.getMenuNavigationFunctions());
	},
  /**
  * Shows an about screen.
  */
  showAbout: function() {
    var about = Ext.create('EatSense.view.About');

    Ext.Viewport.add(about);

  },
	/**
	 * Show settings screen.
	 * 
	 */
	showSettings: function() {
		var   main = this.getMain(),
		      settings = this.getSettingsview();
		
		this.getNicknameSettingsField().setValue(this.getAppState().get('nickname'));
		
		main.switchAnim('left');
   	 	main.setActiveItem(settings);
	},
	/**
	 * Saves the application state in local store.
	 */
	saveNickname: function(component, newData, oldValue, eOpts) {
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
    	    url: Karazy.config.serviceUrl+'/nicknames',
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
		var me = this,
        main = this.getMain(),
		    orderCtr = this.getApplication().getController('Order'),
        messageCtr = this.getApplication().getController('Message'),
        requestCtr = this.getApplication().getController('Request');

        this.setActiveCheckIn(checkIn);
        //reload of application before hitting leave button
        if(checkIn.get('status') == Karazy.constants.PAYMENT_REQUEST) {
            console.log('PAYMENT_REQUEST already issued. Don\'t restore state!');
            this.handleStatusChange(Karazy.constants.COMPLETE);
            this.setActiveCheckIn(null);
            return;
        }

		
        //Set default headers so that always checkInId is send
        Ext.Ajax.setDefaultHeaders({
            'checkInId': checkIn.get('userId'),
            'pathId' : checkIn.get('businessId')
        });              

		//load active spot
		EatSense.model.Spot.load(checkIn.get('spotId'), {
		scope: this,
   		 success: function(record, operation) {
   			 this.setActiveSpot(record);
   			 this.showLounge();
   			    			
   			Ext.Viewport.add(main);
   			
   			//after spot information is restored and stores are initialized load orders
   			
   			this.getActiveCheckIn().orders().load({
   				scope: this,
   				params: {
   					'status': Karazy.constants.Order.CART,
   				},
   				callback: function(records, operation, success) {
   					if(success == true) {
   						orderCtr.refreshCart();
              orderCtr.refreshMyOrdersList();
   					}
   				}						
   			});

         //open a channel for push messags
         try {
              messageCtr.openChannel(checkIn.get('userId'));
          } catch(e) {
              console.log('could not open a channel ' + e);
          }
  	    },
  	    failure: function(record, operation) {
  	    	me.getApplication().handleServerError({
                  'error':operation.error
              });        	    	
  	    }
		});	
    //restore existing requests
    requestCtr.loadRequests();					
	},	
	/**
	 * This method handle status changes. It checks if valid transsions are made.
	 * E. g. You cannot switch from PAYMENT_REQUEST to ORDER_PLACED.
	 * It enables or disbales certain functionalities depending on the status.
     * Furthermore resets ui states and does cleanups.
	 * Always use this method to change the application status. 
	 * @param status
	 */
	handleStatusChange: function(status) {
		console.log('CheckIn Controller -> handleStatusChange' + ' new status '+status);
        var     orderCtr = this.getApplication().getController('Order'),
                menuCtr = this.getApplication().getController('Menu'),
                settingsCtr = this.getApplication().getController('Settings'),
                androidCtr = this.getApplication().getController('Android'),
                requestCtr = this.getApplication().getController('Request'),
                menuStore = Ext.StoreManager.lookup('menuStore');
		//TODO check status transitions, refactor     
				
		if(status == Karazy.constants.PAYMENT_REQUEST) {
			this.getMenuTab().disable();
			this.getCartTab().disable()
            this.getSettingsTab().disable();
            this.getRequestsTab().disable();
			
			this.getActiveCheckIn().set('status', status);
		} else if (status == Karazy.constants.COMPLETE || status == Karazy.constants.CANCEL_ALL || status == Karazy.constants.FORCE_LOGOUT) {
			this.showDashboard();
            this.getMenuTab().enable();
			this.getCartTab().enable();
            this.getSettingsTab().enable();
            this.getRequestsTab().enable();			
			this.getLoungeview().setActiveItem(this.getMenuTab());
            menuCtr.backToMenu();
		    	//remove menu to prevent problems on reload
            menuStore.removeAll();
            orderCtr.refreshCartBadgeText(true);
            orderCtr.refreshMyOrdersBadgeText(true);
            this.getAppState().set('checkInId', null);
            this.resetDefaultAjaxHeaders();
            Karazy.channel.closeChannel();

            requestCtr.resetAllRequests();
            androidCtr.setAndroidBackHandler(null);
		}

    if(status == Karazy.constants.CANCEL_ALL) {
        Ext.Msg.alert(Karazy.i18n.translate('hint'), Karazy.i18n.translate('checkInCanceled'), Ext.emptyFn);
    }
	},
    /**
    *   Handle push notifications for checkins.
    *
    */
    handleCheckInMessage: function(action, updatedCheckIn) {
        var     checkIn = this.getActiveCheckIn();

        if(action == "delete") {
            if(checkIn.get('userId') == updatedCheckIn.userId) {
                this.fireEvent('statusChanged', Karazy.constants.CANCEL_ALL);
            }
        }
    }

});

