package net.eatsense.util;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;

import net.eatsense.domain.Area;
import net.eatsense.domain.Barcode;
import net.eatsense.domain.Restaurant;
import net.eatsense.persistence.AreaRepository;
import net.eatsense.persistence.BarcodeRepository;
import net.eatsense.persistence.RestaurantRepository;

public class DummyDataDumper {

	private RestaurantRepository rr;
	private AreaRepository ar;
	private BarcodeRepository br;

	@Inject
	public DummyDataDumper(RestaurantRepository rr, AreaRepository ar,
			BarcodeRepository br) {
		this.rr = rr;
		this.ar = ar;
		this.br = br;
	}
	
	public void generateDummyRestaurants() {
		System.out.println("Generate Dummy Restaurants.");
		createAndSaveDummyRestaurant("Mc Donald's", "Fast food burger", "Fressecke", "mc123");
		createAndSaveDummyRestaurant("Vappiano", "Pizza und Nudeln, schnell und lecker", "Hauptraum", "vp987");
		createAndSaveDummyRestaurant("Sergio", "Bester Spanier Darmstadts", "Keller", "serg2011");
		
	}
	
	
	private void createAndSaveDummyRestaurant(String name, String desc, String areaName, String barcode) {
		Restaurant r = new Restaurant();
		r.setName(name);
		r.setDescription(desc);
		Key<Restaurant> kR = rr.saveOrUpdate(r);
		
		Area a = new Area();
		a.setName(areaName);
		a.setRestaurant(kR);
		Key<Area> kA = ar.saveOrUpdate(a);
		
		Barcode b = new Barcode();
		b.setBarcode(barcode);
		b.setArea(kA);
		Key<Barcode> kB = br.saveOrUpdate(b); 
	}

}
