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
        	menuview: 'menu',
        	productcomment: '#productComment',
        	//#menutap #menu
//        	createOrderBt :'menu #menuCardPanel #menuProductDetail #prodDetailcartBt',
        	createOrderBt :'menu #menuTopBar #productCartBt',
        	backBt: 'menu button[action=back]',
        	topToolbar: 'menu #menuTopBar',
        	bottomTapToMenu : '#menuBottomBar #bottomTapToMenu',
        	loungeview: 'lounge',
        	loungeTabBar: '#loungeTabBar',
        	myordersview: '#myorderstab #myorders',
		},

		control: {
			menulist: {
             	select: 'showProductlist'
             },
             productlist : {
            	select: 'loadProductDetail' 
             },
             backToMenu: {
            	 tap: 'showMenu'
             }, 
             prodDetailBackBt: {
            	 tap: 'prodDetailBackBtHandler'
             },
             createOrderBt : {
            	 tap: 'createOrder'
             },
             bottomTapToMenu : {
            	 tap: 'showMenu'
             },
             backBt : {
            	 tap: function() {
            		 if(this.menuBackBtContext != null) {
            			 console.log('MenuController -> menuBackBtContext');
            			 this.menuBackBtContext();
            		 }
            	 }
             },
             amountSpinner : {
            	 spin: 'amountChanged'
             },
             //TODO refactor general loungeview control into another controller?!
             loungeview : {
     			activeitemchange : function(container, value, oldValue, opts) {
    				console.log('tab change');
    				var status = true;
    				if(value.getItemId() === 'carttab') {
    					status = this.getApplication().getController('Order').refreshCart();
    				} else if (value.getItemId() === 'myorderstab') {
//    					this.getMyordersview().showLoadScreen(true);
    					this.getApplication().getController('Order').refreshMyOrdersList();
    				}
    				
    				if(status === false) { //TODO not used currently, status always true
    					    this.getLoungeview().setActiveItem(oldValue);					
    				}
    				
    				return status;
    			}
    		},
    		
    		productdetail: {
    			show: function() {
    				this.getCreateOrderBt().show();
    			},
    			hide: function() {
    				this.getCreateOrderBt().hide();
    			}
    		}
		}
    },
    init: function() {
    	console.log('initialized MenuController');
    	 
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
    	this.switchView(pov, record.data.title, i18nPlugin.translate('back'), 'left');
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
		//save original ids
		record.set('genuineId', record.get('id'));
		record.choices().each(function(choice) {
			choice.set('genuineId', choice.get('id'));
			choice.options().each(function(opt) {
				opt.set('genuineId', opt.get('id'));
			});
		});
		this.models.activeProduct = record.copy();
		this.showProductDetail(dataview, this.models.activeProduct);
	},
	/**
	 * Displays detailed information for a product (e.g. Burger)
	 * @param dataview
	 * @param record
	 */
	showProductDetail: function(dataview, record) {
		console.log("Menu Controller -> showProductDetail");		//.getComponent('choicesWrapper')
		 var detail = this.getProductdetail(), 
		 main = this.getMain(), 
		 menu = this.getMenuview(), 
		 choicesPanel =  this.getProductdetail().getComponent('choicesPanel');		 

		 choicesPanel.removeAll(false);
		 //reset product spinner
		 this.getAmountSpinner().setValue(1);
		 this.getProdDetailLabel().getTpl().overwrite(this.getProdDetailLabel().element, {product: record, amount: this.getAmountSpinner().getValue()});
		 //dynamically add choices if present		 
		 if(typeof record.choices() !== 'undefined' && record.choices().getCount() > 0) {

			 record.choices().each(function(_choice) {
				 var choice = _choice;				 			 
				 var optionsDetailPanel = Ext.create('EatSense.view.OptionDetail');
				 optionsDetailPanel.getComponent('choiceTextLbl').setHtml(choice.data.text); //+'<hr/>');
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
						 labelWidth: '80%',
						 label : opt.get('name'),
						 checked: opt.get('selected')
					 }, this);							 
					
					 checkbox.addListener('check',function(cbox) {
						 console.log('check');
						 if(cbox.isXType('radiofield',true)) {
							 choice.options().each(function(innerOpt) {
								 innerOpt.set('selected', false);
							 });
						 };
						 opt.set('selected', true);
						 this.recalculate(this.models.activeProduct);
					 },this);
					 checkbox.addListener('uncheck',function(cbox) {
						 console.log('uncheck');
						 if(cbox.isXType('checkboxfield',true)) {
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
			 },this);
//			 choicesPanel.add( {
//				 html: '<hr/>'
//			 });
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
		 this.switchView(detail, record.data.name, i18nPlugin.translate('back'), 'left');
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
		var pov = this.getProductoverview();

		this.models.activeProduct = null;

		this.getProductdetail().getComponent('choicesPanel').removeAll(false);
			
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
		var productForCart = this.models.activeProduct,
		order,
		validationError = "",
		cartButton = this.getLoungeTabBar().getAt(1),
		productIsValid = true,
		appState = this.getApplication().getController('CheckIn').getAppState(),
		appStateStore = Ext.StoreManager.lookup('appStateStore'),
		activeCheckIn = this.getApplication().getController('CheckIn').models.activeCheckIn;	
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
			order.set('status','CART');
			productForCart.getData(true);
			order.setProduct(productForCart);
			//comment field needed //.getComponent('choicesWrapper')
			order.set('comment', this.getProductdetail().getComponent('choicesPanel').getComponent('productComment').getValue());
			//if valid create order and attach to checkin
			this.getApplication().getController('CheckIn').models.activeCheckIn.orders().add(order);
			
			Ext.Ajax.request({				
	    	    url: Karazy.config.serviceUrl+'/c/businesses/'+activeCheckIn.get('businessId')+'/orders/',
	    	    method: 'POST',    	    
	    	    params: {
	    	    	'checkInId' : activeCheckIn.get('userId'),
	    	    },
	    	    jsonData: order.getRawJsonData(),
	    	    success: function(response, operation) {
	    	    	order.setId(response.responseText);
	    	    }
	    	});
			
			cartButton.setBadgeText(this.getApplication().getController('CheckIn').models.activeCheckIn.orders().data.length);
			//TODO temporarily persist data on phone or send to server	
//			try {
//				appState.cartOrders().add(order.getRawJsonData());
//				appState.save();
//				appStateStore.sync();
//			} catch (e) {
//				console.log('could not persist order to resume state');
//			}
			
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
		this.getApplication().getController('Order').showCart();
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
		var orders = this.getApplication().getController('CheckIn').models.activeCheckIn.orders(), 
		badgeText,
		removedOrder,
		cartButton = this.getLoungeTabBar().getAt(2);
		
		if(orders.data.length > 0) {
			removedOrder = orders.getAt(orders.data.length-1).getProduct().get('name');
			orders.removeAt(orders.data.length - 1);
			badgeText = (orders.data.length > 0) ? orders.data.length : "";
			cartButton.setBadgeText(badgeText);
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

