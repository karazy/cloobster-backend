i18n: (function() {

	// which language should be returned as default
	var defaultLang = 'de';

	// holds the translated strings grouped by language
	var keys = {};

	/**
	 * method that checks if a string is a language string in keys, if yes it
	 * returns the string, if not it returns defaultLang
	 * 
	 * @private
	 * @param l
	 *            {String} "de" or "en"
	 * @return{String} whatever defaultLang is set to
	 */
	var checkLang = function(l) {
		if (l && keys[l] && typeof keys[l] == 'object') {
			return l;
		}
		return defaultLang;
	};

	/**
	 * the singleton with the public methods that handle all the translations
	 */
	return {
		/**
		 * @param l
		 *            {String}
		 * @returns null
		 */
		setLang : function(l) {
			defaultLang = l;
		},
		/**
		 * returns the defaultLang
		 * 
		 * @returns{String}
		 */
		getLang : function() {
			return defaultLang;
		},
		/**
		 * sets the translated string
		 * 
		 * @param key
		 *            {String}
		 * @param trans
		 *            {String}
		 * @param lang
		 *            {String} (optional)
		 * @returns null
		 */
		set : function(key, trans, lang) {
			// the language or defaultLang
			lang = checkLang(lang);

			keys[lang][key] = trans;
		},
		/**
		 * checks if the key exists as translation
		 * 
		 * @param{String} key
		 * @returns{Boolean}
		 */
		hasKey : function(key) {
			if (keys[defaultLang][key] && keys[defaultLang][key] != '') {
				return true;
			}
			return false;
		},
		/**
		 * the mainly used function, retrieves the translated string for a
		 * keyword/phrase
		 * 
		 * @param key
		 *            {String} the key to translate
		 * @param lang
		 *            {String} the target language (optional)
		 * @param fallBack
		 *            {String} the fallback string if the key is not found
		 *            (optional)
		 * @returns{String} the translation or the fallback or an empty string
		 */
		get : function(key, lang, fallBack) {
			// the language or defaultLang
			lang = checkLang(lang);

			// check if the tranlsation is there and return it
			if (keys[lang][key] && keys[lang][key] !== '') {
				return keys[lang][key];
			}
			// check if a fallback is given and return it
			if (fallBack && fallBack !== '') {
				return fallBack;
			}
			// if all else fails we return an empty string
			return '';
		},
		/**
		 * sets a translation array to translate from
		 * 
		 * @param lang
		 *            {String} the language to use
		 * @param translations
		 *            {Object} an array of [ key => transl_key, key2 =>
		 *            transl_key2 ] for a single language
		 * @returns
		 */
		setTranslation : function(lang, translations) {
			keys[lang] = translations;
		},
		/**
		 * the mainly used tranlsation setting function, it just overwrites
		 * completely the keys array
		 * 
		 * @param masterTranslations
		 *            {Object}
		 * @returns null
		 */
		setAllTranslations : function(masterTranslations) {
			keys = masterTranslations;
		}

	};
}());