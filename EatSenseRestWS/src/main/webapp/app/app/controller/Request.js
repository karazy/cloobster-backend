/**
*	Handles customer requests like "Call Waiter".
*	
*/
Ext.define('EatSense.controller.Request',{
	extend: 'Ext.app.Controller',
	config: {
		refs: {
			// requestMenuButton: 'button[action=request]',
			callWaiterButton: 'requeststab button[action=waiter]'
		},
		control: {
			// requestMenuButton: {
			// 	tap: 'showRequestMenu'
			// },
			callWaiterButton: {
				tap: 'sendCallWaiterRequest'
			}
		},

		requestPanel: Ext.create('EatSense.view.fragment.RequestMenu')
	},
	/**
	*	Shows a menu with available customer requests.
	*/
	// showRequestMenu: function(button, event) {
	// 	console.log('Request Controller --> showRequestMenu');
	// 	var 	requestPanel = this.getRequestPanel();

	// 	requestPanel.showBy(button, "br-tc?");
	// },
	/**
	*	Sends a call waiter request.
	*/
	sendCallWaiterRequest: function(button, event) {
		var 	requestPanel = this.getRequestPanel(),
				request = Ext.create('EatSense.model.Request'),
				checkInId = this.getApplication().getController('CheckIn').models.activeCheckIn.getId();

		request.set('type', Karazy.constants.Request.CALL_WAITER);

		request.save({
			params: {
				'pathId' : checkInId
			},
			failure: function(record, operation) {
				Ext.Msg.alert(i18nPlugin.translate('errorTitle'), i18nPlugin.translate('errorRequest'), Ext.emptyFn);
			}
		})
		
		requestPanel.hide();		
	}
});