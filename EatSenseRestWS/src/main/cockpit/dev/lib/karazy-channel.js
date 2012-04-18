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
	var timedOut;
	//token used for this channel
	var channelToken;

	function onOpened() {
		console.log('channel opened');
	};

	function onMessage(data) {
		console.log('channel message received');
		messageHandlerFunction.apply(scopeMessageHandler, [data.data]);
	};

	function onError(error) {		
		console.log('channel error ' + (error && error.description) ? error.description : "");
		if(error && error.code == '401') {
			timedOut = true;
		} else if (error && (error.code == '-1' || error.code == '0')) {
			connectionLost = true;
			socket.close();
		}
	};

	function onClose() {
		if(timedOut === true && Karazy.util.isFunction(requestTokenHandlerFunction)) {
			console.log('timeout: requesting new token');
			requestTokenHandlerFunction.apply(scopeTokenRequestHandler, [setupChannel]);	
			timedOut = false;			
		} else if(connectionLost) {
			console.log('connection lost: reopen channel');
			setupChannel(channelToken);
			connectionLost = false;			
		}
	};

	function setupChannel(token) {
			if(!token) {
				return;
			}

			channelToken = token;
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

			timedOut = false;

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
			timedOut = false;
			connectionLost = false;	
			channelToken = null;

			if(socket) {
				socket.close();
			};			
		}	



	}

	

}());