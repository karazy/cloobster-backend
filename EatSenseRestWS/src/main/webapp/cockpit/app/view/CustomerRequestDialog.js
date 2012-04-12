Ext.define('EatSense.view.CustomerRequestDialog', {
	extend: 'Ext.Panel',
	xtype: 'customerrequest',
	config: {
		modal: true,
		hideOnMaskTap: true,
		layout: 'fit',
		top: '30%',
		right: '30%',
		left: '30%',
		bottom: '30%',
		items: [
		{
			xtype: 'titlebar',
			docked: 'top',
			title: Karazy.i18n.translate('requestMsgboxTitle')
		},
		{
			xtype: 'list',
			store: 'requestStore',
			itemTpl: new Ext.XTemplate('{[this.formatMessage(values)]}', {
				formatMessage: function(values) {
					if(values.type == Karazy.constants.Request.CALL_WAITER) {
						return Karazy.i18n.translate(Karazy.constants.Request.CALL_WAITER, values.CheckIn.nickname);
					}
				}
			}),
			allowDeselect: true,
		},{
			xtype: 'toolbar',
			docked: 'bottom',
			layout: {
				type: 'hbox',
				pack: 'center',
				align: 'middle'
			},
			items: [
				{
					action: 'dismiss',
					text: Karazy.i18n.translate('requestDismiss'),
					ui: 'action'
				}
			]
		}]
	}
});