package net.eatsense.event;

import net.eatsense.domain.Business;

/**
 * Created with IntelliJ IDEA.
 * User: Nils
 * Date: 11.10.13
 * Time: 17:27
 * To change this template use File | Settings | File Templates.
 */
public class UpdateGeoLocation extends LocationEvent {

  public UpdateGeoLocation(Business location) {
    super(location);
  }
}
