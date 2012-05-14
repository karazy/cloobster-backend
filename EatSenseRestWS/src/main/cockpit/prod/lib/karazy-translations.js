/*Karazy namespace. Create if not exists.*/
var Karazy = (Karazy) ? Karazy : {};

/**
 * 
 */
Karazy.translations = (function() {

	return {
		"DE" : {

		// General translations
		"ok" : "Ok",
		"cancel" : "Abbrechen",
		"back" : "zurück",
		"barcode" : "Barcode",		
		"close" : "Schliessen",
		"loadingMsg" : "Laden ...",
		"hint" : "Hinweis",
		"success" : "Erfolg",
		"yes" : "Ja",
		"no" : "Nein",
		"comment" : "Kommentar",
		//tabbar
		"spotsTitle" : "Tische",
		//login
		"needCredentials" : "Bitte gib einen Benutzernamen und Passwort an.",
		"wrongCredentials" : "Benutzername und/oder Passwort falsch.",
		"savePasswordToggle" : "Automatisch<br/>einloggen?",
		"logoutQuestion" : "Wirklich ausloggen?",
		"noBusinessAssigned" : "Dein Benutzer ist keinem Betrieb zugewiesen.",
		"restoreCredentialsErr" : "Login mit gespeicherten Daten fehlgeschlagen.",
		"chooseStore" : "Filiale auswählen",
		//spot
		"errorSpotLoading" : "Laden der Daten fehlgeschlagen!",
		//spot details
		"spotDetailCustomerLabel" : "Kunden",
		"errorSpotDetailCheckInLoading" : "Laden der eingecheckten Personen fehlgeschlagen!",
		"errorSpotDetailOrderLoading" : "Laden der Bestellungen fehlgeschlagen!",
		"errorSpotDetailOrderSave" : "Speichern der Bestellung fehlgeschlagen!",
		"status" : "Status",
		"statistic" : "Infos",
		"processOrdersFirst" : "Bitte arbeite zuerst alle Bestellungen ab.",
		"cancelAllOrders" : "Möchtest du alle Bestellungen stornieren und diesen Gast entfernen?",
		"errorSpotDetailOrderCancel" : "Bestellung {0} konnte nicht storniert werden.",
		"switchSpotButton" : "Tisch wechseln",
		"switchSpotMessage": "{0} an {1} verschoben",
		"cancelAllOrdersButton" : "Gast entfernen",
		"confirmAllOrdersButton" : "Alle abgearbeitet",
		"paidButton" : "Bezahlt",
		"spotSelectionTitle" : "Tisch auswählen",
		"switchSpotError" : "Server Fehler aufgetreten. Tisch wechseln fehlgeschlagen!",
		"paymentMethodLabel" : "Bezahlart",
		"choiceValErrMandatory" : "Bitte triff eine Wahl für {0}",
		"choiceValErrMin" : "Bitte wähle mindestens {0} {1} aus.",
		"choiceValErrMax" : "Du kannst maximal  {0} {1} auswählen.",
		//requests
		"requestMsgboxTitle" : "Kundenwünsche",
		"requestDismiss" : "Kundenwünsche löschen",
		//constants
		"ORDER_PLACED" : "Neue Bestellung!",
		"PLACED" : "Neu!",
		"CHECKEDIN" : "Checked-In",
		"PAYMENT_REQUEST" : "Bezahlen",
		"CALL_WAITER" : "{0} ruft eine Bedienung",
		//general errors
		"error" : "Fehler",
		"errorTitle" : "Fehler",		
		"errorMsg" : "Sorry! Ein Fehler ist aufgetreten.<br/>Wir kümmern uns darum!",
		"errorResource" : "Daten konnten nicht vom Server geladen werden.",
		"errorPermission" : "Deine Sitzung ist ungültig.",
		"errorCommunication" : "Leider ist der eatSense Server nicht erreichbar.<br/>Wir kümmern uns darum!",
		"errorGeneralCommunication" : "Beim Laden von Aktualisierungsdaten trat ein Fehler auf.<br/>Zur Sicherheit sollten Sie sich neu anmelden.",
		"resourceNotAvailable" : "Angeforderte Resource ist nicht erreichbar.",
		"channelTokenError" : "Updates im Hintergrund nicht funktionsfähig."
		}
	}
}());