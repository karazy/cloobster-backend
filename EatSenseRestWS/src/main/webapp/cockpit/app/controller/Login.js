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
			}
		},		
		refs: {
			loginButton: 'login button[action=login]',
			logoutButton: 'button[action=logout]',
			loginField: 'textfield[name=login]',
			passwordField: 'passwordfield[name=password]',
			savePassword: 'togglefield[name=savePasswordToggle]'
		},		

		account : Ext.create('EatSense.model.Account'),
	},

	init: function() {
		console.log('init');


		//private functions
		this.resetAccountProxyHeaders =  function() {
			console.log('resetAccountProxyHeaders');
			EatSense.model.Account.getProxy().setHeaders({});
	 	}
	},

	/**
	* 	Restores saved credentials from local webstorage.
	* 	@return
	*		true if restore was successful, false otherwise
	*/
	restoreCredentials: function() {
		Ext.Logger.info('restoreCredentials');
		console.log('restoreCredentials');
		var accountLocalStore = Ext.data.StoreManager.lookup('cockpitStateStore'),
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

			Karazy.channel.createChannel( {
				token: account.get('token'), 
				messageHandler: spotCtr.loadSpots,
				scope: spotCtr
			});

	   		return true;	   		 	   		
	   	 } else {
	   	 	accountLocalStore.removeAll();	
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

		var account, 
		login = this.getLoginField().getValue(),
		password = this.getPasswordField().getValue(),
		accountLocalStore = Ext.data.StoreManager.lookup('cockpitStateStore'),
		spotCtr = this.getApplication().getController('Spot'),
		me = this;

		if(Ext.String.trim(login).length == 0 || Ext.String.trim(password).length == 0) {
			
			Ext.Msg.alert(i18nPlugin.translate('error'), i18nPlugin.translate('needCredentials')); 
			return;
		}

		EatSense.model.Account.getProxy().setHeaders({
				//provide credentials, they will be added to request header and deleted from params object
				'login': login,
				'password': password
		});

		EatSense.model.Account.load(login, {
			success: function(record, operation){
				console.log('success');
				me.setAccount(record);

				//Set default headers so that always credentials are send
				Ext.Ajax.setDefaultHeaders({
					'login': login,
					'passwordHash': record.get('passwordHash')
				});				

				me.resetAccountProxyHeaders();

				if(me.getSavePassword().getValue() === 1) {
					me.getAccount().setDirty();
					accountLocalStore.add(me.getAccount());
					accountLocalStore.sync();
				}

				Karazy.channel.createChannel( {
					token: account.get('token'), 
					messageHandler: spotCtr.loadSpots,
					scope: spotCtr
				});

				// channel = new goog.appengine.Channel(me.getAccount().get('token'));
			 //    socket = channel.open();
			 // 	socket.onopen = function() {
			 // 		console.log('open channel');
			 // 		//Do something?
			 // 	};
			 //    socket.onmessage = function(data) {
			 //    	// var status = Ext.decode(data);
			 //    	// if(status == 'ORDER_PLACED') {
			 //    		spotCtr.loadSpots();
			 //    	// }			    	
				// };
			 //    socket.onerror = function(error) {
			 //    	console.log('error in channel');
			 //    	//TODO request new token
			 //    };
			 //    socket.onclose = function() {
			 //    	console.log('close channel');
			 //    	//TODO request new token
			 //    };

				//TODO remove in a more reliable way!
				//remove login view				
				Ext.Viewport.remove(Ext.Viewport.down('login'));
				//show main view				
				Ext.create('EatSense.view.Main');
				spotCtr.loadSpots();
			},
			failure: function(record, operation){
				console.log('failure');
					Ext.Msg.alert(i18nPlugin.translate('error'), i18nPlugin.translate('wrongCredentials')); 
			}
		});
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
						//accountLocalStore.sync();

						Ext.Ajax.setDefaultHeaders({});	

						//TODO remove in a more reliable way!
						//remove login view				
						Ext.Viewport.remove(Ext.Viewport.down('main'));
						//show main view				
						Ext.create('EatSense.view.Login');		
					};
				}
		});
	},

	onMessage: function(data) {
		console.log('received a channel message '+data);
	}


});