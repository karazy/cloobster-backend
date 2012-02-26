Ext.define('EatSense.view.Menu', {
	extend : 'Ext.Panel',
	xtype : 'menu',
	config : {
		items : [ {
			docked : 'top',
			xtype : 'titlebar',
			itemId: 'menuTopBar',
			title : i18nPlugin.translate('menuTitle'),
			items : [ {
				xtype : 'button',
				itemId : 'menuBackBt',
				action: 'back',
				text : i18nPlugin.translate('back'),
				ui : 'back',
				align: 'left'
			},
//			{
//				xtype: 'spacer'
//			},
			{
				xtype: 'button',
//				text: i18nPlugin.translate('productCartBt'),
				itemId : 'productCartBt',
				ui: 'confirm',
//				height: '40px',
//				width: '40px',
				icon: '../app/res/images/into_cart.png',
				iconAlign: 'centered',
				hidden: true,
				align: 'right'
			},
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
			        }, 
			        {
			        	xtype: 'productdetail',
			        	itemId: 'menuProductDetail',
			        	layout: 'vbox',
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