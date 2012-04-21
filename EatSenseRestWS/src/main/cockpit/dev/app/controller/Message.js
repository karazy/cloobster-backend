/**
*	This controller handles push messages send from the server
*	and fires events when they arrive. Any component interested in those events
* 	can listen.
*/
Ext.define('EatSense.controller.Message', {
	extend: 'Ext.app.Controller',
	config: {

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
	*	Requests a new token from server and executes the given callback with new token as parameter.
	*	@param callback
	*		callback function to invoke on success
	*/
	requestNewToken: function(callback) {
		var 	account = this.getApplication().getController('Login').getAccount(),
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
		       	callback(token);
		    }, 
		    failure: function(response) {
		    	me.getApplication().handleServerError({
					'error': {
						'status' : response.status,
						'statusText': response.statusText
					}, 
					'forceLogout': false, 
					'hideMessage':false, 
					'message': Karazy.i18n.translate('channelTokenError')
				});
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
				messageHandler: me.processMessages,
				requestTokenHandler: me.requestNewToken,
				messageHandlerScope: me,
				requestTokenHandlerScope: me
			});
		});
	}, 
});