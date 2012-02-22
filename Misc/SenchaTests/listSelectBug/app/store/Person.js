Ext.define('ListTest.store.Person', {
    extend  : 'Ext.data.Store',
    model   : 'ListTest.model.Person',
    requires: ['ListTest.model.Person'],
   data: [
       {firstName: 'Tommy',   lastName: 'Maintz'},
       {firstName: 'Rob',     lastName: 'Dougan'},
       {firstName: 'Ed',      lastName: 'Spencer'},
       {firstName: 'Jamie',   lastName: 'Avins'},
       {firstName: 'Aaron',   lastName: 'Conran'},
       {firstName: 'Dave',    lastName: 'Kaneda'},
       {firstName: 'Jacky',   lastName: 'Nguyen'},
       {firstName: 'Abraham', lastName: 'Elias'},
       {firstName: 'Jay',     lastName: 'Robinson'},
       {firstName: 'Nigel',   lastName: 'White'},
       {firstName: 'Don',     lastName: 'Griffin'},
       {firstName: 'Nico',    lastName: 'Ferrero'},
       {firstName: 'Nicolas', lastName: 'Belmonte'},
       {firstName: 'Jason',   lastName: 'Johnston'}
   ]
});