/*Karazy namespace. Create if not exists.*/
var Karazy = (Karazy) ? Karazy : {};

Karazy.channel = (function() {

	var channel;


	function createChannel(token) {
		channel = new goog.appengine.Channel(token);
	    socket = channel.open();
	    socket.onopen = onOpened;
	    socket.onmessage = onMessage;
	    socket.onerror = onError;
	    socket.onclose = onClose;
	}
	

}());