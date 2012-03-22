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
	//scope in which to execute messageHandler function
	var scopeMessageHandler;
	//scope in which to execute tokenRequestHandler function
	var scopeTokenRequestHandler;
	//indicates if the client forced a close and won't try to request a new token.
	var forcedClose;

	function onOpened() {
		console.log('channel opened');
	};

	function onMessage(data) {
		console.log('channel message received');
		// for(dataEntry in data.data) {
		// 	messageHandlerFunction.apply(scopeMessageHandler, [dataEntry]);
		// }
		messageHandlerFunction.apply(scopeMessageHandler, [data.data]);
	};

	function onError(error) {		
		console.log('channel error ' + (error && error.description) ? error.description : "");
		// if(forcedClose === false && Karazy.util.isFunction(requestTokenHandlerFunction)) {
			// requestTokenHandlerFunction.apply(scopeTokenRequestHandler, [setupChannel]);	
		// }		
	};

	function onClose() {
		console.log('channel closed');
		// if(forcedClose === false && Karazy.util.isFunction(requestTokenHandlerFunction)) {
			// requestTokenHandlerFunction.apply(scopeTokenRequestHandler, [setupChannel]);	
		// }
	};

	function setupChannel(token) {
			channel = new goog.appengine.Channel(token);
			socket = channel.open();
			socket.onopen = onOpened;
		    socket.onmessage = onMessage;
		    socket.onerror = onError;
		    socket.onclose = onClose;
	};



	return {

		/**
		*	Creates a new channel, based on the given token.
		*
		*/
		createChannel: function(options) {			
			console.log('createChannel');

			if(!options.token) {
				throw "No token provided";
			};

			if(!Karazy.util.isFunction(options.messageHandler)) {
				throw "No messageHandler provided";
			};		

			(options.messageHandlerScope) ? scopeMessageHandler = options.messageHandlerScope : this;			
			messageHandlerFunction = options.messageHandler;

			if(options.requestTokenHandler) {
				requestTokenHandlerFunction = options.requestTokenHandler;
				(options.requestTokenHandlerScope) ? scopeTokenRequestHandler = options.requestTokenHandlerScope : this;
			};

			// forcedClose = false;

			setupChannel(options.token);
		},
		/**
		*	Assigns a ne handler that gets called when a message arrives.
		*
		*/
		assignMessageHandler: function(options) {
			console.log('channel: assign a new messageHandler');

			if(!Karazy.util.isFunction(options.messageHandler)) {
				throw "No messageHandler provided";
			};

			(options.scope) ? scopeMessageHandler = options.scope : this;

			messageHandlerFunction = options.messageHandler;
		},

		/**
		* Closes the cannel and prevents a new token request.
		*/
		closeChannel: function() {
			// forcedClose = true;
			if(socket) {
				socket.close();
			};			
		}	



	}

	

}());