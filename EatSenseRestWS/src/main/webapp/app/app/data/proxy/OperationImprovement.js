Ext.define('EatSense.data.proxy.OperationImprovement', {
    override: 'Ext.data.Operation',

    processCreate: function(resultSet) {
    	console.log('OperationImprovement called');
        var updatedRecords = resultSet.getRecords(),
            currentRecords = this.getRecords(),
            ln = updatedRecords.length,
            i, currentRecord, updatedRecord;

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