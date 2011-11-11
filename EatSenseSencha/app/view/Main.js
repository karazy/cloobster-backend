Ext.define('EatSense.view.Main', {
       extend: 'Ext.Container',
           requires: [
        'EatSense.view.Dashboard',
    ],
       config: {
       	 fullscreen: true,
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


/*
Ext.define('EatSense.view.Main', {
       extend: 'Ext.Panel',
       config: {
		 fullscreen: true,
            items: [
                {
                    title: 'Home',
                    iconCls: 'home',
                    html: 'Welcome'
                }
            ]

       }
});
 */
/*Ext.create('Ext.Panel', {
	layout: 'card',
	animation: {
		type: 'slide',
		direction: 'left',
		duration: 250
	},
	items: [ {
		id:
	}	
	]
});
*/