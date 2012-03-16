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
	        var		me = this, 
	        		_serviceUrl = globalConf.serviceUrl, 
	        		url = me.getUrl(request);
	        
	        request.setUrl(_serviceUrl + url);

	        return me.callParent([request]);
	    },
	    
	    doRequest: function(operation, callback, scope) {
//	        var writer  = this.getWriter(),
//	            request = this.buildRequest(operation);
//
//	        request.setConfig({
//	            headers        : this.getHeaders(),
//	            timeout        : this.getTimeout(),
//	            method         : this.getMethod(request),
//	            callback       : this.createRequestCallback(request, operation, callback, scope),
//	            scope          : this
//	        });
//
//	        if (operation.getWithCredentials() || this.getWithCredentials()) {
//	            request.setWithCredentials(true);
//	        }
//
//	        // We now always have the writer prepare the request
//	        request = writer.write(request);
//
//	        Ext.Ajax.request(request.getCurrentConfig());
	    	console.log('custom doRequest');
	    	
	    	var me = this;

	        return me.callParent([operation, callback, scope]);
	    }
});