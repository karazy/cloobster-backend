/**
 * Displays the restaurants menu(s).
 * E. g.
 * Burgers, Drinks, Steaks, Salads
 * 
 */
Ext.define('EatSense.view.MenuOverview', {
	extend : 'Ext.Container',
	xtype : 'menuoverview',
	fullscreen : false,
	config : {
		items : [ {
			docked : 'top',
			xtype : 'toolbar',
			title : i18nPlugin.translate('menuTitle'),
			items: [
					{        		 
					    xtype: 'label',
					    docked: 'right',
					    html: '<img src="../app/res/images/eatSenseLogo.png" width="50" height="50"></img>',  	        
					}
			]
		}, {
			xtype : 'list',
			id : 'menulist',
			type : 'fit',
			allowDeselect: true,
			itemTpl : '<div>{title}</div>',
			listeners: {
				itemtap: function(dv, ix, item, e) {
					// Delay the selection clear
					// so they get a nice blue flash for HCI's sake
//					setTimeout(function(){dv.deselect(ix);},500);
//					dv.deselect(ix);
//					dv.deselect(item);
//					dv.getSelected().clear();
					console.log('MenuOverview -> listener itemtap');
				},
				deactivate: function(eOpts) {
					console.log('MenuOverview -> listener deactivate');
					}
				}
			
		} ]
	}
});
