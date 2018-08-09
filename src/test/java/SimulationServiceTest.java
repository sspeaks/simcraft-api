import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.simcraftwebapi.service.SimulationService;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import java.util.concurrent.TimeUnit;

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
    public void testOneSimulationNoPawn() {
        WebTarget target = target("/simulate").
                queryParam("zone", "eu").
                queryParam("realm", "borean-tundra").
                queryParam("character", "мичикко").
                queryParam("type", "json").
                queryParam("pawn", "false").
                queryParam("iterations", "1000");
        Response output = target.request().get();
        assertEquals("Should return status 200", 200, output.getStatus());
        String res = output.readEntity(String.class);
        assertNotNull("Should return json", res);
        logger.info(String.format("Test testOneSimulationNoPawn result is: %s", res));
    }

    @Test
    public void testOneSimulationWithPawn() {
        WebTarget target = target("/simulate").
                queryParam("zone", "eu").
                queryParam("realm", "borean-tundra").
                queryParam("character", "мичикко").
                queryParam("type", "json").
                queryParam("pawn", "true").
                queryParam("iterations", "100");
        Response output = target.request().get();
        assertEquals("Should return status 200", 200, output.getStatus());
        String res = output.readEntity(String.class);
        assertNotNull("Should return json", res);
        logger.info(String.format("Test testOneSimulationWithPawn result is: %s", res));
    }

    @Test
    public void testOneSimulationWithPawnAndError() {
        WebTarget target = target("/simulate").
                queryParam("ze", "eu").
                queryParam("realm", "borean-tundra").
                queryParam("character", "мичикко").
                queryParam("type", "json").
                queryParam("pawn", "true").
                queryParam("iterations", "100");
        Response output = target.request().get();
        assertEquals("Should return status 500", 500, output.getStatus());
        String res = output.readEntity(String.class);
        assertEquals("Error: Option 'armory' with value 'null,borean-tundra,мичикко': Invalid region 'null'.\n", res);
        logger.info(String.format("Test testOneSimulationWithPawnAndError result is: %s", res));
    }

    @Test
    public void testAsyncSimulationBasics() {
        WebTarget target = target("/simulate/async").
                queryParam("zone", "eu").
                queryParam("realm", "borean-tundra").
                queryParam("character", "мичикко").
                queryParam("type", "json").
                queryParam("pawn", "false").
                queryParam("iterations", "1000");
        Response output = target.request().get();
        assertEquals("Should return status 200", 200, output.getStatus());
        String res = output.readEntity(String.class);
        assertNotNull("Should return json", res);
        logger.info(String.format("Test testAsyncSimulationBasics result is: %s", res));
        target = target("/simulate/async").
                queryParam("zone", "eu").
                queryParam("realm", "borean-tundra").
                queryParam("character", "химмавари").
                queryParam("type", "json").
                queryParam("pawn", "false").
                queryParam("iterations", "1000");
        output = target.request().get();
        assertEquals("Should return status 200", 200, output.getStatus());
        res = output.readEntity(String.class);
        assertNotNull("Should return json", res);
        logger.info(String.format("Test testAsyncSimulationBasics result is: %s", res));
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String uuid = res;
        target = target("/simulate/async/result").
                queryParam("uuid", uuid).
                queryParam("type", "json").
                queryParam("delete", false);
        output = target.request().get();
        assertEquals("Should return status 200", 200, output.getStatus());
        res = output.readEntity(String.class);
        assertNotNull("Should return json", res);
        logger.info(String.format("Test testAsyncSimulationBasics result is: %s", res));

        target = target("/simulate/async/result").
                queryParam("uuid", uuid).
                queryParam("type", "json").
                queryParam("delete", true);
        output = target.request().get();
        assertEquals("Should return status 200", 200, output.getStatus());
        res = output.readEntity(String.class);
        assertNotNull("Should return json", res);
        logger.info(String.format("Test testAsyncSimulationBasics result is: %s", res));

        target = target("/simulate/async/result").
                queryParam("uuid", uuid).
                queryParam("type", "json").
                queryParam("delete", false);
        output = target.request().get();
        assertEquals("Should return status 404", 404, output.getStatus());
    }
}
