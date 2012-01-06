/**
 * Checkin Step 2
 * User has to confirm that he wants to checkin and has to choose a nickname.
 */
Ext.define('EatSense.view.Checkinconfirmation', {
    extend: 'Ext.Panel',
    xtype: 'checkinconfirmation',
    fullscreen : false,
    config: {
        items : [ {
			docked : 'top',
			xtype : 'toolbar',
			title : i18nPlugin.translate('checkInTitle'),
			items: [
				{
					xtype : 'button',
					id : 'cancelCheckInBt',
					text : i18nPlugin.translate('cancel'),
					ui : 'back'
				}  
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
			},
			items : [ {
				xtype : 'label',
				styleHtmlContent: true,
				id: 'checkInDlg1Label1',
				html :  i18nPlugin.translate('checkInStep1Label1')
			}, {
				xtype: 'label',
				styleHtmlContent: true,
				html: i18nPlugin.translate('checkInStep1Label2')
			}, {
				xtype : 'textfield',
				id : 'nicknameTf',
				label: 'Nickname',
				required: true,
			}, {
				xtype : 'button',
				id : 'confirmCheckInBt',
				text : i18nPlugin.translate('checkInStep1Button'),
				ui : 'normal'
			}
			]
		} ]
    }
});