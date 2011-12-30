Ext.Loader.setConfig({enabled:true});
Ext.application({
    name: 'EatSense',
    controllers: ['CheckIn', 'Menu'],
    models: ['CheckIn', 'User', 'Menu', 'Product']
});
