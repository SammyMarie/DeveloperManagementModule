package uk.co.sammy.classes;

import uk.co.sammy.dao.FinancialTransactionDAO;
import uk.co.sammy.dao.MembershipDAO;
import uk.co.sammy.dto.MembershipStatusDTO;
import uk.co.sammy.dto.PaymentAdviceDTO;
import uk.co.sammy.dto.TransactionDTO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;



public class ReconciliationJob {
	
	private final FinancialTransactionDAO financialTxDAO;
	private final MembershipDAO membersDAO;
	private final PayPalFacade paypal;
	
	public ReconciliationJob(FinancialTransactionDAO financialTxDAO, MembershipDAO membersDAO, PayPalFacade paypal){
		
		this.financialTxDAO = financialTxDAO;
		this.membersDAO = membersDAO;
		this.paypal = paypal;
	}

	public int reconcile() {
		
		List<TransactionDTO> unSettledTxs = financialTxDAO.retrieveUnsettledTransactions();
		
		Map<String, List<TransactionDTO>> developerTxMap = new LinkedHashMap<String, List<TransactionDTO>>();
		
		//Setting a developer wise Transaction map.
		for(TransactionDTO transactionDTO : unSettledTxs){
			
			List<TransactionDTO> transactions = developerTxMap.get(transactionDTO.getDeveloperID());
			
			if(transactions == null){
				
				transactions = new ArrayList<TransactionDTO>();
			}
			
			transactions.add(transactionDTO);
			developerTxMap.put(transactionDTO.getDeveloperID(), transactions);
		}
		
		//Looping through the developer ID, only once PayPal is called
		for(String developerID : developerTxMap.keySet()){
			
			MembershipStatusDTO membership = membersDAO.getStatusFor(developerID);
			
			String payPalID = null;
			double totalTxAmount = 0.00;
			
			for(TransactionDTO tx : developerTxMap.get(developerID)){
				
				totalTxAmount += tx.getAmount();
				payPalID = tx.getTargetPayPalID();
			}
			
			double payableAmount = totalTxAmount - totalTxAmount * membership.getDeductable();
			
			paypal.sendAdvice(new PaymentAdviceDTO(payableAmount, payPalID, null));
		}
		return unSettledTxs.size();
	}
}
