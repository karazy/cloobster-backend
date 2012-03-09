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
			passwordField: 'passwordfield[name=password]',
			savePassword: 'togglefield[name=savePasswordToggle]'
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
		var accountLocalStore = Ext.data.StoreManager.lookup('cockpitStateStore'),
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
	   		 account = accountLocalStore.getAt(0);
	   		 this.setAccount(account);

	   		 //Set default headers so that always credentials are send
			Ext.Ajax.setDefaultHeaders({
				'login': account.get('login'),
				'passwordHash': account.get('passwordHash')
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
		accountLocalStore = Ext.data.StoreManager.lookup('cockpitStateStore'),
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
				// me.getAccount().set('login', record.get('login'));
				// me.getAccount().set('email', record.get('email'));
				// me.getAccount().set('role', record.get('role'));
				// me.getAccount().set('passwordHash', record.get('passwordHash'));

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

				//TODO remove in a more reliable way!
				//remove login view				
				Ext.Viewport.remove(Ext.Viewport.down('login'));
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