Ext.define('EatSense.view.Main', {
	extend: 'Ext.TabPanel',
	xtype: 'main',
	requires: ['EatSense.view.Spot'],
	config: {
		fullscreen: true,
		items: [
		{
			xtype: 'spotcard'
		}, 
		{
			xtype: 'toolbar',
			docked: 'bottom',
			items: [
			{
			xtype: 'label',
			itemId: 'info',
			tpl: 'Logged in as <span>{login}</strong> at <strong>{business}</strong>'
			},
			{
				xtype: 'spacer'
			},
			{
				xtype: 'label',
				itemId: 'connectionStatus',
				tpl: 'Status: {0}'
			},
			{
				xtype: 'spacer'
			},
			{
				xtype: 'button',
				iconCls: 'delete',
    			iconMask: true,
    			action: 'logout'
			}]
		}]
	}
});