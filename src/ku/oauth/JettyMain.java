package ku.oauth;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ServerProperties;

public class JettyMain {

private static final int PORT = 8080;
	
	private static Server server = null;
	
	/* Start the server with default port. */
	public static String startServer() throws Exception {
		int port = PORT;
		return startServer( port );
	}
	
	/* Start the server. */
	public static String startServer(int port) throws Exception {
		server = new Server( port );
		
		ServletContextHandler context = new ServletContextHandler( ServletContextHandler.SESSIONS );
		
		context.setContextPath("/");
		
		ServletHolder holder = new ServletHolder( org.glassfish.jersey.servlet.ServletContainer.class );
		
		holder.setInitParameter(ServerProperties.PROVIDER_PACKAGES, "ku.oauth.resource");
		context.addServlet( holder, "/*" );

		server.setHandler( context );
		
		System.out.println("Starting Jetty server on port " + port);
		server.start();
		
		return server.getURI().toString();
	}
	
	/* Start the server with default port. */
	public static void stopServer() throws Exception {
		System.out.println("Stopping server.");
		server.stop();
	}
	
	public static void main(String [] args) throws Exception {
		startServer();
	}
}
