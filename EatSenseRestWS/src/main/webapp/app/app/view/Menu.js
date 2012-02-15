Ext.define('EatSense.view.Menu', {
	extend : 'Ext.Panel',
//	extend : 'EatSense.SpecializedCardPanel',
//	requires: ['EatSense.SpecializedCardPanel'],
	xtype : 'menu',
	config : {
		items : [ {
			docked : 'top',
			xtype : 'toolbar',
			itemId: 'topBar',
			title : i18nPlugin.translate('menuTitle'),
			items : [ {
				xtype : 'button',
				itemId : 'backBt',
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
			itemId: 'cardPanel',
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
	},
	/**
	 * Change the direction of the slide animation.
	 * @param direction
	 * 			left or right
	 */
	switchMenuview : function(view, direction){
		var cardpanel = this.getComponent('cardPanel');
		cardpanel.getLayout().setAnimation({
			 type: 'slide',
	         direction: direction
		});
		cardpanel.setActiveItem(view);
	},
	/**
	 * Hides the back button in top toolbar.
	 */
	hideBackButton: function() {
		this.getComponent('topBar').getComponent('backBt').hide();
	},
	/**
	 * Shows the back button in top toolbar.
	 * @param text
	 * 		Label to display on button.
	 */
	showBackButton: function(text) {
		this.getComponent('topBar').getComponent('backBt').setText(text);
		this.getComponent('topBar').getComponent('backBt').show();
	}
});