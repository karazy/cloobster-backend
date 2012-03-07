Ext.define('EatSense.view.Main', {
	extend: 'Ext.tab.Panel',
	xtype: 'main',
	requires: ['EatSense.view.Spot'],
	config: {
		fullscreen: true,
		items: [
		{
			xtype: 'spot'
		}]
	}
});