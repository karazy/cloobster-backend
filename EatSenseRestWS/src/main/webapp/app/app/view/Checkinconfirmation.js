Ext.define('EatSense.view.Checkinconfirmation', {
    extend: 'Ext.Panel',
    xtype: 'checkinconfirmation',
    fullscreen : false,
    config: {
        items : [ {
			docked : 'top',
			xtype : 'toolbar',
			title : 'CheckIn Confirmation',
			items: [
				{
					xtype : 'button',
					id : 'cancelCheckInBt',
					text : 'Cancel',
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
				html : 'Do you want to check in at'
			}, {
				xtype: 'label',
				styleHtmlContent: true,
				html: 'Choose a nickname associated with your checkIn:'
			}, {
				xtype : 'textfield',
				id : 'nicknameTf',
				label: 'Nickname',
				required: true,
			}, {
				xtype : 'button',
				id : 'confirmCheckInBt',
				text : 'CheckIn',
				ui : 'normal'
			}
			]
		} ]
    }
});