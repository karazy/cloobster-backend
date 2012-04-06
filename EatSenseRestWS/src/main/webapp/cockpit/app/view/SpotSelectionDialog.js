Ext.define('EatSense.view.SpotSelectionDialog', {
	extend: 'Ext.Panel',
	xtype: 'spotselection',
	config: {
		modal: true,
		hideOnMaskTap: true,
		layout: 'fit',
		top: '10%',
		right: '30%',
		width: 200,
		height: 300,
		items: [
		{
			xtype: 'titlebar',
			docked: 'top',
			title: Karazy.i18n.translate('spotSelectionTitle')
		},
		{
			xtype: 'list',
			store: 'spotStore',
			itemId: 'spotList',
			itemTpl: '{name}'
		}]
	}
});