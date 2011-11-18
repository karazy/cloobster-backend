Ext.define('EatSense.view.dialog.CheckInConfirmation', {
    extend: 'Ext.Component',
    xtype: 'checkInConfirmation',
    requires: ['Ext.XTemplate'],

    config: {
      //  cls: 'detail-card',
        styleHtmlContent: true,

        tpl: Ext.create('Ext.XTemplate',
            '<tpl if="{status} == success">Check in at {restaurantName}?</tpl>'
        )
    }
});