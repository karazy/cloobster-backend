Ext.define('EatSense.data.proxy.Rest', {
	extend: 'Ext.data.proxy.Rest',
//	require: 'Ext.data.proxy.Rest',
//	 constructor: function (config) {
//         this.callParent(arguments); // calls Ext.tip.ToolTip's constructor
//         //...
//     },
	  buildUrl: function(request) {
		  alert('test');
	        var me        = this,
	            operation = request.getOperation(),
	            records   = operation.getRecords() || [],
	            record    = records[0],
	            format    = me.getFormat(),
	            url       = me.getUrl(request),
	            params    = request.getParams() || {},
	            id        = (record && !record.phantom) ? record.getId() : params.id;

	        if (me.getAppendId() && id) {
	            if (!url.match(/\/$/)) {
	                url += '/';
	            }
	            url += id;
	            delete params.id;
	        }

	        if (format) {
	            if (!url.match(/\.$/)) {
	                url += '.';
	            }

	            url += format;
	        }

	        request.setUrl(url);

	        return me.callParent([request]);
	    }
});