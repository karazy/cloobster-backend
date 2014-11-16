package net.eatsense.search;

import com.google.appengine.api.search.*;
import com.google.appengine.api.search.SortExpression.SortDirection;
import com.google.common.eventbus.Subscribe;

import net.eatsense.domain.Business;
import net.eatsense.event.UpdateGeoLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: Nils
 * Date: 11.10.13
 * Time: 14:22
 * To change this template use File | Settings | File Templates.
 */
public class LocationSearchService {
  protected Logger logger = LoggerFactory.getLogger(this.getClass());
  private final Index index;

  public LocationSearchService() {
    IndexSpec indexSpec = IndexSpec.newBuilder().setName("location").build();
    this.index = SearchServiceFactory.getSearchService().getIndex(indexSpec);
  }

  private Document buildDocument(Business location) {
    GeoPoint geoPoint = new GeoPoint(location.getGeoLocation().getLatitude(), location.getGeoLocation().getLongitude());

    // Use the websafe key string as Document id
    return Document.newBuilder()
            .setId(location.getKey().getString())
            .addField(Field.newBuilder().setName("name").setText(location.getName()))
            .addField(Field.newBuilder().setName("geolocation").setGeoPoint(geoPoint))
            .build();
  }

  public void updateIndex(Business location) {
    checkNotNull(location, "location must not be null!");
    checkArgument(location.getGeoLocation() != null, "location must have geoLocation set");

    Document doc = buildDocument(location);
    logger.info("search doc={}", doc);
    // put asynchronous for now. dont know if there will be problems
    index.putAsync(doc);
  }

  public Results<ScoredDocument> query(double latitude, double longitude, int distance) {
    String queryString = String.format(Locale.US, "distance(geolocation, geopoint(%f, %f)) < %d", latitude, longitude, distance);
    String distanceExpression = String.format(Locale.US, "distance(geolocation, geopoint(%f, %f))", latitude, longitude);

    logger.info("queryString={}", queryString);
    
    //2014-11-16 apparently it is not possible to search on a field expression, hence we calculate the distance multiple times
    SortOptions.Builder sortOptions = SortOptions.newBuilder().
    	addSortExpression(SortExpression.newBuilder().setExpression(distanceExpression).setDirection(SortDirection.ASCENDING).setDefaultValueNumeric(0)).setLimit(1000);
    
    QueryOptions.Builder options = QueryOptions.newBuilder()
            .addExpressionToReturn(FieldExpression.newBuilder().setName("distanceComputed").setExpression(distanceExpression))
            .setSortOptions(sortOptions);
    
    Query query = Query.newBuilder().setOptions(
            options).build(queryString);
    
    return index.search(query);
  }

  @Subscribe
  public void handleUpdateGeoLocation(UpdateGeoLocation event) {
    updateIndex(event.getLocation());
  }
}
