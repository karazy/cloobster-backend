/*Karazy namespace. Create if not exists.*/
var Karazy = (Karazy) ? Karazy : {};

/**
 * Contains convenient functions.
 */
Karazy.util = (function() {
	
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
		}
		
	};
	
})();