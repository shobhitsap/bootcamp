package com.sap.sdc.hcp.bootcamp.web;


import java.io.IOException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sap.core.connectivity.api.DestinationException;
import com.sap.core.connectivity.api.http.HttpDestination;

/**
 * Servlet implementation class FeedbackServlet
 */

@WebServlet("/FeedbackServlet")
public class FeedbackServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(FeedbackServlet.class);

    public FeedbackServlet() {
        super();
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpClient httpClient = null;
		try {
			Context ctx = new InitialContext();
			HttpDestination destination = (HttpDestination) ctx.lookup("java:comp/env/FeedbackService");

			httpClient = destination.createHttpClient();
			HttpPost post = new HttpPost();
			String data = request.getParameter("JsonData");
			
			JsonParser parse = new JsonParser();
			JsonObject json = parse.parse(data).getAsJsonObject();
			JsonObject jsonObject = json.get("temp").getAsJsonObject();
			
			
			String text = jsonObject.get("text").getAsString();
			String rating0 = jsonObject.get("rating0").getAsString();
			String rating1 = jsonObject.get("rating1").getAsString();
			String rating2 = jsonObject.get("rating2").getAsString();
			String rating3 = jsonObject.get("rating3").getAsString();					
			String page = jsonObject.get("page").getAsString();
			
			String body = "{\"texts\":{\"t1\": \"" + text + "\"}, \"ratings\":{ "
					+ "\"r1\": {\"value\": " + rating0 +"},"
				    + "\"r2\": {\"value\": " +rating1+"}, \"r3\":{\"value\":" +rating2+"},\"r4\":{\"value\":" +rating3+ "}}, \"context\": {\"page\": \"" + page + "\", \"lang\": \"en\", \"attr1\": \"mobile\"}}";

			//Use the proper content type
			post.setEntity(new StringEntity(body, "application/json", "UTF-8"));

			HttpResponse httpResponse = httpClient.execute(post);
			int responceCode = httpResponse.getStatusLine().getStatusCode();

			if (responceCode != HttpServletResponse.SC_OK) {
				LOGGER.error("Feedback Service call failed with HTTP responce code " + responceCode + ". Error: " + httpResponse.getStatusLine().getReasonPhrase());
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Something went wrong please try again later.");
			} else {
				response.getWriter().print("Your feedback was accepted. Thank You!");
			}
		} catch (NamingException e) {
			LOGGER.error("Cannot lookup the feedback service destination", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot lookup the feedback service destination");
		} catch (DestinationException e) {
			LOGGER.error("Cannot create HttpClient", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Something went wrong please try again later.");
		}catch(Exception exc){
			LOGGER.error("Severe error ");
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Something terribly went wrong please try again later.");
		} 
		finally {
			if (httpClient != null) {
				ClientConnectionManager connectionManager = httpClient.getConnectionManager();
				if (connectionManager != null) {
					connectionManager.shutdown();
				}
			}
		}
	}

}
