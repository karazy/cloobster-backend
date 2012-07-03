package net.eatsense.domain.embedded;

import javax.validation.constraints.NotNull;

import org.apache.bval.constraints.NotEmpty;

import com.google.common.base.Objects;

public class PaymentMethod {
	public PaymentMethod() {
		super();
	}

	public PaymentMethod(String name) {
		super();
		this.name = name;
	}
	@NotNull
	@NotEmpty
	private String name;
	
	private Integer order;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PaymentMethod other = (PaymentMethod) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("name", name)
			.toString();
	}
}
