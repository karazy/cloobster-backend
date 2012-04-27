/*Karazy namespace. Create if not exists.*/
var Karazy = (Karazy) ? Karazy : {};

Karazy.constants = (function() {

	return {
		
		//check in status
		INTENT : 'INTENT',
		CHECKEDIN : 'CHECKEDIN',
		ORDER_PLACED: 'ORDER_PLACED',
		SERVED: 'SERVED',
		PAYMENT_REQUEST : 'PAYMENT_REQUEST',
		COMPLETE : 'COMPLETE',
		Order : {
			CART : 'CART',
			PLACED : 'PLACED',
			CANCELED : 'CANCELED',
			RECEIVED: 'RECEIVED',
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