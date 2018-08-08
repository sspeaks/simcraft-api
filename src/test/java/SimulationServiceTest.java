import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.simcraftwebapi.service.SimulationService;

import javax.ws.rs.client.Entity;
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
    public void testManyInOne() {
        Response output = target("/simulate/eu/borean-tundra/мичикко").request().get();
        assertEquals("Should return status 200", 200, output.getStatus());
        String res = output.readEntity(String.class);
        assertNotNull("Should return list of KnowledgeBases", res);
        logger.info(String.format("Test testPOSTKnowledgeBase result is: %s", res));
    }

}
