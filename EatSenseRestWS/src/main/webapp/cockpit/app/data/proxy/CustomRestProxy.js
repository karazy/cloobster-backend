Ext.define('EatSense.data.proxy.CustomRestProxy', {
	override: 'Ext.data.proxy.Rest',
	  buildUrl: function(request) {		
	        var  me = this, _serviceUrl = globalConf.serviceUrl, 
	        	url = me.getUrl(request),
	        	params = request.getParams() || {},
	        	defaultHeaders = Ext.Ajax.getDefaultHeaders() || {};

	        if(params.pathId) {
	        	if(url.match(/(.*){pathId}(.*)/)) {
	        		var replacer = '$1'+params.pathId+'$2';
	        		url = url.replace(/(.*){pathId}(.*)/, replacer);
	        		delete params.pathId;
	        	}	        	
	        } else if(defaultHeaders.pathId) {
	        	if(url.match(/(.*){pathId}(.*)/)) {
	        		var replacer = '$1'+defaultHeaders.pathId+'$2';
	        		url = url.replace(/(.*){pathId}(.*)/, replacer);
	        	}	
	        }
	        	
	        request.setUrl(_serviceUrl + url);

	        return me.callParent([request]);
	    }
});