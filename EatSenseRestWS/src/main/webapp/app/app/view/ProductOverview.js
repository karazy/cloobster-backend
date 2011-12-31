Ext.define('EatSense.view.ProductOverview', {
	extend : 'Ext.Container',
	xtype : 'productoverview',
	fullscreen : true,
	config : {
		items : [ {
			docked : 'top',
			xtype : 'toolbar',
			title : 'Menu',
			items: [
				{
					xtype : 'button',
					id : 'productOvBackBt',
					text : 'back',
					ui : 'back'
				}
			]
		
		}, 
//		{
//			xtype : 'panel',
//			layout : {
//				type : 'vbox',
////				pack : 'center',
//				align : 'center'
//			},
//			items : [ {
//				xtype : 'list',
//				id : 'productlist',
//				type: 'fit',
//				height: '200px',
//				width: '150px',
//				styleHtmlContent: true, 
//				fullScreen: true,				
//				itemTpl: '<div><strong>{name}</strong> - {price}</div>'
//			}]
//		} 
		{
			xtype : 'list',
			layout: {
				align: 'center'
			},
			id : 'productlist',
			type: 'fit',			
			itemTpl: '<div><strong>{name}</strong> - {price}</div>'
		}
		]
	}
});
