package net.mastrgamr.api;

import android.util.Log;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.beust.jcommander.Parameter;

/**
 * Code sample for accessing the Yelp API V2.
 * 
 * This program demonstrates the capability of the Yelp API version 2.0 by using the Search API to
 * query for businesses by a search term and location, and the Business API to query additional
 * information about the top result from the search query.
 * 
 * <p>
 * See <a href="http://www.yelp.com/developers/documentation">Yelp Documentation</a> for more info.
 * 
 */
public class YelpAPI implements Keys {

  private static final String API_HOST = "api.yelp.com";
  private static final String DEFAULT_TERM = "bars";
  public static final String DEFAULT_LOCATION = "10003"; //TODO: public for now, later get GPS coords for default
  private static final int SEARCH_LIMIT = 10;
  private static final String SEARCH_PATH = "/v2/search";
  private static final String BUSINESS_PATH = "/v2/business";

  OAuthService service;
  Token accessToken;

  String[] businessResponseJSON;

  /**
   * Setup the Yelp API OAuth credentials. Stored in Keys interface. (shhh, it's private GitHub!)
   * 
   * @param consumerKey Consumer key
   * @param consumerSecret Consumer secret
   * @param token Token
   * @param tokenSecret Token secret
   */
  public YelpAPI(String consumerKey, String consumerSecret, String token, String tokenSecret) {
    this.service =
        new ServiceBuilder().provider(TwoStepOAuth.class).apiKey(consumerKey)
            .apiSecret(consumerSecret).build();
    this.accessToken = new Token(token, tokenSecret);
  }

  /**
   * Creates and sends a request to the Search API by term and location.
   * <p>
   * See <a href="http://www.yelp.com/developers/documentation/v2/search_api">Yelp Search API V2</a>
   * for more info.
   *
   * @param location <tt>String</tt> of the location
   * @return <tt>String</tt> JSON Response
   */
  public String searchForBusinessesByLocation(String location) {
    OAuthRequest request = createOAuthRequest(SEARCH_PATH);
    request.addQuerystringParameter("term", DEFAULT_TERM);
    request.addQuerystringParameter("location", location);
    request.addQuerystringParameter("limit", String.valueOf(SEARCH_LIMIT));
    return sendRequestAndGetResponse(request);
  }

  /**
   * Creates and sends a request to the Business API by business ID.
   * <p>
   * See <a href="http://www.yelp.com/developers/documentation/v2/business">Yelp Business API V2</a>
   * for more info.
   * 
   * @param businessID <tt>String</tt> business ID of the requested business
   * @return <tt>String</tt> JSON Response
   */
  public String searchByBusinessId(String businessID) {
    OAuthRequest request = createOAuthRequest(BUSINESS_PATH + "/" + businessID);
    return sendRequestAndGetResponse(request);
  }

  /**
   * Creates and returns an {@link OAuthRequest} based on the API endpoint specified.
   * 
   * @param path API endpoint to be queried
   * @return <tt>OAuthRequest</tt>
   */
  private OAuthRequest createOAuthRequest(String path) {
    return new OAuthRequest(Verb.GET, "http://" + API_HOST + path);
  }

  /**
   * Sends an {@link OAuthRequest} and returns the {@link Response} body.
   * 
   * @param request {@link OAuthRequest} corresponding to the API request
   * @return <tt>String</tt> body of API response
   */
  private String sendRequestAndGetResponse(OAuthRequest request) {
    this.service.signRequest(this.accessToken, request);
    Response response = request.send();
    return response.getBody();
  }

  /**
   * Queries the Search API based on the command line arguments and takes the first 3 results to query
   * the Business API.
   * 
   * @param yelpApi <tt>YelpAPI</tt> service instance
   */
  public void queryAPI(YelpAPI yelpApi, String location) {
      String searchResponseJSON = yelpApi.searchForBusinessesByLocation(location);
      Log.d("YelpAPI", searchResponseJSON); //remove this

      JSONParser parser = new JSONParser();
      JSONObject response = null;
      try {
        response = (JSONObject) parser.parse(searchResponseJSON);
      } catch (ParseException pe) {
        Log.e("YelpAPI", "Error: could not parse JSON response:" + searchResponseJSON);
        System.exit(1);
      }

      JSONArray businesses = (JSONArray) response.get("businesses");
      String businessID[] = new String[SEARCH_LIMIT];
      JSONObject business[] = new JSONObject[SEARCH_LIMIT];
      businessResponseJSON = new String[SEARCH_LIMIT];

      for (int i = 0; i < SEARCH_LIMIT; i++) {
	      business[i] = (JSONObject) businesses.get(i);
	      businessID[i] = business[i].get("id").toString();
	      //System.out.println(String.format(
	      //    "%s businesses found, querying business info for the top result \"%s\" ...",
	      //    businesses.size(), businessID[i]));

   	      // Select the business and display business details
	      businessResponseJSON[i] = yelpApi.searchByBusinessId(businessID[i]);
	      //System.out.println(String.format("Result for business \"%s\" found:", businessID[i]));
          Log.d("YelpAPI" ,businessResponseJSON[i]);
      }
  }

  public String getBusinessResponseJSON(int i) {
      return businessResponseJSON[i];
  }

}
