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
			html: '<h1>'+Karazy.i18n.translate('chooseStore')+'</h1>',
			docked: 'top'
		},
		{
			xtype: 'list',
			ui: 'round',
			store: 'businessStore',
			itemTpl: '<h2>{name}</h2>',
			// flex: 3
		},
		{
			xtype: 'button',
			action: 'cancel',
			docked: 'bottom',
			text: Karazy.i18n.translate('cancel'),
		}]
	}
});