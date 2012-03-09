Ext.define('EatSense.data.proxy.CustomRestProxy', {
	override: 'Ext.data.proxy.Rest',
	  buildUrl: function(request) {
//	        var me        = this,
//	            operation = request.getOperation(),
//	            records   = operation.getRecords() || [],
//	            record    = records[0],
//	            format    = me.getFormat(),
//	            url       = me.getUrl(request),
//	            params    = request.getParams() || {},
//	            id        = (record && !record.phantom) ? record.getId() : params.id,
//	            _serviceUrl = globalConf.serviceUrl;
//
//	        if (me.getAppendId() && id) {
//	            if (!url.match(/\/$/)) {
//	                url += '/';
//	            }
//	            url += id;
//	            delete params.id;
//	        }
//
//	        if (format) {
//	            if (!url.match(/\.$/)) {
//	                url += '.';
//	            }
//
//	            url += format;
//	        }
			//------TEST
	        var  me = this, _serviceUrl = globalConf.serviceUrl, 
	        	url = me.getUrl(request),
	        	params = request.getParams() || {};

	        if(params.restaurantId) {
	        	url = '/restaurants/'+params.restaurantId+url;
	        	delete params.restaurantId;
	        };
	        	
	        request.setUrl(_serviceUrl + url);
			//------TEST END

			
	        //  var  me = this, _serviceUrl = globalConf.serviceUrl, url = me.getUrl(request);
	        // request.setUrl(_serviceUrl + url);

	        return me.callParent([request]);
	    }
});