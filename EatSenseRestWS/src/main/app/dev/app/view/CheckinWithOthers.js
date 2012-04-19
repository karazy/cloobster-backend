/**
 * Checkin Step 4
 * Display a list of other users located at this particular spot.
 */
Ext.define('EatSense.view.CheckinWithOthers', {
    extend: 'Ext.Panel',
    xtype: 'checkinwithothers',
    fullscreen : true,
    config: {
        items : [ {
			docked : 'top',
			xtype : 'toolbar',
			title : Karazy.i18n.translate('checkInTitle'),
			items: [

			       ]
		
		}, {
			xtype : 'panel',
			layout : {
				type : 'vbox',
				pack : 'center',
				align : 'center'
			},
			defaults : {
				margin : 5,
				type: 'fit',
				
			},
			items : [
			{
				xtype: 'label',
				id : 'checkInDlg2Label2',
				styleHtmlContent: true,  
				html: Karazy.i18n.translate('checkInStep2Label2')
			}, 
			{
				xtype : 'list',				
				id : 'checkinDlg2Userlist',
				flex: 6,
				type: 'fit',
				width: '100%',	
				ui: 'round',
				itemTpl: '<div><strong>{nickname}</strong></div>',
				listeners : {
					select : function(dv, index, target, record, e, eOpts) {					
						Ext.defer((function() {
							dv.deselectAll();
						}), 100, this);					
					}
				}
				
			}, {
				xtype : 'button',
				id : 'checkinDlg2CancelBt',
				text : Karazy.i18n.translate('checkInStep2OnMyOwnButton'),
				ui : 'action'
			}]
		} ]
    }

});