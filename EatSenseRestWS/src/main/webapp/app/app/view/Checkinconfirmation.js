Ext.define('EatSense.view.Checkinconfirmation', {
    extend: 'Ext.Panel',
    xtype: 'checkinconfirmation',
    fullscreen : false,
    config: {
        items : [ {
			docked : 'top',
			xtype : 'toolbar',
			title : 'CheckIn Confirmation'
				//TODO add cancel button
		}, {
			xtype : 'panel',
			layout : {
				type : 'vbox',
				pack : 'center',
				align : 'center'
			},
			defaults : {
				margin : 5,
//				docked: 'top'
//				flex : 1
			},
			items : [ {
				xtype : 'label',
				styleHtmlContent: true,
				height:'100px',
				html : '<h1>CheckIn</h1>Do you want to check in at ' //+ if(this.data.checkInData !== undefined) this.data.checkInData.restaurantName
			}, {
				xtype: 'label',
				styleHtmlContent: true,
				height: '100px',
				html: 'Choose a nickname associated with your checkIn:'
			}, {
				xtype : 'textfield',
				id : 'nicknameTf',
				label: 'Nickname',
				required: true,
				// this.checkInData.nickname,
			}, {
				xtype : 'button',
				id : 'confirmCheckInBt',
				text : 'CheckIn',
				ui : 'round'
			}]
		} ]
    }
	/*,
    constructor: function() {
    	//this.data =
    	alert('test');
    } */
});