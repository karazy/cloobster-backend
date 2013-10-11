package net.eatsense.event;

import net.eatsense.domain.Business;

/**
 * Created with IntelliJ IDEA.
 * User: Nils
 * Date: 11.10.13
 * Time: 17:18
 * To change this template use File | Settings | File Templates.
 */
public class LocationEvent {
  protected final Business location;

  public LocationEvent(Business location) {
    super();
    this.location = location;
  }

  public Business getLocation() {
    return location;
  }
}
