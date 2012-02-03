/**
 * The menu controller handles everything related to the menu.
 * - Navigation in menu structure
 * - choosing products and put them into the card
 * - configuring products e.g. choosing options
 */
Ext.define('EatSense.controller.Menu', {
    extend: 'Ext.app.Controller',
    config: {
        profile: Ext.os.deviceType.toLowerCase(),
		refs: {
	        main : 'mainview', 
        	menulist :'#menulist',        	
        	productlist :'#productlist',        	
        	productoverview :'productoverview' ,	     
        	menuoverview :'menuoverview' ,	       
        	productdetail :'productdetail' ,        		       
        	backToMenu :'#productOvBackBt' ,	        
        	prodDetailLabel :'#prodDetailLabel' ,	     
        	prodDetailBackBt :'#prodDetailBackBt' ,	        
		}
    },
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
    	main.switchAnim('left');
    	main.setActiveItem(pov);
    },
    /**
     * Shows the menu. At this point the store is already filled with data.
     */
	showMenu : function() {				
		console.log("Menu Controller -> showMenu");
		 var menu = this.getMenuoverview(), main = this.getMain(), detail = this.getProductdetail();
		 this.getMenulist().setClearSelectionOnDeactivate(true);		  
		 main.switchAnim('right');
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
		 this.getProdDetailLabel().setHtml(
				 '<div class="prodDetailWrapper">'+
				 	'<div style="position: relative;">'+
				 		'<h2 style="float: left; width: 80%; margin: 0;">'+record.data.name+'</h2>'+
				 		'<div style="position: absolute; right: 0; top: 50%; width: 20%; text-align: right; font-size:2em;">'+record.data.price+'</div>'+
				 		'<div style="clear: both;">'+
				 	'</div><p style="clear: both;">'+record.data.longDesc+'</p>'+
				 '</div>');
		 //dynamically add choices if present		 
		 if(typeof record.choicesStore !== 'undefined') {
			 var totalHeight = 0;
			 for(var i =0; i < record.choicesStore.data.items.length; i++) {
				 //this.getProductdetail().getComponent('choicesWrapper').getComponent('choicesPanelTitle').setHtml(i18nPlugin.translate('choicesPanelTitle'));
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
				//	 checkbox.setHeight(50);
					 totalHeight += 50;
					 optionsDetailPanel.getComponent('optionsPanel').add(checkbox);
				 });				 
				 choicesPanel.add(optionsDetailPanel);
			 }
			 //choicesPanel.setHeight(totalHeight);
		 }
		 main.switchAnim('left');
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
		main.switchAnim('right');
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

