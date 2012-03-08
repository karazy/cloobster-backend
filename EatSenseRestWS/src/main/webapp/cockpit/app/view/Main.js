Ext.define('EatSense.view.Main', {
	extend: 'Ext.TabPanel',
	xtype: 'main',
	requires: ['EatSense.view.Spot'],
	config: {
		fullscreen: true,
		items: [
		{
			xtype: 'spotcard'
		}]
	}
});