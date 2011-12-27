package net.eatsense.util;

import net.eatsense.domain.Spot;
import net.eatsense.domain.Restaurant;
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

	@Inject
	public DummyDataDumper(RestaurantRepository rr, SpotRepository br) {
		this.rr = rr;
		this.br = br;
	}

	public void generateDummyRestaurants() {
		System.out.println("Generate Dummy Restaurants.");
		createAndSaveDummyRestaurant("Mc Donald's", "Fast food burger", "Fressecke", "mc123");
		createAndSaveDummyRestaurant("Vappiano", "Pizza und Nudeln, schnell und lecker", "Hauptraum", "vp987");
		createAndSaveDummyRestaurant("Sergio", "Bester Spanier Darmstadts", "Keller", "serg2011");

	}

	private void createAndSaveDummyRestaurant(String name, String desc, String areaName, String barcode) {
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
	}

}
