package net.eatsense.domain.embedded;

import javax.validation.constraints.NotNull;

import org.apache.bval.constraints.NotEmpty;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Objects;
import com.googlecode.objectify.annotation.Unindexed;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductOption {
	@Unindexed
	private String id;
	
	@NotNull
	@NotEmpty
	private String name;
	
	private long price;
	
	private Boolean selected;
	
	public Boolean getSelected() {
		return selected;
	}

	public void setSelected(Boolean selected) {
		this.selected = selected;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	@JsonIgnore
	public long getPriceMinor() {
		return price;
	}
	
	@JsonProperty("price")
	public double getPrice() {
		return price/100d;
	}
	
	@JsonProperty("price")
	public void setPrice(double price) {
		this.price = Math.round(price * 100);
	}
	
	public ProductOption(String name, double price) {
		super();
		this.name = name;
		this.setPrice(price);
	}

	public ProductOption() {
		super();
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(name, price, selected);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProductOption other = (ProductOption) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (Double.doubleToLongBits(price) != Double
				.doubleToLongBits(other.price))
			return false;
		if (selected == null) {
			if (other.selected != null)
				return false;
		} else if (!selected.equals(other.selected))
			return false;
		return true;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
