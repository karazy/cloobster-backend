/*Karazy namespace. Create if not exists.*/
var Karazy = (Karazy) ? Karazy : {};

/**
 * 
 */
Karazy.config = (function() {
	
	return {
		
		serviceUrl : '',
		msgboxHideTimeout : 1000,
		msgboxHideLongTimeout: 2000,
		currencyFormat: 'EURO',
		version: 0.1,
		disableCaching: true,
		language: 'DE',
		channelReconnectTimeout: 15000,
		channelReconnectTries: 30,
		heartbeatIntervall: 10000
	};
	
})();