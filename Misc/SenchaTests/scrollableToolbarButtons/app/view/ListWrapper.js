Ext.define('ListTest.view.ListWrapper', {
	extend : 'Ext.Container',
	xtype: 'listwrapper',
	requires : [ 'ListTest.view.PersonList', 'ListTest.view.Detail'],
	config : {
		fullscreen : true,
		layout : {
			type: 'card'		
		},
		activeItem : 0,
		items : [ {
			layout : 'fit',
			xtype : 'personlist'
		}, {
			layout : 'fit',
			xtype : 'detail'
		}
		]
	}
});
