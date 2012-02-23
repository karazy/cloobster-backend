Ext.define('EatSense.controller.Order', {
	extend: 'Ext.app.Controller',
	config: {
		refs: {
			main : 'mainview',
			cartview : '#cart',
			cartoverview: 'cartoverview',
			cartoverviewTotal: 'cartoverview #carttotalpanel label',
			myordersTotal : 'myorders #myorderstotalpanel label',
			menutab: '#menutab',
			orderlist : '#cartCardPanel #orderlist',
			backBt : '#cartTopBar #cartBackBt',
			cancelOrderBt : '#cartTopBar #bottomTapCancel',
			submitOrderBt : '#cartTopBar #bottomTapOrder',
			topToolbar : '#cartTopBar',
			productdetail : '#cartCardPanel #cartProductdetail',						
			editOrderBt : 'cart #cartCardPanel #cartProductdetail #prodDetailcartBt',
			amountSpinner: '#cartCardPanel #cartProductdetail panel #productAmountSpinner',
			prodDetailLabel :'#cartCardPanel #cartProductdetail #prodDetailLabel' ,	
			loungeview : 'lounge',
			//the orderlist shown in lounge in myorders tab lounge tab #myorderstab
			myorderlist: '#myorderlist',
			myordersview: '#myorderstab #myorders',
			myorderstab: '#myorderstab',
			loungeTabBar: '#loungeTabBar'
		},
		/**
		 * Tooltip menu, shown when user taps an order
		 */
		tooltip : ''				
	},
	init: function() {
		 
		this.control({
			 cancelOrderBt : {
				 tap: this.dumpCart
			 }, 
			 submitOrderBt : {
				 tap: this.submitOrders
			 },
			 orderlist : {
				 itemtap: this.cartItemContextMenu
			 },
			 editOrderBt : {
				tap: this.editOrder
			 },
			 backBt : {
            	 tap: function() {
            		 if(this.menuBackBtContext != null) {
            			 console.log('Cart Controller -> menuBackBtContext');
            			 this.menuBackBtContext();
            		 }
            	 }
             },
             amountSpinner : {
            	 spin: this.amountChanged
             }
		 });
		
		//store retrieved models
		 var models = {};
    	 this.models = models;
    	 
    	 //create tooltip for reuse
    	 var tooltip = Ext.create('EatSense.util.CartToolTip');
    	 this.setTooltip(tooltip);
	},
	/**
	 * Load cart orders.
	 * @return
	 * 		<code>false</code> if cart is empty, <code>true</code> otherwise
	 */
	refreshCart: function() {
		console.log('Cart Controller -> showCart');
		var cartview = this.getCartview(), orderlist = this.getOrderlist(),
		orders = this.getApplication().getController('CheckIn').models.activeCheckIn.orders(),
		total = 0;
		//only switch if cart is not empty		
//		if(orders.data.length == 0) {
//			Ext.Msg.alert(i18nPlugin.translate('hint'),i18nPlugin.translate('cartEmpty'), Ext.emptyFn);
//			return false;
//		} else {
			cartview.hideBackButton();
			
			//set filter TEST
	    	orders.filter([
	    	               {property: "status", value: "XYZ"}   	               
	    	]);
	    	
			orderlist.setStore(orders);	
			this.models.activeOrder = null;
			orderlist.refresh();

			
			total = this.calculateOrdersTotal(orders);			
			this.getCartoverviewTotal().getTpl().overwrite(this.getCartoverviewTotal().element, [total]);
			this.refreshCartBadgeText();
			return true;
//		}				
	},
	/**
	 * Switch to cart. Method gets called when editing an order.
	 */
	showCart: function() {
		var orderlist = this.getOrderlist();
		
		orderlist.refresh();
		this.switchView(this.getCartoverview(), i18nPlugin.translate('cartviewTitle'), null, 'right');
	},
	
	/**
	 * Show menu.
	 */
	showMenu: function() {
		console.log('Cart Controller -> showMenu');
		var lounge = this.getLoungeview(), menu = this.getMenutab();		
		lounge.setActiveItem(menu);
	},
	/**
	 * Remove all orders from cart and switch back to menu.
	 */
	dumpCart: function() {
		console.log('Cart Controller -> dumpCart');
		var orders = this.getApplication().getController('CheckIn').models.activeCheckIn.orders();
		
		Ext.Msg.show({
			title: i18nPlugin.translate('hint'),
			message: i18nPlugin.translate('dumpCart'),
			buttons: Ext.MessageBox.YESNO,
			scope: this,
			fn: function(btnId, value, opt) {
			if(btnId=='yes') {
				//workaround, because view stays masked after switch to menu
				Ext.Msg.hide();
				//clear store				
				orders.removeAll();
				//reset badge text on cart button and switch back to menu
				this.refreshCart();
				}
			}
		});				
	},
	/**
	 * Submits orders to server.
	 */
	submitOrders: function() {
		console.log('Cart Controller -> submitOrders');
		var checkIn = this.getApplication().getController('CheckIn').models.activeCheckIn, 
		orders = this.getApplication().getController('CheckIn').models.activeCheckIn.orders(),
		checkInId = checkIn.get('userId'),
		restaurantId = checkIn.get('restaurantId'),
		errorIndicator = false,
		orderlist = this.getOrderlist(),
//		orderStore = Ext.data.StoreManager.lookup('orderStore'),
		cartview = this.getCartview(),
		ajaxOrderCount = 0,
		ordersCount = orders.getCount();
//		loungeview = this.getLoungeview(),
//		myordersview = this.getMyordersview(),
		me = this;
		
		if(ordersCount > 0) {
			Ext.Msg.show({
				title: i18nPlugin.translate('hint'),
				message: i18nPlugin.translate('submitOrdersQuestion'),
				buttons: Ext.MessageBox.YESNO,
				scope: this,
				fn: function(btnId, value, opt) {
				if(btnId=='yes') {
					
					cartview.showLoadScreen(true);
					this.getSubmitOrderBt().disable();
					this.getCancelOrderBt().disable();
					
					orders.each(function(order) {
						console.log('save order' + order.getProduct().get('name'));
						
						if(!errorIndicator) {
					
							Ext.Ajax.request({				
					    	    url: globalConf.serviceUrl+'/restaurants/'+restaurantId+'/orders/',
					    	    method: 'POST',    	    
					    	    params: {
					    	    	'checkInId' : checkInId,
					    	    },
					    	    jsonData: order.getRawJsonData(),
					    	    scope: this,
					    	    success: function(response) {
					    	    	console.log('Saved order checkin.');
					    	    	ajaxOrderCount++;
					    	    	//set generated id
					    	    	order.set('id', response.responseText);
					    	    	order.set('status','PLACED');
					    	    	orders.remove(order);
					    	    	
					    	    	//TODO remove orders or filter them just filter them! load orders from server?
//					    	    	orders.each(function(order) {
//					    	    	orderStore.add(order);
//					    	    	});	
//					    	    	orderStore.add(order);		    	    		    	    			    	    			    	    			    	    
					    	    	
					    	    	if(ajaxOrderCount == ordersCount) {		    	    					    	    	
						    	    	me.refreshCart();
						    	    	cartview.showLoadScreen(false);
						    	    	me.getSubmitOrderBt().enable();
						    	    	me.getCancelOrderBt().enable();
				//		    	    	loungeview.switchTab(myordersview);
				//		    			loungeview.setActiveItem(myordersview);
						    	    	
						    	    	//show success message and switch to next view
						    			Ext.Msg.show({
						    				title : i18nPlugin.translate('success'),
						    				message : i18nPlugin.translate('orderSubmit'),
						    				buttons : []
						    			});
						    			//show short alert and then hide
						    			Ext.defer((function() {
						    				Ext.Msg.hide();
						    			}), globalConf.msgboxHideTimeout, this);
						    			
						    			
					    	    	}
					    	    },
					    	    failure: function(response) {
					    	    	errorIndicator = true;
					    	    	cartview.showLoadScreen(false);
					    	    	me.getSubmitOrderBt().enable();
					    	    	me.getCancelOrderBt().enable();
					    	    	Ext.Msg.alert(i18nPlugin.translate('errorTitle'), i18nPlugin.translate('errorMsg'), Ext.emptyFn);
					    	    }
							});			
						}	else {
							me.getSubmitOrderBt().enable();
							me.getCancelOrderBt().enable();
							cartview.showLoadScreen(false);
							return false;
						};					
					});

					}
				}
			});		
			
			

	}
	},
	/**
	 * Listener for itemTap event of orderlist.
	 * Show a tooltip with buttons to edit, delete the selected item.
	 */
	cartItemContextMenu: function(dv, number, dataitem, model, event, opts) {
		console.log('Cart Controller -> cartItemContextMenu');
		var x = event.pageX,
		y = event.pageY,
		tooltip = this.getTooltip(),
		orderlist = this.getOrderlist(),
		orders = this.getApplication().getController('CheckIn').models.activeCheckIn.orders(),
		productName = model.getProduct().get('name'),
		windowX = Ext.Viewport.getWindowWidth();
		
		tooltip.setSelectedProduct(model);
		
		dv.deselect(tooltip.getSelectedProduct());
		//position tooltip where tap happened
		//- dataitem.getHeight()/2
		tooltip.setTop(y);		
		tooltip.setLeft(((tooltip.getWidth()+x+5)<windowX)? x : windowX-tooltip.getWidth()-5 );
		
		//edit item
		tooltip.getComponent('editCartItem').addListener('tap', function() {
			tooltip.hide();
			this.showOrderDetail(dv, tooltip.getSelectedProduct());
		}, this);
		//dump item
		tooltip.getComponent('deleteCartItem').addListener('tap', function() {
			tooltip.hide();
			//delete item
			orders.remove(tooltip.getSelectedProduct());

			this.refreshCart();
			
			if(orders.data.length > 0) {
				orderlist.refresh();
			} else {
				this.showMenu();
			}
			
			//show success message and switch to next view
			Ext.Msg.show({
				title : i18nPlugin.translate('orderRemoved'),
				message : productName,
				buttons : []
			});
			//show short alert and then hide
			Ext.defer((function() {
				Ext.Msg.hide();
			}), globalConf.msgboxHideTimeout, this);

		}, this);
		
		tooltip.show();
		this.getMain().add(tooltip);
	},
	/**
	 * Displays detailed information for an existing order (e.g. Burger)
	 * @param dataview
	 * @param order
	 */
	showOrderDetail: function(dataview, order) {
		console.log("Cart Controller -> showProductDetail");		
		 var detail = this.getProductdetail(), 
		 choicesPanel =  this.getProductdetail().getComponent('choicesWrapper').getComponent('choicesPanel'),
		 product = order.getProduct();
		 this.models.activeOrder = order;

		this.getProductdetail().getComponent('choicesWrapper').getComponent('choicesPanel').removeAll(false);
		 //reset product spinner
		 this.getAmountSpinner().setValue(order.get('amount'));
		 this.getProdDetailLabel().getTpl().overwrite(this.getProdDetailLabel().element, {product: product, amount: this.getAmountSpinner().getValue()});
		 //dynamically add choices if present		 
		 if(typeof product.choices() !== 'undefined' && product.choices().getCount() > 0) {
			 product.choices().each(function(_choice) {
				 var choice = _choice;				 			 
				 var optionsDetailPanel = Ext.create('EatSense.view.OptionDetail');
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
						 name : choice.data.id,
//						 value : opt,
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
						 this.recalculate(this.models.activeOrder);
					 },this);
					 checkbox.addListener('uncheck',function(cbox) {
						 console.log('uncheck');
						 if(cbox.isXType('checkboxfield',true)) {
							 opt.set('selected', false);
						 } else {
							 //don't allow radio buttons to be deselected
							 cbox.setChecked(true);
						 }
						 this.recalculate(this.models.activeOrder);								 
					 },this);
					 optionsDetailPanel.getComponent('optionsPanel').add(checkbox);					 
				 },this);	 
				 choicesPanel.add(optionsDetailPanel);
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
			value: order.get('comment')
			}
		);
		 this.menuBackBtContext = this.editOrder;

		 this.switchView(detail, Karazy.util.shorten(product.get('name'), 15, true), i18nPlugin.translate('back'), 'left');
	},
	
	editOrder : function() {
		var product = this.models.activeOrder.getProduct(), validationError = "", productIsValid = true;
		
		product.choices().each(function(choice) {
			if(choice.validateChoice() !== true) {
				//coice is not valid
				productIsValid = false;
				validationError += choice.get('text') + '<br/>';
			}
		});
		
		this.models.activeOrder.set('comment', this.getProductdetail().getComponent('choicesWrapper').getComponent('choicesPanel').getComponent('productComment').getValue());
		
		if(productIsValid) {
			this.refreshCart();
			this.showCart();
		} else {
			//show validation error
			Ext.Msg.alert(i18nPlugin.translate('orderInvalid'),validationError, Ext.emptyFn);
		}
		
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
		console.log('Cart Controller -> switchView');
		var panel = this.getCartview();
    	this.getTopToolbar().setTitle(title);
    	(labelBackBt == null || labelBackBt.length == 0) ? panel.hideBackButton() : panel.showBackButton(labelBackBt);
    	panel.switchView(view,direction);
	},
	
	/**
	 * Called when the product spinner value changes. 
	 * Recalculates the price.
	 * @param spinner
	 * @param value
	 * @param direction
	 */
	amountChanged: function(spinner, value, direction) {
		console.log('Cart Controller > amountChanged (value:'+value+')');
		this.models.activeOrder.set('amount', value);
		this.recalculate(this.models.activeOrder);
	},
	/**
	 * Recalculates the total price for the active product.
	 */
	recalculate: function(order) {
		console.log('Cart Controller -> recalculate');
		this.getProdDetailLabel().getTpl().overwrite(this.getProdDetailLabel().element, {product: order.getProduct(), amount: order.get('amount')});
	},
	/**
	 * Refreshes the badge text on cart tab icon.
	 * Displays the number of orders.
	 */
	refreshCartBadgeText: function() {
		var cartButton = this.getLoungeTabBar().getAt(1),
		orders = this.getApplication().getController('CheckIn').models.activeCheckIn.orders(),
		badgeText;
		
		badgeText = (orders.data.length > 0) ? orders.data.length : "";
		
		cartButton.setBadgeText(badgeText);
	},
	/**
	 * Refresh myorderlist and recalculate the total price.
	 */
	refreshMyOrdersList: function() {
		var myorderlist = this.getMyorderlist(),
		orderStore = Ext.data.StoreManager.lookup('orderStore'),
		checkInId = this.getApplication().getController('CheckIn').models.activeCheckIn.get('userId'),
		me = this;
		
		//TODO investigate if this is a bug
		orderStore.removeAll();
		myorderlist.getStore().removeAll()
		
		myorderlist.getStore().load({
			scope   : this,
			params : {
				'checkInId' : checkInId,				
			},
			callback: function(records, operation, success) {
				try {
					if(success == true) {
						//refresh the order list
						total = me.calculateOrdersTotal(orderStore);
						myorderlist.refresh();
						me.getMyordersTotal().getTpl().overwrite(me.getMyordersTotal().element, [total]);
					}	
				} catch(e) {
					
				}
				
				me.getMyordersview().showLoadScreen(false);
			}
		});
		
	},
	/**
	 * Calculates and returns the total price of all orders.
	 * 
	 * @param orderStore
	 * 		An order store instance for which to calculate the total price.
	 * 
	 * @return
	 * 		total price or 0 if an error occured or no orders exist.
	 */
	calculateOrdersTotal: function(orderStore) {
		var total = 0;
		
		if(orderStore != null && orderStore !== 'undefined') {
			orderStore.each(function(order) {
				total += order.calculate();
				total = Math.round(total * 100) / 100;
			});
		}
			
		return total;
	}
	
});

Ext.define('EatSense.util.CartToolTip', {
	extend: 'Ext.Panel',
	xtype: 'cartToolTip',
	config: {
		layout: {
			type: 'hbox'
		},
		centered: true,
		width: 150,
		height:70,
		modal: true,
		selectedProduct : null,
		hideOnMaskTap: true,
		defaults : {
			margin: 5
		},
		items: [ {
			xtype: 'button',
			itemId: 'editCartItem',
			iconCls : 'compose',
			iconMask : true,
			flex: 1,
		}, {
			xtype: 'spacer',
			width: 7
		} ,{
			xtype: 'button',
			itemId: 'deleteCartItem',
			iconCls : 'trash',
			iconMask : true,
			flex: 1	,
		}]
	}	
});