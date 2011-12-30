package net.eatsense.util;

import net.eatsense.domain.Menu;
import net.eatsense.domain.Product;
import net.eatsense.domain.Spot;
import net.eatsense.domain.Restaurant;
import net.eatsense.persistence.MenuRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.persistence.RestaurantRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;

public class DummyDataDumper {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private RestaurantRepository rr;
	private SpotRepository br;

	private MenuRepository mr;

	private ProductRepository pr;

	@Inject
	public DummyDataDumper(RestaurantRepository rr, SpotRepository br, MenuRepository mr, ProductRepository pr) {
		this.rr = rr;
		this.br = br;
		this.mr = mr;
		this.pr = pr;
	}

	public void generateDummyRestaurants() {
		logger.info("Generate Dummy Restaurants.");
		createAndSaveDummyRestaurant("Mc Donald's", "Fast food burger", "Fressecke", "mc123");
		createAndSaveDummyRestaurant("Vappiano", "Pizza und Nudeln, schnell und lecker", "Hauptraum", "vp987");
		
		createSergioMenu( createAndSaveDummyRestaurant("Sergio", "Bester Spanier Darmstadts", "Keller", "serg2011") );

	}

	private void createSergioMenu(Key<Restaurant> kR) {

		//Getränke
		Key<Menu> kM = createMenu(kR, "Getränke", "Alkoholische, nicht-Alkoholische, heisse und kalte Getränke.");
		createProduct(kM, "kalte Milch", 1.5f, "lecker Milch", "Frische Bio-Milch von glücklichen Kühen direkt aus dem Schloss Bauernhof");
		createProduct(kM, "Weizen", 3.0f, "helles Hefeweizen vom Fass 0,5l",
				"Helles Hefeweizen vom Fass aus der Darmstädter Hofbrauerei im 0.5l Glas, 4.9% vol. Alkohol.");
		
		kM = createMenu(kR, "Hauptgerichte", "Schwein, Rind und vegetarische Speisen");
		createProduct(kM, "Classic Burger", 8.5f, "Burger mit Salat, Tomate, Zwiebel und Käse.",
				"Dies ist eine lange Beschreibung eines Burgers der Herstellung, seiner Zutaten und den Inhaltstoffen.");
		
				
	}

	private Key<Restaurant> createAndSaveDummyRestaurant(String name, String desc, String areaName, String barcode) {
		logger.info("Create dummy with data " + name + " " + desc + " " + areaName + " " + barcode);
		Restaurant r = new Restaurant();
		r.setName(name);
		r.setDescription(desc);
		Key<Restaurant> kR = rr.saveOrUpdate(r);

		Spot spot = new Spot();
		
		spot.setBarcode(barcode);
		spot.setRestaurant(kR);
		spot.setName(areaName);
		
		Key<Spot> kB = br.saveOrUpdate(spot);
		
		return kR;
	}
	
	private Key<Menu> createMenu (Key<Restaurant> restaurant, String title, String description) {
		
		Menu menu = new Menu();
		
		menu.setTitle(title);
		menu.setRestaurant(restaurant);
		menu.setDescription(description);
		
		return mr.saveOrUpdate(menu);
	}

	private Key<Product> createProduct(Key<Menu> menu, String name, Float price, String shortDesc, String longDesc)	{
		Product product = new Product();
		
		product.setMenu(menu);
		product.setName(name);
		product.setPrice(price);
		product.setShortDesc(shortDesc);
		product.setLongDesc(longDesc);
		
		return pr.saveOrUpdate(product);
	}
}
