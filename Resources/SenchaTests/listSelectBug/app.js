	Ext.Loader.setConfig({
		enabled : true
	});
	Ext.application({
		name : 'ListTest',
		controllers : [ 'PersonList' ],
		models : [ 'Person'],
		init : function() {
			console.log('init');
		//	 this.getView('main').create();
		},
		launch : function() {
			console.log('launch');
		}
	});