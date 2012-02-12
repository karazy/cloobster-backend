Ext.require('Ext.container.Viewport');

Ext.application({
	name : 'eatSense DickPit',
	launch : function() {
		Ext.create('Ext.container.Viewport', {
			layout : 'fit',
			id : 'viewport',
			items : [ {
				xtype : 'panel',
				title : 'eatSense DickPit',
				itemId : 'wrapper',
				layout : {
					type : 'fit',
				},
				items : [ {
					xtype : 'tabpanel',
					items : [ {
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
//							anchor : '100%',
//							height : '100%',
							style: {
								margin: '10px'
							}
//							width : 500
						}, 
//						{
//							xtype : 'panel',
//							layout : {
//								type : 'hbox',
//								pack : 'center',
//								align : 'middle'
//							},
//							frame: false,
//							bodyBorder: false,
////							width : 200,
////							height : 200,
//							 defaults : {
//								 style: {
//							            margin : '10px'
//							        }
//							 },
//							items : [ {
//								xtype : 'button',
//								text : 'Clear data',
//								handler : function() {
//									Ext.getCmp('viewport').getComponent('wrapper').getComponent('jsonData').setValue('');
//								}
//							}, {
//								xtype : 'button',
//								text : 'Upload data',
//								handler : function() {
//									Ext.Ajax.request({
//										url : '/restaurant/import',
//										// 'file://Users/fred/karazy/Dropbox/karazy_entwicklung/import_Sergio.json',
//										method : 'PUT',
//										scope : this,
//										jsonData : Ext.getCmp('viewport').getComponent('wrapper').getComponent('jsonData').getValue(),
//										success : function(response) {
//											Ext.Msg.alert('Success', 'Upload finished');
//										},
//										failure : function(response) {
//											Ext.Msg.alert('Error', response.statusText);
//										}
//									});
//								}
//							} ]
//						}
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
												url : '/restaurants/import',
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
															url : '/restaurants/deleteall', 
															method : 'GET',
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
									}
						    ]
						}]
						
					}, {
						xtype: 'panel',
						title: 'empty tab',
						listeners: {
			                render: function() {
			                    Ext.MessageBox.alert('Genius', 'Guess what. I\'m empty!');
			                }
			            },
			            dockedItems: [{
						    xtype: 'toolbar',
						    dock: 'bottom',
						    items: [
						        { xtype: 'button', text: 'Button 1' }
						    ]
						}]
					}

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