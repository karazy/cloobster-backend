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
				id : 'menuBackBt',
				text : i18nPlugin.translate('back'),
				ui : 'back'
			},
			{        		 
	            xtype: 'label',
	            docked: 'right',
	            html: '<img src="../app/res/images/eatSenseLogo.png" width="50" height="50"></img>',  	        
    		}
			]
		}, {
			docked : 'bottom',
			xtype : 'toolbar',
			itemId : 'menuBottomBar',
			layout: {
				type: 'hbox',
				pack : 'center'
			},
			items: [
				{
				    title: 'Menu',
				    iconCls: 'reply',
				    id: 'bottomTapUndo',
				    iconMask: true
				},
				{
				    title: 'Card',
				    iconCls: 'organize',
				    id: 'menuCartBt',
				    iconMask: true,
				}
			        ]
		}, {
			xtype: 'panel',
			itemId: 'menuCard',
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
			        	layout: 'fit'
			        }
			]
			
		}
		],
		//store: 'menuStore'
	},
	/**
	 * Change the direction of the slide animation.
	 * @param direction
	 * 			left or right
	 */
	switchMenuview : function(view, direction){
		var _menucard = this.getComponent('menuCard');
		_menucard.getLayout().setAnimation({
			 type: 'slide',
	         direction: direction
		});
		_menucard.setActiveItem(view);
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