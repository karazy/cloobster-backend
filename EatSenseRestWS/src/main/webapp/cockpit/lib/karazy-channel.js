/*Karazy namespace. Create if not exists.*/
var Karazy = (Karazy) ? Karazy : {};

/**
*
*	Wraps for appengine channel api functionality for convenience.
*
*/
Karazy.channel = (function() {

	//private members

	//holds a reference to the channel
	var channel;	
	//socket used for communication
	var socket;
	//function called when a message is received
	var messageHandlerFunction;
	//function called to request a new token when an error occurs or channel is closed
	var requestTokenHandlerFunction;
	//scope in which to execute functions
	var currentScope;
	//indicates if the client forced a close and won't try to request a new token.
	var forcedClose;

	function onOpened() {
		console.log('channel opened');
	};

	function onMessage() {
		console.log('channel message received');
		messageHandlerFunction.call(currentScope);
	};

	function onError() {
		console.log('channel error: request a new token');
		if(!forcedClose && Karazy.util.isFunction(requestTokenHandlerFunction)) {
			requestTokenHandlerFunction.call(currentScope);
		}		
	};

	function onClose(dontRequestToken) {
		console.log('channel closed: request a new token');
		if(!forcedClose && Karazy.util.isFunction(requestTokenHandlerFunction)) {
			requestTokenHandlerFunction.call(currentScope);	
		}
	};



	return {

		/**
		*	Creates a new channel, based on the given token.
		*
		*/
		createChannel: function(options) {
			if(!options.token) {
				throw "No token provided";
			};

			if(!Karazy.util.isFunction(options.messageHandler)) {
				throw "No messageHandler provided";
			};		

			(options.scope) ? currentScope = options.scope : this;

			messageHandlerFunction = options.messageHandler;

			if(options.requestTokenHandler) {
				this.requestTokenHandlerFunction = options.requestTokenHandler;
			};

			forcedClose = false;

			channel = new goog.appengine.Channel(options.token);

			socket = channel.open();
			socket.onopen = onOpened;
		    socket.onmessage = onMessage;
		    socket.onerror = onError;
		    socket.onclose = onClose;
		},

		assignMessageHandler: function(messageHandler) {
			console.log('channel: assign a new messageHandler');
			this.messageHandlerFunction = messageHandler;
		},

		/**
		* Closes the cannel and prevents a new token request.
		*/
		closeChannel: function() {
			forcedClose = true;
			socket.close();
		}	



	}

	

}());