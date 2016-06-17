import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import cs.technion.ac.il.sd.Attendance;
import cs.technion.ac.il.sd.Input;
import cs.technion.ac.il.sd.Output;
import cs.technion.ac.il.sd.app.PartyApp;
import cs.technion.ac.il.sd.app.PartyModule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;

import static org.mockito.Mockito.verify;

/**
 * Created by Nati on 6/17/2016.
 */
public class PartyAppTest {
    class TestInput implements Input {
        private final List<BiConsumer<String, Optional<Boolean>>> listener = new LinkedList<>();

        @Override
        public void listen(BiConsumer<String, Optional<Boolean>> listener) {
            this.listener.add(listener);
        }

        public void publish(String name, Boolean attending) {
            this.listener.forEach(c -> c.accept(name, Optional.ofNullable(attending)));
        }
    }

    private final TestInput input = new TestInput();
    private final Output output = Mockito.mock(Output.class);
    private final Injector injector = Guice.createInjector(new PartyModule(), new AbstractModule() {
        @Override
        protected void configure() {
            bind(Output.class).toInstance(output);
            bind(Input.class).toInstance(input);
        }
    });

    public Map<String, Attendance> map(Map.Entry<String, Attendance>... expected) {
        Map<String, Attendance> map = new HashMap<>();
        for (Map.Entry<String, Attendance> e : expected)
            map.put(e.getKey(), e.getValue());
        return map;
    }

    private final PartyApp $ = injector.getInstance(PartyApp.class);

    private void processFile(String name) {
        $.processFile(new File(getClass().getResource(name + ".txt").getFile()));
    }

    @Rule
    public Timeout globalTimeout = Timeout.seconds(10);

    private static Map.Entry<String, Attendance> entry(String name, Attendance a) {
        return new AbstractMap.SimpleEntry<>(name, a);
    }

    @Test
    public void simpleInitAllToUnknown() {
        processFile("simple");
        input.publish("A", null);
        verify(output).attendance(map(
                entry("A", Attendance.UNKNOWN),
                entry("B", Attendance.UNKNOWN),
                entry("C", Attendance.UNKNOWN),
                entry("D", Attendance.UNKNOWN),
                entry("E", Attendance.UNKNOWN),
                entry("F", Attendance.UNKNOWN),
                entry("G", Attendance.UNKNOWN)
        ));
    }
    @Test
    public void simpleDependencyUnknown() {
        processFile("simple");
        input.publish("C", true);
        input.publish("F", false);
        input.publish("G", false);
        verify(output).attendance(map(
                entry("A", Attendance.UNKNOWN),
                entry("B", Attendance.UNKNOWN),
                entry("C", Attendance.ATTENDING),
                entry("D", Attendance.UNKNOWN),
                entry("E", Attendance.UNKNOWN),
                entry("F", Attendance.NOT_ATTENDING),
                entry("G", Attendance.NOT_ATTENDING)
        ));
    }

    @Test
    public void dependencyProbablyYes() {
        processFile("simple");
        input.publish("D", true);
        verify(output).attendance(map(
                entry("A", Attendance.PROBABLY_ATTENDING),
                entry("B", Attendance.PROBABLY_ATTENDING),
                entry("C", Attendance.PROBABLY_ATTENDING),
                entry("D", Attendance.ATTENDING),
                entry("E", Attendance.PROBABLY_ATTENDING),
                entry("F", Attendance.PROBABLY_ATTENDING),
                entry("G", Attendance.UNKNOWN)
        ));
    }

    @Test
    public void simpleProbablyYes() {
        processFile("simple");
        input.publish("A", false);
        input.publish("B", true);
        input.publish("D", true);
        input.publish("G", true);
        verify(output).attendance(map(
                entry("A", Attendance.NOT_ATTENDING),
                entry("B", Attendance.ATTENDING),
                entry("C", Attendance.PROBABLY_ATTENDING),
                entry("D", Attendance.ATTENDING),
                entry("E", Attendance.PROBABLY_ATTENDING),
                entry("F", Attendance.PROBABLY_ATTENDING),
                entry("G", Attendance.ATTENDING)
        ));
    }

    @Test
    public void dependencyProbablyNo() {
        processFile("simple");
        input.publish("C", true);
        input.publish("D", false);
        verify(output).attendance(map(
                entry("A", Attendance.PROBABLY_NOT_ATTENDING),
                entry("B", Attendance.PROBABLY_NOT_ATTENDING),
                entry("C", Attendance.ATTENDING),
                entry("D", Attendance.NOT_ATTENDING),
                entry("E", Attendance.PROBABLY_NOT_ATTENDING),
                entry("F", Attendance.PROBABLY_ATTENDING),
                entry("G", Attendance.UNKNOWN)
        ));
    }

    @Test
    public void simpleProbablyNo() {
        processFile("simple");
        input.publish("B", false);
        input.publish("C", true);
        input.publish("D", false);
        verify(output).attendance(map(
                entry("A", Attendance.PROBABLY_NOT_ATTENDING),
                entry("B", Attendance.NOT_ATTENDING),
                entry("C", Attendance.ATTENDING),
                entry("D", Attendance.NOT_ATTENDING),
                entry("E", Attendance.PROBABLY_NOT_ATTENDING),
                entry("F", Attendance.PROBABLY_ATTENDING),
                entry("G", Attendance.UNKNOWN)
        ));
    }





}
