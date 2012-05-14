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
		channelReconnectFactor = 1.3,
		//the status for the connection
		connectionStatus = 'INITIALIZING',
		//previous connection status
		previousStatus = 'NONE',
		//online check interval object
		interval,
		//true when the listener for window event pageshow has been connected
		pageshowListenerRegistered;

	function onOpen() {
		if(connectionStatus == 'ONLINE') {
			console.log('channel opened already received');
			return;
		}
		console.log('channel opened');
		connectionLost = false;	
		timedOut = false;		
		channelReconnectTimeout = Karazy.config.channelReconnectTimeout;

		setStatusHelper('ONLINE');
		statusHandlerFunction.apply(executionScope, [{
			'status' : connectionStatus, 
			'prevStatus': previousStatus
		}]);

		/*		
		 This is mainly for mobile devices when going into standby. 
		 It is not possible in a webapp to be notified when standy is entered.
		 However we can listen to the pageshow event which gets called when the window gets resumed. 
		 http://stackoverflow.com/questions/4401764/what-event-fires-when-a-webkit-webapp-is-terminated
		*/
		if(!pageshowListenerRegistered) {
			pageshowListenerRegistered = true;
			window.addEventListener("pageshow", function(){
				setStatusHelper('RECONNECT');
				statusHandlerFunction.apply(executionScope, [{
					'status' : connectionStatus, 
					'prevStatus': previousStatus
				}]);
				console.log('online check');
				// repeatedOnlineCheck();
				checkOnlineFunction.apply(executionScope, [function() {
					timedOut = true;
					socket.close();
				}]);
			}, false);
		}
	};

	function onMessage(data) {
		console.log('channel message received');
		messageHandlerFunction.apply(executionScope, [data.data]);
	};

	function onError(error) {
		var errorDesc = (error && error.description) ? error.description : "";

		console.log('channel error: ' + errorDesc);

		if(error && ( error.code == '401' || error.code == '400') ) {
			console.log('onError: reason TIMEOUT, code: ' + error.code);
			timedOut = true;
			setStatusHelper('TIMEOUT');
			statusHandlerFunction.apply(executionScope, [{
				'status' : connectionStatus, 
				'prevStatus': previousStatus
			}]);
			socket.close();
			
		} else if (!connectionLost && error && (error.code == '-1' || error.code == '0')) {
			console.log('onError: reason CONNECTION_LOST');
			connectionLost = true;
			setStatusHelper('CONNECTION_LOST');
			statusHandlerFunction.apply(executionScope, [{
				'status' : connectionStatus, 
				'prevStatus': previousStatus
			}]);
			console.log('start online check interval every 5s');
			interval = window.setInterval(repeatedOnlineCheck , 5000);			
		}
	};

	function onClose() {
		if(!Karazy.util.isFunction(requestTokenHandlerFunction)) {
			console.log('requestTokenHandlerFunction is not of type function!');
			return;
		};

		if(timedOut === true && connectionStatus == 'TIMEOUT') {
			console.log('onClose: reason TIMEOUT');
			setStatusHelper('RECONNECT');
			repeatedConnectionTry();
		} else if(connectionLost === true && connectionStatus == 'CONNECTION_LOST') {
			console.log('onClose: reason CONNECTION_LOST');
			setStatusHelper('RECONNECT');
			repeatedConnectionTry();
		} else if(connectionStatus == 'DISCONNECTED'){
			console.log('onClose: reason DISCONNECTED');
			statusHandlerFunction.apply(executionScope, [{
				'status' : connectionStatus, 
				'prevStatus': previousStatus
			}]);
		}
	};
	
	function repeatedOnlineCheck() {
		if(connectionStatus == 'ONLINE' || connectionStatus == 'DISCONNECTED') {
			if(interval) {
				console.log('stopping online check');
				window.clearInterval(interval);
			}
		}
		if(connectionStatus == 'CONNECTION_LOST') {
			checkOnlineFunction.apply(executionScope, [
				function() {
					if(interval) {
						window.clearInterval(interval);	
					}
					timedOut = true;
					setStatusHelper('TIMEOUT');
					socket.close();
				}
			]);
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

		var tries = 0;
		var connect = function() {
				if(connectionStatus == 'ONLINE' || connectionStatus == 'DISCONNECTED') {
					return;
				}

				if(tries > Karazy.config.channelReconnectTries) {
					console.log('Maximum tries reached. No more connection attempts.')
					setStatusHelper('DISCONNECTED');	
					if(Karazy.util.isFunction(statusHandlerFunction)) {
						statusHandlerFunction.apply(executionScope, [{
							'status' : connectionStatus, 
							'prevStatus': previousStatus
						}]);
					}
					return;
				}

				statusHandlerFunction.apply(executionScope, [{
					'status' : connectionStatus, 
					'prevStatus': previousStatus,
					'reconnectIteration' : tries
				}]);

				console.log('repeatedConnectionTry: iteration ' + tries);
				tries += 1;
				channelReconnectTimeout = (channelReconnectTimeout > 300000) ? channelReconnectTimeout : channelReconnectTimeout * channelReconnectFactor;
				// setupChannel(channelToken);
				
				requestTokenHandlerFunction.apply(executionScope, [setupChannel, function() {
					console.log('repeatedConnectionTry: Next reconnect try in '+channelReconnectTimeout);					
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
			
			var handler = new Object();
			handler.onopen = onOpen;
			handler.onmessage = onMessage;
			handler.onerror = onError;
			handler.onclose = onClose;

			console.log('setupChannel: token ' + token);

			channelToken = token;
			try {
				channel = new goog.appengine.Channel(token);	
			} catch(e) {
				console.log('setupChannel: failed to open channel! reason '+ e);
				return;
			}
			
			socket = channel.open(handler);
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

			if(!Karazy.util.isFunction(options.checkOnlineHandler)) {
				throw "No checkOnlineHandler provided";
			};

			checkOnlineFunction = options.checkOnlineHandler;
			

			(options.executionScope) ? executionScope = options.executionScope : this;
			connectionLost = true;
			connectionStatus = 'INITIALIZING';
			repeatedConnectionTry();

		},
		connectedReceived: function () {
			onOpen();
		},
		/**
		* Closes the cannel and prevents a new token request.
		*/
		closeChannel: function() {
			timedOut = false;
			connectionLost = false;	
			channelToken = null;

			console.log('normal channel closing');

			if(socket) {
				setStatusHelper('DISCONNECTED');	
				socket.close();
			};			
		}	
	}

	

}());