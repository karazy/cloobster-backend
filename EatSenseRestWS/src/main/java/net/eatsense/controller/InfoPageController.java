package net.eatsense.controller;

import net.eatsense.persistence.InfoPageRepository;

/**
 * Handles creation and loading of info pages ("Gäste ABC")
 * 
 * @author Nils Weiher
 *
 */
public class InfoPageController {
	private final InfoPageRepository infoPageRepo;

	public InfoPageController(InfoPageRepository infoPageRepo) {
		super();
		this.infoPageRepo = infoPageRepo;
	}
	
	
}
