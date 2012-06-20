package net.eatsense.util;

import java.util.ArrayList;
import java.util.List;

import net.eatsense.auth.Role;
import net.eatsense.domain.Business;
import net.eatsense.domain.Choice;
import net.eatsense.domain.Menu;
import net.eatsense.domain.Product;
import net.eatsense.domain.Spot;
import net.eatsense.domain.embedded.PaymentMethod;
import net.eatsense.domain.embedded.ProductOption;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.ChoiceRepository;
import net.eatsense.persistence.MenuRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.SpotRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;

public class DummyDataDumper {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private BusinessRepository rr;
	private SpotRepository br;

	private MenuRepository mr;

	private ProductRepository pr;
	private ChoiceRepository cr;

	private AccountRepository ar;

	@Inject
	public DummyDataDumper(BusinessRepository rr, SpotRepository br, MenuRepository mr, ProductRepository pr, ChoiceRepository cr, AccountRepository ar) {
		this.ar = ar;
		this.rr = rr;
		this.br = br;
		this.mr = mr;
		this.pr = pr;
		this.cr = cr;
	}
	
	public void generateDummyUsers() {
		//generate admin user for businesses
		List<Key<Business>> testBusinesses = rr.getKeysByProperty("name", "Cloobster Club");
		if(!testBusinesses.isEmpty()) {
			logger.info("Create account for Cloobster Club");
			ar.createAndSaveAccount("Test Account","cloobster", "test!1", "developer@karazy.net", Role.COMPANYOWNER, testBusinesses, null, null, null, true, true);
		}
			
		List<Key<Business>> hup = rr.getKeysByProperty("name", "Heidi und Paul");
		if(!hup.isEmpty()) {
			logger.info("Create account for Heidi und Paul");
			ar.createAndSaveAccount("HuP Account","hup", "test", "info@cloobster.com", Role.COMPANYOWNER,  hup, null, null, null, true, true);
		}
		
		ar.createAndSaveAccount("Administrator","admin", "cl00bster!", "developer@karazy.net", Role.COMPANYOWNER, rr.getAllKeys(), null, null, null, true, true);
	}	

	public void generateDummyBusinesses() {
		logger.info("Generate Dummy Businesses.");
		createAndSaveDummyBusiness("Mc Donald's", "Fast food burger", "Fressecke", "mc123");
		createAndSaveDummyBusiness("Vappiano", "Pizza und Nudeln, schnell und lecker", "Hauptraum", "vp987");
		
		createSergioMenu( createAndSaveDummyBusiness("Sergio", "Bester Spanier Darmstadts", "Keller", "serg2011") );

	}

