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
				html : 'Others are already at this spot.'
			}, {
				xtype: 'label',
				id : 'checkInDlg2Label2',
				styleHtmlContent: true,  
				html: 'Select the person in the list you want to check in with or hit <i>On my own!</i>!'
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
				text : 'On my own!',
				ui : 'normal'
			}]
		} ]
    }

});