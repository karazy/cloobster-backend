Ext.define('EatSense.view.ChooseBusiness', {
	extend: 'Ext.Panel',
	xtype: 'choosebusiness',
	config: {
		layout: {
			type: 'fit',
			// align: 'middle',
			// pack: 'center'
		},
		items: [
		{
			xtype: 'label',
			html: '<h1>Filiale auswählen</h1>',
			docked: 'top'
		},
		{
			xtype: 'list',
			store: 'businessStore',
			itemTpl: '<h2>{name}</h2>',
			// flex: 3
		},
		{
			xtype: 'button',
			action: 'cancel',
			docked: 'bottom',
			text: i18nPlugin.translate('cancel'),
		}]
	}
});