Ext.define('EatSense.controller.Menu', {
    extend: 'Ext.app.Controller',
    config: {
        profile: Ext.os.deviceType.toLowerCase()
    },
    
	views : [
		'Main',
		'Dashboard',
		'MenuOverview',
		'ProductOverview'
	],
	stores : [
	'Menu'
	],
	refs: [
        {
            ref       : 'main',
            selector  : 'mainview',
            xtype     : 'mainview',
            autoCreate: true
        },
        {
            ref       : 'main',
            selector  : 'main',
            xtype     : 'main'
        },
        {
        	ref: 'menulist',
        	selector : '#menulist'        	
        },
        {
        	ref: 'productlist',
        	selector : '#productlist'        	
        }, 
        {
        	ref: 'productoverview',
        	selector: 'productoverview'
        }
    ],
    init: function() {
    	console.log('initialized MenuController');
    	 this.control({
    		 '#menulist': {
             	select: this.showProductlist
             }
        });
    	 
    	 //store retrieved models
    	 var models = {};
    	 this.models = models;
    	 //models.menudata holds all menu related data
    },
    /**
     * shows the list of products of a menu 
     */
    showProductlist: function(dataview, record) {
    	var main = this.getMain(), pov = this.getProductoverview(),
    	prodStore = record.productsStore;
    	//load products into a store 
//    	 var menuListStore = Ext.create('Ext.data.Store', {
//			   model: 'EatSense.model.Product',
//			   data: record.
//		 });
    	this.getProductlist().setStore(prodStore);
    	//this.getProductlist().getStore().load();
    	main.setActiveItem(pov);
    	//show product list
    }
        	
});

