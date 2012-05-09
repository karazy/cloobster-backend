Ext.define('EatSense.view.Menu', {
	extend : 'Ext.Panel',
	xtype : 'menu',
	config : {
		items : [ {
			docked : 'top',
			xtype : 'toolbar',
			itemId: 'menuTopBar',
			title : i18nPlugin.translate('menuTitle'),
			items : [ {
				xtype : 'button',
				itemId : 'menuBackBt',
				text : i18nPlugin.translate('back'),
				ui : 'back'
			},
			{        		 
	            xtype: 'label',
	            docked: 'right',
	            html: '<img src="../app/res/images/eatSenseLogo.png" width="50" height="50"></img>',  	        
    		}
			]
		},
//		{
//			docked : 'bottom',
//			xtype : 'toolbar',
//			itemId : 'menuBottomBar',
//			layout: {
//				type: 'hbox',
//				pack : 'center'
//			},
//			items: [
//				{
//				    title: 'Menu',
//				    iconCls: 'reply',
//				    itemId: 'bottomTapUndo',
//				    iconMask: true
//				},
//				{
//				    title: 'Card',
//				    iconCls: 'organize',
//				    itemId: 'menuCartBt',
//				    iconMask: true,
//				}
//			        ]
//		}, 
		{
			xtype: 'panel',
			itemId: 'menuCardPanel',
			layout: {
				type: 'card'
			},
			activeItem : 0,
			items: [
			        {
			        	xtype: 'menuoverview',
			        	layout: 'fit'
			        },
			        {
			        	xtype: 'productoverview',
			        	layout: 'fit'
			        }, 
			        {
			        	xtype: 'productdetail',
			        	itemId: 'menuProductDetail',
			        	layout: 'fit'
			        }
			]
			
		}
		],
	},
	/**
	 * Change the direction of the slide animation.
	 * @param direction
	 * 			left or right
	 */
	switchMenuview : function(view, direction){
		var cardpanel = this.getComponent('menuCardPanel');
		cardpanel.getLayout().setAnimation({
			 type: 'cube',
	         direction: direction
		});
		cardpanel.setActiveItem(view);
	},
	/**
	 * Hides the back button in top toolbar.
	 */
	hideBackButton: function() {
		this.getComponent('menuTopBar').getComponent('menuBackBt').hide();
	},
	/**
	 * Shows the back button in top toolbar.
	 * @param text
	 * 		Label to display on button.
	 */
	showBackButton: function(text) {
		this.getComponent('menuTopBar').getComponent('menuBackBt').setText(text);
		this.getComponent('menuTopBar').getComponent('menuBackBt').show();
	}
});