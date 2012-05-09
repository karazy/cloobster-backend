/**
 * 
 */
//Ext.define('EatSense.SpecializedCardPanel', {
//	extend: 'Ext.Panel',
//	xtype: 'espanel',
//	/**
//	 * Change the direction of the slide animation.
//	 * @param direction
//	 * 			left or right
//	 */
//	switchMenuview : function(view, direction){
//		var cardpanel = this.getComponent('cardPanel');
//		cardpanel.getLayout().setAnimation({
//			 type: 'slide',
//	         direction: direction
//		});
//		cardpanel.setActiveItem(view);
//	},
//	/**
//	 * Hides the back button in top toolbar.
//	 */
//	hideBackButton: function() {
//		this.getComponent('topBar').getComponent('backBt').hide();
//	},
//	/**
//	 * Shows the back button in top toolbar.
//	 * @param text
//	 * 		Label to display on button.
//	 */
//	showBackButton: function(text) {
//		this.getComponent('topBar').getComponent('backBt').setText(text);
//		this.getComponent('topBar').getComponent('backBt').show();
//	}
//});