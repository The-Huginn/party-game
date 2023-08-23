package com.thehuginn.external;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "task-api")
public interface GameRestClientTask extends GameRestClient {}
