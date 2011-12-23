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
			},
			items : [ {
				xtype : 'label',
				styleHtmlContent: true,
				height:'100px',
				html : 'Others are already at this spot.' //+ if(this.data.checkInData !== undefined) this.data.checkInData.restaurantName
			}, {
				xtype: 'label',
				styleHtmlContent: true,
				height: '100px',
				html: 'Do you want to check in with somebody?'
			}, {
				xtype : 'list',
				id : 'checkinDlg2Userlist',
				required: true,   			   
				itemTpl: '<div><strong>{nickname}</strong></div>'
				// this.checkInData.nickname,
			}, {
				xtype : 'button',
				id : 'checkinDlg2CancelBt',
				text : 'Cancel',
				ui : 'round'
			}]
		} ]
    }

});