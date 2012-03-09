Ext.define('EatSense.store.Spot', {
			extend: 'Ext.data.Store',
			config: {
				model: 'EatSense.model.Spot',
				storeId: 'spotStore',
			// proxy: {
			// 	type: 'rest',
			// 	url: 'restaurants/1/spots',
			// 	reader: {
			// 		type: 'json'
			// 	}
			// },
			// data: [
			// {barcode: 'hup001', restaurant: 'Heidi und Paul', restaurantId: 1, name: 'Tisch 1', status: 'CHECKEDIN', checkInTime: '', currentTotal: '10'},
			// {barcode: 'hup002', restaurant: 'Heidi und Paul', restaurantId: 1, name: 'Tisch 2', status: 'PLACED', checkInTime: '', currentTotal: '10'},
			// {barcode: 'hup003', restaurant: 'Heidi und Paul', restaurantId: 1, name: 'Tisch 3', status: 'PLACED', checkInTime: '', currentTotal: '10'},
			// {barcode: 'hup004', restaurant: 'Heidi und Paul', restaurantId: 1, name: 'Tisch 4', status: 'CHECKEDIN', checkInTime: '', currentTotal: '10'},
			// {barcode: 'hup005', restaurant: 'Heidi und Paul', restaurantId: 1, name: 'Tisch 5', status: 'PAYMENT_REQUEST', checkInTime: '', currentTotal: '10'},
			// {barcode: 'hup006', restaurant: 'Heidi und Paul', restaurantId: 1, name: 'Tisch 6', status: 'PLACED', checkInTime: '', currentTotal: '10'},
			// {barcode: 'hup007', restaurant: 'Heidi und Paul', restaurantId: 1, name: 'Tisch 7', status: '', checkInTime: '', currentTotal: '10'},
			// {barcode: 'hup008', restaurant: 'Heidi und Paul', restaurantId: 1, name: 'Tisch 8', status: '', checkInTime: '', currentTotal: '10'},
			// {barcode: 'hup009', restaurant: 'Heidi und Paul', restaurantId: 1, name: 'Tisch 9', status: 'CHECKEDIN', checkInTime: '', currentTotal: '10'},
			// {barcode: 'hup010', restaurant: 'Heidi und Paul', restaurantId: 1, name: 'Tisch 10',status: '', checkInTime: '', currentTotal: '10'},
			// {barcode: 'hup011', restaurant: 'Heidi und Paul', restaurantId: 1, name: 'Tisch 11',status: 'PLACED', checkInTime: '', currentTotal: '10'},
			// {barcode: 'hup012', restaurant: 'Heidi und Paul', restaurantId: 1, name: 'Tisch 12',status: '', checkInTime: '', currentTotal: '10'},
			// {barcode: 'hup013', restaurant: 'Heidi und Paul', restaurantId: 1, name: 'Tisch 13',status: 'PAYMENT_REQUEST', checkInTime: '', currentTotal: '10'},
			// {barcode: 'hup014', restaurant: 'Heidi und Paul', restaurantId: 1, name: 'Tisch 14',status: 'CHECKEDIN', checkInTime: '', currentTotal: '10'},
			// {barcode: 'hup015', restaurant: 'Heidi und Paul', restaurantId: 1, name: 'Tisch 15',status: '', checkInTime: '', currentTotal: '10'},
			// {barcode: 'hup016', restaurant: 'Heidi und Paul', restaurantId: 1, name: 'Tisch 16',status: '', checkInTime: '', currentTotal: '10'},
			// {barcode: 'hup017', restaurant: 'Heidi und Paul', restaurantId: 1, name: 'Tisch 17',status: '', checkInTime: '', currentTotal: '10'},
			// {barcode: 'hup018', restaurant: 'Heidi und Paul', restaurantId: 1, name: 'Tisch 18',status: 'CHECKEDIN', checkInTime: '', currentTotal: '10'},
			// {barcode: 'hup019', restaurant: 'Heidi und Paul', restaurantId: 1, name: 'Tisch 19',status: '', checkInTime: '', currentTotal: '10'},
			// {barcode: 'hup020', restaurant: 'Heidi und Paul', restaurantId: 1, name: 'Tisch 20',status: 'PLACED', checkInTime: '', currentTotal: '10'},
			// {barcode: 'hup021', restaurant: 'Heidi und Paul', restaurantId: 1, name: 'Tisch 21',status: '', checkInTime: '', currentTotal: '10'},
			// {barcode: 'hup022', restaurant: 'Heidi und Paul', restaurantId: 1, name: 'Tisch 22',status: 'PLACED', checkInTime: '', currentTotal: '10'},
			// {barcode: 'hup023', restaurant: 'Heidi und Paul', restaurantId: 1, name: 'Tisch 23',status: '', checkInTime: '', currentTotal: '10'},
			// {barcode: 'hup024', restaurant: 'Heidi und Paul', restaurantId: 1, name: 'Tisch 24',status: '', checkInTime: '', currentTotal: '10'},

			// ]
			}
			
		});