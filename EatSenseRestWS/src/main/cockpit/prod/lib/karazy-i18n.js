/*Karazy namespace. Create if not exists.*/
var Karazy = (Karazy) ? Karazy : {};

/**
 *  Karazy LocaleManager Singleton 
 */
Karazy.i18n = (function() {
	
	
	//private members
	/**
	 * Singleton instance.
	 */
	var instance = null;
	/**
	 * Holds the translations.
	 */
	var translations = null;
	
	/**
	 * default language.
	 */
	var defaultLang = "";//"de";
	
	/**
	 * Chosen language.
	 */
	var lang = null;
	
	
	//private functions
	/**
	 * returns the browser language 
	 * e.g. de, en
	 */
	function getLanguage() {
		var userLang = (navigator.language) ? navigator.language : navigator.userLanguage; 
		console.log('browser language: '+userLang);
		if(userLang === 'undefined'|| userLang.length == 0) {
			//use default language
			userLang = defaultLang;
		}
		return userLang.substring(0,2).toUpperCase();
	}

	
	/**
	 * 
	 * Constructor used for initialization.
	 */
	function constructor() {
		//get browser/system locale 
		lang = getLanguage();
		translations = translation;

		/*public methods*/
		return {
			/**
			 * Translates the given key into the corresponding value in selected language.
			 * @param key
			 * 		The key used to find a specific translation.
			 * 			if the translated string contains placeholders in form of {0}, {1} ... 
			 * 			1. additional parameters with replacing values 
			 * 			OR
			 * 			2. an array containing placeholders
			 * 			can be submited
			 * @returns
			 * 		Translation or key if none was found
			 */
			 translate: function(key) {
				 //alternativ with custom object and no sencha store
				 var value = "";
				 if (translations[lang] && translations[lang][key] && translations[lang][key] !== '') {
					 value = translations[lang][key];
					 if(arguments.length > 1) {
						 //this is a string with placeholders
						 //replace key with retrieved value and the call Ext.String.format
						 //we need apply because we don't know the number of arguments
						 var _array;
						 
						 if(Object.prototype.toString.call(arguments[1]) === '[object Array]') {
							 _array = new Array();
							 _array[0] = value;
							 for(var i = 0; i < arguments[1].length; i++) {
								 _array[i+1] = arguments[1][i];
							 }
						 }	else {
							 arguments[0] = value;
							 _array = arguments;
						 }						 						 						 
						 
						 value = Ext.String.format.apply(this, _array);
					 }
				 }
				 return (value == "") ? key : value;
			 },
			 /**
			  * Set the language.
			  */
			 setLang: function(language){
				 lang = language;
			 },

			
		};
		
	}
	
	/*Used to create one singleton instance.*/
	var getInstance = function() {
		if (!instance) {
            // create a instance
            instance = constructor();
        }
        // return the instance of the singletonClass
        return instance;
	};
	
	
	return getInstance();
	
})();