package immogram.feed;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;

public interface FeedServer {
	void setAdminPassword(String password);
	void start(InetSocketAddress address, URI endpoint) throws IOException;
}
