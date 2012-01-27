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
		 var detail = this.getProductdetail(), main = this.getMain(), choicesPanel =  this.getProductdetail().getComponent('choicesWrapper').getComponent('choicesPanel');
		 this.getProdDetailBackBt().setText(this.models.activeMenu.data.title);
		 this.getProductdetail().getComponent('toolbar').setTitle(record.data.name);
		 this.getProdDetailLabel().setHtml('<span style ="float:right;">'+record.data.longDesc+'</span><span style="font-size:2em; ">'+record.data.price+'</span>');
		 //dynamically add choices if present		 
		 if(typeof record.choicesStore !== 'undefined') {
			 for(var i =0; i < record.choicesStore.data.items.length; i++) {
				 var choice = record.choicesStore.data.items[i];
				 var optionsDetailPanel = Ext.create('EatSense.view.OptionDetail');
				 //.getComponent('choiceInfoPanel')
				 optionsDetailPanel.getComponent('choiceTextLbl').setHtml(choice.data.text+'<hr/>');
				 //single choice. Create Radio buttons
				 var optionType = '';
				 if(choice.data.minOccurence == 1 && choice.data.maxOccurence == 1) {
					 optionType = 'Ext.field.Radio';
				 	} 
				 else {//multiple choice
					 optionType = 'Ext.field.Checkbox';					 
				 }
				 Ext.each(choice.data.options, function(item) {
					 var checkbox = Ext.create(optionType, {
						 name : choice.data.id,
						 value : item,
						 label : item.name
					 });
					 optionsDetailPanel.getComponent('optionsPanel').add(checkbox);
				 });
				 choicesPanel.add(optionsDetailPanel);
			 }
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
		this.getProductdetail().getComponent('choicesWrapper').getComponent('choicesPanel').removeAll(false);
		var main = this.getMain(), pov = this.getProductoverview();
		 main.setActiveItem(pov);
	},
	/**
	 * Adds the current product to card.
	 * @param button
	 */
	addProductToCard: function(button) {
		//get active product and set choice values
		this.backToProductOverview();
	}
     	
});

