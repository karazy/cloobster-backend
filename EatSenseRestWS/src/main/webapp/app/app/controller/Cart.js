Ext.define('EatSense.controller.Cart', {
	extend: 'Ext.app.Controller',
	config: {
		refs: {
			main : 'mainview',
			cartview : 'cartview',
			orderlist : '#orderlist'
		}
	},
	init: function() {
		 
		this.control({
			 
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
//			for(order in orders.data) {
//				
//			}
			//switch to cart
			main.switchAnim('left');
			main.setActiveItem(cartview);
		}
	}
	
	
});