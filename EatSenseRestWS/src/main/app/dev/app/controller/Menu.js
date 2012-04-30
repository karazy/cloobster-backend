/**
 * The menu controller handles everything related to the menu.
 * - Navigation in menu structure
 * - choosing products and put them into the card
 * - configuring products e.g. choosing options
 */
Ext.define('EatSense.controller.Menu', {
    extend: 'Ext.app.Controller',    
    config: {
		refs: {
	        main : 'mainview', 
        	menulist :'menuoverview list',        	
        	productlist :'#menuCardPanel #productlist',        	
        	productoverview :'productoverview' ,	     
        	menuoverview :'menuoverview' ,	       
        	productdetail :{
                selector: 'tabpanel panel[name=menu] productdetail',
                xtype: 'productdetail',
                autoCreate: true
            },	        
        	prodDetailLabel :'productdetail #prodDetailLabel' ,	 
        	prodPriceLabel :'productdetail #prodPriceLabel' ,    
        	amountSpinner: 'productdetail spinnerfield',
        	createOrderBt :'productdetail button[action="cart"]',
        	closeProductDetailBt: 'productdetail button[action=close]',
        	menuview: 'menutab',
        	productcomment: 'productdetail #productComment',
        	backBt: 'menutab button[action=back]',
        	topToolbar: 'menutab #menuTopBar',
        	loungeview: 'lounge',
        	loungeTabBar: 'lounge tabbar',
		},

		control: {
			menulist: {
             	select: 'showProductlist'
             },
             productlist : {
            	select: 'loadProductDetail' 
             },
             createOrderBt : {
            	 tap: 'createOrder'
             },
             closeProductDetailBt: {
             	tap: 'closeProductDetail'
             },
             backBt : {
            	 tap: 'backToMenu'
             },
             amountSpinner : {
            	 spin: 'amountChanged'
             },
             //TODO refactor general loungeview control into another controller?!
             loungeview : {
     			activeitemchange : function(container, value, oldValue, opts) {
    				console.log('tab change for %s', value.getItemId());
    				if(value.getItemId() === 'carttab') {
    					status = this.getApplication().getController('Order').refreshCart();
    				} else if (value.getItemId() === 'myorderstab') {
    					this.getApplication().getController('Order').refreshMyOrdersList();
    				}

    				return status;
    			}
    		},
		},
		/**
		*	Current selected menu.
		*/
		activeMenu: null,
		/**
		*	Current selected product.
		*/
		activeProduct: null
    },
    /**
     * Shows the products of a menuitem
     * e. g. Beverages, Drinks, Burgers
     */
    showProductlist: function(dataview, record) {
    	console.log("Menu Controller -> showProductlist");
    	var pov = this.getProductoverview(),
    		prodStore = record.productsStore;

    	this.setActiveMenu(record);
    	this.getProductlist().setStore(prodStore);
    	this.getMenulist().refresh();
    	this.switchView(pov, record.get('title'), Karazy.i18n.translate('back'), 'left');
    },
    /**
    *	Load menus and products and show menutab.
    *
    */
    showMenu: function() {
    	var me = this,
    	    menu = this.getMenuview(), 
    		lounge = this.getLoungeview(),
    		main = this.getMain(),
    		checkInCtr = this.getApplication().getController('CheckIn'),
    		businessId = Ext.String.trim(checkInCtr.getActiveCheckIn().get('businessId')),
    		menuStore = Ext.StoreManager.lookup('menuStore');
		 
		if(businessId.toString().length != 0) {
			menuStore.load({
				scope   : this,
				params: {
					'includeProducts' : true,
					'pathId': businessId
				},
			    callback: function(records, operation, success) {
			    	if(!success) { 
                        me.getApplication().handleServerError({
                        	'error': operation.error, 
                        	'forceLogout': {403:true}
                        }); 
                    }
			    }
			 });

            //always show menuoverview on first access
            //TODO schoener loesen
            menu.getComponent('menuCardPanel').setActiveItem(0);
            menu.hideBackButton();
            main.switchAnim('left');
            main.setActiveItem(lounge);
		}
    },
    /**
     * Shows the menu. At this point the store is already filled with data.
     */
	backToMenu: function() {
		 this.switchView(this.getMenuoverview(), Karazy.i18n.translate('menuTitle'), null, 'right');
	},
	/**
	 * Displays detailed information for a product (e.g. Burger)
	 * @param dataview
	 * @param record
	 */
	loadProductDetail: function(dataview, record) {
		var me = this,
			detail = this.getProductdetail(), 
			main = this.getMain(), 
			menu = this.getMenuview(), 
			choicesPanel =  this.getProductdetail().getComponent('choicesPanel'),
			titlebar = detail.down('titlebar'),
			activeProduct;

		//save original ids
		record.set('genuineId', record.get('id'));
		record.choices().each(function(choice) {
			choice.set('genuineId', choice.get('id'));
			choice.options().each(function(opt) {
				opt.set('genuineId', opt.get('id'));
			});
		});
		this.setActiveProduct(record.copy());
		activeProduct = this.getActiveProduct()

		choicesPanel.removeAll(false);

		titlebar.setTitle(activeProduct.get('name'));

		 //reset product spinner
		 this.getAmountSpinner().setValue(1);
		 this.getProdDetailLabel().getTpl().overwrite(this.getProdDetailLabel().element, {product: activeProduct, amount: this.getAmountSpinner().getValue()});
		 this.getProdPriceLabel().getTpl().overwrite(this.getProdPriceLabel().element, {product: activeProduct, amount: this.getAmountSpinner().getValue()});

		 //dynamically add choices and dependend choices if present		 
		 if(typeof activeProduct.choices() !== 'undefined' && activeProduct.choices().getCount() > 0) {
		 	 //render all main choices
		 	 activeProduct.choices().queryBy(function(rec) {
		 	 	if(!rec.get('parent')) {
		 	 		return true;
		 	 	}}).each(function(choice) {
					var optionsDetailPanel = Ext.create('EatSense.view.OptionDetail');

					optionsDetailPanel.getComponent('choiceTextLbl').setHtml(choice.data.text);
					//recalculate when selection changes
					choice.on('recalculate', function() {
						me.recalculate(activeProduct);
					});

					me.createOptions(choice, optionsDetailPanel);
					//process choices assigned to a this choice
					activeProduct.choices().queryBy(function(rec) {
						if(rec.get('parent') == choice.get('id')) {
							return true;
						}
					}).each(function(memberChoice) {
						memberChoice.setParentChoice(choice);
						me.createOptions(memberChoice, optionsDetailPanel, choice);
						//recalculate when selection changes
						memberChoice.on('recalculate', function() {
							me.recalculate(activeProduct);
						});
					});

					choicesPanel.add(optionsDetailPanel);
		 	 });
		 }
		 
		//insert comment field after options have been added so it is positioned correctly
		choicesPanel.add({
			xtype: 'textareafield',
			label: Karazy.i18n.translate('orderComment'),
			labelAlign: 'top',
			itemId: 'productComment',
			maxRows: 4,
			value: '',
			cls: 'choice'
		});
		 
		Ext.Viewport.add(detail);
		detail.getScrollable().getScroller().scrollToTop();
		detail.show();
	},
	/**
	* @private
	* Creates Ext.field.Radio and Ext.field.Checkbox option elements and adds them to given panel.
	* @param choice
	*	Choice containing options to create.
	* @param panel
	*	Panel to add options to
	* @param parentChoice
	*	parent to given choice
	*/
	createOptions: function(choice, panel, parentChoice) {
		if(!choice || !panel) {
			console.log('You have to provide options and panel')
			return;
		}

		var me = this,
			optionType = '',
			field,
			isChecked;

		if(choice.get('minOccurence') <= 1 && choice.get('maxOccurence') == 1) {
			optionType = 'Ext.field.Radio';
		} 
		else {//multiple choice
			optionType = 'Ext.field.Checkbox';					 
		}

		choice.options().each(function(opt) {
			 field = Ext.create(optionType, {
				 			name : choice.get('id'),
				 			labelWidth: '80%',
							label : opt.get('name'),
							checked: opt.get('selected'),
							cls: 'option',
							disabled: (parentChoice && !parentChoice.isActive()) ? true : false
					}, me);							 
			//TODO this is sooo dirty
			field.addListener('check',function(cbox, event) {
			 	console.log('check');
				if(cbox.isXType('radiofield',true)) {				 	
					choice.options().each(function(innerOpt) {
						if(innerOpt != opt) {
					 		innerOpt.set('selected', false);	
					 	}
					});
					if(choice.get('minOccurence') == 0) {
				 	 	cbox.setChecked(!opt.get('selected'));
				 	 	opt.set('selected', !opt.get('selected'));
				 	} else {
				 		opt.set('selected', true);
				 	}
				 } else {
				 	opt.set('selected', true);	
				 }
				 if(!parentChoice) {
				 	choice.isActive();
				 }
				 choice.fireEvent('recalculate');
				 // me.recalculate(productOrOrder);
			 },me);

			 field.addListener('uncheck',function(cbox) {
			 	console.log('uncheck');
				 if(cbox.isXType('checkboxfield',true)) {
					 opt.set('selected', false);
				 }
				 if(!parentChoice) {
				 	choice.isActive();
				 }
				 choice.fireEvent('recalculate');
				 // me.recalculate(productOrOrder);								 
			 },me);
			 panel.getComponent('optionsPanel').add(field);

			 if(parentChoice) {
			 	parentChoice.on('activeChanged', function(isActive) {			 		
			 		field.setDisabled(!isActive);
			 		if(!isActive) {
				 		//TODO leave active?
				 		field.uncheck();
				 		opt.set('selected', false);			 		
			 		}
			 	});
			 }				 
		});
	},
	/**
	*	Hides Product detail.
	*/
	closeProductDetail: function() {
		var detail = this.getProductdetail();		
		detail.hide();
	},
	/**
	 * Adds the current product to card.
	 * @param button
	 */
	createOrder: function(button) {
		//get active product and set choice values
		var me = this,	
			productForCart = this.getActiveProduct(),
			order,
			validationError = "",
			cartButton = this.getLoungeTabBar().getAt(1),
			productIsValid = true,
			appState = this.getApplication().getController('CheckIn').getAppState(),
			appStateStore = Ext.StoreManager.lookup('appStateStore'),
			activeCheckIn = this.getApplication().getController('CheckIn').getActiveCheckIn(),
			detail = this.getProductdetail(),
			message;
		
		//validate choices 
		productForCart.choices().each(function(choice) {
			//only validate dependend choices if parent choice is active!
			if(!choice.get('parent') || choice.getParentChoice().isActive()) {
				if(choice.validateChoice() !== true) {
					//coice is not valid
					productIsValid = false;
					validationError += choice.get('text') + '<br/>';
				}
			};
		});
		
		if(productIsValid === true) {
			order = Ext.create('EatSense.model.Order');
			order.set('amount', this.getAmountSpinner().getValue());
			order.set('status','CART');
			productForCart.getData(true);
			order.setProduct(productForCart);
			
			order.set('comment', this.getProductdetail().getComponent('choicesPanel').getComponent('productComment').getValue());
			//if valid create order and attach to checkin
			activeCheckIn.orders().add(order);
			
			Ext.Ajax.request({
	    	    url: Karazy.config.serviceUrl+'/c/businesses/'+activeCheckIn.get('businessId')+'/orders/',
	    	    method: 'POST',
	    	    jsonData: order.getRawJsonData(),
	    	    success: function(response, operation) {
	    	    	order.setId(response.responseText);
	    	    	order.phantom = false;
	    	    },
	    	    failure: function(response, operation) {
	    	    	me.getApplication().handleServerError({
                        	'error': { 'status' : response.status, 'statusText' : response.statusText}, 
                        	'forceLogout': {403:true}
                    }); 
	    	    }
	    	});
			
			cartButton.setBadgeText(activeCheckIn.orders().data.length);
			
			detail.hide();
			message = Karazy.i18n.translate('productPutIntoCardMsg', this.getActiveProduct().get('name'));
			this.setActiveProduct(null);

			this.getProductdetail().getComponent('choicesPanel').removeAll(false);
			

			if (message) {
				Ext.Msg.show({
					title : Karazy.i18n.translate('orderPlaced'),
					'message' : message,
					buttons : []
				});
				//show short alert and then hide
				Ext.defer((function() {
					if(!Karazy.util.getAlertActive()) {
						Ext.Msg.hide();
					}					
				}), Karazy.config.msgboxHideTimeout, this);
			}
		} else {
			//show validation error
			Ext.Msg.alert(Karazy.i18n.translate('orderInvalid'),validationError, Ext.emptyFn);
		}
		
	},
	/**
	 * Switches to card view.
	 */
	showCart: function(){		
		this.getApplication().getController('Order').showCart();
	},
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
	 * Recalculates the total price for the active product.
	 */
	recalculate: function(product) {
		console.log('Menu Controller -> recalculate');
		this.getProdPriceLabel().getTpl().overwrite(this.getProdPriceLabel().element, {product: product, amount:  this.getAmountSpinner().getValue()});
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
		this.recalculate(this.getActiveProduct());
	},

     	
});

