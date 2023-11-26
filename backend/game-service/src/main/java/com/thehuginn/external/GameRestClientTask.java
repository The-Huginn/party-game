package com.thehuginn.external;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@ApplicationScoped
@RegisterRestClient(configKey = "task-api")
public interface GameRestClientTask extends GameRestClient {
}
