Ext.define('EatSense.override.OperationImprovement', {
    override: 'Ext.data.Operation',

    processCreate: function(resultSet) {
    	console.log('Ext.ux.OperationImprovement');
    	
        var updatedRecords = resultSet.getRecords(),
            currentRecords = this.getRecords(),
            ln = updatedRecords.length,
            i, currentRecord, updatedRecord;
        
        // <debug>
        if (ln==0) {
                Ext.Logger.warn('Unable to find any record to match in the result set that came back from the server.');
        }
        // </debug>

        for (i = 0; i < ln; i++) {
            updatedRecord = updatedRecords[i];

            if (updatedRecord.clientId === null && currentRecords.length == 1 && updatedRecords.length == 1) {
                currentRecord = currentRecords[i];
            } else {
                currentRecord = this.findCurrentRecord(updatedRecord.clientId);
            }

            if (currentRecord) {
                this.updateRecord(currentRecord, updatedRecord);
            }
            // <debug>
            else {
                Ext.Logger.warn('Unable to match the record that came back from the server.');
            }
            // </debug>
        }

        return true;
    }
});