package uk.co.sammy.dto;

public class TransactionDTO {

	private String developerID;
	private String targetPayPalID;
	private double amount;

	public void setDeveloperID(String developerID) {
		
		this.developerID = developerID;
	}
	
	public void setTargetPayPalID(String targetPayPalID) {
		
		this.targetPayPalID = targetPayPalID;
	}
	
	public void setAmount(double amount) {
		
		this.amount = amount;
	}

	public String getDeveloperID() {
		
		return developerID;
	}

	public String getTargetPayPalID() {
		
		return targetPayPalID;
	}

	public double getAmount() {
		
		return amount;
	}
	
	public TransactionDTO createTxDTO(String developerID, String PayPalID, double amount) {		
		TransactionDTO transactDTO = new TransactionDTO();
		
		transactDTO.setDeveloperID(developerID);
		transactDTO.setTargetPayPalID(PayPalID);
		transactDTO.setAmount(amount);
		
		return transactDTO;
	}

}
