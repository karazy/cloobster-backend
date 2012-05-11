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
		"change" : "Ändern",
		"barcode" : "Barcode",
		"close" : "Schliessen",
		"loadingMsg" : "Laden ...",
		"hint" : "Hinweis",
		"success" : "Erfolg",
		"yes" : "Ja",
		"no" : "Nein",
		"leave" : "Verlassen",
		"channelTokenError" : "Updates im Hintergrund nicht funktionsfähig.",
		// main menu
		"checkInButton" : "CheckIn",
		"currentDealsButton" : "Deals",
		"newRestaurantsButton" : "Neue Restaurants",
		"settingsButton" : "Einstellungen",
		// Checkin
		"checkInTitle" : "CheckIn",
		"barcodePromptTitle" : "Barcode Abfrage",
		"barcodePromptText" : "Bitte gib den Tischcode ein.",
		"checkInStep1Label1" : "Wähle einen Nickname",
		"refreshNicknameBt" : "Neu generieren",
		"checkInStep1Button" : "Los gehts!",
		"checkInStep2Label1" : "Andere haben hier bereits eingecheckt.",
		"checkInStep2Label2" : "Willst du mit jemand Anderem einchecken?",
		"checkInStep2OnMyOwnButton" : "Ich bin alleine hier",
		"checkInErrorBarcode" : "Der Barcode ist nicht valide oder leer!",
		"checkInErrorNickname" : "Der Nickname muss zwischen {0} und {1} Zeichen lang sein.",
		"checkInErrorNicknameExists" : "Der Nickname wird an diesem Spot bereits benutzt.",
		"saveNicknameToggle" : "Nickname als Standard setzen?",
		"checkInCanceled" : "Deine Sitzung wurde durch das Restaurant beendet.",
		"nickname" : "Nickname",
		// Menu
		"menuTitle" : "Karte",
		"choicesPanelTitle" : "Optionen",
		"putIntoCartButton" : "In den Warenkorb",
		"choiceValErrMandatory" : "Bitte triff eine Wahl für {0}",
		"choiceValErrMin" : "Bitte wähle mindestens {0} {1} aus.",
		"choiceValErrMax" : "Du kannst maximal  {0} {1} auswählen.",
		//Order
		"orderInvalid" : "Bitte überprüfe deine Auswahl",
		"orderPlaced" : "Bestellung im Warenkorb",
		"cartEmpty" : "Du hast noch keine Bestellung getätigt.",
		"productPutIntoCardMsg" : "{0} &gt; Warenkorb",
		"orderRemoved" : "Bestellung entfernt",
		"orderComment" : "Optionaler Kommentar",
		"amount" : "Menge",
		"orderSubmit" : "Bestellung abgeschickt ...<br/> Guten Appetit!",
		"productCartBt" : "In den Warenkorb",
		"orderCanceled" : "{0} wurde storniert.",
		//Cart
		"cartviewTitle" : "Warenkorb",
		"cartTabBt" : "Warenkorb",
		"dumpCart" : "Warenkorb leeren?",
		"dumpItem" : "{0} entfernen?",
		"submitButton" : "Bestellen",
		"submitOrderProcess" : "Sende Bestellung ...",
		"submitOrdersQuestion" : "Bestellung absenden?",
		//Lounge
		"loungeviewTitle" : "Die Lounge",
		//MyOrders
		"myOrdersTitle" : "Bestellübersicht",
		"myOrdersTabBt" : "Kasse",
		"payRequestButton" : "Bezahlen",
		"leaveButton" : "Verlassen",
		//Payment Request
		"paymentPickerTitle" : "Bezahlmethode",
		"paymentRequestSend": "Bitte warte einen Moment,</br>deine Rechnung wird vorbereitet ...",
		//Settings
		"settingsTitle" : "Einstellungen",
		"nickname" : "Nickname",
		"nicknameDesc" : "Mit deinem Spitznamen wirst du im Restaurant angezeigt.</br>Änderungen wirken sich erst auf den nächsten CheckIn aus.",
		"newsletterRegisterBt": "Registrieren",
		"newsletterEmail" : "E-Mail",
		"newsletterRegisterSuccess" : "Danke! Eine Bestätigungsmail wird an {0} geschickt.",
		"newsletterInvalidEmail" : "Bitte gib eine gültige E-Mail Adresse an.",
		"newsletterDuplicateEmail" : "Diese E-Mail Adresse ist bereits registriert",
		"newsletterPopupTitle" : "Newsletter abonnieren?", 
		"newsletterDontAskButton" : "Nicht mehr nachfragen!",
		"newsletterLabel" : "Stay up-to-date und verpasse nicht den Cloobster Big Bang! Melde dich für den Newsletter an.",
		//Request
		"errorRequest" : "Deine Anfrage konnte leider nicht bearbeitet werden.",
		"requestsButton" : "VIP",
		"requestsTitle" : "VIP",
		"requestCallWaiterSendMsd" : "Bitte habe einen Moment Geduld!<br>Es wird gleich jemand kommen.",
		"callWaiterButton" : "Bedienung rufen",
		"callWaiterRequestBadge" : "Kellner gerufen",	
		//general errors
		"errorTitle" : "Fehler",		
		"errorMsg" : "Sorry! Ein Fehler ist aufgetreten.<br/>Wir kümmern uns darum!",
		"errorResource" : "Daten konnten nicht vom Server geladen werden.",
		"errorPermission" : "Deine Sitzung ist ungültig.",
		"errorCommunication" : "Leider ist der eatSense Server nicht erreichbar.<br/>Wir kümmern uns darum!"
		}
	}
	
}());
