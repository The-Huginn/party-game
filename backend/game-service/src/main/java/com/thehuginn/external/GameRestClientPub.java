package com.thehuginn.external;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@ApplicationScoped
@RegisterRestClient(configKey = "pub-api")
public interface GameRestClientPub extends GameRestClient {
}
