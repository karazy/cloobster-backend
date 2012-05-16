Ext.define('EatSense.view.Menu', {
	extend : 'Ext.Panel',
	xtype : 'menutab',
	config : {
		layout: 'fit',
		iconCls : 'menu',
		title: Karazy.i18n.translate('menuTab'),
		iconMask : true,
		itemId : 'menutab',
		cls: 'menu-panel',
		items : [ {
			docked : 'top',
			xtype : 'titlebar',
			itemId: 'menuTopBar',
			title : Karazy.i18n.translate('menuTitle'),
			items : [ {
				xtype : 'button',
				itemId : 'menuBackBt',
				action: 'back',
				text : Karazy.i18n.translate('back'),
				ui : 'back',
				align: 'left'
			}
			]
		},
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
			 type: 'slide',
	         direction: direction
		});
		cardpanel.setActiveItem(view);
	},
	/**
	 * Shows or hides the product cart button.
	 * @param show
	 * 		<code>true</code> to show, <code>false</code> to hide
	 */
	toggleProductCartButton: function(show) {		
		this.query('#menuTopBar #productCartBt')[0].setHidden(!show);
	},
	/**
	 * Hides the back button in top toolbar.
	 */
	hideBackButton: function() {
		this.query('#menuBackBt')[0].hide();
	},
	/**
	 * Shows the back button in top toolbar.
	 * @param text
	 * 		Label to display on button.
	 */
	showBackButton: function(text) {
		this.query('#menuBackBt')[0].setText(text);
		this.query('#menuBackBt')[0].show();
	}
});