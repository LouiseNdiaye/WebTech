package isep.hem.bdd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import isep.hem.beans.ClientBean;
import isep.hem.beans.LogementBean;

public class LogementsDatabase {

	public static LogementBean getLogementById(int homeId){
		Statement statement = null;
        ResultSet result = null;
        LogementBean logement = new LogementBean();
        
		LoadDatabase.loadDatabase();
		String query = "SELECT * FROM housing WHERE housingID=" + homeId +";";

		try{
			statement = LoadDatabase.getConnexion().createStatement();
			result = statement.executeQuery(query);
			if(result.next()) {
				logement = getLogementProperties(result, statement);
			}
		} catch (SQLException e){
		} finally {
			LoadDatabase.closeConnexion(result, statement);
		}

		return logement;
	}

	public static void addLogement(LogementBean logement, int userId){
		LoadDatabase.loadDatabase();
		String query = "INSERT INTO housing(userId,address,city,description,rate,isAvailable) VALUES(?,?,?,?,5,1)";
		try{
		Connection conn = LoadDatabase.getConnexion();
		PreparedStatement pStatement = conn.prepareStatement(query);
		pStatement.setInt(1,userId);
		pStatement.setString(2,logement.getAdresseLogement());
		pStatement.setString(3,logement.getVilleLogement());
		pStatement.setString(4,logement.getDescriptionLogement());
		pStatement.execute();
		pStatement.close();
		conn.close();
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	
	public List<LogementBean> getLogements(){
	
		//Init vars 
		List<LogementBean> logements = new ArrayList<LogementBean>();
		Statement statement = null;
		ResultSet result = null;
		
		LoadDatabase.loadDatabase();
		String query = "SELECT * FROM housing;";
		
		try {
			
			statement = LoadDatabase.getConnexion().createStatement();
	    	result = statement.executeQuery(query);
	    	while(result.next()) {
	    		logements.add(getLogementProperties(result, statement));
	    	}
		
		} catch (SQLException e) {
		}finally {
	        LoadDatabase.closeConnexion(result, statement);
	    }
		
		return logements;
		}



	public static LogementBean getLogementProperties(ResultSet result, Statement statement)
		throws SQLException{

		/** 
		 * Recupere les differents champs du resultat de la requete SQL
		 * 	Cree un LogementBean et attribue les valeurs a ses proprietes
		 ******************************************************************/

		LogementBean logement = new LogementBean();
			int logementId = result.getInt("housingId");
			int userId = result.getInt("userId");
	    	String address = result.getString("address");
	    	String city = result.getString("city");
	    	String description = result.getString("description");
	    	int rate = result.getInt("rate");
	    	int renterId = result.getInt("renterId");
	    	int isAvailable = result.getInt("isAvailable");
			
			logement.setidLogement(logementId);
			logement.setAdresseLogement(address);
	    	logement.setVilleLogement(city);
	    	logement.setDescriptionLogement(description);
			logement.setRateLogement(rate);
			logement.setIdRenter(renterId);
			logement.setIsAvailable(isAvailable);
			if (userId != 0) {
				ClientBean proprietaire = UsersDatabase.getUserFromId(userId);
				logement.setProprietaireLogement(proprietaire);
			}
		return logement;
	}
	
	public static String deleteLogementById(int IdLogement, int userId) {
		Statement statement = null;
		ResultSet result = null;
		String res = null;
		
		LogementBean logement = getLogementById(IdLogement);
		int proprietaireId = logement.getProprietaireLogement().getUserId();
		
		if(userId == 1 || userId == proprietaireId) {
			
			LoadDatabase.loadDatabase();
			String query = "DELETE FROM housing where housingID=" + IdLogement + ";";
			
			try{
				statement = LoadDatabase.getConnexion().createStatement();
				statement.executeUpdate(query);
			} catch (SQLException e){
			} finally {
				LoadDatabase.closeConnexion(result, statement);
			}
			
			res = "Logement supprim� !";
		}
		
		else {
			res = "Vous n'avez pas les droits pour supprimer ce logement !";
		}
		
		return res;
		
	}
	
	public static String bookLogement(int IdLogement, int userId) {
		Statement statement = null;
		
		System.out.println("Book logement");
		
		LogementBean logement = getLogementById(IdLogement);
		
		int available  = logement.getIsAvailable();
		System.out.println("Is logement available ?" + available);
		
		if (available == 0) {
			return "Ce logement est d�j� r�serv� !";
		}
		
		else {
			String res = null;
			String query = "UPDATE housing SET isAvailable='0', renterId='" + userId + "' where housingID='" + IdLogement + "';";
			try {
				statement = LoadDatabase.getConnexion().createStatement();
		    	statement.executeUpdate(query);
		    	res = "Vous avez r�serv� ce logement !";
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return res;
		}
	}
	
	public static String cancelBooking(int IdLogement, int userId) {
		Statement statement = null;
		
		LogementBean logement = getLogementById(IdLogement);
		int available  = logement.getIsAvailable();
		System.out.println("Is logement available ?" + available);
		if (available == 0 && logement.getIdRenter() == userId) {
			String res = null;
			String query = "UPDATE housing SET isAvailable='1', renterId='0' where housingID='" + IdLogement + "';";
			try {
				statement = LoadDatabase.getConnexion().createStatement();
		    	statement.executeUpdate(query);
		    	res = "Vous avez lib�r� ce logement !";
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return res;
		}
		
		else {
			return "Vous ne pouvez pas annuler cette r�servation";
		}
	}
	
	public static List<LogementBean> getMyHomes(int userId) {
		List<LogementBean> logements = new ArrayList<LogementBean>();
		Statement st = null;
		ResultSet res = null;
		
		LoadDatabase.loadDatabase();
		String query = "SELECT * FROM housing WHERE userID='" + userId + "';";
		try {
			st = LoadDatabase.getConnexion().createStatement();
			res = st.executeQuery(query);
			while(res.next()) {
				logements.add(getLogementProperties(res, st));
			}
		} catch (SQLException e) {
		} finally {
			LoadDatabase.closeConnexion(res, st);
		} return logements;
	}
	
	public static List<LogementBean> getRentedHomes(int renterId) {
		List<LogementBean> logements = new ArrayList<LogementBean>();
		Statement st = null;
		ResultSet res = null;
		
		LoadDatabase.loadDatabase();
		String query = "SELECT * FROM housing WHERE RenterId='" + renterId + "';";
		try {
			st = LoadDatabase.getConnexion().createStatement();
			res = st.executeQuery(query);
			while(res.next()) {
				logements.add(getLogementProperties(res, st));
			}
		} catch (SQLException e) {
		} finally {
			LoadDatabase.closeConnexion(res, st);
		} return logements;
	}
	
}
