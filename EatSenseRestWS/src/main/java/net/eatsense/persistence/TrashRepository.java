package net.eatsense.persistence;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.eatsense.domain.TrashEntry;

import com.googlecode.objectify.Key;

public class TrashRepository extends GenericRepository<TrashEntry> {
	
	public TrashRepository() {
		super(TrashEntry.class);
	}
	
	public void deleteTrash(Iterable<TrashEntry> trashEntries) {
		List<Key<?>> keysToDelete = new ArrayList<Key<?>>();
		for (TrashEntry trashEntry : trashEntries) {
			keysToDelete.add(trashEntry.getEntityKey());
			
			trashEntry.setDeletionDate(new Date());
		}
		ofy().put(trashEntries);
		
		ofy().delete(keysToDelete);
	}
	
	public TrashEntry saveNewTrashEntry(Key<?> entityKey, String loginResponsible) {
		TrashEntry trashEntry = new TrashEntry(entityKey, new Date(), loginResponsible);
		ofy().put(trashEntry);
		return trashEntry;
	}
}
