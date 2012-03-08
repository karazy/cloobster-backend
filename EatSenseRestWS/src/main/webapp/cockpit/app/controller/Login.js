/*
* The login controller handles login, as well as 
* registering a new account ...
*/
Ext.define('EatSense.controller.Login', {
	extend: 'Ext.app.Controller',
	config: {
		control: {
			loginButton: {
				tap: 'login'
			}
		},		
		refs: {
			loginButton: 'login button[action=login]',
			loginField: 'textfield[name=login]',
			passwordField: 'passwordfield[name=password]'
		},		

		account : Ext.create('EatSense.model.Account')
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
	* 	
	*/
	restoreCredentials: function() {
		Ext.Logger.info('restoreCredentials');
		console.log('restoreCredentials');
		var accountLocalStore = Ext.data.StoreManager.lookup('appStateStore'),
		account;

		if(!accountLocalStore) {
			return false;
		}

	   	 try {
	   		accountLocalStore.load();
	   	 } catch (e) {
	   		accountLocalStore.removeAll();
	   		return false;
	   	 }

	   	 if(accountLocalStore.getCount() == 1) {
	   		 console.log('app state found');
	   		 account = accountLocalStore.getAt(0);
	   		 this.setAccount(account);

	   		 //Set default headers so that always credentials are send
			Ext.Ajax.setDefaultHeaders({
				'login': login,
				'passwordHash': record.get('passwordHash')
			});

	   		 return true;	   		 	   		
	   	 } else {
	   	 	accountLocalStore.removeAll();	
	   	 	return false;
	   	 }
	},

	login: function() {
		Ext.Logger.info('login');
		console.log('login');

		var account, 
		login = this.getLoginField().getValue(),
		password = this.getPasswordField().getValue(),
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

				me.this.resetAccountProxyHeaders();

				//show main view				
				Ext.create('EatSense.view.Main');
			},
			failure: function(record, operation){
				console.log('failure');
					Ext.Msg.alert(i18nPlugin.translate('error'), i18nPlugin.translate('wrongCredentials')); 
			}
		});
	},


});