Ext.define('EatSense.view.Menu', {
	extend : 'Ext.Panel',
	xtype : 'menu',
	config : {
		items : [ {
			docked : 'top',
			xtype : 'toolbar',
			itemId: 'toolbar',
			title : i18nPlugin.translate('menuTitle'),
			items : [ {
				xtype : 'button',
				id : 'menuBack',
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
//				{
//				    title: 'Menu',
//				    iconCls: 'compose',
//				    id: 'bottomTapToMenu',
//				    iconMask: true
//				},
				{
				    title: 'Card',
				    iconCls: 'organize',
				    id: 'menuCartBt',
				    iconMask: true,
				}
			        ]
		}, {
			xtype: 'panel',
			layout: {
				type: 'card'
			},
			items: [
			        {
			        	xtype: 'menuoverview'
			        },
			        {
			        	xtype: 'productoverview'
			        }, 
			        {
			        	xtype: 'productdetail'
			        }
			]
			
		}
		]
	}
});