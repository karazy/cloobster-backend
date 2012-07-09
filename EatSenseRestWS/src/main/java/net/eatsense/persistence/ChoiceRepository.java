package net.eatsense.persistence;

import net.eatsense.domain.Choice;

public class ChoiceRepository extends GenericRepository<Choice> {
	final static Class<Choice> entityClass = Choice.class;
	
	public ChoiceRepository() {
		super(Choice.class);
	}

}
