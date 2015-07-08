import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.TreeMap;

public class TwitterAnalysisBAL_q5{
	Connection conn = null;
	String string1[];
	String sql = "select count(*),max(frndCount),max(follCount) from ranksterAnalysis where userid=? and timestampval>=? and timestampval<=?";
	PreparedStatement preparedStatement;

	TwitterAnalysisBAL_q5(){
		try
		{
			String connDriver= "com.mysql.jdbc.Driver";
			Class.forName(connDriver);
			conn= DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/twitanalysis","root","");
			preparedStatement = conn.prepareStatement(sql);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public String extractUserList(String requestAnalysis) {
		String userlist= null;
		String string2[]= requestAnalysis.split("&");
		String string3[]=string2[0].split("=");
		userlist= string3[1];
		return userlist;
	}

	public String extractStartTimeStamp (String requestAnalysis)
	{
		String StartTimeStamp= null;
		String string2[]= requestAnalysis.split("&");
		String string3[]=string2[1].split("=");
		StartTimeStamp= string3[1];
		StartTimeStamp = StartTimeStamp.replaceAll("%20", " ");
		return StartTimeStamp;
	}

	public String extractEndTimeStamp (String requestAnalysis)
	{
		String EndTimeStamp= null;
		String string2[]= requestAnalysis.split("&");
		String string3[]=string2[2].split("=");
		EndTimeStamp= string3[1];
		EndTimeStamp = EndTimeStamp.replaceAll("%20", " ");
		return EndTimeStamp;
	}

	public String getanalysisResponse (String userList, String StartTimeStamp, String EndTimeStamp)
	{
		String tweetDataOutput = "";
		String[] users = userList.split(",");
		StartTimeStamp += " 00:00:00";
		EndTimeStamp += " 23:59:59";
		int score = 0;
		TreeMap<Integer,Integer> res = new TreeMap<Integer,Integer>();
		for(String user:users)
		{
			try
			{
				preparedStatement.setString(1, user);
				preparedStatement.setTimestamp(2, Timestamp.valueOf(StartTimeStamp));
				preparedStatement.setTimestamp(3, Timestamp.valueOf(EndTimeStamp));
				ResultSet results= preparedStatement.executeQuery();

				while (results.next()) {
					score = results.getInt(1) + (3*results.getInt(2)) + (5*results.getInt(3)); 
				}			
				res.put(score, Integer.parseInt(user));
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
		 ArrayList<Integer> keys = new ArrayList<Integer>(res.keySet());
	        for(int i=keys.size()-1; i>=0;i--){
	        	tweetDataOutput += res.get(keys.get(i))+","+keys.get(i)+"\n";
	        }
		return tweetDataOutput;
	}	
}