/**
 * 
 */
Ext.define('EatSense.view.OptionDetail', {
	extend : 'Ext.Panel',
	xtype : 'optiondetail',

	layout : {
		type : 'vbox',
//		align : 'stretch',
		pack : 'center',
	},
	config : {
		items : [ {
			xtype : 'label',
			itemId : 'choiceTextLbl',
		}, {
			xtype : 'panel',
			layout : {
				type : 'vbox',
				pack : 'center'
			},
			itemId : 'optionsPanel',

		} ]
	}
});
