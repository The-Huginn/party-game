package com.thehuginn.external;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "pub-api")
public interface GameRestClientPub extends GameRestClient {
}
