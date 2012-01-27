package net.eatsense.persistence;

import net.eatsense.domain.NicknameAdjective;

public class NicknameAdjectiveRepository extends GenericRepository<NicknameAdjective> {
	
	
	
	public NicknameAdjectiveRepository() {
		super();
		this.clazz = NicknameAdjective.class;
	}

}