	private void createSergioMenu(Key<Business> kR) {

		//Getränke
		Key<Menu> kM = createMenu(kR, "Getränke", "Alkoholische, nicht-Alkoholische, heisse und kalte Getränke.");
		createAndSaveProduct(kM,kR, "kalte Milch", 1.5f, "lecker Milch", "Frische Bio-Milch von glücklichen Kühen direkt aus dem Schloss Bauernhof");
		createAndSaveProduct(kM,kR, "Weizen", 3.0f, "helles Hefeweizen vom Fass 0,5l",
				"Helles Hefeweizen vom Fass aus der Darmstädter Hofbrauerei im 0.5l Glas, 4.9% vol. Alkohol.");
		
		kM = createMenu(kR, "Hauptgerichte", "Schwein, Rind und vegetarische Speisen");
		Product burger = createProduct(kM,kR, "Classic Burger", 8.5f, "Burger mit Salat, Tomate, Zwiebel und Käse.",
				"Dies ist eine lange Beschreibung eines Burgers der Herstellung, seiner Zutaten und den Inhaltstoffen.");
		
		Key<Product> kP = pr.saveOrUpdate(burger);
		
		kM = createMenu(kR, "Beilagen", "Kartoffelprodukte und sonstiges");
		Product fries = createProduct(kM,kR, "Pommes Frites", 1.5f, "Pommes Frites",
				"Super geile Pommes Frites.");
		Product kraut = createProduct(kM,kR, "Krautsalat", 1f, "Weisskraut Salat mit Karotten (Coleslaw)",
				"");
		
		
		Key<Product> friesKey = pr.saveOrUpdate(fries);
		Key<Product> krautKey = pr.saveOrUpdate(kraut);
		
		
		Choice one = new Choice();
		
		one.setText("Wählen sie einen Gargrad:");
		ArrayList<ProductOption> options = new ArrayList<ProductOption>();
		options.add(new ProductOption("Roh", 0));
		options.add(new ProductOption("Medium", 0));
		options.add(new ProductOption("Brikett", 0));
		
		one.setOptions(options);
		one.setMaxOccurence(1);
		one.setMinOccurence(1);
		one.setProduct(kP);
		one.setBusiness(kR);
		one.setPrice(0f);
		
		Key<Choice> oneKey = cr.saveOrUpdate(one);
		
		Choice two = new Choice();
		two.setText("Extras");
		options = new ArrayList<ProductOption>();
		options.add(new ProductOption("Extra Käse", 1f));
		options.add(new ProductOption("Chili Sauce", 0.5f));
		options.add(new ProductOption("Salatgurken", 0.5f));
		options.add(new ProductOption("Ei", 1.0f));
		
		two.setOptions(options);
		two.setMaxOccurence(0);
		two.setMinOccurence(0);
		two.setProduct(kP);
		two.setBusiness(kR);
		two.setPrice(0f);
		
		Key<Choice> twoKey = cr.saveOrUpdate(two);
		
		Choice three = new Choice();
		three.setText("Menü");
		options = new ArrayList<ProductOption>();
		options.add(new ProductOption("Cola", 3f));
		options.add(new ProductOption("Cola Light", 3f));
		
		three.setOptions(options);
		three.setMaxOccurence(1);
		three.setMinOccurence(0);
		three.setProduct(kP);
		three.setBusiness(kR);
		three.setPrice(3f);
		
		Key<Choice> threeKey = cr.saveOrUpdate(three);
		
		Choice four = new Choice();
		four.setText("Menü Beilage");
		options = new ArrayList<ProductOption>();
		options.add(new ProductOption("mit Pommes", 0f));
		options.add(new ProductOption("mit Salat", 0.5f));
		
		four.setOptions(options);
		four.setMaxOccurence(1);
		four.setMinOccurence(1);
		four.setParentChoice(threeKey);
		four.setProduct(kP);
		four.setBusiness(kR);
		four.setPrice(3f);
		
		Key<Choice> fourKey = cr.saveOrUpdate(four);
		
		
		
	    List<Key<Choice>> choices = new ArrayList<Key<Choice>>();
	    
	    
	    
	    choices.add(oneKey);
	    choices.add(twoKey);
	    choices.add(threeKey);
	    choices.add(fourKey);
	    
	    burger.setChoices(choices);
	    
	    pr.saveOrUpdate(burger);
	    
	}

	private Key<Business> createAndSaveDummyBusiness(String name, String desc, String areaName, String barcode) {
		logger.info("Create dummy with data " + name + " " + desc + " " + areaName + " " + barcode);
		Business r = new Business();
		r.setName(name);
		r.setDescription(desc);
		ArrayList<PaymentMethod> methods = new ArrayList<PaymentMethod>();
		methods.add(new PaymentMethod("EC"));
		methods.add(new PaymentMethod("Bar"));
		r.setPaymentMethods(methods);
		Key<Business> kR = rr.saveOrUpdate(r);

		Spot spot = new Spot();
		
		spot.setBarcode(barcode);
		spot.setBusiness(kR);
		spot.setName(areaName);
		
		
		
		Key<Spot> kB = br.saveOrUpdate(spot);
		
		return kR;
	}
	
	private Key<Menu> createMenu (Key<Business> business, String title, String description) {
		
		Menu menu = new Menu();
		
		menu.setTitle(title);
		menu.setBusiness(business);
		menu.setDescription(description);
		
		return mr.saveOrUpdate(menu);
	}

	private Product createProduct(Key<Menu> menu, Key<Business> business, String name, Float price, String shortDesc, String longDesc)	{
		Product product = new Product();
		
		product.setMenu(menu);
		product.setBusiness(business);
		product.setName(name);
		product.setPrice(price);
		product.setShortDesc(shortDesc);
		product.setLongDesc(longDesc);
		
		return product;
	}
	
	private Key<Product> createAndSaveProduct(Key<Menu> menu,Key<Business> business , String name, Float price, String shortDesc, String longDesc)	{
	
		return pr.saveOrUpdate(createProduct(menu,business,name,price,shortDesc,longDesc));
	}
}
