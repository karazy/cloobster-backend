Ext.define('EatSense.view.Dashboard', {
    extend: 'Ext.Container',
    xtype: 'dashboard',
    config: {
        layout: 'hbox',
        items: [
        	{
        	dock: 'bottom',
        	xtype: 'toolbar',
        	title: 'EatSense'
        	},
        	{
        	
        	xtype:'panel',
        	items: [
            {xtype: 'button',
           	itemId: 'checkInBtn',
            text: 'CheckIn'
            }
            ]
        }]
    }
});

