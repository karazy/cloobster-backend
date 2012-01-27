package net.eatsense.restws;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import net.eatsense.domain.NicknameAdjective;
import net.eatsense.domain.NicknameNoun;
import net.eatsense.persistence.NicknameAdjectiveRepository;
import net.eatsense.persistence.NicknameNounRepository;

import com.google.inject.Inject;

@Path("/nickname")
public class NicknameResource {
	
	private NicknameAdjectiveRepository adjectiveRepo;
	private NicknameNounRepository nounRepo;

	@Inject
	public NicknameResource(NicknameAdjectiveRepository repo1, NicknameNounRepository repo2) {
		this.adjectiveRepo = repo1;
		this.nounRepo = repo2;
	}
	
	@PUT
	@Path("/adjective/list")
	@Consumes("application/json; charset=UTF-8")
	public void addNicknameAdjectives(List<NicknameAdjective> adjectives) {
		adjectiveRepo.ofy().put(adjectives);
	}
	
	@PUT
	@Path("/noun/list")
	@Consumes("application/json; charset=UTF-8")
	public void addNicknameNouns(List<NicknameNoun> nouns) {
		nounRepo.ofy().put(nouns);
	}

}
