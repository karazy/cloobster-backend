Ext.define('EatSense.controller.Cart', {
	extend: 'Ext.app.Controller',
	config: {
		refs: {
			main : 'mainview',
			cartview : 'cartview',
			menuview: 'menu',
			orderlist : '#orderlist',
			backBt : 'cartview #topBar #backBt'
		},
		/**
		 * Tooltip menu, shown when user taps an order
		 */
		tooltip : ''				
	},
	init: function() {
		 
		this.control({
			backBt : {
				 tap: this.showMenu
			 },
			 '#bottomTapCancel' : {
				 tap: this.dumpCart
			 }, 
			 '#bottomTapOrder': {
				 tap: this.disposeOrders
			 },
			 '#orderlist' : {
				 itemtap: this.cartItemContextMenu
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
		orders = this.getApplication().getController('CheckIn').models.activeCheckIn.orders();
		//only switch if cart is not empty
		if(orders.data.length == 0) {
			Ext.Msg.alert(i18nPlugin.translate('hint'),i18nPlugin.translate('cartEmpty'), Ext.emptyFn);
		} else {
			//add all orders to cart list
			orderlist.setStore(orders);
			orderlist.refresh();
			//switch to cart
			main.switchAnim('left');
			main.setActiveItem(cartview);
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
				this.getApplication().getController('CheckIn').models.activeCheckIn.orders().removeAll();
				//reset badge text on cart button and switch back to menu
				this.getApplication().getController('Menu').getCardBt().setBadgeText('');
				this.showMenu();
				}
			}
		});				
	},
	/**
	 * Submits orders to server.
	 */
	disposeOrders: function() {
		Ext.Msg.alert(i18nPlugin.translate('hint'),'Noch nicht funktionsfähig', Ext.emptyFn);
	},
	/**
	 * Listener for itemTap event of orderlist.
	 * Show a tooltip with buttons to edit, delete the selected item.
	 */
	cartItemContextMenu: function(dv, number, dataitem, model, event, opts) {
		console.log('Cart Controller -> cartItemContextMenu');
		var menu = this.getMenuview(), 
		main = this.getMain(),
		x = event.pageX,
		y = event.pageY,
		tooltip = this.getTooltip(),
		controller = this.getApplication().getController('Menu'),
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
			controller.models.activeProduct = model;							
			main.switchAnim('right');
			main.setActiveItem(menu);
			controller.showProductDetail(null, model.data.product);
		}, this);
		//dump item
		tooltip.getComponent('deleteCartItem').addListener('tap', function() {
			tooltip.hide();
			Ext.Msg.show({
				title: i18nPlugin.translate('hint'),
				message: i18nPlugin.translate('dumpItem', model.get('product').get('name')),
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
					//refresh list
					orderlist.refresh();
					}
				}
			});	
		}, this);
		
		tooltip.show();
	}
});

Ext.define('EatSense.util.CartToolTip', {
	extend: 'Ext.Panel',
	xtype: 'cartToolTip',
	config: {
		layout:'hbox',
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
		},{
			xtype: 'button',
			itemId: 'deleteCartItem',
			iconCls : 'trash',
			iconMask : true,
			flex: 1				
		}]
	}	
});