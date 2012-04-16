	Ext.define('EatSense.controller.Order', {
	extend: 'Ext.app.Controller',
	config: {
		refs: {
			main : 'mainview',
			cartview : '#cart',
			cartoverview: 'cartoverview',
			cartoverviewTotal: 'cartoverview #carttotalpanel label',
			myordersTotal : 'myorders #myorderstotalpanel label',
			myordersComplete: 'myorders #myorderscompletepanel',
			myordersCompleteButton: 'myorders button[action=complete]',
			menutab: '#menutab',
			// orderlist : '#cartCardPanel #orderlist',
			orderlist : 'cartoverview #orderlist',
			backBt : '#cartTopBar button[action="back"]',
			cancelAllOrdersBt : '#cartTopBar button[action="trash"]',
			submitOrderBt : '#cartTopBar button[action="order"]',
			topToolbar : '#cartTopBar',
			// productdetail : '#cartCardPanel #cartProductdetail',	
			productdetail : {
                selector: 'orderdetail',
                xtype: 'orderdetail',
                autoCreate: true
            },
			choicespanel : 'orderdetail #choicesPanel',
			// editOrderBt : 'cart #cartCardPanel #cartProductdetail #prodDetailcartBt',
			editOrderBt : 'cartoverviewitem button[action=edit]',
			cancelOrderBt : 'cartoverviewitem button[action=cancel]',
			// amountSpinner: '#cartCardPanel #cartProductdetail panel #productAmountSpinner',
			amountSpinner : 'orderdetail spinnerfield',
			prodDetailLabel :'orderdetail #prodDetailLabel' ,
			prodPriceLabel :'orderdetail #prodPriceLabel' ,
			closeOrderDetailBt: 'orderdetail button[action=close]',
			loungeview : 'lounge',
			//the orderlist shown in lounge in myorders tab lounge tab #myorderstab
			myorderlist: '#myorderlist',
			myordersview: '#myorderstab #myorders',
			myorderstab: '#myorderstab',
			loungeTabBar: '#loungeTabBar',
			paymentButton: '#myorderstab button[action="pay"]',
			leaveButton: '#myorderstab button[action="leave"]',
			confirmEditButton: 'orderdetail button[action="edit"]',
			undoEditButton: 'orderdetail button[action="undo"]'
		},
		control: {
			cancelAllOrdersBt : {
				 tap: 'dumpCart'
			 }, 
			 submitOrderBt : {
				 tap: 'submitOrders'
			 },
			 // orderlist : {
				//  itemtap: 'cartItemContextMenu'
			 // },
			 editOrderBt : {
				// tap: 'editOrder'
				tap: 'showOrderDetail'
			 },
			 cancelOrderBt : {
			 	tap: 'cancelOrder'
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
            	 spin: 'amountChanged'
             },
             paymentButton: {
            	 tap: 'choosePaymentMethod'
             },
             myordersCompleteButton : {
            	 tap: 'completePayment'
             },
             leaveButton : {
            	 tap: 'leave'
             }, 
             productdetail : {
             	// hide: 'editOrder'
             },
             closeOrderDetailBt: {
             	tap: 'closeOrderDetail'
             },
             confirmEditButton: {
             	tap: 'editOrder'
             },
             undoEditButton: {
             	tap: 'closeOrderDetail'
             }
		}		
	},
	init: function() {
		//store retrieved models
		var 	models = {},
		 		messageCtr = this.getApplication().getController('Message');

    	this.models = models;
    	messageCtr.on('eatSense.order', this.handleOrderMessage, this);
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
		this.hideBackButton();
    	
		orderlist.setStore(orders);	
		this.models.activeOrder = null;
		orderlist.refresh();

		
		total = this.calculateOrdersTotal(orders);			
		this.getCartoverviewTotal().getTpl().overwrite(this.getCartoverviewTotal().element, {'price':total});
		this.refreshCartBadgeText();
		this.toggleCartButtons();
		return true;
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
		var activeCheckIn = this.getApplication().getController('CheckIn').models.activeCheckIn;
		
		Ext.Msg.show({
			title: i18nPlugin.translate('hint'),
			message: i18nPlugin.translate('dumpCart'),
			buttons: [{
				text: 'Ja',
				itemId: 'yes',
				ui: 'action'
			}, {
				text: 'Nein',
				itemId: 'no',
				ui: 'action'
			}],
			scope: this,
			fn: function(btnId, value, opt) {
			if(btnId=='yes') {
				//workaround, because view stays masked after switch to menu
				Ext.Msg.hide();
				activeCheckIn.orders().each(function(order) {
					Ext.Ajax.request({				
			    	    url: Karazy.config.serviceUrl+'/c/businesses/'+activeCheckIn.get('businessId')+'/orders/'+order.getId(),
			    	    method: 'DELETE',    	    
			    	    params: {
			    	    	'checkInId' : activeCheckIn.get('userId'),
			    	    }
			    	});
				});
				
				//clear store				
				activeCheckIn.orders().removeAll();
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
		businessId = checkIn.get('businessId'),
		errorIndicator = false,
		cartview = this.getCartview(),
		ajaxOrderCount = 0,
		ordersCount = orders.getCount();
		me = this;
		
		if(ordersCount > 0) {
			Ext.Msg.show({
				title: i18nPlugin.translate('hint'),
				message: i18nPlugin.translate('submitOrdersQuestion'),
				buttons: [{
					text: 'Ja',
					itemId: 'yes',
					ui: 'action'
				}, {
					text: 'Nein',
					itemId: 'no',
					ui: 'action'
				}],
				scope: this,
				fn: function(btnId, value, opt) {
				if(btnId=='yes') {
					
					cartview.showLoadScreen(true);
					this.getSubmitOrderBt().disable();
					this.getCancelOrderBt().disable();
					
					orders.each(function(order) {
						console.log('save order id:' + order.get('id') + ' genuineId: '+order.getProduct().get('genuineId'));
						
						if(!errorIndicator) {
							order.set('status',Karazy.constants.Order.PLACED);
							
							Ext.Ajax.request({				
					    	    url: Karazy.config.serviceUrl+'/c/businesses/'+businessId+'/orders/'+order.getId(),
					    	    method: 'PUT',    	    
					    	    params: {
					    	    	'checkInId' : checkInId,
					    	    },
					    	    jsonData: order.getRawJsonData(),
					    	    scope: this,
					    	    success: function(response) {
					    	    	console.log('Saved order checkin.');
					    	    	ajaxOrderCount++;					    	    	
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
						    	    	
						    	    	//show success message
						    			Ext.Msg.show({
						    				title : i18nPlugin.translate('success'),
						    				message : i18nPlugin.translate('orderSubmit'),
						    				buttons : []
						    			});
						    			
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
	 * Displays detailed information for an existing order (e.g. Burger)
	 * @param dataview
	 * @param order
	 */	 
	// showOrderDetail: function(dataview, order) {
	showOrderDetail: function(button, eventObj, eOpts) {
		console.log("Cart Controller -> showProductDetail");		
		 var 	detail = this.getProductdetail(), 
		 		choicesPanel =  this.getChoicespanel(),
		 		order = button.getParent().getRecord(),
		 		product = order.getProduct();		 		
		 		this.models.activeOrder = order,
		 		main = this.getMain(),
		 		titlebar = detail.down('titlebar');

		 //save state of order to undo changes
		 order.saveState();

		 choicesPanel.removeAll(false);
		 //reset product spinner
		 this.getAmountSpinner().setValue(order.get('amount'));

		 //set title
		 titlebar.setTitle(product.get('name'));

		 this.getProdDetailLabel().getTpl().overwrite(this.getProdDetailLabel().element, {product: product, amount: this.getAmountSpinner().getValue()});
		 this.getProdPriceLabel().getTpl().overwrite(this.getProdPriceLabel().element, {product: product, amount: this.getAmountSpinner().getValue()});
		 //dynamically add choices if present		 
		 if(typeof product.choices() !== 'undefined' && product.choices().getCount() > 0) {
			 product.choices().each(function(_choice) {
				 var choice = _choice;				 			 
				 var optionsDetailPanel = Ext.create('EatSense.view.OptionDetail');
				 optionsDetailPanel.getComponent('choiceTextLbl').setHtml(choice.data.text);
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
						labelWidth: '80%',
						label : opt.get('name'),
						checked: opt.get('selected'),
						cls: 'option'
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
		}
		 
		 
		 //insert comment field after options have been added so it is positioned correctly
		 choicesPanel.add({
				xtype: 'textfield',
				label: i18nPlugin.translate('orderComment'),
				labelAlign: 'top',
				itemId: 'productComment',
				value: order.get('comment'),
				cls: 'choice'
			}
		);
		Ext.Viewport.add(detail);
		detail.getScrollable().getScroller().scrollToTop();
		detail.show();
	},
	/**
	 * Edit an existing order.
	 */
	editOrder : function(component, eOpts) {
		var order = this.models.activeOrder,
		product = this.models.activeOrder.getProduct(), 
		validationError = "", 
		productIsValid = true,
		activeCheckIn = this.getApplication().getController('CheckIn').models.activeCheckIn,
		detail = this.getProductdetail();
		
		order.getData(true);

		product.choices().each(function(choice) {
			if(choice.validateChoice() !== true) {
				//coice is not valid
				productIsValid = false;
				validationError += choice.get('text') + '<br/>';
			}
		});
		
		if(productIsValid) {
			this.models.activeOrder.set('comment', this.getChoicespanel().getComponent('productComment').getValue());	
		
			Ext.Ajax.request({				
	    	    url: Karazy.config.serviceUrl+'/c/businesses/'+activeCheckIn.get('businessId')+'/orders/'+order.getId(),
	    	    method: 'PUT',
	    	    params: {
	    	    	'checkInId' : activeCheckIn.get('userId'),
	    	    },
	    	    jsonData: order.getRawJsonData()
			});

			detail.hide();
			this.refreshCart();
			return true;
		} else {
			//show validation error
			Ext.Msg.alert(i18nPlugin.translate('orderInvalid'),validationError, Ext.emptyFn, detail);
			if(component) {
				//component exists if this was called by hide listener
				component.show();
			}
			return false;
		}
		
	},

	/**
	*	Deletes a single order.
	* 	Called by cancelButton of an individual order.
	*
	*/
	cancelOrder: function(button, eventObj, eOpts) {
		var 	order = button.getParent().getRecord(),
				activeCheckIn = this.getApplication().getController('CheckIn').models.activeCheckIn,
				productName = order.getProduct().get('name');
			//delete item
			activeCheckIn.orders().remove(order);
			
			Ext.Ajax.request({
	    	    url: Karazy.config.serviceUrl+'/c/businesses/'+activeCheckIn.get('businessId')+'/orders/'+order.getId(),
	    	    method: 'DELETE',
	    	    params: {
	    	    	'checkInId' : activeCheckIn.get('userId'),
	    	    }
	    	});
			
			this.refreshCart();
			
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
	},

	closeOrderDetail: function() {
		var 	detail = this.getProductdetail();
		
		this.models.activeOrder.restoreState();
		//try to avoid unecessary calculation, only needed to update price after cancelation
		this.recalculate(this.models.activeOrder);
		this.refreshCart();
		detail.hide();		
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
		this.getProdPriceLabel().getTpl().overwrite(this.getProdPriceLabel().element, {product: order.getProduct(), amount: order.get('amount')});
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
		var 	me = this,
				myorderlist = me.getMyorderlist(),
				myordersStore = Ext.data.StoreManager.lookup('orderStore'),
				activeCheckIn = me.getApplication().getController('CheckIn').models.activeCheckIn,
				payButton = me.getPaymentButton();
				leaveButton = me.getLeaveButton();
		
		//TODO investigate if this is a bug
		myordersStore.removeAll();
//		myorderlist.getStore().removeAll();
		
		myordersStore.load({
			scope   : this,
			enablePagingParams: false,
			params : {
				'checkInId' : activeCheckIn.get('userId'),
				pathId: activeCheckIn.get('businessId')
			},
			callback: function(records, operation, success) {
				try {
					if(success == true) {
						(myordersStore.getCount() > 0) ? payButton.show() : payButton.hide();
						(myordersStore.getCount() > 0) ? leaveButton.hide() : leaveButton.show();
						
						//WORKAROUND to make sure all data is available in data property
						//otherwise nested choices won't be shown
						Ext.each(records, function(order) {
							order.getProduct().getData(true);
						});

						
						//refresh the order list
						total = me.calculateOrdersTotal(myordersStore);
						myorderlist.refresh();
						me.getMyordersTotal().getTpl().overwrite(me.getMyordersTotal().element, {'price': total});
					} else {
						payButton.disable();
						leaveButton.enable();
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
	},
	/**
	 * Choose a payment method to issue the paymentRequest.
	 */
	choosePaymentMethod: function() {
		console.log('Order Controller -> choosePaymentMethod');
		var availableMethods = this.getApplication().getController('CheckIn').models.activeSpot.payments(),
		orderCount = this.getMyorderlist().getStore().getCount(),
		checkIn = this.getApplication().getController('CheckIn').models.activeCheckIn,
		picker,
		choosenMethod,
		me = this;
		
		if(orderCount>0 && checkIn.get('status') !== Karazy.constants.PAYMENT_REQUEST && checkIn.get('status') !== Karazy.constants.COMPLETE) {
			//create picker
			picker = Ext.create('Ext.Picker', {
				doneButton: {
					text: i18nPlugin.translate('ok'),
					listeners: {
						tap: function() {
							//TODO investigate if bug
							choosenMethod = picker.getValue()['null'];
							picker.hide();						
							me.paymentRequest(choosenMethod);
						}
					}
				},
				cancelButton: {
					text: i18nPlugin.translate('cancel'),
					listeners: {
						tap: function() {
							picker.hide();					
						}
					}
				},
			    slots: [
			        {
			        	align: 'center',
			        	 valueField: 'name',
			             displayField: 'name',
			            title: i18nPlugin.translate('paymentPickerTitle'),
			            store: availableMethods
			        }
			    ]
			});
									
			Ext.Viewport.add(picker);
			picker.show();
		}
	},
	
	/**
	 * Request the payment.
	 * Creates a new bill object and sends via POST to the server. 
	 * CheckIn gets the status PAYMENT_REQUEST and no more orders can be issued.
	 * 
	 * @param paymentMethod
	 * 			The chose payment method.
	 * 
	 */
	paymentRequest: function(paymentMethod) {
		var bill = Ext.create('EatSense.model.Bill'),
		checkInCtr = this.getApplication().getController('CheckIn'),
		checkIn = this.getApplication().getController('CheckIn').models.activeCheckIn,
		myordersComplete = this.getMyordersComplete(),
		payButton = this.getPaymentButton(),
		me = this;		
		bill.set('paymentMethod', paymentMethod);
		//workaround to prevent sencha from sending phantom id
		bill.setId('');
		//TODO show load mask to prevent users from issuing orders?!
		
		bill.save({
			scope: this,
			params: {
				'checkInId' : checkIn.getId(),
				pathId: checkIn.get('businessId')
			},
			success: function(record, operation) {
					me.models.activeBill = record;
					checkInCtr.fireEvent('statusChanged', Karazy.constants.PAYMENT_REQUEST);
					payButton.hide();
					myordersComplete.show();					
			},
			failure: function(record, operation) {
				if(operation.getError() != null && operation.getError().status == 404) {
					Ext.Msg.alert(i18nPlugin.translate('errorTitle'), operation.getError().statusText, Ext.emptyFn);
				} else if(operation.getError() != null && operation.getError().status == 500) {
					Ext.Msg.alert(i18nPlugin.translate('errorTitle'), operation.getError().statusText, Ext.emptyFn);
				}
				
			}
		});			
	},
	/**
	 * Called when user checks in and wants to leave without issuing an order.
	 */
	leave: function() {
		var		checkIn = this.getApplication().getController('CheckIn').models.activeCheckIn,
				myordersStore = Ext.data.StoreManager.lookup('orderStore');	
		if(checkIn.get('status') != Karazy.constants.PAYMENT_REQUEST && myordersStore.getCount() ==  0) { 
			checkIn.erase(
//			{
//				callback: function(records, operation, success) {
//					if(operation.success) {
//						
//					}					
//				}				
//			}
			);
			this.getApplication().getController('CheckIn').fireEvent('statusChanged', Karazy.constants.COMPLETE);
		}				
	},
	/**
	 * Marks the process as complete and returns to home menu
	 */
	completePayment: function() {
		var myordersComplete = this.getMyordersComplete();
		
		myordersComplete.hide();
		this.getApplication().getController('CheckIn').fireEvent('statusChanged', Karazy.constants.COMPLETE);
	},	
	//UI Actions
	/**
	 * Hides the back button in top toolbar.
	 */
	hideBackButton: function() {
		var  cartview = this.getCartview(),
		backBt = cartview.down('#cartTopBar button[action="back"]');
		
		backBt.hide();
		
		this.toggleCartButtons();
	},
	/**
	 * Shows (cart is not empty) or hides (cart is empty) cart buttons (trash, order).
	 */
	toggleCartButtons: function() {
		var cartview = this.getCartview(),
			trashBt = cartview.down('#cartTopBar button[action="trash"]'),	
			orderBt = cartview.down('#cartTopBar button[action="order"]'),
			hidden = (this.cartCount() > 0) ? false : true;
		
		trashBt.setHidden(hidden);
		orderBt.setHidden(hidden);
	},
	/**
	 * Shows (issued orders are not empty) or hides (issued orders are empty) myorders buttons (pay).
	 */
	toggleMyordersButtons: function() {
		var payButton = this.getPayButton();
		
	},
	/**
	 * Shows the back button in top toolbar.
	 * @param text
	 * 		Label to display on button.
	 */
	showBackButton: function(text) {
		var cartview = this.getCartview(), 
		backBt = cartview.down('#cartTopBar button[action="back"]'),
		trashBt = cartview.down('#cartTopBar button[action="trash"]'),
		orderBt = cartview.down('#cartTopBar button[action="order"]');
		
		backBt.setText(text);
		backBt.show();
		trashBt.hide();
		orderBt.hide();
	},
	/**
	 * 
	 */
	showCompletePanel: function() {
		
	},
	
	//Utility methods
	/**
	 * Returns number of orders in cart.
	 */
	cartCount: function() {
		var orders = this.getApplication().getController('CheckIn').models.activeCheckIn.orders();
		
		if(orders == null) {
			return 0;
		}
		
		return orders.getCount();		
	},
	/**
	 * Returns number of issued orders.
	 * May not always reflect the current state!!
	 * 
	 * @returns
	 */
	myordersCount: function() {
		var myordersStore = Ext.data.StoreManager.lookup('orderStore');
		
		if(myordersStore == null) {
			return 0;
		}
		
		return myordersStore.getCount();		
	},
	/**
	*	Handles push notifications for orders.
	*/
	handleOrderMessage: function(action, updatedOrder) {
		var 	orderStore = Ext.StoreManager.lookup('orderStore'),
				oldOrder;

		if(action == "update") {
			oldOrder = orderStore.getById(updatedOrder.id);
			if(oldOrder) {
				oldOrder.setRawJsonData(updatedOrder, true)
			} else {
				console.log('updatedOrder ' + updatedOrder.id + ' does not exist');
			}

		}
	}
});