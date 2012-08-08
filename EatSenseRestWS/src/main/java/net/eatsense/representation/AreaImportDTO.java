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
}
