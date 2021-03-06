package net.eatsense.representation;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.bval.constraints.NotEmpty;

import net.eatsense.domain.Area;

public class AreaImportDTO extends AreaDTO {
	
	@NotNull
	@Valid
	@NotEmpty
	private List<SpotDTO> spots;
	private List<String> menus;
	
	private String masterBarcode;

	public AreaImportDTO() {
		super();
	}

	public AreaImportDTO(Area area) {
		super(area);
	}

	public List<SpotDTO> getSpots() {
		return spots;
	}

	public void setSpots(List<SpotDTO> spots) {
		this.spots = spots;
	}

	public List<String> getMenus() {
		return menus;
	}

	public void setMenus(List<String> menus) {
		this.menus = menus;
	}

	public String getMasterBarcode() {
		return masterBarcode;
	}

	public void setMasterBarcode(String masterBarcode) {
		this.masterBarcode = masterBarcode;
	}
}
