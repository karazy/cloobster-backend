/**
*	Handles customer requests like "Call Waiter".
*	
*/
Ext.define('EatSense.controller.Request',{
	extend: 'Ext.app.Controller',
	config: {
		refs: {
			callWaiterButton: 'requeststab button[action=waiter]',
			callWaiterLabel: 'requeststab #callWaiterLabel',
			accountLabel: 'requeststab #accountLabel'
		},
		control: {
			callWaiterButton: {
				tap: 'toggleCallWaiterRequest'
			}
		}
	},
	init: function() {
		var messageCtr = this.getApplication().getController('Message');

		messageCtr.on('eatSense.request', this.handleRequestMessage, this);
	},
	//<call-waiter-request>
	toggleCallWaiterRequest: function(button, event) {
		if(!button.mode || button.mode == 'call') {
			this.sendCallWaiterRequest(button, event);
		} else if (button.mode == 'cancel'){
			this.cancelCallWaiterRequest(button, event);
		}
	},
	/**
	*	Sends a call waiter request.
	*/
	sendCallWaiterRequest: function(button, event) {
		var 	me = this,
				request = Ext.create('EatSense.model.Request'),
				requestStore = Ext.StoreManager.lookup('requestStore'),
				label = this.getCallWaiterLabel(),
				checkInId = this.getApplication().getController('CheckIn').getActiveCheckIn().getId();
		
		console.log('Request Controller -> sendCallWaiterRequest');

		button.disable();
		button.mode = 'cancel';
		me.getCallWaiterButton().setText(Karazy.i18n.translate('cancelCallWaiterRequest'));
		label.setHtml(Karazy.i18n.translate('callWaiterCancelHint'));
		
		//TODO validate!?!?!

		request.set('type', Karazy.constants.Request.CALL_WAITER);
		//workaround to prevent sencha from sending phantom id
		request.setId('');

		requestStore.add(request);
		// requestStore.sync();

		request.save({
			success: function(record, operation) {
				button.enable();				
			},
			failure: function(record, operation) {				
				button.enable();
				button.mode = 'call';
				me.getCallWaiterButton().setText(Karazy.i18n.translate('callWaiterButton'));
				label.setHtml(Karazy.i18n.translate('callWaiterCallHint'));

				me.getApplication().handleServerError({
					'error': operation.error,
					'forceLogout': {403: true}
				});					
			}
		});

		//show success message to give user the illusion of success
		// Ext.Msg.show({
		// 	title : Karazy.i18n.translate('hint'),
		// 	message : Karazy.i18n.translate('requestCallWaiterSendMsd'),
		// 	buttons : []
		// });
		
		// Ext.defer((function() {
		// 	if(!Karazy.util.getAlertActive()) {
		// 		Ext.Msg.hide();
		// 	}
		// }), Karazy.config.msgboxHideLongTimeout, this);
	},
	cancelCallWaiterRequest: function(button, event) {
		var me = this,
			label = this.getCallWaiterLabel(),
			requestStore = Ext.StoreManager.lookup('requestStore'),
			request;

		console.log('Request Controller -> cancelCallWaiterRequest');

		request = requestStore.findRecord('type', Karazy.constants.Request.CALL_WAITER, false, true, true);

		if(request) {
			button.disable();
			button.mode = 'call';
			me.getCallWaiterButton().setText(Karazy.i18n.translate('callWaiterButton'));
			label.setHtml(Karazy.i18n.translate('callWaiterCallHint'));

			// requestStore.setSyncRemovedRecords(true);
			requestStore.remove(request);
			// requestStore.sync();
			// requestStore.setSyncRemovedRecords(false);

			//try catch is due to android aborting the action because sencha throws a warning which causes and undefined error
			try {
				request.erase({
					callback: function(record, operation) {
						button.enable();
					},
					failure: function(record, operation) {
						if(operation.error.status != 404) {
							button.enable();
							button.mode = 'cancel';
							me.getCallWaiterButton().setText(Karazy.i18n.translate('cancelCallWaiterRequest'));
							label.setHtml(Karazy.i18n.translate('callWaiterCancelHint'));

							me.getApplication().handleServerError({
								'error': operation.error,
								'forceLogout': {403: true}
							});
						} else {
							console.log('Tried to revoke an already confirmed request. Maybe channel communication is offline.');
						}
					}
				});
			} catch(e) {
				console.log('Request Controller -> cancelCallWaiterRequest error: '+ e);
				button.enable();
			}
		}
	},
	//</call-waiter-request>
	/**
	* Load existing requests for this checkin.
	*/
	loadRequests: function() {
		var me = this,
			label = this.getCallWaiterLabel(),
			requestStore = Ext.StoreManager.lookup('requestStore');

		console.log('Request Controller -> loadRequests');

		requestStore.load({
			callback: function(records, operation, success) {
			   	if(!success) { 
                    me.getApplication().handleServerError({
                       	'error': operation.error, 
                     	'forceLogout': {403:true},
                     	hideMessage: true
                    });
                 } 
                else {
                	Ext.each(records,(function(rec) {
                		if(rec.get('type') ==  Karazy.constants.Request.CALL_WAITER) {
                			me.getCallWaiterButton().mode = 'cancel';
                			label.setHtml(Karazy.i18n.translate('callWaiterCancelHint'));
							me.getCallWaiterButton().setText(Karazy.i18n.translate('cancelCallWaiterRequest'));
                		}
                	}));
                }
            }
		})
	},
	/*
	* Used for cleanup methods. Resets the state of button to call mode.
	*/
	resetAllRequests: function() {
		var requestStore = Ext.StoreManager.lookup('requestStore'),
			label = this.getCallWaiterLabel();

		console.log('Request Controller -> resetAllRequests');

		this.getCallWaiterButton().mode = 'call';
		this.getCallWaiterButton().setText(Karazy.i18n.translate('callWaiterButton'));
		label.setHtml(Karazy.i18n.translate('callWaiterCallHint'));

		requestStore.removeAll();
	},
	/**
	* Handle push messages for requests.
	*/
	handleRequestMessage: function(action, data) {
		var me = this,
			requestStore = Ext.StoreManager.lookup('requestStore'),
			label = this.getCallWaiterLabel(),
			request;

		request = requestStore.getById(data.id);
		if(request) {
			if(action == 'delete' && data.type == Karazy.constants.Request.CALL_WAITER) {
				requestStore.remove(request);
				this.getCallWaiterButton().mode = 'call';
				this.getCallWaiterButton().setText(Karazy.i18n.translate('callWaiterButton'));
				label.setHtml(Karazy.i18n.translate('callWaiterCallHint'));
			}
		}

	},
	/*
	*	Sets the account label in request tab displaying nickname of current checkin
	*/
	refreshAccountLabel: function() {
		var accountLabel = this.getAccountLabel(),
			checkInCtr = this.getApplication().getController('CheckIn');

		accountLabel.setHtml(Karazy.i18n.translate('vipGreetingMessage', checkInCtr.getActiveCheckIn().get('nickname')));
	}
});