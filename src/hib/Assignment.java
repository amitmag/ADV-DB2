package hib;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import javax.transaction.Transaction;

import org.hibernate.Session;

public class Assignment {
	
	public static boolean isExistUsername (String username) {
		List<Users> users = getUsers();
		for(Users user: users) {
			if(user.getUsername().equals(username))
				return true;
		}
		return false;	
	}
	
	@SuppressWarnings("finally")
	public static String insertUser(String username, String password, String first_name, String
			last_name, String day_of_birth, String month_of_birth, String year_of_birth) {
		if(isExistUsername(username))
			return null;
		Session session = HibernateUtil.currentSession();
		int userId = 0;
		Transaction tx = null;
		try {
			tx = (Transaction) session.beginTransaction();
			
			// Create date of birth for user
			int year = Integer.parseInt(year_of_birth);
			int month = Integer.parseInt(month_of_birth);
			int day = Integer.parseInt(day_of_birth);
			Calendar c = Calendar.getInstance();
			c.set(year, month - 1, day, 0, 0);  
			Timestamp ts = new Timestamp(new Date().getTime());
			// Create user instance
			Users user = new Users(username, password, first_name, last_name, c.getTime(), ts, null, null);
			userId = (Integer) session.save(user);
			tx.commit();
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			return userId + "";
		}
		
	}
	
	public static List<Mediaitems> getTopNItems (int top_n){
		Session session = HibernateUtil.currentSession();
		String query = "SELECT FROM MetaItems WHERE ROWNUM<=" + top_n + " DESC";
		@SuppressWarnings("unchecked")
		List<Mediaitems> mediaItems = (List<Mediaitems>)session.createQuery(query).list();
		return mediaItems;
	}
	
	public static String validateUser (String username, String password) {
		List<Users> users = getUsers();
		for(Users user: users) {
			if(user.getUsername().equals(username) && user.getPassword().equals(password))
				return user.getUserid() + "";
		}
		return "NOT FOUND";
	}
	
	public static String validateAdministrator (String username, String password) {
		Session session = HibernateUtil.currentSession();
		String query = "from Administrators";
		@SuppressWarnings("unchecked")
		List<Administrators> administrators = (List<Administrators>)session.createQuery(query).list();
		for(Administrators admin: administrators) {
			if(admin.getUsername().equals(username) && admin.getPassword().equals(password))
				return admin.getAdminid() + "";
		}
		return "NOT FOUND";
	}
	
	public static void insertToHistory (String userid, String mid) {
		Session session = HibernateUtil.currentSession();
		Transaction tx = null;
		Timestamp timeStamp = new Timestamp(new Date().getTime());
		try {
			tx = (Transaction) session.beginTransaction();
			Users user = getUser(userid);
			Mediaitems mediaItem = getMediaItem(mid);
			HistoryId historyId = new HistoryId(Integer.parseInt(userid), Integer.parseInt(mid), timeStamp);
			History history = new History(historyId, user, mediaItem);		
			session.save(history);
			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("The insertion to history table was successful" + timeStamp.toString());
	}
	
	@SuppressWarnings("rawtypes")
	public static List<Entry<String, Date>> getHistory (String userid){
		List<Entry<String, Date>> userHistory = new ArrayList<>();
		String query = "FROM History * where USERID=" + userid + "ORDER BY VIEWTIME ASC";
		Session session = HibernateUtil.currentSession();
		@SuppressWarnings("unchecked")
		List<History> historyItems = (List<History>)session.createQuery(query).list();
		for(History history: historyItems) {
			Mediaitems mediaItem = getMediaItem(history.getMediaitems().getMid() + "");
			Timestamp ts = (Timestamp) history.getId().getViewtime();
			userHistory.add(new AbstractMap.SimpleEntry(mediaItem.getTitle(), ts));
		}
		return userHistory;
	}
	
	public static void insertToLog (String userid) {
		Session session = HibernateUtil.currentSession();
		Transaction tx = null;
		Timestamp timeStamp = new Timestamp(new Date().getTime());
		try {
			tx = (Transaction) session.beginTransaction();
			Users user = getUser(userid);
			LoginlogId loginId = new LoginlogId(Integer.parseInt(userid), timeStamp);
			Loginlog loginLog = new Loginlog(loginId, user);
			session.save(loginLog);
			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("“The insertion to log table was successful" + timeStamp.toString());
	}
	
	public static int getNumberOfRegistredUsers(int n) {
		LocalDate nBeforeToday = LocalDate.now().minusDays(n);
		return 0;
	}
	
	// TODO: Check how to sort the results!
	public static List<Users> getUsers(){
		Session session = HibernateUtil.currentSession();
		String query = "from Users";
		@SuppressWarnings("unchecked")
		List<Users> users = (List<Users>)session.createQuery(query).list();
		return users;
	}
	
	public static Users getUser (String userid) {
		Session session = HibernateUtil.currentSession();
		Users user = (Users)session.get(Users.class, userid);
		return user;
	}
	
	private static Mediaitems getMediaItem (String mediaItemId) {
		Session session = HibernateUtil.currentSession();
		Mediaitems mediaItem = (Mediaitems)session.get(Mediaitems.class, mediaItemId);
		return mediaItem;
	}

	

	

	
	
	
	


}
