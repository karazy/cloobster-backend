var i18nPlugin = Karazy.i18n;
i18nPlugin.setTranslations(de_translation);

Ext.Loader.setConfig({enabled:true});
Ext.application({
    name: 'EatSense',
    controllers: ['CheckIn', 'Menu'],
    models: ['CheckIn', 'User', 'Menu', 'Product'],
    launch: function() {
    	this.launched = true;
        this.mainLaunch();
    },
    mainLaunch: function() {
        if (!device || !this.launched) {return;}
        console.log('mainLaunch');
    }
});
