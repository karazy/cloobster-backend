Ext.define('EatSense.override.CustomSpinner', {
	override: 'Ext.field.Spinner',
	    updateComponent: function(newComponent) {
        this.callParent(arguments);

        var innerElement = this.innerElement,
            cls = this.getCls();

        if (newComponent) {
            this.spinDownButton = Ext.Element.create({
                // cls : cls + '-button ' + cls + '-button-down',
                cls: 'productdetail-spinner-down',
                html: '<img src="res/images/spinner_down_big.png"></img>'          
            });

            this.spinUpButton = Ext.Element.create({
                // cls : cls + '-button ' + cls + '-button-up',
                cls: 'productdetail-spinner-up',
                html: '<img src="res/images/spinner_up_big.png"></img>'
            });

            this.downRepeater = this.createRepeater(this.spinDownButton, this.onSpinDown);
            this.upRepeater = this.createRepeater(this.spinUpButton,     this.onSpinUp);
        }
    }
});