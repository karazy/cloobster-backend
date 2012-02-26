/**
 * 
 */
Ext.define('EatSense.view.OptionDetail', {
	extend : 'Ext.Panel',
	xtype : 'optiondetail',

	layout : {
		type : 'vbox',
		 align : 'middle'
	},
	config : {
		defaults : {
			labelWidth : '50%'
		},
		items : [ {
			xtype : 'label',
			itemId : 'choiceTextLbl',
		}, {
			xtype : 'panel',
			layout : {
				type : 'vbox',
				align : 'stretch'
			},
			itemId : 'optionsPanel',

		} ]
	}
});
