Ext.define('EatSense.view.ProductOverview', {
	extend : 'Ext.Container',
	xtype : 'productoverview',
	fullscreen : true,
	config : {
		items : [ {
			docked : 'top',
			xtype : 'toolbar',
			title : 'Menu',
			items : [ {
				xtype : 'button',
				id : 'productOvBackBt',
				text : 'back',
				ui : 'back'
			} ]

		}, {
			xtype : 'list',
			layout : {
				align : 'center'
			},
			id : 'productlist',
			type : 'fit',
			allowDeselect : true,
			itemTpl : '<div><strong>{name}</strong> - {price}</div>',
			listeners : {
				itemtap : function(dv, ix, item, e) {
//					dv.getSelected().clear();
					console.log('ProductOverview -> listener itemtap');
				}
			}
		} ]
	}
});
