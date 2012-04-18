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
		channelId: ''
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

		//fire event based on the message
		me.fireEvent(evtPrefix+message.type.toLowerCase(), message.action, message.content);
	},
	/**
	*	Requests a new token from server and executes the given callback with new token as parameter.
	*	@param callback
	*		callback function to invoke on success
	*/
	requestNewToken: function(callback, id) {		
		Ext.Ajax.request({
		    url: Karazy.config.serviceUrl+'/c/checkins/'+id+'/tokens',		    
		    method: 'POST',
		    jsonData: true,
		    success: function(response){
		       	token = response.responseText;
		       	callback(token);
		    }, 
		    failure: function(response, opts) {
		    	console.log('request token failed ' + response);
		    	Ext.Msg.alert(Karazy.i18n.translate('error'), Karazy.i18n.translate('channelTokenError')); 
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

		this.requestNewToken(function(newToken) {
			Karazy.channel.createChannel( {
				token: newToken, 
				messageHandler: me.processMessages,
				requestTokenHandler: me.requestNewToken,
				messageHandlerScope: me,
				requestTokenHandlerScope: me
			});
		}, id);
	},
	/**
	*	Closes active channel and reopens it. 
	*	Uses the cannelId member for creation.
	*/
	reopenChannel: function() {
		Karazy.channel.closeChannel();
		this.openChannel(this.getChannelId());
	}
});