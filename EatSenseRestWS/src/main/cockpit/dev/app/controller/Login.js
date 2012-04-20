/*
* The login controller handles login, registering a new account ...
*/
Ext.define('EatSense.controller.Login', {
	extend: 'Ext.app.Controller',
	requires: ['EatSense.model.Account', 'Ext.data.proxy.LocalStorage'],
	config: {
		control: {
			loginButton: {
				tap: 'login'
			},
			logoutButton: {
				tap: 'showLogoutDialog'
			},
		 	businessList: {
		 		itemtap: 'chooseBusiness'
		 	},
		 	cancelLoginButton: {
	 			tap: 'cancelLogin'
		 	}
		},		
		refs: {
			loginPanel: 'login',
			loginButton: 'login button[action=login]',
			logoutButton: 'button[action=logout]',
			loginField: 'textfield[name=login]',
			passwordField: 'passwordfield[name=password]',
			savePassword: 'login togglefield[name=savePasswordToggle]',
			businessList: 'choosebusiness list',
			cancelLoginButton: 'choosebusiness button[action=cancel]',			
		},		

		account : {} //Ext.create('EatSense.model.Account'),
	},

	init: function() {
		console.log('init');
		var		me = this;


		//private functions
		/*
		*	Resets Account proxy headers to prevent passwort from being send plain.
		*/
		this.resetAccountProxyHeaders =  function() {
			console.log('resetAccountProxyHeaders');
			EatSense.model.Account.getProxy().setHeaders({});
	 	};
	 	/*
		*	Resets default Ajax headers.
		*/
	 	this.resetDefaultAjaxHeaders = function() {
	 		Ext.Ajax.setDefaultHeaders({});
	 	};

	 	/*
	 	*	Save application state by using localstorage when getSavePassword is checked.
	 	*/
	 	this.saveAppState = function() {
	 		var 	accountLocalStore = Ext.data.StoreManager.lookup('cockpitStateStore');

			if(me.getSavePassword().getValue() === 1) {
				me.getAccount().setDirty();
				accountLocalStore.add(me.getAccount());
				accountLocalStore.sync();
			} else {
				accountLocalStore.removeAll();
				accountLocalStore.sync();
			};
	 	};
	 	/*
	 	*	Reset login fields.
	 	*/
	 	this.resetLoginFields = function() {
	 		me.getLoginField().setValue("");
	 		me.getPasswordField().setValue("");
	 	};
	},
	/**
	* 	Tries to restore saved credentials from local webstorage.
	*	If this fails login screen is shown.
	*/
	restoreCredentials: function() {
		console.log('restoreCredentials');
		var 	me = this,
				accountLocalStore = Ext.data.StoreManager.lookup('cockpitStateStore'),
				spotCtr = this.getApplication().getController('Spot'),
				account;

	   	 try {

	   			accountLocalStore.load();	   	
		   	 if(accountLocalStore.getCount() == 1) {
		   		 console.log('app state found');
		   		 account = accountLocalStore.first();
		   		 this.setAccount(account);

		   		 //Set default headers so that always credentials are send
				Ext.Ajax.setDefaultHeaders({
					'login': account.get('login'),
					'passwordHash': account.get('passwordHash'),
					'pathId' : account.get('businessId')
				});

				//check if saved credentials are valid
				EatSense.model.Account.load(account.get('login'), {
					success: function(record, operation){
						//credentials are valid, proceed

						//generate clientId for channel
						account.set('clientId', account.get('login') + new Date().getTime());

						//ToDo check if business still exists

						Ext.create('EatSense.view.Main');
						spotCtr.loadSpots();
						me.openChannel();
					},
					failure: function(record, operation) {					
						//error verifying credentials, maybe account changed on server or server ist not aaccessible
						me.resetDefaultAjaxHeaders();
						me.resetAccountProxyHeaders();

						Ext.create('EatSense.view.Login');
						//TODO handle 401 unauthorized

						me.getLoginField().setValue(account.get('login'));


						if(operation.error) {
							//not authorized
							if(operation.error.status == "401" || operation.error.status == "403") {
								errorMessage = Karazy.i18n.translate('restoreCredentialsErr');
								//login data not valid. delete
								accountLocalStore.removeAll();
								accountLocalStore.sync();
							} else if (operation.error.status == "404") {
								errorMessage = Karazy.i18n.translate('resourceNotAvailable');
							}
						} 

						(!errorMessage || errorMessage == "") ?	errorMessage = Karazy.i18n.translate('restoreCredentialsErr') : errorMessage;

						Ext.Msg.alert(Karazy.i18n.translate('error'), errorMessage); 
					}
				});							   			   		 	   		
		   	 } else {
		   	 	//more than one local account exists. That should not happen!
		   	 	accountLocalStore.removeAll();
		   	 	Ext.create('EatSense.view.Login');	
		   	 }
	   	  } catch (e) {
	   	 	console.log('Failed restoring cockpit state.');
	   		accountLocalStore.removeAll();	
	   	 	Ext.create('EatSense.view.Login');	   		
	   	 }
	},
 	/**
 	*	Action called from login button.
 	*	Reads login fields and makes an login attempt. If request is successfull,
 	* 	main application screen is shown.
 	*	If user sets automatic login then credentials will be saved in localstorage.
 	*
 	*/
	login: function() {
		console.log('login');

		var 	me = this,
				login = this.getLoginField().getValue(),
				password = this.getPasswordField().getValue(),				
				spotCtr = this.getApplication().getController('Spot'),
				me = this,
				errorMessage;

		if(Ext.String.trim(login).length == 0 || Ext.String.trim(password).length == 0) {
			
			Ext.Msg.alert(Karazy.i18n.translate('error'), Karazy.i18n.translate('needCredentials')); 
			return;
		}

		EatSense.model.Account.getProxy().setHeaders({
				//provide credentials, they will be added to request header
				'login': login,
				'password': password
		});

		EatSense.model.Account.load(login, {
			success: function(record, operation){
				me.setAccount(record);
				//generate clientId for channel
				me.getAccount().set('clientId', me.getAccount().get('login') + new Date().getTime());

				//Set default headers so that always credentials are send
				Ext.Ajax.setDefaultHeaders({
					'login': login,
					'passwordHash': record.get('passwordHash'),
				});				

				me.resetAccountProxyHeaders();
				me.showBusinesses();				
			},
			failure: function(record, operation){
				me.resetAccountProxyHeaders();
				me.resetDefaultAjaxHeaders();
				if(operation.error) {
					//not authorized
					if(operation.error.status == "401" || operation.error.status == "403") {
						errorMessage = Karazy.i18n.translate('wrongCredentials');
					} else if (operation.error.status == "404") {
						errorMessage = Karazy.i18n.translate('resourceNotAvailable');
					}
				} 

				(!errorMessage || errorMessage == "") ?	errorMessage = Karazy.i18n.translate('wrongCredentials') : errorMessage;			
				Ext.Msg.alert(Karazy.i18n.translate('error'), errorMessage); 
			}
		});
	},
	/**
	*	Cancel login process in choose business view.
	*
	*/
	cancelLogin: function() {
		var		me = this,
				loginPanel = this.getLoginPanel();

		loginPanel.setActiveItem(0);
		me.resetLoginFields();

		Ext.Ajax.setDefaultHeaders({});	
		me.setAccount({});
		me.resetAccountProxyHeaders();		
	},
	/**
	*	Logout signed in user and show login screen.
	*	Removes credentials.
	*	
	*/
	logout: function() {
		console.log('logout');
		var 	accountLocalStore = Ext.data.StoreManager.lookup('cockpitStateStore');
		
		Karazy.channel.closeChannel();
		//remove all stored credentials
		accountLocalStore.removeAll();
		accountLocalStore.sync();

		Ext.Ajax.setDefaultHeaders({});	

		//TODO remove in a more reliable way!
		//remove main view				
		Ext.Viewport.remove(Ext.Viewport.down('main'));
		//show main view				
		Ext.create('EatSense.view.Login');		

	},
	/**
	*	Displays a logout dialog and logs user out if he confirms.
	*	
	*/
	showLogoutDialog: function() {
		var 	me = this;

			Ext.Msg.show({
				title: Karazy.i18n.translate('hint'),
				message: Karazy.i18n.translate('logoutQuestion'),
				buttons: [{
					text: Karazy.i18n.translate('yes'),
					itemId: 'yes',
					ui: 'action'
				}, {
					text:  Karazy.i18n.translate('no'),
					itemId: 'no',
					ui: 'action'
				}],
				scope: this,
				fn: function(btnId, value, opt) {
					if(btnId=='yes') {
						me.logout();	
					};
				}
		});
	},
	/**
	*	Requests a new token from server and executes the given callback with new token as parameter.
	*	@param callback
	*		callback function to invoke on success
	*/
	requestNewToken: function(callback) {		
		var 	account = this.getAccount(),
				login = account.get('login'),
				clientId = login + new Date().getTime();
		
		account.set('clientId', clientId);
		console.log('request new token. clientId: ' + clientId);
		Ext.Ajax.request({
		    url: Karazy.config.serviceUrl+'/accounts/'+login+'/tokens',		    
		    method: 'POST',
		    params: {
		    	'businessId' :  this.getAccount().get('businessId'),
		    	'clientId' : clientId
		    },
		    success: function(response){
		       	token = response.responseText;
		       	callback(token);
		    }, 
		    failure: function(response) {
		    	Ext.Msg.alert(Karazy.i18n.translate('error'), Karazy.i18n.translate('channelTokenError')); 
		    }
		});
	},
	/**
	* 	Requests a token and
	*	opens a channel for server side push messages.
	*
	*/
	openChannel: function() {
		var		me = this,
				messageCtr = this.getApplication().getController('Message');

		me.requestNewToken(function(newToken) {
			Karazy.channel.createChannel( {
				token: newToken, 
				messageHandler: messageCtr.processMessages,
				requestTokenHandler: me.requestNewToken,
				messageHandlerScope: messageCtr,
				requestTokenHandlerScope: me
			});
		});
	}, 
	/**
	*	Loads all businesses (e. g. restaurants or hotels) this user account is assigned to.
	*
	*/
	showBusinesses: function() {
		console.log('showBusinesses');
		var 	me = this,
				businessStore = Ext.StoreManager.lookup('businessStore'),
				account = this.getAccount(),
				spotCtr = this.getApplication().getController('Spot'),
				loginPanel = this.getLoginPanel();

		Ext.create('EatSense.view.ChooseBusiness');

		this.getBusinessList().getStore().load({
			params: {
				pathId: account.get('login')
			},
			callback: function(records, operation, success) {
			 	if(success) {

			 		if(!records || records.length == 0) {
			 			loginPanel.setActiveItem(0);
			 			Ext.Msg.alert(Karazy.i18n.translate('error'), Karazy.i18n.translate('noBusinessAssigned'), Ext.emptyFn);
			 		}

			 		if(records.length > 1) {
			 			//more than one assigned business exists. show chooseBusiness view
			 			loginPanel.setActiveItem(1);
			 		} else if(records.length == 1){
			 			me.setBusinessId(records[0]);					
			 		} 

			 	} else {
			 		//TODO user can't log in because he is not assigned to a business
			 		loginPanel.setActiveItem(0);
			 		// Ext.Msg.alert(Karazy.i18n.translate('error'), Karazy.i18n.translate('errorSpotLoading'), Ext.emptyFn);
			 	}				
			 },
			 scope: this
		});
	},

	/**
	*	Sets the businessId in account the user wants to log in for.
	*	This Id will be used for calls to the webservice.
	* 	e.g. /businesses/{id}/spots
	*
	*	
	*/
	setBusinessId: function(record) {
		var 	me = this,
				account = this.getAccount(),
				spotCtr = this.getApplication().getController('Spot'); 

		account.set('businessId', record.get('id'));
		account.set('business', record.get('name'));
		
		//set pathId in default Ajax headers to avoid setting it with every request
		Ext.Ajax.getDefaultHeaders().pathId = account.get('businessId');

		me.saveAppState();

		Ext.Viewport.remove(Ext.Viewport.down('login'));
		Ext.create('EatSense.view.Main');
		spotCtr.loadSpots();

		me.openChannel();		
	},
	/**
	*	Event handler for choose business list tap.
	*	
	*/
	chooseBusiness: function(dv, index, target, record) {
		this.setBusinessId(record);
	}


});