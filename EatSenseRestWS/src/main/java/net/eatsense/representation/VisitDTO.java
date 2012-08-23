package net.eatsense.representation;

import java.util.Date;

import net.eatsense.domain.Bill;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;

public class VisitDTO {
	private String nickname;
	private String checkInId;
	private Date checkInTime;
	private Long businessId;
	private String businessName;
	private Long billId;
	private Date billTime;
	private Float billTotal;
	private String paymentMethod;
	
	/**
	 * 
	 */
	public VisitDTO() {
	}
	
	/**
	 * Create the transfer object from the given source entities.
	 * 
	 * @param checkIn
	 * @param business
	 * @param bill
	 */
	public VisitDTO(CheckIn checkIn, Business business, Bill bill) {
		if(checkIn != null) {
			nickname = checkIn.getNickname();
			checkInId = checkIn.getUserId();
			checkInTime = checkIn.getCheckInTime();
		}
		if(business != null) {
			businessId = business.getId();
			businessName = business.getName();
		}
		if(bill != null) {
			billId = bill.getId();
			billTime = bill.getCreationTime();
			paymentMethod = bill.getPaymentMethod().getName();
		}
	}
	
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getCheckInId() {
		return checkInId;
	}
	public void setCheckInId(String checkInId) {
		this.checkInId = checkInId;
	}
	public Date getCheckInTime() {
		return checkInTime;
	}
	public void setCheckInTime(Date checkInTime) {
		this.checkInTime = checkInTime;
	}
	public Long getBusinessId() {
		return businessId;
	}
	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}
	public String getBusinessName() {
		return businessName;
	}
	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}
	public Long getBillId() {
		return billId;
	}
	public void setBillId(Long billId) {
		this.billId = billId;
	}
	public Date getBillTime() {
		return billTime;
	}
	public void setBillTime(Date billTime) {
		this.billTime = billTime;
	}
	public Float getBillTotal() {
		return billTotal;
	}
	public void setBillTotal(Float billTotal) {
		this.billTotal = billTotal;
	}
	public String getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
	
}
