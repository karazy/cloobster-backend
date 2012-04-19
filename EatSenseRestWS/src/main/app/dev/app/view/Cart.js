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
			title : Karazy.i18n.translate('cartviewTitle'),
			items : [ 
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
			    text: Karazy.i18n.translate('submitButton'),
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
			}		        
			]
			
		} ]
	},
	/**
	 * Show a loading screen
	 * @param mask
	 */
    showLoadScreen : function(mask) {
    	if(mask) {
    		this.setMasked({
    			message : Karazy.i18n.translate('submitOrderProcess'),
        		xtype: 'loadmask' 
    		});
    	} else {
    		this.setMasked(false);
    	}
    }

});