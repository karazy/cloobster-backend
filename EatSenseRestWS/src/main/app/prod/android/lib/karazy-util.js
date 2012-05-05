/*Karazy namespace. Create if not exists.*/
var Karazy = (Karazy) ? Karazy : {},
	requires = {
		'Karazy.config': Karazy.config, 
		'Karazy.constants': Karazy.constants
	};

/**
 * Contains convenient functions.
 */
Karazy.util = (function() {

	for(precondition in requires) {
		if(!requires[precondition]) {
			console.log('Some functions of this class may need %s to properly work. Make sure inclusion order is correct.', precondition);
		}
	}
	
	return {
		
		/**
		 * Shortens the given string (like substring). 
		 * 
		 * @param text
		 * 		Text to shorten
		 * @param length
		 * 		Length of returned string.
		 * @param appendDots
		 * 		Append 3 dots at the end. E. g. "Beef Burg..."
		 * @returns
		 * 		shortened string
		 */
		shorten : function(text, length, appendDots) {
			var _textLength = text.trim().length;
			if(_textLength > length) {
				return text.substring(0, length) + ((appendDots === true) ? "..." : "");
			} else {
				return text;
			}			
		},
		/**
		*	Checks if the given argument is of type function.
		*
		*/
		isFunction: function(functionToCheck) {
		 	var getType = {};
		 	return functionToCheck && getType.toString.call(functionToCheck) == '[object Function]';
		},
		/**
		*	Rounds a price to number of given decimals.
		*	@param price
		*		Price to round
		*	@param decimal
		*		number of decimals, defaults to 2	
		*/
		roundPrice: function(price, decimal) {
			var 	_decimal = (decimal) ? decimal : 2,
					_factor = Math.pow(10, _decimal),
					_result;

			_result = Math.round(price*_factor)/_factor;

			return _result;
		},
		/**
		*	Takes a price and formats it in the configured currency
		*	@param price
		*		price to format
		*/
		formatPrice: function(price) {
			var 	priceRegExp = /([0123456789]+)\.([0123456789]*)/,
					fixedPrice,
					matcher = Karazy.constants.Currency[Karazy.config.currencyFormat],
					formattedPrice = "";

			if(!price && price == null) {
				return;
			}		

			try {
				fixedPrice = price.toFixed(2);
				formattedPrice = fixedPrice.replace(priceRegExp, matcher);
			} catch(e) {
				console.log('price formatting failed reason:' + e);
			}


			return (formattedPrice != "") ? formattedPrice : price;

		}
		
	};
	
})();