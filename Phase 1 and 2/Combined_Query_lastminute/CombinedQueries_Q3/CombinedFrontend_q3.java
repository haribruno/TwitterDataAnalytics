import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.sql.Timestamp;

import org.apache.commons.lang3.StringEscapeUtils;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

// The comments are similar to that in the HBase code.
public class CombinedFrontend {
	private static String tweettime= "";
	private static String userID="";
	private static String analysisresponse= "";
	private static String requestAnalysis= "";

	//for q4
	private static String Starttweettime = "";
	private static String Endtweettime = "";
	private static String hashtag = "";

	static TwitterAnalysisBAL_q2 twitteranalysisBAL_q2 = new TwitterAnalysisBAL_q2();
	static TwitterAnalysisBAL_q3 twitteranalysisBAL_q3 = new TwitterAnalysisBAL_q3();
	static TwitterAnalysisBAL_q4 twitteranalysisBAL_q4 = new TwitterAnalysisBAL_q4();
	static Cipher decodeReq = new Cipher();

	static Map<String, String> tweetCache = new LinkedHashMap<String, String>(100000, 1f) {
		protected boolean removeEldestEntry(Map.Entry eldest) {
			return size() > 99999;
		}
	};

	public static void main(String[] args) {
		Undertow server = Undertow.builder()
//				.setWorkerThreads(4096)
//				.setIoThreads(Runtime.getRuntime().availableProcessors()*2)
				.addHttpListener(80, "ec2-52-5-87-150.compute-1.amazonaws.com")
				.setHandler(new HttpHandler() {
					@Override
					public void handleRequest(final HttpServerExchange exchange) throws Exception {
						requestAnalysis = exchange.getQueryString();
						if(requestAnalysis.length() != 0)
						{
							String stepCompare = exchange.getRelativePath();
							if(stepCompare.contains("/q1"))
							{
								String res = decodeReq.parseHttpReq(requestAnalysis);
								Date date= Calendar.getInstance().getTime();
								Timestamp  ts = new Timestamp(date.getTime());
								String time = ts.toString();
								SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								Date currentDate;
								currentDate = date_format.parse(time);
								String time_val = date_format.format(currentDate);
								analysisresponse = time_val + "\n"+ res + "\n";
							}
							//query2 check
							else if(stepCompare.contains("/q2"))
							{
								tweettime = twitteranalysisBAL_q2.extractTime(requestAnalysis);
								userID = twitteranalysisBAL_q2.extractUserID(requestAnalysis);
								if(twitteranalysisBAL_q2.checkTwitterDate(tweettime)) {
									analysisresponse = twitteranalysisBAL_q2.getCachedData(userID, tweettime);
									if(analysisresponse.equals("")) {
										analysisresponse = twitteranalysisBAL_q2.getanalysisResponse(userID, tweettime);
									} else {
										twitteranalysisBAL_q2.addToCache(userID+"#"+tweettime, analysisresponse);
									}
									analysisresponse = StringEscapeUtils.unescapeJava(analysisresponse);
								} else {
									analysisresponse = "";
								}
							}
							//query3 check
							else if (stepCompare.contains("/q3"))
							{
								userID = twitteranalysisBAL_q3.extracthashtag(requestAnalysis);
								
								analysisresponse = twitteranalysisBAL_q3.getanalysisResponse(userID);
									
								analysisresponse = StringEscapeUtils.unescapeJava(analysisresponse);
								
							}
							//query4 check
							else if (stepCompare.contains("/q4"))
							{
								//retrieving starttime, endtime and hashtag
								Starttweettime = twitteranalysisBAL_q4.extractStartTimeStamp(requestAnalysis);
								Endtweettime = twitteranalysisBAL_q4.extractEndTimeStamp(requestAnalysis);
								hashtag = twitteranalysisBAL_q4.extracthashtag(requestAnalysis);
								//caching part and analysis response how to show?
								//analysisresponse = twitteranalysisBAL_q4.getCachedData(hashtag);
								//if(analysisresponse.equals("")) {
								analysisresponse = twitteranalysisBAL_q4.getanalysisResponse(hashtag,Starttweettime,Endtweettime);
								//} else {
								//twitteranalysisBAL_q4.addToCache(analysisresponse);
								//}
								analysisresponse = StringEscapeUtils.unescapeJava(analysisresponse);

								//analysisresponse = "";
							}
							else if(stepCompare.contains("/index.html"))
							{
								analysisresponse = "heartbeat";
							}
							exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain; charset=UTF-8");
							exchange.getResponseSender().send("TeamRockStars,327811142774,617128749441,910842319737\n" + analysisresponse);
						}
					}
				}).build();
		server.start();
	}
}
