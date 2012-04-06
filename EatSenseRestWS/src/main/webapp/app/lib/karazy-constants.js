/*Karazy namespace. Create if not exists.*/
var Karazy = (Karazy) ? Karazy : {};

Karazy.constants = (function() {

	return {
		
		//check in status
		INTENT : 'INTENT',
		CHECKEDIN : 'CHECKEDIN',
		ORDER_PLACED : 'ORDER_PLACED',
		PAYMENT_REQUEST : 'PAYMENT_REQUEST',
		COMPLETE : 'COMPLETE',
		Order : {
			CART : 'CART',
			PLACED : 'PLACED',
			RECEIVED: 'RECEIVED',
			CANCELED : 'CANCELED',
			COMPLETE : 'COMPLETE'
		},
		Request : {
			CALL_WAITER : 'CALL_WAITER'
		}

	};

})();