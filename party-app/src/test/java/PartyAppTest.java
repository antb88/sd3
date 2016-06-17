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
        input.publish("Arya Stark", null);
        verify(output).attendance(map(
                entry("Arya Stark", Attendance.UNKNOWN),
                entry("Bran Stark", Attendance.UNKNOWN),
                entry("Ned Stark", Attendance.UNKNOWN),
                entry("Sansa Stark", Attendance.UNKNOWN),
                entry("Brienne of Tarth", Attendance.UNKNOWN),
                entry("Jon Snow", Attendance.UNKNOWN),
                entry("Hodor", Attendance.UNKNOWN)
        ));
    }
    @Test
    public void simpleDependencyUnknown() {
        processFile("simple");
        input.publish("Ned Stark", true);
        input.publish("Jon Snow", false);
        input.publish("Hodor", false);
        verify(output).attendance(map(
                entry("Arya Stark", Attendance.UNKNOWN),
                entry("Bran Stark", Attendance.UNKNOWN),
                entry("Ned Stark", Attendance.ATTENDING),
                entry("Sansa Stark", Attendance.UNKNOWN),
                entry("Brienne of Tarth", Attendance.UNKNOWN),
                entry("Jon Snow", Attendance.NOT_ATTENDING),
                entry("Hodor", Attendance.NOT_ATTENDING)
        ));
    }

    @Test
    public void simpleAllDefinite() {
        processFile("simple");
        input.publish("Arya Stark", true);
        input.publish("Bran Stark", false);
        input.publish("Ned Stark", true);
        input.publish("Sansa Stark", false);
        input.publish("Brienne of Tarth", true);
        input.publish("Jon Snow", false);
        input.publish("Hodor", false);
        verify(output).attendance(map(
                entry("Arya Stark", Attendance.ATTENDING),
                entry("Bran Stark", Attendance.NOT_ATTENDING),
                entry("Ned Stark", Attendance.ATTENDING),
                entry("Sansa Stark", Attendance.NOT_ATTENDING),
                entry("Brienne of Tarth", Attendance.ATTENDING),
                entry("Jon Snow", Attendance.NOT_ATTENDING),
                entry("Hodor", Attendance.NOT_ATTENDING)
        ));
    }

    @Test
    public void dependencyProbablyYes() {
        processFile("simple");
        input.publish("Sansa Stark", true);
        verify(output).attendance(map(
                entry("Arya Stark", Attendance.PROBABLY_ATTENDING),
                entry("Bran Stark", Attendance.PROBABLY_ATTENDING),
                entry("Ned Stark", Attendance.PROBABLY_ATTENDING),
                entry("Sansa Stark", Attendance.ATTENDING),
                entry("Brienne of Tarth", Attendance.PROBABLY_ATTENDING),
                entry("Jon Snow", Attendance.PROBABLY_ATTENDING),
                entry("Hodor", Attendance.UNKNOWN)
        ));
    }

    @Test
    public void simpleProbablyYes() {
        processFile("simple");
        input.publish("Arya Stark", false);
        input.publish("Bran Stark", true);
        input.publish("Sansa Stark", true);
        input.publish("Hodor", true);
        verify(output).attendance(map(
                entry("Arya Stark", Attendance.NOT_ATTENDING),
                entry("Bran Stark", Attendance.ATTENDING),
                entry("Ned Stark", Attendance.PROBABLY_ATTENDING),
                entry("Sansa Stark", Attendance.ATTENDING),
                entry("Brienne of Tarth", Attendance.PROBABLY_ATTENDING),
                entry("Jon Snow", Attendance.PROBABLY_ATTENDING),
                entry("Hodor", Attendance.ATTENDING)
        ));
    }

    @Test
    public void dependencyProbablyNo() {
        processFile("simple");
        input.publish("Ned Stark", true);
        input.publish("Sansa Stark", false);
        verify(output).attendance(map(
                entry("Arya Stark", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Bran Stark", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Ned Stark", Attendance.ATTENDING),
                entry("Sansa Stark", Attendance.NOT_ATTENDING),
                entry("Brienne of Tarth", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Jon Snow", Attendance.PROBABLY_ATTENDING),
                entry("Hodor", Attendance.UNKNOWN)
        ));
    }

    @Test
    public void simpleProbablyNo() {
        processFile("simple");
        input.publish("Bran Stark", false);
        input.publish("Ned Stark", true);
        input.publish("Sansa Stark", false);
        verify(output).attendance(map(
                entry("Arya Stark", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Bran Stark", Attendance.NOT_ATTENDING),
                entry("Ned Stark", Attendance.ATTENDING),
                entry("Sansa Stark", Attendance.NOT_ATTENDING),
                entry("Brienne of Tarth", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Jon Snow", Attendance.PROBABLY_ATTENDING),
                entry("Hodor", Attendance.UNKNOWN)
        ));
    }

    @Test
    public void simpleChangeYesToNo() {
        processFile("simple");
        input.publish("Sansa Stark", true);
        input.publish("Hodor", true);
        verify(output).attendance(map(
                entry("Arya Stark", Attendance.PROBABLY_ATTENDING),
                entry("Bran Stark", Attendance.PROBABLY_ATTENDING),
                entry("Ned Stark", Attendance.PROBABLY_ATTENDING),
                entry("Sansa Stark", Attendance.ATTENDING),
                entry("Brienne of Tarth", Attendance.PROBABLY_ATTENDING),
                entry("Jon Snow", Attendance.PROBABLY_ATTENDING),
                entry("Hodor", Attendance.ATTENDING)
        ));

        input.publish("Hodor", false);
        verify(output).attendance(map(
                entry("Arya Stark", Attendance.PROBABLY_ATTENDING),
                entry("Bran Stark", Attendance.PROBABLY_ATTENDING),
                entry("Ned Stark", Attendance.PROBABLY_ATTENDING),
                entry("Sansa Stark", Attendance.ATTENDING),
                entry("Brienne of Tarth", Attendance.PROBABLY_ATTENDING),
                entry("Jon Snow", Attendance.PROBABLY_ATTENDING),
                entry("Hodor", Attendance.NOT_ATTENDING)
        ));

        input.publish("Sansa Stark", false);
        verify(output).attendance(map(
                entry("Arya Stark", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Bran Stark", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Ned Stark", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Sansa Stark", Attendance.NOT_ATTENDING),
                entry("Brienne of Tarth", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Jon Snow", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Hodor", Attendance.NOT_ATTENDING)
        ));


    }

    @Test
    public void simpleChangeNoToYes() {
        processFile("simple");
        input.publish("Bran Stark", false);
        input.publish("Sansa Stark", false);
        verify(output).attendance(map(
                entry("Arya Stark", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Bran Stark", Attendance.NOT_ATTENDING),
                entry("Ned Stark", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Sansa Stark", Attendance.NOT_ATTENDING),
                entry("Brienne of Tarth", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Jon Snow", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Hodor", Attendance.UNKNOWN)
        ));

        input.publish("Sansa Stark", true);
        verify(output).attendance(map(
                entry("Arya Stark", Attendance.PROBABLY_ATTENDING),
                entry("Bran Stark", Attendance.NOT_ATTENDING),
                entry("Ned Stark", Attendance.PROBABLY_ATTENDING),
                entry("Sansa Stark", Attendance.ATTENDING),
                entry("Brienne of Tarth", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Jon Snow", Attendance.PROBABLY_ATTENDING),
                entry("Hodor", Attendance.UNKNOWN)
        ));

        input.publish("Bran Stark", true);
        verify(output).attendance(map(
                entry("Arya Stark", Attendance.PROBABLY_ATTENDING),
                entry("Bran Stark", Attendance.ATTENDING),
                entry("Ned Stark", Attendance.PROBABLY_ATTENDING),
                entry("Sansa Stark", Attendance.ATTENDING),
                entry("Brienne of Tarth", Attendance.PROBABLY_ATTENDING),
                entry("Jon Snow", Attendance.PROBABLY_ATTENDING),
                entry("Hodor", Attendance.UNKNOWN)
        ));
    }

    @Test
    public void simpleChangeYesToUnknown() {
        processFile("simple");
        input.publish("Hodor", true);
        input.publish("Sansa Stark", true);
        verify(output).attendance(map(
                entry("Arya Stark", Attendance.PROBABLY_ATTENDING),
                entry("Bran Stark", Attendance.PROBABLY_ATTENDING),
                entry("Ned Stark", Attendance.PROBABLY_ATTENDING),
                entry("Sansa Stark", Attendance.ATTENDING),
                entry("Brienne of Tarth", Attendance.PROBABLY_ATTENDING),
                entry("Jon Snow", Attendance.PROBABLY_ATTENDING),
                entry("Hodor", Attendance.ATTENDING)
        ));

        input.publish("Hodor", null);
        verify(output).attendance(map(
                entry("Arya Stark", Attendance.PROBABLY_ATTENDING),
                entry("Bran Stark", Attendance.PROBABLY_ATTENDING),
                entry("Ned Stark", Attendance.PROBABLY_ATTENDING),
                entry("Sansa Stark", Attendance.ATTENDING),
                entry("Brienne of Tarth", Attendance.PROBABLY_ATTENDING),
                entry("Jon Snow", Attendance.PROBABLY_ATTENDING),
                entry("Hodor", Attendance.UNKNOWN)
        ));

        input.publish("Sansa Stark", null);
        verify(output).attendance(map(
                entry("Arya Stark", Attendance.UNKNOWN),
                entry("Bran Stark", Attendance.UNKNOWN),
                entry("Ned Stark", Attendance.UNKNOWN),
                entry("Sansa Stark", Attendance.UNKNOWN),
                entry("Brienne of Tarth", Attendance.UNKNOWN),
                entry("Jon Snow", Attendance.UNKNOWN),
                entry("Hodor", Attendance.UNKNOWN)
        ));


    }

    @Test
    public void simpleChangeNoToUnknown() {
        processFile("simple");
        input.publish("Sansa Stark", false);
        input.publish("Bran Stark", false);
        verify(output).attendance(map(
                entry("Arya Stark", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Bran Stark", Attendance.NOT_ATTENDING),
                entry("Ned Stark", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Sansa Stark", Attendance.NOT_ATTENDING),
                entry("Brienne of Tarth", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Jon Snow", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Hodor", Attendance.UNKNOWN)
        ));

        input.publish("Sansa Stark", null);
        verify(output).attendance(map(
                entry("Arya Stark", Attendance.UNKNOWN),
                entry("Bran Stark", Attendance.NOT_ATTENDING),
                entry("Ned Stark", Attendance.UNKNOWN),
                entry("Sansa Stark", Attendance.UNKNOWN),
                entry("Brienne of Tarth", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Jon Snow", Attendance.UNKNOWN),
                entry("Hodor", Attendance.UNKNOWN)
        ));

        input.publish("Bran Stark", null);
        verify(output).attendance(map(
                entry("Arya Stark", Attendance.UNKNOWN),
                entry("Bran Stark", Attendance.UNKNOWN),
                entry("Ned Stark", Attendance.UNKNOWN),
                entry("Sansa Stark", Attendance.UNKNOWN),
                entry("Brienne of Tarth", Attendance.UNKNOWN),
                entry("Jon Snow", Attendance.UNKNOWN),
                entry("Hodor", Attendance.UNKNOWN)
        ));
    }





}
