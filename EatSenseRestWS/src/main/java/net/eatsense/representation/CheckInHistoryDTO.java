package net.eatsense.representation;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import net.eatsense.domain.Bill;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Spot;

/**
 * @author Nils Weiher
 *
 */
@XmlRootElement
public class CheckInHistoryDTO {
	private Long checkInId;
	private String nickname;
	private Date checkInTime;
	private Long billId;
	private String paymentMethod;
	private Date billTime;
	private double billTotal;
	private Long spotId;
	private Long areaId;
	private String spotName;
	
	public CheckInHistoryDTO() {
	}
	
	/**
	 * @param checkIn
	 * @param bill
	 * @param spot
	 */
	public CheckInHistoryDTO(CheckIn checkIn, Bill bill, Spot spot) {
		if(checkIn != null) {
			checkInId = checkIn.getId();
			nickname = checkIn.getNickname();
			checkInTime = checkIn.getCheckInTime();
		}
		if(bill != null) {
			billId = bill.getId();
			paymentMethod = bill.getPaymentMethod().getName();
			billTime = bill.getCreationTime();
			billTotal = bill.getTotal() / 100.0;
		}
		if(spot != null) {
			spotId = spot.getId();
			areaId = spot.getArea().getId();
			spotName = spot.getName();
		}
	}

	
	public Long getCheckInId() {
		return checkInId;
	}
	public void setCheckInId(Long checkInId) {
		this.checkInId = checkInId;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public Date getCheckInTime() {
		return checkInTime;
	}
	public void setCheckInTime(Date checkInTime) {
		this.checkInTime = checkInTime;
	}
	public Long getBillId() {
		return billId;
	}
	public void setBillId(Long billId) {
		this.billId = billId;
	}
	public String getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
	public Date getBillTime() {
		return billTime;
	}
	public void setBillTime(Date billTime) {
		this.billTime = billTime;
	}
	public double getBillTotal() {
		return billTotal;
	}
	public void setBillTotal(double billTotal) {
		this.billTotal = billTotal;
	}
	public Long getSpotId() {
		return spotId;
	}
	public void setSpotId(Long spotId) {
		this.spotId = spotId;
	}
	public Long getAreaId() {
		return areaId;
	}
	public void setAreaId(Long areaId) {
		this.areaId = areaId;
	}
	public String getSpotName() {
		return spotName;
	}
	public void setSpotName(String spotName) {
		this.spotName = spotName;
	}
}
