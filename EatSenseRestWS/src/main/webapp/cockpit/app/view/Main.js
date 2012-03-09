Ext.define('EatSense.view.Main', {
	extend: 'Ext.TabPanel',
	xtype: 'main',
	requires: ['EatSense.view.Spot'],
	config: {
		fullscreen: true,
		items: [
		{
			xtype: 'spotcard'
		}, {
			xtype: 'toolbar',
			docked: 'bottom',
			items: [
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