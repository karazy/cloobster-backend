Ext.define('EatSense.view.CheckinWithOthers', {
    extend: 'Ext.Panel',
    xtype: 'checkinwithothers',
    fullscreen : true,
    config: {
        items : [ {
			docked : 'top',
			xtype : 'toolbar',
			title : 'CheckIn Confirmation'
		
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
			items : [ {
				xtype : 'label',
				id : 'checkInDlg2Label1',
				styleHtmlContent: true, 
				html : i18nPlugin.translate('checkInStep2Label1')
			}, {
				xtype: 'label',
				id : 'checkInDlg2Label2',
				styleHtmlContent: true,  
				html: i18nPlugin.translate('checkInStep2Label2')
			}, 
			{
				xtype : 'list',
				id : 'checkinDlg2Userlist',
				flex: 6,
				type: 'fit',
				width: '100%',				
				itemTpl: '<div><strong>{nickname}</strong></div>'
				
			}, {
				xtype : 'button',
				id : 'checkinDlg2CancelBt',
				text : i18nPlugin.translate('checkInStep2OnMyOwnButton'),
				ui : 'normal'
			}]
		} ]
    }

});