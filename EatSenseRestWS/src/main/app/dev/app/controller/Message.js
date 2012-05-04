/**
*	This controller handles push messages send from the server
*	and fires events when they arrive. Any component interested in those events
* 	can listen.
* 	It also requests tokens from the server and initiates open channel calls.
*/
Ext.define('EatSense.controller.Message', {
	extend: 'Ext.app.Controller',
	config: {
		//id used for channel creation		
		channelId: null
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

		if(!message) {
			console.log('no message send');
			return;
		}	

		console.log('broadcast message type %s, action %s', message.type, message.action);

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
		var me = this;
			
		if(!this.getChannelId()) {
			console.log('no channel id is set');
			return;
		};

		console.log('request new token. clientId: ' + this.getChannelId());

		Ext.Ajax.request({
		    url: Karazy.config.serviceUrl+'/c/checkins/'+this.getChannelId()+'/tokens',		    
		    method: 'POST',
		    jsonData: true,
		    success: function(response){
		       	token = response.responseText;
		       	successCallback(token);
		       	connectionCallback();
		    },
		    failure: function(response, opts) {
		    	me.getApplication().handleServerError({
					'error': {
						'status' : response.status,
						'statusText': response.statusText
					}, 
					'forceLogout': false, 
					'hideMessage':true, 
					'message': Karazy.i18n.translate('channelTokenError')
				});
				connectionCallback();
		    }
		});
	},
	/**
	* 	Requests a token and
	*	opens a channel for server side push messages.
	*	@param id
	*		Id to open channel for
	*/
	openChannel: function(id) {
		var		me = this;

		this.setChannelId(id);

		Karazy.channel.setup({
			messageHandler: me.processMessages,
			requestTokenHandler: me.requestNewToken,			
			statusHandler: me.handleStatus,
			executionScope: me
		});

	},
	/**
	*	Called when the connection status changes.
	*
	*/
	handleStatus: function(connectionStatus, previousStatus, reconnectIteration) {
		//render status in UI
		console.log('Connection status changed to %s from %s. (%s call)', connectionStatus, previousStatus, reconnectIteration);
	}
});