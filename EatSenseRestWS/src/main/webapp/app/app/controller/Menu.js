/**
 * The menu controller handles everything related to the menu.
 * - Navigation in menu structure
 * - choosing products and put them into the card
 * - configuring products e.g. choosing options
 */
Ext.define('EatSense.controller.Menu', {
    extend: 'Ext.app.Controller',
    config: {
        profile: Ext.os.deviceType.toLowerCase()
    },
    
	views : [
		'Main',
		'Dashboard',
		'MenuOverview',
		'ProductOverview',
		'ProductDetail',
		'OptionDetail'
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
        	ref: 'productdetail',
        	selector: 'productdetail'        	
        },
        {
        	ref: 'backToMenu',
        	selector: '#productOvBackBt'
        },
        {
        	ref: 'prodDetailLabel',
        	selector: '#prodDetailLabel'
        },
        {
        	ref: 'prodDetailBackBt',
        	selector: '#prodDetailBackBt'
        }
    ],
    init: function() {
    	console.log('initialized MenuController');
    	 this.control({
    		 '#menulist': {
             	select: this.showProductlist
             },
             '#productlist' : {
            	select: this.showProductDetail 
             },
             '#productOvBackBt': {
            	 tap: this.showMenu
             }, 
             '#prodDetailBackBt': {
            	 tap: this.backToProductOverview
             },
             '#prodDetailCardBt' : {
            	 tap: this.addProductToCard
             }
        });
    	 
    	 //store retrieved models
    	 var models = {};
    	 this.models = models;
    	 //models.menudata holds all menu related data
    },
    /**
     * Shows the products of a menuitem
     * e. g. Beverages, Drinks, Burgers
     */
    showProductlist: function(dataview, record) {
    	console.log("Menu Controller -> showProductlist");
    	var main = this.getMain(), pov = this.getProductoverview(),
    	prodStore = record.productsStore;
    	this.models.activeMenu = record;
    	this.getProductlist().setStore(prodStore);
    	this.getProductoverview().getComponent('toolbar').setTitle(record.data.title);
    	this.getMenulist().refresh();
    	main.setActiveItem(pov);
    },
    /**
     * Shows the menu. At this point the store is already filled with data.
     */
	showMenu : function() {				
		console.log("Menu Controller -> showMenu");
		 var menu = this.getMenuoverview(), main = this.getMain(), detail = this.getProductdetail();
		 this.getMenulist().setClearSelectionOnDeactivate(true);		  
//		 detail.floating = false;
//		 detail.hide();
		 main.setActiveItem(menu);			  	 
	},
	/**
	 * Displays detailed information for a product (e.g. Burger)
	 * @param dataview
	 * @param record
	 */
	showProductDetail: function(dataview, record) {
		console.log("Menu Controller -> showProductDetail");
		this.models.activeProduct = record.data;
		 var detail = this.getProductdetail(), main = this.getMain(), optionsPanel =  this.getProductdetail().getComponent('optionsPanel');
		 this.getProdDetailBackBt().setText(this.models.activeMenu.data.title);
		 this.getProductdetail().getComponent('toolbar').setTitle(record.data.name);
		 this.getProdDetailLabel().setHtml('<p>'+record.data.longDesc+'</p><p style="text-align:right; font-size:6em;">'+record.data.price+'</p>');
		 //dynamically add options		 
		 for(var choice in record.choices) {
			 var optionsDetailPanel = Ext.create('EatSense.view.OptionDetail');
			 optionsDetailPanel.getComponent('choiceTextLbl').setText(choice.text);
			 //single choice. Create Radio buttons
			 if(choice.minOccurence == 1 && choice.maxOccurence == 1) {
				 for(var item in choice.options) {
					 var radioBt = Ext.create('Ext.field.Radio');
					 radioBt.name = choice.id;
					 radioBt.value = item;
					 radioBt.label = item.name;
					 optionsDetailPanel.getComponent('optionsPanel').add(radioBt);
				 }
			 } else {
				 for(var item in choice.options) {
					 var checkbox = Ext.create('Ext.field.Checkbox');
					 checkbox.name = choice.id;
					 checkbox.value = item;
					 checkbox.label = item.name;
					 optionsDetailPanel.getComponent('optionsPanel').add(checkbox);
				 }
			 }
			 optionsPanel.add(optionsDetailPanel);
		 }
		 
		 main.setActiveItem(detail);
	},
	/**
	 * Called when user navigates back from productdetail view to productoverview (the list of products)
	 * @param button
	 */
	backToProductOverview: function(button) {
		console.log("Menu Controller -> backToProductOverview");
		this.models.activeProduct = null;
		var main = this.getMain(), pov = this.getProductoverview();
		 main.setActiveItem(pov);
	},
	/**
	 * Adds the current product to card.
	 * @param button
	 */
	addProductToCard: function(button) {
		//get active product and set choice values
	}
     	
});

