package net.eatsense.persistence;

import net.eatsense.domain.Choice;

public class ChoiceRepository extends GenericRepository<Choice> {

	public ChoiceRepository() {
		super();
		super.clazz = Choice.class;
	}

}
