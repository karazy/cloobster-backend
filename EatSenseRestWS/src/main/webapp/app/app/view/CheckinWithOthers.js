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
				width: '150px',
				height: '200px'
			},
			items : [ {
				xtype : 'label',
				styleHtmlContent: true, 
				html : 'Others are already at this spot.' //+ if(this.data.checkInData !== undefined) this.data.checkInData.restaurantName
			}, {
				xtype: 'label',
				styleHtmlContent: true,  
				html: 'Do you want to check in with somebody?'
			}, 
			
			{
				layout : {
					type: 'fit',
					height: '200px',
					width: '150px',
					styleHtmlContent: true, 
				},
				items: [{
					xtype : 'list',
					id : 'checkinDlg2Userlist',
					fullScreen: true,
				
					itemTpl: '<h2><strong>{nickname}</strong></h2>'
					/*,store: {
						model: 'EatSense.model.User',
		
			   			   data: [
			   				 {userId: '1',   nickname: 'Nils'},
			   				 {userId: '2',   nickname: 'Fred'}
			   			   ]
					}*/
					
				}
				]
				
			}, {
				xtype : 'button',
				id : 'checkinDlg2CancelBt',
				text : 'Cancel',
				ui : 'round'
			}]
		} ]
    }

});