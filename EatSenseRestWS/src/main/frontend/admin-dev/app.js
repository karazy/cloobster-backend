Ext.require('Ext.container.Viewport');

Ext.application({
	name : 'eatSense DickPit',
	launch : function() {
		Ext.create('Ext.container.Viewport', {
			layout : 'fit',
			id : 'viewport',
			items : [ {
				xtype : 'panel',
				title : 'eatSense admin panel',
				itemId : 'wrapper',
				layout : {
					type : 'fit',
				},
				items : [ {
					xtype : 'tabpanel',
					items : [ 
					//<mass-import>
					{ 
						xtype : 'panel',
						title : 'data mass import',
											
						layout: {
							type: 'vbox',
								align: 'stretch'
						},
						items : [ {
							xtype: 'container',
							html: '<h1>Use Textarea below to input json data containing restaurant, spot, menu informations. </h1>',	
							height: 20,
							style: {
								margin: '10px'
							}
						},{
							xtype : 'textareafield',
							 flex: 1,
							id : 'jsonData',
							allowBlank : false,
							name : 'message',
							fieldLabel : 'JSON data',
							labelAlign : 'top',
							style: {
								margin: '10px'
							}
						}, 
						],
			            dockedItems: [{
						    xtype: 'toolbar',
						    dock: 'bottom',
						    items: [
						            {
										xtype : 'button',
										text : 'Clear data field',
										handler : function() {
											Ext.getCmp('jsonData').setValue('');
										}
									}, {
										xtype : 'button',
										text : 'Upload data',
										handler : function() {
											Ext.Ajax.request({
												url : '/c/businesses/import',
												// 'file://Users/fred/karazy/Dropbox/karazy_entwicklung/import_Sergio.json',
												method : 'PUT',
												scope : this,
												jsonData : Ext.getCmp('jsonData').getValue(),
												success : function(response) {
													Ext.Msg.alert('Success', 'Upload finished');
												},
												failure : function(response) {
													Ext.Msg.alert('Error', response.statusText);
												}
											});
										}
									},
									{
										xtype: 'button',
										text: 'Delete all datastore content',
										handler: function() {
											Ext.Msg.show({
												title: 'ATTENTION',
												msg: 'This will <strong>delete</strong> all data. <br/> This function will be removed in production.',
												buttons: Ext.Msg.YESNO,
												icon: Ext.Msg.QUESTION,
												fn: function(btn) {
													if (btn == 'yes'){
														Ext.Ajax.request({
															url : '/c/businesses/all', 
															method : 'DELETE',
															success : function(response) {
																Ext.Msg.alert('Success', "All data deleted. I'm sure you know what you did!");
															},
															failure : function(response) {
																Ext.Msg.alert('Error', response.statusText);
															}
														});
											    }
												}
											});
										}
									},
									{
										xtype: 'button',
										text: 'Delete live data',
										handler: function() {
											Ext.Msg.show({
												title: 'ATTENTION',
												msg: 'This will <strong>delete</strong> all only live data. <br/> This function will be removed in production.',
												buttons: Ext.Msg.YESNO,
												icon: Ext.Msg.QUESTION,
												fn: function(btn) {
													if (btn == 'yes'){
														Ext.Ajax.request({
															url : '/c/businesses/livedata', 
															method : 'DELETE',
															success : function(response) {
																Ext.Msg.alert('Success', "All live data deleted. I'm sure you know what you did!");
															},
															failure : function(response) {
																Ext.Msg.alert('Error', response.statusText);
															}
														});
											    }
												}
											});
										}
									}
						    ]
						}]
						
					}, 
					//</mass-import>
					//<nickname-import>
					{

						xtype : 'panel',
						title : 'nickname import',											
						layout: {
							type: 'vbox',
								align: 'stretch'
						},
						items : [ {
							xtype: 'container',
							html: '<h1>Use Textarea below to input json data containing nickname  informations. </h1>',	
							height: 20,
							style: {
								margin: '10px'
							}
						},{
							xtype : 'textareafield',
							 flex: 1,
							id : 'nicknameData',
							allowBlank : false,
							name : 'message',
							fieldLabel : 'JSON data',
							labelAlign : 'top',
							style: {
								margin: '10px'
							}
						},
						{
					        xtype: 'radiogroup',
					        fieldLabel: 'Nickname type',
					        id: 'nicknameType',
					        // Arrange radio buttons into two columns, distributed vertically
					        columns: 1,
					        vertical: true,
					        items: [
					            { boxLabel: 'Nickname', name: 'nicknameRb', inputValue: 'noun', checked: true },
					            { boxLabel: 'Adjective', name: 'nicknameRb', inputValue: 'adjective'}
					        ]
					    }	 
						],
			            dockedItems: [{
						    xtype: 'toolbar',
						    dock: 'bottom',
						    items: [
						            {
										xtype : 'button',
										text : 'Clear data field',
										handler : function() {
											Ext.getCmp('nicknameData').setValue('');
										}
									}, 									
																	
									{
										xtype : 'button',
										text : 'Upload data',
										handler : function() {
											//get type
											var radios = Ext.getCmp('nicknameType'),
												type = radios.getValue();

											Ext.Ajax.request({
												url : '/nicknames/'+type.nicknameRb+'/list',
												method : 'PUT',
												scope : this,
												jsonData : Ext.getCmp('nicknameData').getValue(),
												success : function(response) {
													Ext.Msg.alert('Success', 'Upload finished');
												},
												failure : function(response) {
													Ext.Msg.alert('Error', response.statusText);
												}
											});
										}
									}
						    ]
						}]											
					},
					//</nickname-import>
					]
				}
				// {
				// xtype : 'label',
				// html : '<h1>Data upload for restaurant mass import</h1>',
				// },
				/*
				 * { xtype : 'filefield', name : 'jsonData', itemId: 'jsonFile',
				 * fieldLabel : 'JSON', labelWidth : 50, msgTarget : 'side',
				 * allowBlank : false, anchor : '100%', buttonText : 'Select
				 * JSON file' }, { xtype: 'button', text: 'Upload file', handler :
				 * function() { Ext.Ajax.request({ url: '/restaurant/import',
				 * //'file://Users/fred/karazy/Dropbox/karazy_entwicklung/import_Sergio.json',
				 * method : 'PUT', scope: this, jsonData :
				 * this.ownerCt.getComponent('jsonData').getValue(), success:
				 * function(response){ Ext.Msg.alert('Success', 'Upload
				 * finished'); }, failure: function(response) {
				 * Ext.Msg.alert('Error', response.statusText); } }); } },
				 */
				]
			}

			]
		});
	}
});