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
        	cardBt : '#menuCartBt',
        	menuview: 'menu'      	
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
             },
             '#menuBackBt' : {
            	 tap: function() {
            		 if(this.menuBackBtContext != null) {
            			 this.menuBackBtContext();
            		 }
            	 }
             }
        });
    	 
    	 //store retrieved models
    	 var models = {};
    	 this.models = models;
    },
    /**
     * Shows the products of a menuitem
     * e. g. Beverages, Drinks, Burgers
     */
    showProductlist: function(dataview, record) {
    	console.log("Menu Controller -> showProductlist");
    	var mov = this.getMenuoverview(), pov = this.getProductoverview(),
    	prodStore = record.productsStore;
    	this.models.activeMenu = record;
    	this.getProductlist().setStore(prodStore);
    	this.getMenulist().refresh();
    	this.menuBackBtContext = this.showMenu;
    	this.switchView(pov,record.data.title, i18nPlugin.translate('back'), 'left');
    },
    /**
     * Shows the menu. At this point the store is already filled with data.
     */
	showMenu : function() {				
		console.log("Menu Controller -> showMenu");
		 this.menuBackBtContext = null;
		 this.switchView(this.getMenuoverview(), i18nPlugin.translate('menuTitle'), null, 'right');
	},
	/**
	 * Displays detailed information for a product (e.g. Burger)
	 * @param dataview
	 * @param record
	 */
	showProductDetail: function(dataview, record) {
		console.log("Menu Controller -> showProductDetail");
		this.models.activeProduct = record;
		 var detail = this.getProductdetail(), main = this.getMain(), menu = this.getMenuview(), choicesPanel =  this.getProductdetail().getComponent('choicesWrapper').getComponent('choicesPanel');
//		 this.getProdDetailLabel().setHtml(
//				 '<div class="prodDetailWrapper" style="font-size:1em, margin-bottom: 10px">'+
//				 	'<div style="position: relative;">'+
//				 		'<h2 style="float: left; width: 80%; margin: 0, font-size:1.5em;">'+record.data.name+'</h2>'+
//				 		//right: 0 , top : 50%
//				 		'<div style="position: absolute; right: 0; top: 10; width: 20%; text-align: right; font-size:1.5em;">'+record.data.price+'</div>'+
//				 		'<div style="clear: both;">'+
//				 	'</div><p>'+record.data.longDesc+'</p>'+
//				 '</div>');
		 this.getProdDetailLabel().getTpl().overwrite(this.getProdDetailLabel().element, record.data);
		 //dynamically add choices if present		 
		 if(typeof record.choices() !== 'undefined') {
			 for(var i =0; i < record.choices().data.items.length; i++) {
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
		 this.menuBackBtContext = this.backToProductOverview;
		 this.switchView(detail,record.data.name, this.models.activeMenu.data.title, 'left');
	},
	/**
	 * Handler for prodDetailBackBt Button. Takes the user back to productoverview
	 * without issuing an order.
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
		var pov = this.getProductoverview();	
		this.menuBackBtContext = this.showMenu;
		this.switchView(pov, this.models.activeMenu.data.title, i18nPlugin.translate('back'), 'right');
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
			order.getProduct(function(prod, operation) {
			    alert(prod.get('name')); 
			});
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
		this.getApplication().getController('Cart').showCart();
	},
	
	//Menu navigation functions
	switchView: function(view, title, labelBackBt, direction) {
		var menu = this.getMenuview();
    	menu.getComponent('menuTopBar').setTitle(title);
    	(labelBackBt == null || labelBackBt.length == 0) ? menu.hideBackButton() : menu.showBackButton(labelBackBt);
    	menu.switchMenuview(view,direction);
	},
	
	/**
	 * Holds the function executed when menu back button is tapped.
	 * The executed function depends on the current context.
	 */
	menuBackBtContext: null
     	
});

