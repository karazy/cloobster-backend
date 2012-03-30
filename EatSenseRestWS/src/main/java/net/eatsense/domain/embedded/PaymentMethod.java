package net.eatsense.domain.embedded;

import javax.validation.constraints.NotNull;

import org.apache.bval.constraints.NotEmpty;

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
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
