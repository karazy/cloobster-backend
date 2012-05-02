/*Karazy namespace. Create if not exists.*/
var Karazy = (Karazy) ? Karazy : {},
	requires = {
		'Karazy.util': Karazy.util
	};

/**
*
*	Wraps for appengine channel api functionality for convenience.
*
*/
Karazy.channel = (function() {

	for(precondition in requires) {
		if(!requires[precondition]) {
			console.log('Some functions of this class may need %s to properly work. Make sure inclusion order is correct.', precondition);
		}
	}

	//private members

	//holds a reference to the channel
	var channel,	
		//socket used for communication
		socket,
		//function called when a message is received
		messageHandlerFunction,
		//function called to request a new token when an error occurs or channel is closed
		requestTokenHandlerFunction,
		//called whenever connection status changes
		statusHandlerFunction,
		//scope in which to execute handler functions function
		executionScope,
		//indicates if the client forced a close and won't try to request a new token.
		timedOut = false,
		//indicates if connection was lost or none existed
		connectionLost = true,
		//token used for this channel
		channelToken,
		//timeout used when attempting to reconnect the channel
		channelReconnectTimeout = Karazy.config.channelReconnectTimeout,
		//a factor by which the intervall for requesting a new token increases over time to prevent mass channel creations
		channelReconnectFactor = 1.1,
		//the status for the connection
		connectionStatus = 'INITIALIZING',
		previousStatus = 'NONE';

	function onOpened() {		
		console.log('channel opened');
		connectionLost = false;	
		timedOut = false;		
		channelReconnectTimeout = Karazy.config.channelReconnectTimeout;

		setStatusHelper('ONLINE');
		statusHandlerFunction.apply(executionScope, [connectionStatus, previousStatus]);
	};

	function onMessage(data) {
		console.log('channel message received');
		messageHandlerFunction.apply(executionScope, [data.data]);
	};

	function onError(error) {		
		console.log('channel error ' + (error && error.description) ? error.description : "");
		if(error && ( error.code == '401' || error.code == '400') ) {
			timedOut = true;
			socket.close();
		} else if (!connectionLost && error && (error.code == '-1' || error.code == '0')) {
			connectionLost = true;
			socket.close();
		}
	};

	function onClose() {
		if(!Karazy.util.isFunction(requestTokenHandlerFunction)) {
			console.log('requestTokenHandlerFunction is not of type function!');
			return;
		};

		if(timedOut === true && connectionStatus != 'RECONNECT') {
			console.log('channel timeout');	
			setStatusHelper('RECONNECT');		
			repeatedConnectionTry();
		} else if(connectionLost === true && connectionStatus != 'RECONNECT') {
			console.log('channel connection lost');
			setStatusHelper('RECONNECT');
			repeatedConnectionTry();
		} else {
			setStatusHelper('DISCONNECTED');
			statusHandlerFunction.apply(executionScope, [connectionStatus, previousStatus]);
		}
	};
	/**
	*	Repeatedly tries to reopen a channel after it has been close.
	*
	*/
	function repeatedConnectionTry() {
		if(!connectionLost && !timedOut) {
			return;
		}

		console.log('Trying to connect and request new token.');

		var tries = 1;
		var connect = function() {
				if(connectionStatus == 'ONLINE') {
					return;
				}

				if(tries > Karazy.config.channelReconnectTries) {
					console.log('Maximum tries reached. No more connection attempts.')
					setStatusHelper('DISCONNECTED');	
					if(Karazy.util.isFunction(statusHandlerFunction)) {
						statusHandlerFunction.apply(executionScope, [connectionStatus, previousStatus]);
					}
					return;
				}

				statusHandlerFunction.apply(executionScope, [connectionStatus, previousStatus, tries]);

				console.log('Connection try %s iteration.', tries);
				tries += 1;
				channelReconnectTimeout = (channelReconnectTimeout > 1800000) ? channelReconnectTimeout : channelReconnectTimeout * channelReconnectFactor;
				// setupChannel(channelToken);
				
				requestTokenHandlerFunction.apply(executionScope, [setupChannel, function() {
					console.log('Next reconnect try in %s msec.', channelReconnectTimeout);					
					window.setTimeout(connect, channelReconnectTimeout);	
				}]);
		};
		window.setTimeout(connect, (connectionStatus == 'INITIALIZING') ? 0 : channelReconnectTimeout);
	};

	function setStatusHelper(newStatus) {
		previousStatus = connectionStatus;
		connectionStatus = newStatus;
	};

	/**
	* Creates the channel and set the handler.
	* @param token
	*	Token for channel generation
	*/
	function setupChannel(token) {
			if(!token) {
				return;
			}

			console.log('setup channel for token %s', token);

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
		* Setup channel comunication and try to establish a connection.
		* @param options
		*/
		setup: function(options) {
			console.log('setup channel communication');

			if(!Karazy.util.isFunction(options.messageHandler)) {
				throw "No messageHandler provided";
			};

			messageHandlerFunction = options.messageHandler;

			if(!Karazy.util.isFunction(options.requestTokenHandler)) {
				throw "No requestTokenHandler provided";
			};

			requestTokenHandlerFunction = options.requestTokenHandler;

			if(!Karazy.util.isFunction(options.statusHandler)) {
				throw "No statusHandler provided";
			};

			statusHandlerFunction = options.statusHandler;

			(options.executionScope) ? executionScope = options.executionScope : this;

			repeatedConnectionTry();

		},
		/**
		* Closes the cannel and prevents a new token request.
		*/
		closeChannel: function() {
			timedOut = false;
			connectionLost = false;	
			channelToken = null;

			if(socket) {
				setStatusHelper('DISCONNECTED');	
				socket.close();
			};			
		}	



	}

	

}());