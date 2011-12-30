Ext.define('EatSense.view.ProductOverview', {
	extend : 'Ext.Container',
	xtype : 'productoverview',
	fullscreen : false,
	config : {
		items : [ {
			docked : 'top',
			xtype : 'toolbar',
			title : 'Menu'
		}, {
			xtype : 'panel',
			layout : {
				type : 'vbox',
				pack : 'center',
				align : 'center',
			},
			items : [ {
				xtype : 'list',
				id : 'productlist',
				type: 'fit',
				height: '200px',
				width: '150px',
				styleHtmlContent: true, 
				fullScreen: true,				
				itemTpl: '<div><strong>{name}</strong> - {price}</div>'
			}]
		} ]
	}
});
