Ext.Loader.setConfig({enabled:true});
Ext.application({
    name: 'EatSense',
    controllers: ['CheckIn'],
    models: ['CheckIn', 'User']
});
