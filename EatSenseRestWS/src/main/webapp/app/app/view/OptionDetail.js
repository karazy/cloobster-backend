/**
 * 
 */
Ext.define('EatSense.view.OptionDetail', {
	extend : 'Ext.Panel',
	xtype : 'optiondetail',
	layout : {
		type : 'fit',
	},
	config : {
		styleHtmlContent : true,
		items : [ {
			docked : 'top',
			xtype : 'panel',
			items : [ {
				xtype : 'label',
				itemId : 'choiceTextLbl'
			}, {
				xtype : 'label',
				itemId : 'choiceTitleLbl',
				text : 'Wähle'
			} ]
		}, {
			xtype : 'formpanel',
			layout : 'fit',
			itemId : 'optionsPanel',
			
		} ]
	}
});
