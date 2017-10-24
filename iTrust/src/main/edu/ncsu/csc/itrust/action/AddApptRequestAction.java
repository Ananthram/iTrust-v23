package edu.ncsu.csc.itrust.action;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import edu.ncsu.csc.itrust.exception.DBException;
import edu.ncsu.csc.itrust.logger.TransactionLogger;
import edu.ncsu.csc.itrust.model.old.beans.ApptBean;
import edu.ncsu.csc.itrust.model.old.beans.ApptRequestBean;
import edu.ncsu.csc.itrust.model.old.beans.ApptTypeBean;
import edu.ncsu.csc.itrust.model.old.dao.DAOFactory;
import edu.ncsu.csc.itrust.model.old.dao.mysql.ApptDAO;
import edu.ncsu.csc.itrust.model.old.dao.mysql.ApptRequestDAO;
import edu.ncsu.csc.itrust.model.old.dao.mysql.ApptTypeDAO;
import edu.ncsu.csc.itrust.model.old.enums.TransactionType;

/**
 * 
 *
 */
public class AddApptRequestAction {

	private ApptDAO aDAO;
	private ApptRequestDAO arDAO;
	private ApptTypeDAO atDAO;
	
	public AddApptRequestAction(DAOFactory factory) {
		aDAO = factory.getApptDAO();
		arDAO = factory.getApptRequestDAO();
		atDAO = factory.getApptTypeDAO();
	}

	public String addApptRequest(ApptRequestBean bean) throws SQLException, DBException {
		return addApptRequest(bean, 0L, 0L);
	}
	
	public String addApptRequest(ApptRequestBean bean, long loggedInMID, long hcpid) throws SQLException, DBException {

		List<ApptBean> conflicts = aDAO.getAllHCPConflictsForAppt(bean.getRequestedAppt().getHcp(),
				bean.getRequestedAppt());

		if (conflicts != null && !conflicts.isEmpty()) {
			return "I95da9c71Lcys8vvQyLoTBGlkDdMCYjJbhJ_pGkB4DkYQJ_1NaFmRQYLyNiGV9EL5vsqsLXxa";
		}

		arDAO.addApptRequest(bean);
		
		TransactionLogger.getInstance().logTransaction(TransactionType.APPOINTMENT_REQUEST_SUBMITTED, loggedInMID, hcpid, "");

		return "RtT9A3LtXgHClbMnac9dmXaRnxbn_W1iYSdD9JD_DDLA68BBxz7TD4P";
	}

	public List<ApptBean> getNextAvailableAppts(int num, ApptBean bean) throws SQLException, DBException {
		List<ApptBean> appts = new ArrayList<ApptBean>(num);
		for (int i = 0; i < num; i++) {
			ApptBean b = new ApptBean();
			b.setApptType(bean.getApptType());
			b.setHcp(bean.getHcp());
			b.setPatient(bean.getPatient());
			b.setDate(new Timestamp(bean.getDate().getTime()));
			
			List<ApptBean> conflicts = null;
			do {
				conflicts = aDAO.getAllHCPConflictsForAppt(b.getHcp(), b);
				if (conflicts != null && !conflicts.isEmpty()) {
					ApptBean lastConflict = conflicts.get(conflicts.size() - 1);
					Timestamp afterConflict = endTime(lastConflict);
					b.setDate(afterConflict);
				}
			} while (!conflicts.isEmpty());
			appts.add(b);
			Timestamp nextTime = endTime(b);
			bean.setDate(nextTime);
		}
		return appts;
	}

	private Timestamp endTime(ApptBean bean) throws SQLException, DBException {
		Timestamp d = new Timestamp(bean.getDate().getTime());
		ApptTypeBean type = atDAO.getApptType(bean.getApptType());
		d.setTime(d.getTime() + type.getDuration() * 60L * 1000);
		return d;
	}

}
