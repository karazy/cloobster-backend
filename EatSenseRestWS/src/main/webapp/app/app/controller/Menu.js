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
        	//TODO improve selector
        	amount : 'panel panel spinnerfield',
        	cartview : 'cartview',
        	cardBt : '#menuCartBt'      	
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
            	 tap: this.prodDetailBackBtHandler
             },
             '#prodDetailCardBt' : {
            	 tap: this.createOrder
             },
             '#menuCartBt' : {
            	 tap: this.showCart
             },
             '#bottomTapToMenu' : {
            	 tap: this.showMenu
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
		this.models.activeProduct = record;
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
		 if(typeof record.choices() !== 'undefined') {
//			 var totalHeight = 0;
			 for(var i =0; i < record.choices().data.items.length; i++) {
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
				
				 choice.options().each(function(opt) {
					 var checkbox = Ext.create(optionType, {
						 name : choice.data.id,
						 value : opt,
						 label : opt.get('name'),
						 listeners : {
							 check : function(cbox) {
								 console.log('check');
								 if(optionType == "Ext.field.Radio") {
									 choice.options().each(function(innerOpt) {
										 innerOpt.set('selected', false);
									 });
								 };
								 opt.set('selected', true);
							 },
							 uncheck: function(cbox) {
								 console.log('uncheck');
								 if(optionType == 'Ext.field.Checkbox') {
									 opt.set('selected', false);
								 } else {
									 //don't allow radio buttons to be deselected
									 cbox.setChecked(true);
								 }	
								 
							 }
						 }
						
					 });					 
					 optionsDetailPanel.getComponent('optionsPanel').add(checkbox);
					 
				 });	 
				 choicesPanel.add(optionsDetailPanel);
			 }
		 }
		 main.switchAnim('left');
		 main.setActiveItem(detail);
	},
	/**
	 * Handler for prodDetailBackBt Button. Takes the user back to productoverview
	 * withoug issuing an order.
	 * @param button
	 */
	prodDetailBackBtHandler : function(button) {
		this.backToProductOverview();
	},
	/**
	 * Called when user navigates back from productdetail view to productoverview (the list of products)
	 * @param message
	 * 		A message to show after switching back to productoverview
	 */
	backToProductOverview: function(message) {
		console.log("Menu Controller -> backToProductOverview");
		this.models.activeProduct = null;
		this.getProductdetail().getComponent('choicesWrapper').getComponent('choicesPanel').removeAll(false);
		var main = this.getMain(), pov = this.getProductoverview();
		main.switchAnim('right');
		main.setActiveItem(pov);
		if (message) {
			Ext.Msg.show({
				title : i18nPlugin.translate('orderPlaced'),
				message : message,
				buttons : []
			});
			//show short alert and then hide
			Ext.defer((function() {
				Ext.Msg.hide();
			}), globalConf.msgboxHideTimeout, this);
		}
	},
	/**
	 * Adds the current product to card.
	 * @param button
	 */
	createOrder: function(button) {
		//get active product and set choice values
		var productForCart = this.models.activeProduct, order, validationError = "", productIsValid = true;
		//validate choices or 
		//validate each choice on tap?
		productForCart.choices().each(function(choice) {
			if(choice.validateChoice() !== true) {
				//coice is not valid
				productIsValid = false;
				validationError += choice.get('text') + '<br/>';
			};
		});
		
		if(productIsValid === true) {
			order = Ext.create('EatSense.model.Order');
			order.set('amount', this.getAmount().getValue());
			order.set('status','PLACED');
			order.setProduct(productForCart);
//			order.getProduct(function(prod, operation) {
//			    alert(prod.get('name')); 
//			}, this);
			//comment field needed
//			order.setComment();
			//if valid create order and attach to checkin
			this.getApplication().getController('CheckIn').models.activeCheckIn.orders().add(order);
			this.getCardBt().setBadgeText(this.getApplication().getController('CheckIn').models.activeCheckIn.orders().data.length);
			//temporarily persist data on phone 		
			
			this.backToProductOverview(i18nPlugin.translate('productPutIntoCardMsg', this.models.activeProduct.get('name')));
		} else {
			//show validation error
			Ext.Msg.alert(i18nPlugin.translate('orderInvalid'),validationError, Ext.emptyFn);
		}
		
	},
	/**
	 * Switches to card view.
	 */
	showCart: function(){
		//only switch if cart is not empty
		if(this.getApplication().getController('CheckIn').models.activeCheckIn.orders().data.length == 0) {
			Ext.Msg.alert(i18nPlugin.translate('hint'),i18nPlugin.translate('cartEmpty'), Ext.emptyFn);
		} else {
			var main = this.getMain(), cartview = this.getCartview();
			main.switchAnim('left');
			main.setActiveItem(cartview);
		}
	}
     	
});

