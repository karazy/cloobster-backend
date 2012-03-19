//Fixes Problem in 2.0 FINAL of Radio Buttons not firing checked
Ext.define('EatSense.override.RadioOverride', {
    override: 'Ext.field.Radio',
    onMaskTap: function(component, e) {
        var me = this,
            dom = component.input.dom;


        if (me.getDisabled()) {
            return false;
        }
        
        if(!me.getChecked())
            dom.checked = !dom.checked;


        //calling getchecked will sync the new checked value
        if (me.getChecked()) {
            me.fireEvent('check', me, e);
        }
        else {
            me.fireEvent('uncheck', me, e);
        }


        //return false so the mask does not disappear
        return false;
    }
});