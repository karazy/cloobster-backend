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
		var 	me = this,
				request = Ext.create('EatSense.model.Request'),
				checkInId = this.getApplication().getController('CheckIn').getActiveCheckIn().getId();

		request.set('type', Karazy.constants.Request.CALL_WAITER);
		//workaround to prevent sencha from sending phantom id
		request.setId('');

		request.save({
			failure: function(record, operation) {
				me.getApplication().handleServerError(operation.error);
			}
		})
	}
});