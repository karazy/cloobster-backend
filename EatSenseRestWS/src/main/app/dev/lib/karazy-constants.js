/*Karazy namespace. Create if not exists.*/
var Karazy = (Karazy) ? Karazy : {};

/**
*	Contains contants used throughout the application.
*	
*/
Karazy.constants = (function() {

	return {
		
		//check in status
		INTENT : 'INTENT',
		CHECKEDIN : 'CHECKEDIN',
		ORDER_PLACED : 'ORDER_PLACED',
		PAYMENT_REQUEST : 'PAYMENT_REQUEST',
		COMPLETE : 'COMPLETE',
		CANCEL_ALL : 'CANCEL_ALL',
		Order : {
			CART : 'CART',
			PLACED : 'PLACED',
			RECEIVED: 'RECEIVED',
			CANCELED : 'CANCELED',
			COMPLETE : 'COMPLETE'
		},
		Request : {
			CALL_WAITER : 'CALL_WAITER'
		},
		//regular expressions for different currencies
		Currency : {
			EURO: '$1,$2 €',
			US_DOLLAR: '\$ $1.$2'
		}

	};

})();