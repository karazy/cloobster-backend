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
        },
        {
        	ref: 'menuoverview',
        	selector: 'menuoverview'
        }, 
        {
        	ref: 'backToMenu',
        	selector: '#productOvBackBt'
        }
    ],
    init: function() {
    	console.log('initialized MenuController');
    	 this.control({
    		 '#menulist': {
             	select: this.showProductlist
             },
             '#productOvBackBt': {
            	 tap: this.showMenu
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
    	this.getProductlist().setStore(prodStore);
    	this.getMenulist().refresh();
    	main.setActiveItem(pov);
    },
	showMenu : function(a, b, c, d, e) {
		console.log("Menu Controller -> showMenu");
		 var menu = this.getMenuoverview(), main = this.getMain();
		 this.getMenulist().setClearSelectionOnDeactivate(true);
		 main.setActiveItem(menu);			  	 
		 }
     	
});

