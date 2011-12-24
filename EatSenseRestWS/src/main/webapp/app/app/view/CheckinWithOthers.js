Ext.define('EatSense.view.CheckinWithOthers', {
    extend: 'Ext.Panel',
    xtype: 'checkinwithothers',
    fullscreen : false,
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
				//width: '150px',
				//height: '200px'
				//flex : 1
			},
			items : [ {
				xtype : 'label',
				id : 'checkInDlg2Label1',
				styleHtmlContent: true, 
				html : 'Others are already at this spot.' //+ if(this.data.checkInData !== undefined) this.data.checkInData.restaurantName
			}, {
				xtype: 'label',
				id : 'checkInDlg2Label2',
				styleHtmlContent: true,  
				html: 'Do you want to check in with somebody?'
			}, 
			{
				xtype : 'list',
				id : 'checkinDlg2Userlist',
				type: 'fit',
				height: '200px',
				width: '150px',
				styleHtmlContent: true, 
				fullScreen: true,				
				itemTpl: '<div><strong>{nickname}</strong></div>'
				
			}, {
				xtype : 'button',
				id : 'checkinDlg2CancelBt',
				text : 'Cancel',
				ui : 'round'
			}]
		} ]
    }

});