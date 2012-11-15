package net.eatsense.persistence;

import net.eatsense.domain.NicknameAdjective;

public class NicknameAdjectiveRepository extends GenericRepository<NicknameAdjective> {
	final static Class<NicknameAdjective> entityClass = NicknameAdjective.class;
	
	public NicknameAdjectiveRepository() {
		super(NicknameAdjective.class);
	}

}
