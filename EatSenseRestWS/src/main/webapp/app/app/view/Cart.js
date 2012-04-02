Ext.define('EatSense.view.Cart', {
	extend : 'Ext.Panel',
	xtype : 'cart',
	layout : {
		type : 'vbox',
		align : 'middle'
	},
	config : {
		items : [ 
		          {
			docked : 'top',
			xtype : 'titlebar',
			itemId: 'cartTopBar',
			title : i18nPlugin.translate('cartviewTitle'),
			items : [ {
				xtype : 'button',
				itemId : 'cartBackBt',
				text : i18nPlugin.translate('back'),
				action : 'back',
				ui : 'back',
				align: 'left'
			},
			{
				xtype: 'button',
				action: 'trash',
			    iconCls: 'trash',
			    iconMask: true,
			    align: 'left'
			},
			{
				xtype: 'button',
				action: 'order',
			    text: i18nPlugin.translate('submitButton'),
			    ui: 'forward',
			    align: 'right'
			}]
		}, 
		{
			xtype: 'panel',
			itemId: 'cartCardPanel',
			layout : {
				type: 'card'
			},
			items: [ {
				xtype: 'cartoverview',
				itemId: 'cartoverview',
				layout: 'fit'
			}, 
			// {
			// 	xtype: 'productdetail',
			// 	itemId : 'cartProductdetail',
			// 	layout: 'vbox'
			// }			        
			]
			
		} ]
	},
	
	removeItem: function() {
		console.log('disclosure');
	},
	
	/**
	 * Change the direction of the slide animation.
	 * @param direction
	 * 			left or right
	 */
	switchView : function(view, direction){
		var cardpanel = this.getComponent('cartCardPanel');
		cardpanel.getLayout().setAnimation({
			 type: 'slide',
	         direction: direction
		});
		cardpanel.setActiveItem(view);
	},
	/**
	 * Show a loading screen
	 * @param mask
	 */
    showLoadScreen : function(mask) {
    	if(mask) {
    		this.setMasked({
    			message : i18nPlugin.translate('submitOrderProcess'),
        		xtype: 'loadmask' 
    		});
    	} else {
    		this.setMasked(false);
    	}
    }

});