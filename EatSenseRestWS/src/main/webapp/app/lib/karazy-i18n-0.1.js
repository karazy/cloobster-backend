/*Karazy namespace. Create if not existend.*/
var Karazy = (Karazy) ? Karazy : {};

/* Karazy LocaleManager Singleton */
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
	var defaultLang = "de";
	
	/**
	 * 
	 */
	var resFolder = "../res/", prefix = "eatsense", suffix=".json";
	
	
	//private functions
	/**
	 * returns the browser language 
	 * e.g. de, en
	 */
	function getLanguage() {
		var userLang = (navigator.language) ? navigator.language : navigator.userLanguage; 
		if(userLang === 'undefined'|| userLang.length == 0) {
			//use default language
			userLang = defaultLang;
		}
		return userLang.substring(0,2);
	}
	

	
	//Phonegap functions
	
//	 function onDeviceReady() {
//	        window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, gotFS, fail);
//	    }

		function readLocaleFile(locale) {
			window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, gotFS, fail);
		}
	
	    function gotFS(fileSystem) {
	        fileSystem.root.getFile(resFolder+prefix+"-"+locale+suffix, null, gotFileEntry, fail);
	    }

	    function gotFileEntry(fileEntry) {
	        fileEntry.file(gotFile, fail);
	    }

	    function gotFile(file){
	        readAsText(file);
	    }

	    function readAsText(file) {
	        var reader = new FileReader();
	        reader.onloadend = function(evt) {
	            translations = evt.target.result;
	        };
	        reader.readAsText(file);
	    }

	    function fail(evt) {
	        console.log(evt.target.error.code);
	    }

	
	
	/**
	 * 
	 * Constructor used for initialization.
	 */
	function constructor() {
		//check browser/system locale and load corresponding locale file
		var lang = getLanguage();
		//in a later state read language files from system
		//translations = readLocaleFile(lang);

		/*public methods*/
		return {
			/**
			 * Translates the given key into the corresponding value in selected language.
			 * @param key
			 * 		The key used to find a specific translation.
			 * @returns
			 * 		Translation.
			 */
			 translate: function(key) {		
				 if (translations[key] && translations[key] !== '') {
					 var value = translations[key];
					 if(arguments.length > 1) {
						 //this is a string with placeholders
						 value = Ext.String.format(value, arguments[1], arguments[2]);
					 }
					 return value;
				 }
				 return '';
			 },
			 /**
			  * Used to manually set translation object.
			  * 
			  * @param translations
			  * @returns
			  */
			setTranslations: function(trans) {
				translations = trans;
			}
			
		};
		
	}
	
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