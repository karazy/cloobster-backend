/**
 * Displays the restaurants menu(s).
 * E. g.
 * Burgers, Drinks, Steaks, Salads
 * 
 */
Ext.define('EatSense.view.MenuOverview', {
	extend : 'Ext.Container',
	xtype : 'menuoverview',
	fullscreen : false,
	config : {
		items : [ 
//		          {
//			docked : 'top',
//			xtype : 'toolbar',
//			title : i18nPlugin.translate('menuTitle'),
//			items: [
//					{        		 
//					    xtype: 'label',
//					    docked: 'right',
//					    html: '<img src="../app/res/images/eatSenseLogo.png" width="50" height="50"></img>',  	        
//					}
//			]
//		},  {
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
//				    iconCls: 'compose',
//				    id: 'bottomTapToMenu',
//				    iconMask: true
//				},
//				{
//				    title: 'Card',
//				    iconCls: 'organize',
//				    id: 'menuCartBt',
//				    iconMask: true,
//				}
//			        ]
//		}, 
{
			xtype : 'list',
			id : 'menulist',
		//	store: 'menuStore',
			type : 'fit',
			allowDeselect: true,
			itemTpl : '<div>{title}</div>',
			listeners: {
				itemtap: function(dv, ix, item, e) {
					dv.deselect(ix);
				}
			}
			
		} ]
	}
});
