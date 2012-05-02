/**
*	This controller handles push messages send from the server
*	and fires events when they arrive. Any component interested in those events
* 	can listen.
*/
Ext.define('EatSense.controller.Message', {
	extend: 'Ext.app.Controller',
	config: {
		refs: 
		{
			connectionStatus: 'toolbar[docked=bottom] #connectionStatus'
		},
		evtPrefix: 'eatSense',
		interval: null,
		//indicates if polling is active and a refreshAll events get fired 
		pollingActive: false
	},


	/**
	*	Called after receiving a channel message.
	*	
	*	@param rawMessages	
	*		The raw string message(s) which will be parsed as JSON.
	*		This could be a single object or an array.
	*/
	processMessages: function(rawMessages) {
		var 	me = this,
				message = Ext.JSON.decode(rawMessages, true);

		if(Ext.isArray(message)) {
				for(index = 0; index < message.length; index++) {
				if(message[index]) {					
					this.broadcastMessage(message[index]);
				}	
			}
		}
		else if(message) {
			this.broadcastMessage(message);
		}				
	},
	/**
	*	Fires events to notify listeners about a new message.
	*	Naming schmeme: eatSense.messageType
	*   where message type can be something like spot, order ...
	*
	*	The fired event submits two additional parameters
	*	1. action type (e. g. update, new , delete)
	*	2. content - the data
	*   
	*	@param message	
	*		A message consists of 3 fields
	*			type	- a type like spot
	*			action	- an action like update, new ... 
	*			content - the data
	*/
	broadcastMessage: function(message) {
		var 	me = this,
				evtPrefix = 'eatSense.',
				model = message.content;
				
		console.log('broadcast message type %s, action %s', message.type, message.action);

		if(!message) {
			console.log('no message send');
			return;
		}	

		//fire event based on the message
		me.fireEvent(evtPrefix+message.type.toLowerCase(), message.action, message.content);
	},
	/**
	* Requests a new token from server and executes the given callback with new token as parameter.
	* @param successCallback
	*	callback function to invoke on success
	* @param connectionCallback
	*	
	*/
	requestNewToken: function(successCallback, connectionCallback) {
		var me = this,
			account = this.getApplication().getController('Login').getAccount(),
			login = account.get('login'),
			clientId = login + new Date().getTime();
		
		account.set('clientId', clientId);
		console.log('request new token. clientId: ' + clientId);
		Ext.Ajax.request({
		    url: Karazy.config.serviceUrl+'/accounts/'+login+'/tokens',		    
		    method: 'POST',
		    params: {
		    	'businessId' :  account.get('businessId'),
		    	'clientId' : clientId
		    },
		    success: function(response){
		       	token = response.responseText;
		       	successCallback(token);
		       	connectionCallback();
		    }, 
		    failure: function(response) {
		    	//just log don't show message or force logout!
		    	me.getApplication().handleServerError({
					'error': {
						'status' : response.status,
						'statusText': response.statusText
					}, 
					'forceLogout': false, 
					'hideMessage':true, 
				});
				connectionCallback();
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

		Karazy.channel.setup({
			messageHandler: me.processMessages,
			requestTokenHandler: me.requestNewToken,			
			statusHandler: me.handleStatus,
			executionScope: me
		});
	},
	/**
	* When push communication fails this method acts like a heartbeat for the application.
	* It will fire a refreshAll event in a configured intervall. Every component that needs
	* current data can listen to this event and refresh its view.
	*/
	refreshAll: function(start) {
		var me = this,
			heartbeatInterval = Karazy.config.heartbeatInterval || 10000,
			interval;

		if(start === true && !me.getPollingActive()) {
			console.log('start refresh all');
			interval = window.setInterval(function() {
				console.log('fire refresh all event');
				me.fireEvent(me.getEvtPrefix()+'.refresh-all');
			},heartbeatInterval);
			me.setInterval(interval);
			me.setPollingActive(true);
		} else if(me.getPollingActive()) {
			console.log('stop refresh all');
			window.clearInterval(me.getInterval());
			me.setInterval(null);
			me.setPollingActive(false);
		}
	},
	/**
	*	Called when the connection status changes.
	*
	*/
	handleStatus: function(connectionStatus, previousStatus, reconnectIteration) {
		var statusLabel = this.getConnectionStatus();
		//render status in UI
		console.log('Connection status changed to %s from %s. %s iteration', connectionStatus, previousStatus, reconnectIteration);
		statusLabel.getTpl().overwrite(statusLabel.element, [connectionStatus]);


		if((previousStatus == 'DISCONNECTED' || previousStatus == 'RECONNECT') && connectionStatus == 'ONLINE') {
			this.fireEvent(this.getEvtPrefix()+'.refresh-all');
			this.refreshAll(false);
		} else if((!reconnectIteration || reconnectIteration > 5) && (connectionStatus == 'DISCONNECTED' || connectionStatus == 'RECONNECT')) {
			this.refreshAll(true);
		}
	}
});