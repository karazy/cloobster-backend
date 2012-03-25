/*
* The login controller handles login, registering a new account ...
*/
Ext.define('EatSense.controller.Login', {
	extend: 'Ext.app.Controller',
	config: {
		control: {
			loginButton: {
				tap: 'login'
			},
			logoutButton: {
				tap: 'showLogoutDialog'
			},
		 	businessList: {
		 		itemtap: 'setBusinessId'
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

		account : Ext.create('EatSense.model.Account'),
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
		Ext.Logger.info('restoreCredentials');
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
					'passwordHash': account.get('passwordHash')
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
					failure: function(record, operation){					
						//error verifying credentials, maybe account changed on server or server ist not aaccessible
						me.resetDefaultAjaxHeaders();
						Ext.create('EatSense.view.Login');
						//TODO handle 401 unauthorized

						me.getLoginField().setValue(account.get('login'));

						Ext.Msg.alert(i18nPlugin.translate('error'), i18nPlugin.translate('restoreCredentialsErr')); 
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
		Ext.Logger.info('login');
		console.log('login');

		var 	me = this,
				login = this.getLoginField().getValue(),
				password = this.getPasswordField().getValue(),				
				spotCtr = this.getApplication().getController('Spot'),
				me = this;

		if(Ext.String.trim(login).length == 0 || Ext.String.trim(password).length == 0) {
			
			Ext.Msg.alert(i18nPlugin.translate('error'), i18nPlugin.translate('needCredentials')); 
			return;
		}

		EatSense.model.Account.getProxy().setHeaders({
				//provide credentials, they will be added to request header
				'login': login,
				'password': password
		});

		EatSense.model.Account.load(login, {
			success: function(record, operation){
				console.log('success');
				me.setAccount(record);
				//generate clientId for channel
				me.getAccount().set('clientId', me.getAccount().get('login') + new Date().getTime());

				//Set default headers so that always credentials are send
				Ext.Ajax.setDefaultHeaders({
					'login': login,
					'passwordHash': record.get('passwordHash')
				});				

				me.resetAccountProxyHeaders();
				me.showBusinesses();				
			},
			failure: function(record, operation){
				console.log('failure');
				Ext.Msg.alert(i18nPlugin.translate('error'), i18nPlugin.translate('wrongCredentials')); 
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
				title: i18nPlugin.translate('hint'),
				message: i18nPlugin.translate('logoutQuestion'),
				buttons: [{
					text: i18nPlugin.translate('yes'),
					itemId: 'yes',
					ui: 'action'
				}, {
					text:  i18nPlugin.translate('no'),
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
		console.log('request new token');

		var 	account = this.getAccount(),
				login = account.get('login'),
				clientId = account.get('clientId');

		Ext.Ajax.request({
		    url: 'accounts/'+login+'/tokens',		    
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
	*	Called after receiving a channel message.
	*	
	*	@param rawMessages	
	*		The raw string message(s) which will be parsed as JSON.
	*		This could be a single object or an array.
	*/
	// processMessages: function(rawMessages) {
	// 	var 	message = Ext.JSON.decode(rawMessages, true),
	// 			ctr;

	// 	if(Ext.isArray(message)) {
	// 			for(index = 0; index < message.length; index++) {
	// 			if(message[index]) {
	// 				this.routeMessage(message[index]);
	// 			}	
	// 		}
	// 	}
	// 	else if(message) {
	// 		this.routeMessage(message);
	// 	}				
	// },
	/**
	*	Processes a single message delivered.
	*	Delegates to the responsible method.
	*
	*	@param message	
	*		A message consists of 3 fields
	*			type	- a type like spot
	*			action	- an action like update, new ...
	*			content - the data
	*/
	// routeMessage: function(message) {
	// 	var 	ctr;

	// 	if(!message) {
	// 		console.log('param is no message');
	// 		return;
	// 	}	

	// 	switch(message.type.toLowerCase()) {
	// 		case 'spot': 
	// 			ctr = this.getApplication().getController('Spot');
	// 			if(message.action == 'update') {
	// 				ctr.updateSpotIncremental(message.content);
	// 			}
	// 			break;
	// 		case 'checkin':
	// 			ctr = this.getApplication().getController('Spot');
	// 			ctr.updateSpotDetailCheckInIncremental(message.action, Ext.create('EatSense.model.CheckIn',message.content));
	// 			break;
	// 		case 'order':
	// 			ctr = this.getApplication().getController('Spot');
	// 			ctr.updateSpotDetailOrderIncremental(message.action, message.content);
	// 			break;
	// 		case 'bill':

	// 			break;
	// 		default: console.log('unmapped message.type');
	// 	}


	// },

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
			 			Ext.Msg.alert(i18nPlugin.translate('error'), i18nPlugin.translate('noBusinessAssigned'), Ext.emptyFn);
			 		}

			 		if(records.length > 1) {
			 			//more than one assigned business exists. show chooseBusiness view
			 			loginPanel.setActiveItem(1);
			 		} else if(records.length == 1){
			 			account.set('businessId', records[0].get('id'));
			 			account.set('business', records[0].get('name'));						
			 			me.saveAppState();

			 			Ext.Viewport.remove(Ext.Viewport.down('login'));
			 			//show main view				
						Ext.create('EatSense.view.Main');
						spotCtr.loadSpots();

						me.openChannel();						
			 		} 

			 	} else {
			 		//TODO user can't log in because he is not assigned to a business
			 		loginPanel.setActiveItem(0);
			 		// Ext.Msg.alert(i18nPlugin.translate('error'), i18nPlugin.translate('errorSpotLoading'), Ext.emptyFn);
			 	}				
			 },
			 scope: this
		});
	},

	/**
	*	Sets the businessId in account the user wants to log in for.
	*	This Id will be used for calls to the webservice.
	* 	e.g. /restaurants/{id}/spots
	*	
	*/
	setBusinessId: function(dv, index, target, record) {
		console.log('setBusiness');
		var 	me = this,
				account = this.getAccount(),
				spotCtr = this.getApplication().getController('Spot'); 

		account.set('businessId', record.get('id'));
		account.set('business', record.get('name'));
		me.saveAppState();

		Ext.Viewport.remove(Ext.Viewport.down('login'));
		Ext.create('EatSense.view.Main');
		spotCtr.loadSpots();

		me.openChannel();		
	}


});