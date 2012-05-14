/**
*	Handles customer requests like "Call Waiter".
*	
*/
Ext.define('EatSense.controller.Request',{
	extend: 'Ext.app.Controller',
	config: {
		refs: {
			callWaiterButton: 'requeststab button[action=waiter]'
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
				checkInId = this.getApplication().getController('CheckIn').getActiveCheckIn().getId();
		
		console.log('send call waiter request');
		button.disable();
		//TODO validate!

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
				me.getApplication().handleServerError({
					'error': operation.error,
					'forceLogout': {403: true}
				});				
			}
		});

		button.mode = 'cancel';

		//show info badge to indicate waiter is called
		// me.getCallWaiterButton().setBadgeText(Karazy.i18n.translate('callWaiterRequestBadge'));
		me.getCallWaiterButton().setText(Karazy.i18n.translate('cancelCallWaiterRequest'));

		//show success message to give user the illusion of success
		Ext.Msg.show({
			title : Karazy.i18n.translate('hint'),
			message : Karazy.i18n.translate('requestCallWaiterSendMsd'),
			buttons : []
		});
		
		Ext.defer((function() {
			if(!Karazy.util.getAlertActive()) {
				Ext.Msg.hide();
			}
		}), Karazy.config.msgboxHideLongTimeout, this);
	},
	cancelCallWaiterRequest: function(button, event) {
		var me = this,
			requestStore = Ext.StoreManager.lookup('requestStore'),
			request;

		console.log('cancel call waiter request');

		request = requestStore.findRecord('type', Karazy.constants.Request.CALL_WAITER, false, true, true);

		button.mode = 'call';
		//show info badge to indicate waiter is called	
		// me.getCallWaiterButton().setBadgeText("");
		me.getCallWaiterButton().setText(Karazy.i18n.translate('callWaiterButton'));

		if(request) {
			button.disable();
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
						me.getApplication().handleServerError({
							'error': operation.error,
							'forceLogout': {403: true}
						});
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
			requestStore = Ext.StoreManager.lookup('requestStore');

		console.log('load requests');

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
                			//show info badge to indicate waiter is called
							// me.getCallWaiterButton().setBadgeText(Karazy.i18n.translate('callWaiterRequestBadge'));
							me.getCallWaiterButton().setText(Karazy.i18n.translate('cancelCallWaiterRequest'));
                		}
                	}));
                }
            }
		})
	},
	/**
	* Handle push messages for requests.
	*/
	handleRequestMessage: function(action, data) {
		var me = this,
			requestStore = Ext.StoreManager.lookup('requestStore'),
			request;

		request = requestStore.getById(data.id);
		if(request) {
			if(action == 'delete' && data.type == Karazy.constants.Request.CALL_WAITER) {
				requestStore.remove(request);
				this.getCallWaiterButton().mode = 'call';
				//show info badge to indicate waiter is called	
				this.getCallWaiterButton().setBadgeText("");
				this.getCallWaiterButton().setText(Karazy.i18n.translate('callWaiterButton'));
			}
		}

	}
});