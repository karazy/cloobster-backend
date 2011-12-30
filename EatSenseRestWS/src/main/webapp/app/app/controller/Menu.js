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
        }, {
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
    },
    /**
     * shows the list of products of a menu 
     */
    showProductlist: function() {
    	var main = this.getMain, productoverview = this.getProductoverview();
    	//load products 
    	//show product list
    }
        	
});

