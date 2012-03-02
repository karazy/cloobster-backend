/*Karazy namespace. Create if not exists.*/
var Karazy = (Karazy) ? Karazy : {};

Karazy.constants = (function() {

	return {
		
		//check in status
		INTENT : 'INTENT',
		CHECKEDIN : 'CHECKEDIN',
		PAYMENT_REQUEST : 'PAYMENT_REQUEST',
		COMPLETE : 'COMPLETE',
		Order : {
			CART : 'CART',
			PLACED : 'PLACED',
			CANCELED : 'CANCELED',
			COMPLETE : 'COMPLETE'
		}

	};

})();