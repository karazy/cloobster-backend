/**
 * Displays details and options/extras of a product.
 */
Ext.define('ListTest.view.Detail', {
	extend : 'Ext.Panel',
	xtype : 'detail',	
	layout : {
		type : 'fit',
		align : 'middle'
	},
	config : {

		// styleHtmlContent : true,
		items : [ {
			docked : 'top',
			xtype : 'toolbar',
			itemId : 'toolbar',
			title : 'Detail title',
			items : [ {
				xtype : 'button',
				id : 'backToList',
				ui : 'back'
			} ]

		} ]
	}
});