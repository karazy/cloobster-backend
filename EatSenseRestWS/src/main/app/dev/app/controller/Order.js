	Ext.define('EatSense.controller.Order', {
	extend: 'Ext.app.Controller',
	requires: ['Ext.picker.Picker'],
	config: {
		refs: {
			main : 'mainview',
			cartview : 'carttab',
			cartoverviewTotal: 'carttab #carttotalpanel label',
			myordersTotal : 'myorderstab #myorderstotalpanel label',
			myordersComplete: 'myorderstab #myorderscompletepanel',
			myordersCompleteButton: 'myorderstab button[action=complete]',
			menutab: 'menutab',
			orderlist : 'carttab #orderlist',
			cancelAllOrdersBt : 'carttab #cartTopBar button[action="trash"]',
			submitOrderBt : 'carttab #cartTopBar button[action="order"]',
			topToolbar : 'carttab #cartTopBar',
			productdetail : {
                selector: 'orderdetail',
                xtype: 'orderdetail',
                autoCreate: true
            },
			choicespanel : 'orderdetail #choicesPanel',
			editOrderBt : 'cartoverviewitem button[action=edit]',
			cancelOrderBt : 'cartoverviewitem button[action=cancel]',
			amountSpinner : 'orderdetail spinnerfield',
			prodDetailLabel :'orderdetail #prodDetailLabel' ,
			prodPriceLabel :'orderdetail #prodPriceLabel' ,
			closeOrderDetailBt: 'orderdetail button[action=close]',
			loungeview : 'lounge',
			//the orderlist shown in lounge in myorders tab lounge tab #myorderstab
			myorderlist: 'menutab list',
			myordersview: 'myorderstab',
			loungeTabBar: '#loungeTabBar',
			paymentButton: 'myorderstab button[action="pay"]',
			leaveButton: 'myorderstab button[action="leave"]',
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
			 editOrderBt : {
				tap: 'showOrderDetail'
			 },
			 cancelOrderBt : {
			 	tap: 'cancelOrder'
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
		},
		/**
		*	Current active order.
		*/
		activeOrder: null,
		/**
		*	Current active bill.
		*/
		activeBill: null	
	},
	init: function() {
		//store retrieved models
		var	messageCtr = this.getApplication().getController('Message');

    	messageCtr.on('eatSense.order', this.handleOrderMessage, this);
	},
	/**
	 * Load cart orders.
	 * @return
	 * 		<code>false</code> if cart is empty, <code>true</code> otherwise
	 */
	refreshCart: function() {
		console.log('Cart Controller -> showCart');
		var cartview = this.getCartview(), 
			orderlist = this.getOrderlist(),
			orders = this.getApplication().getController('CheckIn').getActiveCheckIn().orders(),
			total = 0;
    	
		orderlist.setStore(orders);	
		this.setActiveOrder(null);
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
		//TODO not used
		console.log('Cart Controller -> showMenu');
		var lounge = this.getLoungeview(), menu = this.getMenutab();		
		lounge.setActiveItem(menu);
	},
	/**
	 * Remove all orders from cart and switch back to menu.
	 */
	dumpCart: function() {
		console.log('Cart Controller -> dumpCart');
		var activeCheckIn = this.getApplication().getController('CheckIn').getActiveCheckIn();
		
		Ext.Msg.show({
			title: Karazy.i18n.translate('hint'),
			message: Karazy.i18n.translate('dumpCart'),
			buttons: [{
				text: Karazy.i18n.translate('yes'),
				itemId: 'yes',
				ui: 'action'
			}, {
				text:  Karazy.i18n.translate('yes'),
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
			    	    method: 'DELETE'
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
		var checkIn = this.getApplication().getController('CheckIn').getActiveCheckIn(), 
			orders = checkIn.orders(),
			checkInId = checkIn.get('userId'),
			businessId = checkIn.get('businessId'),
			errorIndicator = false,
			cartview = this.getCartview(),
			ajaxOrderCount = 0,
			ordersCount = orders.getCount();
			me = this;
		
		if(ordersCount > 0) {
			Ext.Msg.show({
				title: Karazy.i18n.translate('hint'),
				message: Karazy.i18n.translate('submitOrdersQuestion'),
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
						    				title : Karazy.i18n.translate('success'),
						    				message : Karazy.i18n.translate('orderSubmit'),
						    				buttons : []
						    			});
						    			
						    			Ext.defer((function() {
						    				Ext.Msg.hide();
						    			}), Karazy.config.msgboxHideTimeout, this);
						    			
						    			
					    	    	}
					    	    },
					    	    failure: function(response) {
					    	    	errorIndicator = true;
					    	    	cartview.showLoadScreen(false);
					    	    	me.getSubmitOrderBt().enable();
					    	    	me.getCancelOrderBt().enable();
					    	    	me.getApplication().handleServerError({'error' : { 'status' : response.status, 'statusText' : response.statusText}}); 
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
		 var 	me = this,
		 		detail = this.getProductdetail(), 
		 		choicesPanel =  this.getChoicespanel(),
		 		order = button.getParent().getRecord(),
		 		product = order.getProduct();		 		
		 		this.setActiveOrder(order),
		 		main = this.getMain(),
		 		titlebar = detail.down('titlebar'),
		 		menuCtr = this.getApplication().getController('Menu');

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

		 	//render all main choices
		 	product.choices().queryBy(function(rec) {
		 	 	if(!rec.get('parent')) {
		 	 		return true;
		 	 	}}).each(function(choice) {
					var optionsDetailPanel = Ext.create('EatSense.view.OptionDetail');

					optionsDetailPanel.getComponent('choiceTextLbl').setHtml(choice.data.text);

					menuCtr.createOptions.apply(me, [choice, optionsDetailPanel, null, order]);
					//process choices assigned to a this choice
					product.choices().queryBy(function(rec) {
						if(rec.get('parent') == choice.get('id')) {
							return true;
						}
					}).each(function(memberChoice) {
						memberChoice.setParentChoice(choice);
						// menuCtr.createOptions(memberChoice, optionsDetailPanel, choice);
						menuCtr.createOptions.apply(me, [memberChoice, optionsDetailPanel, choice, order]);
					});

					choicesPanel.add(optionsDetailPanel);
		 	 });
			//  product.choices().each(function(_choice) {
			// 	 var choice = _choice;				 			 
			// 	 var optionsDetailPanel = Ext.create('EatSense.view.OptionDetail');
			// 	 optionsDetailPanel.getComponent('choiceTextLbl').setHtml(choice.data.text);
			// 	 //single choice. Create Radio buttons
			// 	 var optionType = '';
			// 	 if(choice.data.minOccurence <= 1 && choice.data.maxOccurence == 1) {
			// 		 optionType = 'Ext.field.Radio';
			// 	 	} 
			// 	 else {//multiple choice
			// 		 optionType = 'Ext.field.Checkbox';					 
			// 	 }
				
			// 	choice.options().each(function(opt) {
			// 		var checkbox = Ext.create(optionType, {
			// 			name : choice.data.id,
			// 			labelWidth: '80%',
			// 			label : opt.get('name'),
			// 			checked: opt.get('selected'),
			// 			cls: 'option'
			// 		}, this);
					 
			// 		checkbox.addListener('check',function(cbox) {
			// 			console.log('check');
			// 			if(cbox.isXType('radiofield',true)) {
			// 				choice.options().each(function(innerOpt) {
			// 					innerOpt.set('selected', false);
			// 				});
			// 			};
			// 			opt.set('selected', true);
			// 			this.recalculate(this.getActiveOrder());
			// 		},this);
			// 		checkbox.addListener('uncheck',function(cbox) {
			// 			console.log('uncheck');
			// 			if(cbox.isXType('checkboxfield',true)) {
			// 				 opt.set('selected', false);
			// 			} else {
			// 				 //don't allow radio buttons to be deselected
			// 				 cbox.setChecked(true);
			// 			}
			// 			 this.recalculate(this.getActiveOrder());								 
			// 		},this);
			// 		 optionsDetailPanel.getComponent('optionsPanel').add(checkbox);					 
			// 	},this);	 
			// 	 choicesPanel.add(optionsDetailPanel);
			// },this);
		}
		 
		 
		 //insert comment field after options have been added so it is positioned correctly
		 choicesPanel.add({
				xtype: 'textfield',
				label: Karazy.i18n.translate('orderComment'),
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
		var order = this.getActiveOrder(),
		product = this.getActiveOrder().getProduct(), 
		validationError = "", 
		productIsValid = true,
		activeCheckIn = this.getApplication().getController('CheckIn').getActiveCheckIn(),
		detail = this.getProductdetail();
		
		order.getData(true);

		//validate choices 
		product.choices().each(function(choice) {
			//only validate dependend choices if parent choice is active!
			if(!choice.get('parent') || choice.getParentChoice().isActive()) {
				if(choice.validateChoice() !== true) {
					//coice is not valid
					productIsValid = false;
					validationError += choice.get('text') + '<br/>';
				}
			};
		});
		
		if(productIsValid) {
			this.getActiveOrder().set('comment', this.getChoicespanel().getComponent('productComment').getValue());	
		
			Ext.Ajax.request({				
	    	    url: Karazy.config.serviceUrl+'/c/businesses/'+activeCheckIn.get('businessId')+'/orders/'+order.getId(),
	    	    method: 'PUT',
	    	    jsonData: order.getRawJsonData(),
	    	    failure: function(response) {
					me.getApplication().handleServerError({'error' : { 'status' : response.status, 'statusText' : response.statusText}}, {403: true}); 
				}
			});

			detail.hide();
			this.refreshCart();
			return true;
		} else {
			//show validation error
			Ext.Msg.alert(Karazy.i18n.translate('orderInvalid'),validationError, Ext.emptyFn, detail);
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
				activeCheckIn = this.getApplication().getController('CheckIn').getActiveCheckIn(),
				productName = order.getProduct().get('name');
			//delete item
			activeCheckIn.orders().remove(order);
			
			Ext.Ajax.request({
	    	    url: Karazy.config.serviceUrl+'/c/businesses/'+activeCheckIn.get('businessId')+'/orders/'+order.getId(),
	    	    method: 'DELETE',
	    	    failure: function(response) {
					me.getApplication().handleServerError({'error' : { 'status' : response.status, 'statusText' : response.statusText}}, {403: true}); 
				}
	    	});
			
			this.refreshCart();
			
			//show success message and switch to next view
			Ext.Msg.show({
				title : Karazy.i18n.translate('orderRemoved'),
				message : productName,
				buttons : []
			});
			//show short alert and then hide
			Ext.defer((function() {
				Ext.Msg.hide();
			}), Karazy.config.msgboxHideTimeout, this);
	},

	closeOrderDetail: function() {
		var detail = this.getProductdetail();
		
		this.getActiveOrder().restoreState();
		//try to avoid unecessary calculation, only needed to update price after cancelation
		this.recalculate(this.getActiveOrder());
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
		this.getActiveOrder().set('amount', value);
		this.recalculate(this.getActiveOrder());
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
			orders = this.getApplication().getController('CheckIn').getActiveCheckIn().orders(),
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
				activeCheckIn = me.getApplication().getController('CheckIn').getActiveCheckIn(),
				payButton = me.getPaymentButton();
				leaveButton = me.getLeaveButton();
		
		//TODO investigate if this is a bug
		myordersStore.removeAll();
//		myorderlist.getStore().removeAll();
		
		myordersStore.load({
			scope   : this,			
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
						me.getApplication().handleServerError(operation.error, {403: true});
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
		var availableMethods = this.getApplication().getController('CheckIn').getActiveSpot().payments(),
			orderCount = this.getMyorderlist().getStore().getCount(),
			checkIn = this.getApplication().getController('CheckIn').getActiveCheckIn(),
			picker,
			choosenMethod,
			me = this;
		
		if(orderCount>0 && checkIn.get('status') !== Karazy.constants.PAYMENT_REQUEST && checkIn.get('status') !== Karazy.constants.COMPLETE) {
			//create picker
			picker = Ext.create('Ext.Picker', {
				doneButton: {
					text: Karazy.i18n.translate('ok'),
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
					text: Karazy.i18n.translate('cancel'),
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
			            title: Karazy.i18n.translate('paymentPickerTitle'),
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
			checkIn = this.getApplication().getController('CheckIn').getActiveCheckIn(),
			myordersComplete = this.getMyordersComplete(),
			payButton = this.getPaymentButton(),
			me = this;		

		bill.set('paymentMethod', paymentMethod);
		//workaround to prevent sencha from sending phantom id
		bill.setId('');
		//TODO show load mask to prevent users from issuing orders?!
		
		bill.save({
			scope: this,
			success: function(record, operation) {
					me.setActiveBill(record);
					checkInCtr.fireEvent('statusChanged', Karazy.constants.PAYMENT_REQUEST);
					payButton.hide();
					myordersComplete.show();					
			},
			failure: function(record, operation) {
				me.getApplication().handleServerError(operation.error, {403: true});
			}
		});			
	},
	/**
	 * Called when user checks in and wants to leave without issuing an order.
	 */
	leave: function() {
		var		me = this,
				checkIn = this.getApplication().getController('CheckIn').getActiveCheckIn(),
				myordersStore = Ext.data.StoreManager.lookup('orderStore');	
		if(checkIn.get('status') != Karazy.constants.PAYMENT_REQUEST && myordersStore.getCount() ==  0) { 
			checkIn.erase( {
				failure: function(response, operation) {
					me.getApplication().handleServerError(operation.error, {403: true});
				}
			}
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
	//Utility methods
	/**
	 * Returns number of orders in cart.
	 */
	cartCount: function() {
		var orders = this.getApplication().getController('CheckIn').getActiveCheckIn().orders();
		
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
		var orderStore = Ext.StoreManager.lookup('orderStore'),
			oldOrder,
			total;

		if(action == "update") {
			oldOrder = orderStore.getById(updatedOrder.id);
			if(oldOrder) {
				oldOrder.setRawJsonData(updatedOrder, true)
				//refresh the order list
				total = me.calculateOrdersTotal(orderStore);
				me.getMyordersTotal().getTpl().overwrite(me.getMyordersTotal().element, {'price': total});
			} else {
				console.log('updatedOrder ' + updatedOrder.id + ' does not exist');
			}

		}
	}
});