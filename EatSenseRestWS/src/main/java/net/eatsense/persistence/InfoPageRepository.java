package net.eatsense.persistence;

import net.eatsense.domain.InfoPage;

public class InfoPageRepository extends LocalisedRepository<InfoPage> {

	public InfoPageRepository() {
		super(InfoPage.class);
	}
}
