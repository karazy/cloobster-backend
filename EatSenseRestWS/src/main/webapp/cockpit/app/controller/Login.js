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
				tap: 'logout'
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
	* 	Restores saved credentials from local webstorage.
	* 	@return
	*		true if restore was successful, false otherwise
	*/
	restoreCredentials: function() {
		Ext.Logger.info('restoreCredentials');
		console.log('restoreCredentials');
		var 	me = this,
				accountLocalStore = Ext.data.StoreManager.lookup('cockpitStateStore'),
				spotCtr = this.getApplication().getController('Spot'),
				account;

		if(!accountLocalStore) {
			return false;
		}

	   	 try {
	   		accountLocalStore.load();
	   	 } catch (e) {
	   	 	console.log('Failed restoring cockpit state.');
	   		accountLocalStore.removeAll();	
	   		accountLocalStore.removeAll();
	   	 	Ext.create('EatSense.view.Login');	   		
	   		return false;
	   	 }

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

					//set account data to update record? 
					//me.setAccount(record);
					
					//generate token for channel
					account.set('token', account.get('login') + new Date());

					Ext.create('EatSense.view.Main');
					spotCtr.loadSpots();
					me.openChannel();
					return true;
				},
				failure: function(record, operation){					
					//error verifying credentials, maybe account changed on server or server ist not aaccessible
					me.resetDefaultAjaxHeaders();
					Ext.create('EatSense.view.Login');
					//TODO handle 401 unauthorized

					me.getLoginField().setValue(account.get('login'));

					Ext.Msg.alert(i18nPlugin.translate('error'), i18nPlugin.translate('restoreCredentialsErr')); 
					return false;
				}
			});							   			   		 	   		
	   	 } else {
	   	 	//more than one local account exists. That should not happen!
	   	 	accountLocalStore.removeAll();
	   	 	Ext.create('EatSense.view.Login');	
	   	 	return false;
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
				account, 
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
				//generate token for channel
				account.set('token', account.get('login') + new Date());

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
		var accountLocalStore = Ext.data.StoreManager.lookup('cockpitStateStore');

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
				token = account.get('token');

		Ext.Ajax.request({
		    url: 'accounts/'+login+'/tokens',		    
		    method: 'POST',
		    params: {
		    	'businessId' :  this.getAccount().get('businessId'),
		    	'token' : token
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
		var		me = this;

		me.requestNewToken(function(newToken) {
			Karazy.channel.createChannel( {
				token: newToken, 
				messageHandler: me.routeMessage,
				requestTokenHandler: me.requestNewToken,
				messageHandlerScope: me,
				requestTokenHandlerScope: me
			});
		});
	}, 
	/**
	*	Called after receiving a channel message.
	*	Delegates to the responsible method.
	*
	*	@param rawMessage	
	*		The raw string message which will be parsed as JSON
	*/
	routeMessage: function(rawMessage) {
		var 	message = Ext.JSON.decode(rawMessage, true),
				ctr;

		if(message) {
			if(message.type == 'spot') {
				ctr = this.getApplication().getController('Spot');
				if(message.action == 'update') {
					ctr.updateSpotIncremental(message.content);
				}
			}
		}
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