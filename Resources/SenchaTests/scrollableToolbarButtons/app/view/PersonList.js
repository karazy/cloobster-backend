Ext.define('ListTest.view.PersonList', {
	extend : 'Ext.Panel',
	xtype : 'personlist',
	config : {
		items : [ {
			docked : 'top',
			xtype : 'toolbar',
			title : 'This is a list'
		}, {
			xtype : 'list',
			id : 'thelist',
			type : 'fit',
			allowDeselect: true,
			itemTpl : '<div>{firstName} {lastName}</div>',
			listeners: {
				itemtap: function(dv, ix, item, e) {
				 	dv.deselect(ix);
 					dv.deselect(item);
 					console.log('listener itemtap');
				}
			}
			
		} ]
	}
});
