Ext.Loader.setConfig({enabled:true});
Ext.application({
    name: 'EatSense',
    controllers: ['CheckIn'],
    models: ['CheckIn'],
    launch: function() {
        this.launched = true;
        this.mainLaunch();
    },
    mainLaunch: function() {
        if (!device || !this.launched) {return;}
        console.log('mainLaunch');
    }
});
