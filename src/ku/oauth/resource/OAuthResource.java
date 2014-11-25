package ku.oauth.resource;

import java.net.URI;
import java.net.URISyntaxException;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import ku.oauth.OAuthTokenResponse;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthClientResponse;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;

@Singleton
@Path("")
public class OAuthResource {

	private static final String client_id = "KEY";
	private static final String client_secret = "SECRET";

	@Context
	UriInfo uriInfo;
	
	private OAuthClient client;
	private OAuthClientRequest request;
	private OAuthClientResponse response;
	private String ACCESS_TOKEN;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response index() {
		return Response.ok().build();
	}

	@GET
	@Path("/google")
	public Response authenticate() {
		try {
			request = OAuthClientRequest
					.authorizationProvider(OAuthProviderType.GOOGLE)
					.setClientId(client_id)
					.setResponseType("code")
					.setScope(
							"email")
					.setRedirectURI( UriBuilder.fromUri(uriInfo.getBaseUri())
							.path("oauth2callback").build().toString()
							)
					.buildQueryMessage();
			
			URI redirect = new URI(request.getLocationUri());
			return Response.seeOther(redirect).build();
		} catch (OAuthSystemException e) {
			throw new WebApplicationException(e);
		} catch (URISyntaxException e) {
			throw new WebApplicationException(e);
		}
	}

	@GET
	@Path("/oauth2callback")
	public Response authorize(@QueryParam("code") String code,
			@QueryParam("state") String state) {
		// path to redirect after authorization
		// final URI uri = uriInfo.getBaseUriBuilder().path("").build();

		try {
			// Request to exchange code for access token and id token
			request = OAuthClientRequest
					.tokenProvider(OAuthProviderType.GOOGLE)
					.setCode(code)
					.setClientId(client_id)
					.setClientSecret(client_secret)
					.setRedirectURI( UriBuilder.fromUri(uriInfo.getBaseUri())
							.path("oauth2callback").build().toString()
							)
					.setGrantType(GrantType.AUTHORIZATION_CODE)
					.buildBodyMessage();
			
			client = new OAuthClient(new URLConnectionClient());
			response = client
					.accessToken(request, OAuthTokenResponse.class);
			
			// Get the access token from the response
			ACCESS_TOKEN = ((OAuthJSONAccessTokenResponse) response).getAccessToken();
			
			// Add code to notify application of authenticated user
		} catch (OAuthProblemException | OAuthSystemException e) {
			e.printStackTrace();
		} 

		return Response.ok().build();
	}
	
	public OAuthClientResponse getClientResource() {
		try {
			request  = new OAuthBearerClientRequest("https://www.googleapis.com/plus/v1/people/me").setAccessToken(ACCESS_TOKEN).buildQueryMessage();
			return client.resource(request, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
		} catch (OAuthSystemException | OAuthProblemException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public OAuthClientResponse logout() {
		try {
			request = new OAuthBearerClientRequest("https://accounts.google.com/logout").setAccessToken(ACCESS_TOKEN).buildQueryMessage();
			return client.resource(request, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
			} catch (OAuthSystemException | OAuthProblemException e) {
			e.printStackTrace();
		}
		return null;
	}
}
