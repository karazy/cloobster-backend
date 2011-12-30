Ext.define('EatSense.store.Menu', {
    extend  : 'Ext.data.Store',
    model   : 'EatSense.model.Menu',
    requires: ['EatSense.model.Menu'],
    data: [
           {title: 'Speisen'},
           {title: 'Getraenke'}
       ]
});