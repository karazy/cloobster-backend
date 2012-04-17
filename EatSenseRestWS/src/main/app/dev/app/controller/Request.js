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
				tap: 'sendCallWaiterRequest'
			}
		}
	},
	/**
	*	Sends a call waiter request.
	*/
	sendCallWaiterRequest: function(button, event) {
		var 	request = Ext.create('EatSense.model.Request'),
				checkInId = this.getApplication().getController('CheckIn').models.activeCheckIn.getId();

		request.set('type', Karazy.constants.Request.CALL_WAITER);
		//workaround to prevent sencha from sending phantom id
		request.setId('');

		request.save({
			params: {
				'pathId' : checkInId
			},
			failure: function(record, operation) {
				Ext.Msg.alert(i18nPlugin.translate('errorTitle'), i18nPlugin.translate('errorRequest'), Ext.emptyFn);
			}
		})
	}
});