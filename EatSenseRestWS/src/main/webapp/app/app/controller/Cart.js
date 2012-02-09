Ext.define('EatSense.controller.Cart', {
	extend: 'Ext.app.Controller',
	config: {
		refs: {
			main : 'mainview',
			cartview : 'cartview',
			menuview: 'menu',
			orderlist : '#orderlist'
		}
	},
	init: function() {
		 
		this.control({
			 '#cartBackBt' : {
				 tap: this.showMenu
			 },
			 '#bottomTapCancel' : {
				 tap: this.dumpCart
			 }, 
			 '#bottomTapOrder': {
				 tap: this.disposeOrders
			 },
			 '#orderlist' : {
				 itemtap: this.alterOrder
			 }
		 });
		
		//store retrieved models
		 var models = {};
    	 this.models = models;
	},
	
	showCart: function() {
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
		var main = this.getMain(), menu = this.getMenuview();		
		main.switchAnim('right');
		main.setActiveItem(menu);
	},
	
	dumpCart: function() {
		Ext.Msg.show({
			title: i18nPlugin.translate('hint'),
			message: i18nPlugin.translate('dumpCart'),
			buttons: Ext.MessageBox.YESNO,
			scope: this,
//			modal: false,
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
	 * Alter an order. Switches to product detail view of this particular order.
	 */
	alterOrder: function(dv, ix, item, model) {
		var menu = this.getMenuview();
		//TODO implement
		dv.deselect(ix);
		//attach product to item
//		this.showMenu();
//		this.getApplication().getController('Menu').showProductDetail(null, model.data.product);
//		this.showMenu();
		//Ext.Msg.alert(i18nPlugin.translate('hint'),'Noch nicht funktionsfähig', Ext.emptyFn);
	}
	
	
});