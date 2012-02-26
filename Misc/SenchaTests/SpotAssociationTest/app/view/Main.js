/**
 * The viewport. Defining the global layout for the application.
 */
Ext.define('EatSense.view.Main', {
	extend : 'Ext.Container',	
	requires : [ 'EatSense.view.Dashboard', 'EatSense.view.MenuOverview', 'EatSense.view.Checkinconfirmation','EatSense.view.ProductOverview','EatSense.view.CheckinWithOthers', 'EatSense.view.Cart', 'EatSense.view.Menu', 'EatSense.view.Lounge' ],
	xtype: 'mainview',
	config : {
		itemId : 'globalContainer',
		fullscreen : true,
		layout : {
			type: 'card',
			animation: {
	            type: 'slide',
	            direction: 'left'
	        }			
		},
		activeItem : 0,		
		items : [ {
			layout : 'fit',			
			xtype : 'dashboard'
		},
		{
			layout : 'fit',
			xtype : 'checkinconfirmation'
		}, 
		{
			layout: 'fit',
			xtype: 'checkinwithothers'
		},
//		{
//			layout: 'fit',
//			xtype: 'cart'
//		},
//		{
//			layout: 'fit',
//			xtype: 'menu'
//		}, 
		{
//			No fit layout! Won't display correctly otherwise.
			xtype: 'lounge'
		}
		]
	},
	/**
	 * Change the direction of the slide animation.
	 * @param direction
	 * 			left or right
	 */
	switchAnim : function(direction){
		this.getLayout().setAnimation({
			 type: 'slide',
	         direction: direction
		});
	}
});
