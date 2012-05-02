/*Karazy namespace. Create if not exists.*/
var Karazy = (Karazy) ? Karazy : {};

/**
 * 
 */
Karazy.config = (function() {
	
	return {
		
		serviceUrl : '',
		msgboxHideTimeout: 1000,
		msgboxHideLongTimeout: 1500,
		currencyFormat: 'EURO',
		version: 0.1,
		disableCaching: true,
		language: 'DE',
		channelReconnectTimeout: 5000,
		channelReconnectTries: 100,
		heartbeatInterval: 10000
	};
	
})();