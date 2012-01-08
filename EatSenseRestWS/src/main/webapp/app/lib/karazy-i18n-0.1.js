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
	var defaultLang = "de";
	
	/**
	 * 
	 */
	//var resFolder = "../res/", prefix = "eatsense", suffix=".json";

	
	/**
	 * Ext store holding translation values
	 */
	var _store;
	
	
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
	//This is another approach. Using phonegaps file capabilities
	/*
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
*/
	
	
	/**
	 * 
	 * Constructor used for initialization.
	 */
	function constructor() {
		//get browser/system locale 
		var lang = getLanguage();
		
		//Create Ext model and store
		Ext.define('Translation', {
			extend: 'Ext.data.Model',
			idProperty: 'key',
			fields: [
				{name: 'key', type: 'string'},
				{name: 'translation', type: 'string'}
			],
			proxy: {
				type: 'ajax',
				url: 'res/eatsense-'+lang+'.json', 
				//appendId: false,
				reader: {
					type: 'json',
				}
			}
		});

		_store = Ext.create('Ext.data.Store', {
		    model   : 'Translation'
		});
		
	
		/*public methods*/
		return {
			/**
			 * Translates the given key into the corresponding value in selected language.
			 * @param key
			 * 		The key used to find a specific translation.
			 * 			if the translated string contains placeholders in form of {0}, {1} ... additional parameters
			 * 			with replacing values can be submited
			 * @returns
			 * 		Translation.
			 */
			 translate: function(key) {
				 var translationObj = this.getStore().getById(key), value ="";
				 if(translationObj !== undefined && translationObj != null) {
					 value = translationObj.data.translation;
					 if(arguments.length > 1) {
						 //this is a string with placeholders
						 //replace key with retrieved value and the call Ext.String.format
						 //we need apply because we don't know the number of arguments
						 arguments[0] = value;
						 value = Ext.String.format.apply(this, arguments);
					 }
				 }
//				 if (translations[key] && translations[key] !== '') {
//					 value = translations[key];
//					 if(arguments.length > 1) {
//						 //this is a string with placeholders
//						 //replace key with retrieved value and the call Ext.String.format
//						 //we need apply because we don't know the number of arguments
//						 arguments[0] = value;
//						 value = Ext.String.format.apply(this, arguments);
//					 }
//					 
//				 }
				 return value;
			 },
			 /**
			  * Used to manually set translation object.
			  * 
			  * @param translations
			  * @returns
			  */
			setTranslations: function(trans) {
				translations = trans;
			},
			
			getStore: function() {
				return _store;
			},
			/**
			 * Loads translation data into the store. When operation is finished executes the given callback function.
			 * @param callback
			 */
			init: function(callback) {
				_store.load({
					     scope   : this,
					     callback: function(records, operation, success) {
					     //the operation object contains all of the details of the load operation
						     callback();
					     }
				     });
			}
			
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