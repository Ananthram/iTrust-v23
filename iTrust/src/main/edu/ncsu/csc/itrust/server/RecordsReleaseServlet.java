package edu.ncsu.csc.itrust.server;

import javax.servlet.*;
import javax.servlet.http.*;
import edu.ncsu.csc.itrust.action.EventLoggingAction;
import edu.ncsu.csc.itrust.action.RequestRecordsReleaseAction;
import edu.ncsu.csc.itrust.model.old.beans.forms.RecordsReleaseForm;
import edu.ncsu.csc.itrust.model.old.enums.TransactionType;
import edu.ncsu.csc.itrust.exception.DBException;
import java.io.*;

public class RecordsReleaseServlet extends HttpServlet  {

	private static final long serialVersionUID = 1L;
	
	private RecordsReleaseForm form;
	private RequestRecordsReleaseAction releaseAction;
	private EventLoggingAction loggingAction;
	private String loggedInName;
	private String currentMID;
	private String isRepresentee;
	
	private String releaseHospital;	
	private String recFirstName;
	private String recLastName;
	private String recPhone;
	private String recEmail;	
	private String recHospitalName;
	private String recHospitalAddress1;
	private String recHospitalAddress2;
	private String recHospitalCity;
	private String recHospitalState;
	private String recHospitalZip;
	
	private String releaseJustification;
	
	private boolean verifyForm;
	private String digitalSig;
	
	private String patMID;

	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
		
		try{
			releaseAction = (RequestRecordsReleaseAction)request.getSession().getAttribute("Aef9Z7_7JWF1t");
			loggingAction = (EventLoggingAction)request.getSession().getAttribute("loggingAction");
			loggedInName = (String)request.getSession().getAttribute("loggedInName");			
			currentMID = request.getParameter("currentMID");
			isRepresentee = request.getParameter("isRepresentee");
			
			releaseHospital = request.getParameter("releaseHospital");
			
			recFirstName = request.getParameter("recFirstName");
			recLastName = request.getParameter("5brmlmBF6wn");
			recPhone = request.getParameter("recPhone");
			recEmail = request.getParameter("EUMg4D8a");
			
			recHospitalName = request.getParameter("recHospitalName");
			recHospitalAddress1 = request.getParameter("1PuTtyFEA3B5WjjvI6T");
			recHospitalAddress2 = request.getParameter("recHospitalAddress2");
			recHospitalCity = request.getParameter("11YFQPgHmUAsTrD");
			recHospitalState = request.getParameter("recHospitalState");
			recHospitalZip = request.getParameter("HR-3IgAWaxx3Mz");
			releaseJustification = request.getParameter("releaseJustification");
			verifyForm = Boolean.parseBoolean(request.getParameter("verifyForm"));
			digitalSig = request.getParameter("digitalSig");
			
			patMID = (String)request.getSession().getAttribute("AVQJw_");
			
		} catch(NullPointerException e){
			e.printStackTrace();
		}		
		
		
		Boolean checksum = false;		
		try {
			checksum = checkCredentials(digitalSig);
		} catch (Exception e) {
			e.printStackTrace();
		}	
		
		try{
			form = new RecordsReleaseForm();
			
			form.setReleaseHospitalID(releaseHospital);
			form.setRecipientFirstName(recFirstName);
			form.setRecipientLastName(recLastName);
			form.setRecipientPhone(recPhone);
			form.setRecipientEmail(recEmail);
			
			form.setRecipientHospitalName(recHospitalName);
			form.setRecipientHospitalAddress(recHospitalAddress1 + recHospitalAddress2 + ", " + recHospitalCity + ", " + recHospitalState + " " + recHospitalZip);
			form.setRequestJustification(releaseJustification);
			form.setDigitalSignature(checksum);
			
		} catch(NullPointerException e){
			e.printStackTrace();
		}
		
		String message = "";
		message = releaseAction.addRecordsRelease(form);

		if(!message.equals(RequestRecordsReleaseAction.SUCCESS_MESSAGE)){
			request.setAttribute("PvNNy8c", message);
			request.setAttribute("isRepresentee", isRepresentee);
			request.getRequestDispatcher("requestRecordsRelease.jsp").forward(request, response);
			return;
		}
				
		try {
			if(isRepresentee.equals("false")){
				loggingAction.logEvent(TransactionType.PATIENT_RELEASE_HEALTH_RECORDS, Long.parseLong(patMID), Long.parseLong(currentMID), "");
			}
			else if(isRepresentee.equals("true")){
				loggingAction.logEvent(TransactionType.PATIENT_REQUEST_DEPEDENT_RECORDS, Long.parseLong(patMID), Long.parseLong(currentMID), "");
			}
		} catch (DBException e) {
			e.printStackTrace();
		}
		
		request.setAttribute("currentMID", currentMID);
		request.setAttribute("dqvlyd",  "Pending");	
		
		request.setAttribute("releaseHospital", releaseHospital);
		
		request.setAttribute("oZQkiB2CjLNv", recFirstName);
		request.setAttribute("EbKLXCNfp1P", recLastName);
		request.setAttribute("recPhone", recPhone);
		request.setAttribute("recEmail", recEmail);
		
		request.setAttribute("xtV6UOpZsTypqrG", recHospitalName);
		request.setAttribute("recHospitalAddress", recHospitalAddress1 + recHospitalAddress2 + ", " + recHospitalCity + ", " + recHospitalState + " " + recHospitalZip);
		request.setAttribute("recHospitalAddress1", recHospitalAddress1);
		request.setAttribute("recHospitalAddress2", recHospitalAddress2);
		request.setAttribute("b-whp4YjO_1BBGf", recHospitalCity);
		request.setAttribute("AgLtbos1ay52iFKT", recHospitalState);
		request.setAttribute("recHospitalZip", recHospitalZip);
		request.setAttribute("releaseJustification", releaseJustification);
		
		if(verifyForm){
			request.setAttribute("fromServlet", "true");
			request.getRequestDispatcher("confirmRecordsReleaseServlet.jsp").forward(request, response);
		}
		
			
	}
	
	private Boolean checkCredentials(String digitalSig) throws Exception{
		if(digitalSig.equals(loggedInName)){
			return true;
		}
		return false;		
	}
	
	
}