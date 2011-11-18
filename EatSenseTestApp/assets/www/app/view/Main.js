Ext.define('EatSense.view.Main', {
       extend: 'Ext.Container',
           requires: [
        'EatSense.view.Dashboard',
    ],
       config: {
       	 fullscreen: false,
       	 layout: 'fit',
       	 activeItem: 0,
           items: [
           	{
           	layout: 'fit',
           	xtype:'dashboard'
           	}
           ]        
       }
});



