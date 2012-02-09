package net.eatsense.restws;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import net.eatsense.domain.NicknameAdjective;
import net.eatsense.domain.NicknameNoun;
import net.eatsense.persistence.NicknameAdjectiveRepository;
import net.eatsense.persistence.NicknameNounRepository;
import net.eatsense.util.NicknameGenerator;

import com.google.inject.Inject;

@Path("/nicknames")
public class NicknameResource {
	
	private NicknameAdjectiveRepository adjectiveRepo;
	private NicknameNounRepository nounRepo;
	private NicknameGenerator nicknameGenerator;

	@Inject
	public NicknameResource(NicknameAdjectiveRepository repo1, NicknameNounRepository repo2, NicknameGenerator nnGen) {
		this.adjectiveRepo = repo1;
		this.nounRepo = repo2;
		this.nicknameGenerator = nnGen;
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
	
	@GET	
	@Produces	
	public String generateNickname(@QueryParam("random") String random) {
		String nickname = "";
		if(random != null) {
			nickname = nicknameGenerator.generateNickname(); 
		}
		return nickname;
	}

}
