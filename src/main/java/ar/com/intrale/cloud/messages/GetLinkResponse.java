package ar.com.intrale.cloud.messages;

import java.util.ArrayList;
import java.util.Collection;

import ar.com.intrale.cloud.Response;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class GetLinkResponse extends Response {
	
	private Collection<Link> links = new ArrayList<Link>();

	public Collection<Link> getLinks() {
		return links;
	}

	public void setLinks(Collection<Link> links) {
		this.links = links;
	}

	public void add(Link link) {
		links.add(link);
	}
}
