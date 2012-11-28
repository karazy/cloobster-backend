package net.eatsense.persistence;

import net.eatsense.domain.InfoPage;
import net.eatsense.domain.translation.InfoPageT;

public class InfoPageRepository extends LocalisedRepository<InfoPage, InfoPageT> {

	public InfoPageRepository() {
		super(InfoPage.class, InfoPageT.class);
	}
}
