/*Karazy namespace. Create if not existend.*/
var Karazy = (Karazy) ? Karazy : {};

/* Karazy LocaleManager Singleton */
Karazy.i18n = (function() {
	
	
	//private members
	var instance = null, translations = null, resFolder = "../res/", prefix = "eatsense", suffix=".json";
	
	
	//private functions
	/*
	 * returns the browser language 
	 * e.g. de, en
	 */
	function getLanguage() {
		var userLang = (navigator.language) ? navigator.language : navigator.userLanguage; 
		return userLang.substring(0,2);
	}
	
	function readLocaleFile(locale) {
		window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, gotFS, fail);
	}
	
	function translate(key) {
		//TODO check if key exists
		return translations[key];
	}
	
	//Phonegap functions
	
//	 function onDeviceReady() {
//	        window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, gotFS, fail);
//	    }

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

	
	
	/*Initialisation*/
	function constructor() {
		//check browser/system locale and load corresponding locale file
		var lang = getLanguage();
		translations = readLocaleFile(lang);

		
		return {
			/*public methods*/
			 translate: function(key) {
				 
				 return translate(key);
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