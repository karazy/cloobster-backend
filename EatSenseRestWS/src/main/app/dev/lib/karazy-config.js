/*Karazy namespace. Create if not exists.*/
var Karazy = (Karazy) ? Karazy : {};

/**
 * 
 */
Karazy.config = (function() {
	
	return {
		
		serviceUrl : '#SERVICE_URL',
		msgboxHideTimeout : 1000,
		msgboxHideLongTimeout: 2500,
		currencyFormat: 'EURO',
		version: 0.1,
		disableCaching: true,
		language: 'DE'
	};
	
})();