Ext.define('EatSense.controller.Cart', {
	extend: 'Ext.app.Controller',
	config: {
		refs: {
			main : 'mainview',
			cartview : 'cart',
			cartoverview: 'cartoverview',
			cartoverviewTotal: 'cartoverview #carttotalpanel label',
			menuview: 'menu',
			orderlist : '#cartCardPanel #orderlist',
			backBt : '#cartTopBar #cartBackBt',
			cancelOrderBt : '#cartBottomBar #bottomTapCancel',
			submitOrderBt : '#cartBottomBar #bottomTapOrder',
			topToolbar : '#cartTopBar',
			productdetail : '#cartCardPanel #productdetail',
			editOrderBt : '#cartCardPanel #productdetail #prodDetailCardBt',
			amountSpinner: '#cartCardPanel #productdetail panel #productAmountSpinner',
			prodDetailLabel :'#cartCardPanel #productdetail #prodDetailLabel' ,	
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
	
	showCart: function() {
		console.log('Cart Controller -> showCart');
		var main = this.getMain(), cartview = this.getCartview(), orderlist = this.getOrderlist(),
		orders = this.getApplication().getController('CheckIn').models.activeCheckIn.orders(),
		total = 0;
		//only switch if cart is not empty		
		if(orders.data.length == 0) {
			Ext.Msg.alert(i18nPlugin.translate('hint'),i18nPlugin.translate('cartEmpty'), Ext.emptyFn);
		} else {
			this.menuBackBtContext = this.showMenu;			
			//switch to cart coming from menu
			if(main.getActiveItem() != cartview) {
				//add all orders to cart list
				orderlist.setStore(orders);				
				main.switchAnim('left');
				main.setActiveItem(cartview);
			} else {
				//allready in cart view
				this.models.activeOrder = null;
				orderlist.refresh();
				this.switchView(this.getCartoverview(), i18nPlugin.translate('cartviewTitle'), i18nPlugin.translate('back'), 'right');
			} 
			
			orders.each(function(order) {
				total += order.calculate();
				total = Math.round(total * 100) / 100;
			});
			
			this.getCartoverviewTotal().getTpl().overwrite(this.getCartoverviewTotal().element, [total]);
			
		}				
	},
	
	showMenu: function() {
		console.log('Cart Controller -> showMenu');
		var main = this.getMain(), menu = this.getMenuview();		
		main.switchAnim('right');
		main.setActiveItem(menu);
	},
	
	dumpCart: function() {
		console.log('Cart Controller -> dumpCart');
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
				var orders = this.getApplication().getController('CheckIn').models.activeCheckIn.orders();
				orders.removeAll();
				//reset badge text on cart button and switch back to menu
				this.getApplication().getController('Menu').getCardBt().setBadgeText('');

					if(orders.data.length > 0) {
						this.showCart();
					} else {
						this.showMenu();
					}
				
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
		orders = this.getApplication().getController('CheckIn').models.activeCheckIn.orders();
		
		orders.each(function(order) {
			console.log('save order' + order.getProduct().get('name'));
			order.save({
				params: {
					checkinId: checkIn.get('userId')
				},
				success: function(response) {
					console.log('saved '+response);
//					Ext.Msg.show({
//						title : i18nPlugin.translate('hint'),
//						message : 'test test',
//						buttons : []
//					});
//					//show short alert and then hide
//					Ext.defer((function() {
//						Ext.Msg.hide();
//					}), globalConf.msgboxHideTimeout, this);
				}
				
			});
		});
		
	//set orders to send and switch view
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
		badgeText;
		
		dv.deselect(model);
		//position tooltip where tap happened
		tooltip.setTop(y - dataitem.getHeight()/2);
		tooltip.setLeft(x);
		//edit item
		tooltip.getComponent('editCartItem').addListener('tap', function() {
			tooltip.hide();
			this.showOrderDetail(dv, model);
		}, this);
		//dump item
		tooltip.getComponent('deleteCartItem').addListener('tap', function() {
//			tooltip.hide();
			this.getMain().remove(tooltip);
			Ext.Msg.show({
				title: i18nPlugin.translate('hint'),
				message: i18nPlugin.translate('dumpItem', model.getProduct().get('name')),
				buttons: Ext.MessageBox.YESNO,
				scope: this,
				fn: function(btnId, value, opt) {
				if(btnId=='yes') {
						//workaround, because view stays masked after switch to menu
						Ext.Msg.hide();
						//delete item
						orders.remove(model);
						badgeText = (orders.data.length > 0) ? orders.data.length : "";
						//reset badge text on cart button and switch back to menu
						this.getApplication().getController('Menu').getCardBt().setBadgeText(badgeText);
						if(orders.data.length > 0) {
							orderlist.refresh();
						} else {
							this.showMenu();
						}
					}
				}
			});	
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
						 this.recalculate(this.models.activeOrder);
					 },this);
					 checkbox.addListener('uncheck',function(cbox) {
						 console.log('uncheck');
						 if(cbox.type == 'Ext.field.Checkbox') {
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
		 this.menuBackBtContext = this.showCart;

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
		
		//WORKAROUND
		//because options select doesn't get correctly set after copy of object
//		product.choices().each(function(choice, cIndex) {
//			choice.options().each(function(option, oIndex) {
//				product.data.choices[cIndex].options[oIndex].selected = option.get('selected');
//			});
//		});
		//WORKAROUND _ END	
		
		if(productIsValid) {
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
	
	calculateTotal : function() {
		
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
		width: 120,
		height:50,
		modal: true,
		hideOnMaskTap: true,
		items: [ {
			xtype: 'button',
			itemId: 'editCartItem',
			iconCls : 'compose',
			iconMask : true,
			flex: 1	
		}, {
			xtype: 'spacer',
			width: 7
		} ,{
			xtype: 'button',
			itemId: 'deleteCartItem',
			iconCls : 'trash',
			iconMask : true,
			flex: 1				
		}]
	}	
});