Ext.define('EatSense.view.MenuOverview', {
	extend : 'Ext.Container',
	xtype : 'menuoverview',
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
				id : 'menulist',
				type: 'fit',
				height: '200px',
				width: '150px',
				styleHtmlContent: true, 
				fullScreen: true,				
				itemTpl: '<div>{title}</div>'
			}]
		} ]
	}
});
