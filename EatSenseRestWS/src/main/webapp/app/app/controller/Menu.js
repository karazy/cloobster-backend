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
        	productlist :'#menuCardPanel #productlist',        	
        	productoverview :'productoverview' ,	     
        	menuoverview :'menuoverview' ,	       
        	productdetail :'#menuCardPanel #menuProductDetail' ,        		       
        	backToMenu :'#productOvBackBt' ,	        
        	prodDetailLabel :'#menuCardPanel #menuProductDetail #prodDetailLabel' ,	     
        	prodDetailBackBt :'#prodDetailBackBt' ,	   
        	amountSpinner : 'menu panel panel #productAmountSpinner',
        	cartview : 'cartview',
        	cardBt : '#menuBottomBar #menuCartBt',
        	menuview: 'menu',
        	productcomment: '#productComment',
        	createOrderBt: '#menuCardPanel #menuProductDetail #prodDetailCardBt',
        	backBt: 'menu #menuTopBar #menuBackBt',
        	topToolbar: 'menu #menuTopBar',
        	bottomTapToMenu : '#menuBottomBar #bottomTapToMenu',
        	bottomTapUndo : '#menuBottomBar #bottomTapUndo'
		}
    },
    init: function() {
    	console.log('initialized MenuController');
    	 this.control({
    		 menulist: {
             	select: this.showProductlist
             },
             productlist : {
            	select: this.loadProductDetail 
             },
             backToMenu: {
            	 tap: this.showMenu
             }, 
             prodDetailBackBt: {
            	 tap: this.prodDetailBackBtHandler
             },
             createOrderBt : {
            	 tap: this.createOrder
             },
             cardBt : {
            	 tap: this.showCart
             },
             bottomTapToMenu : {
            	 tap: this.showMenu
             },
             backBt : {
            	 tap: function() {
            		 if(this.menuBackBtContext != null) {
            			 console.log('MenuController -> menuBackBtContext');
            			 this.menuBackBtContext();
            		 }
            	 }
             },
             bottomTapUndo : {
            	 tap: this.undoOrder
             },
             amountSpinner : {
            	 spin: this.amountChanged
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
    	var pov = this.getProductoverview(),
    	prodStore = record.productsStore;
    	this.models.activeMenu = record;
    	this.getProductlist().setStore(prodStore);
    	this.getMenulist().refresh();
    	this.menuBackBtContext = this.showMenu;
    	this.switchView(pov, Karazy.util.shorten(record.data.title,10,true), i18nPlugin.translate('back'), 'left');
    },
    /**
     * Shows the menu. At this point the store is already filled with data.
     */
	showMenu : function() {				
		console.log("Menu Controller -> showMenu");
		 this.menuBackBtContext = null;
		 this.switchView(this.getMenuoverview(), i18nPlugin.translate('menuTitle'), null, 'right');
	},
	
	loadProductDetail: function(dataview, record) {
		console.log('Menu Controller -> loadProductDetail');
		this.models.activeProduct = record;
		this.showProductDetail(dataview, this.models.activeProduct);
//		var _id = record.get('id'), _rId = this.getApplication().getController('CheckIn').models.activeCheckIn.data.restaurantId;

		//BUG
//		Ext.ModelManager.getModel('EatSense.model.Product').load(_id, {
//			scope: this,
//			success: function(product, operation) {
//				this.models.activeProduct = product;
//				this.showProductDetail(dataview, this.models.activeProduct);
//			}
//		});
	},
	/**
	 * Displays detailed information for a product (e.g. Burger)
	 * @param dataview
	 * @param record
	 */
	showProductDetail: function(dataview, record) {
		console.log("Menu Controller -> showProductDetail");		
		 var detail = this.getProductdetail(), main = this.getMain(), menu = this.getMenuview(), choicesWrapper = this.getProductdetail().getComponent('choicesWrapper'), choicesPanel =  this.getProductdetail().getComponent('choicesWrapper').getComponent('choicesPanel');

		this.getProductdetail().getComponent('choicesWrapper').getComponent('choicesPanel').removeAll(false);
		 //reset product spinner
		 this.getAmountSpinner().setValue(1);
		 this.getProdDetailLabel().getTpl().overwrite(this.getProdDetailLabel().element, {product: record, amount: this.getAmountSpinner().getValue()});
		 //dynamically add choices if present		 
		 if(typeof record.choices() !== 'undefined' && record.choices().getCount() > 0) {
			 record.choices().each(function(_choice) {
				 var choice = _choice;				 			 
//			 for(var i =0; i < record.choices().data.items.length; i++) {
//				 var choice = record.choicesStore.data.items[i];
				 var optionsDetailPanel = Ext.create('EatSense.view.OptionDetail');
				 //.getComponent('choiceInfoPanel')
				 optionsDetailPanel.getComponent('choiceTextLbl').setHtml(choice.data.text+'<hr/>');
				 //single choice. Create Radio buttons
				 var optionType = '';
				 if(choice.data.minOccurence <= 1 && choice.data.maxOccurence == 1) {
					 optionType = 'Ext.field.Radio';
				 	} 
				 else {//multiple choice
					 optionType = 'Ext.field.Checkbox';					 
				 }
				
				 choice.options().each(function(opt) {
					 var checkbox = Ext.create(optionType, {
						 name : choice.get('id'),
						 value : opt,
						 type: optionType,
						 labelWidth: '80%',
						 label : opt.get('name'),
						 checked: opt.get('selected')
					 }, this);		
					 
					 checkbox.addListener('check',function(cbox) {
						 console.log('check');
						 if(cbox.type == "Ext.field.Radio") {
							 choice.options().each(function(innerOpt) {
								 innerOpt.set('selected', false);
							 });
						 };
						 opt.set('selected', true);
						 this.recalculate(this.models.activeProduct);
					 },this);
					 checkbox.addListener('uncheck',function(cbox) {
						 console.log('uncheck');
						 if(cbox.type == 'Ext.field.Checkbox') {
							 opt.set('selected', false);
						 } else {
							 //don't allow radio buttons to be deselected
							 cbox.setChecked(true);
						 }
						 this.recalculate(this.models.activeProduct);								 
					 },this);
					 optionsDetailPanel.getComponent('optionsPanel').add(checkbox);					 
				 },this);	 
				 choicesPanel.add(optionsDetailPanel);
//			 }
			 },this);
			 choicesPanel.add( {
				 html: '<hr/>'
			 });
		 }
		 
		 
		 //insert comment field after options have been added so it is positioned correctly
		 choicesPanel.add({
			xtype: 'textfield',
			label: i18nPlugin.translate('orderComment'),
			labelAlign: 'top',
			itemId: 'productComment',
			value: ''
			}
		);
		 this.menuBackBtContext = this.backToProductOverview;

		 this.switchView(detail, Karazy.util.shorten(record.data.name, 15, true), i18nPlugin.translate('back'), 'left');
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
//		this.models.activeProduct.choices().each(function(choice) {
//			choice.resetOptions();
//		});
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
		console.log('Menu Controller -> createOrder');
		//get active product and set choice values
		var productForCart = this.models.activeProduct, order, validationError = "", productIsValid = true;	
		//validate choices 
		productForCart.choices().each(function(choice) {
			if(choice.validateChoice() !== true) {
				//coice is not valid
				productIsValid = false;
				validationError += choice.get('text') + '<br/>';
			};
		});
		
		if(productIsValid === true) {
			order = Ext.create('EatSense.model.Order');
			order.set('amount', this.getAmountSpinner().getValue());
			order.set('status','PLACED');
			//WORKAROUND
			//because options select doesn't get correctly set after copy of object
			productForCart.choices().each(function(choice, cIndex) {
				choice.options().each(function(option, oIndex) {
					productForCart.data.choices[cIndex].options[oIndex].selected = option.get('selected');
				});
			});
			//WORKAROUND _ END			
			order.setProduct(productForCart);
			//comment field needed
			order.set('comment', this.getProductdetail().getComponent('choicesWrapper').getComponent('choicesPanel').getComponent('productComment').getValue());
			//if valid create order and attach to checkin
			this.getApplication().getController('CheckIn').models.activeCheckIn.orders().add(order);
			this.getCardBt().setBadgeText(this.getApplication().getController('CheckIn').models.activeCheckIn.orders().data.length);
			//TODO temporarily persist data on phone 		
			
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
	
	/**
	 * Switches to another view
	 * @param view
	 * 		new view
	 * @param title
	 * 			Toolbar title
	 * @param labelBackBt
	 * 			label of back button. If <code>null</code> back button will be hidden.
	 * @param direction
	 * 			Direction for switch animation.
	 */
	switchView: function(view, title, labelBackBt, direction) {
		console.log('Menu Controller -> switchView');
		var menu = this.getMenuview();
    	this.getTopToolbar().setTitle(title);
    	(labelBackBt == null || labelBackBt.length == 0) ? menu.hideBackButton() : menu.showBackButton(labelBackBt);
    	menu.switchMenuview(view,direction);
	},
	/**
	 * Removes the last order, if orders exist.
	 */
	undoOrder: function() {
		console.log('Menu Controller -> undoOrder');
		var orders = this.getApplication().getController('CheckIn').models.activeCheckIn.orders(), badgeText,
		removedOrder;
		
		if(orders.data.length > 0) {
			removedOrder = orders.getAt(orders.data.length-1).getProduct().get('name');
			orders.removeAt(orders.data.length - 1);
			badgeText = (orders.data.length > 0) ? orders.data.length : "";
			this.getCardBt().setBadgeText(badgeText);
			Ext.Msg.show({
				title : i18nPlugin.translate('orderRemoved'),
				message : removedOrder,
				buttons : []
			});
			//show short alert and then hide
			Ext.defer((function() {
				Ext.Msg.hide();
			}), globalConf.msgboxHideTimeout, this);			
		} else {
			
		}
	},
	/**
	 * Recalculates the total price for the active product.
	 */
	recalculate: function(product) {
		console.log('Menu Controller -> recalculate');
		this.getProdDetailLabel().getTpl().overwrite(this.getProdDetailLabel().element, {product: product, amount: this.getAmountSpinner().getValue()});
	},
	/**
	 * Called when the product spinner value changes. 
	 * Recalculates the price.
	 * @param spinner
	 * @param value
	 * @param direction
	 */
	amountChanged: function(spinner, value, direction) {
		console.log('MenuController > amountChanged (value:'+value+')');
		this.recalculate(this.models.activeProduct);
	},
	
	/**
	 * Holds the function executed when menu back button is tapped.
	 * The executed function depends on the current context.
	 */
	menuBackBtContext: null
     	
});
