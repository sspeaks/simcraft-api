import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.simcraftwebapi.service.SimulationService;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SimulationServiceTest extends JerseyTest{

    final Logger logger = LoggerFactory.getLogger(SimulationServiceTest.class);

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        return new ResourceConfig(SimulationService.class);
    }

    @Test
    public void testMainApi() {
        WebTarget target = target("/simulate").
        queryParam("zone", "eu").
        queryParam("realm", "borean-tundra").
        queryParam("character", "мичикко").
        queryParam("type", "json");
        Response output = target.request().get();
        assertEquals("Should return status 200", 200, output.getStatus());
        String res = output.readEntity(String.class);
        assertNotNull("Should return json", res);
        logger.info(String.format("Test testMainApi result is: %s", res));
    }

}
