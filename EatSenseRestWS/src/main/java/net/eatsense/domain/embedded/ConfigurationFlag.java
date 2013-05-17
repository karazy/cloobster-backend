package net.eatsense.domain.embedded;

import com.google.common.base.Objects;

public class ConfigurationFlag {
	private String name;
	private boolean active;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public ConfigurationFlag() {
	}
	
	public ConfigurationFlag(String name, boolean active) {
		super();
		this.name = name;
		this.active = active;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getName(), isActive());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConfigurationFlag other = (ConfigurationFlag) obj;
		if (active != other.active)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this.getClass()).add("name", getName()).add("active", isActive()).toString();
	}
	
	
}
