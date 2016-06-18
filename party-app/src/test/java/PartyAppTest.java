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
import org.mockito.Mockito;

import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;

import static org.mockito.Mockito.verify;

/**
 * Created by Nati on 6/17/2016.
 */
@SuppressWarnings("unchecked")
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

    private Map<String, Attendance> map(Map.Entry<String, Attendance>... expected) {
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
    public void simpleDependencyProbablyYes() {
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
    public void simpleDependencyProbablyNo() {
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

    @Test
    public void cyclicInitAllToUnknown() {
        processFile("cyclic");
        input.publish("A", null);
        verify(output).attendance(map(
                entry("A", Attendance.UNKNOWN),
                entry("B", Attendance.UNKNOWN),
                entry("C", Attendance.UNKNOWN),
                entry("D", Attendance.UNKNOWN),
                entry("E", Attendance.UNKNOWN),
                entry("F", Attendance.UNKNOWN),
                entry("G", Attendance.UNKNOWN),
                entry("H", Attendance.UNKNOWN),
                entry("I", Attendance.UNKNOWN),
                entry("J", Attendance.UNKNOWN),
                entry("K", Attendance.UNKNOWN)
        ));
    }

    @Test
    public void cyclicDependencyProbablyYes() {
        processFile("cyclic");
        input.publish("I", true);
        input.publish("B", true);
        verify(output).attendance(map(
                entry("A", Attendance.UNKNOWN),
                entry("B", Attendance.ATTENDING),
                entry("C", Attendance.PROBABLY_ATTENDING),
                entry("D", Attendance.PROBABLY_ATTENDING),
                entry("E", Attendance.PROBABLY_ATTENDING),
                entry("F", Attendance.UNKNOWN),
                entry("G", Attendance.UNKNOWN),
                entry("H", Attendance.PROBABLY_ATTENDING),
                entry("I", Attendance.ATTENDING),
                entry("J", Attendance.PROBABLY_ATTENDING),
                entry("K", Attendance.UNKNOWN)
        ));
    }

    @Test
    public void cyclicDependencyProbablyNo() {
        processFile("cyclic");
        input.publish("I", false);
        input.publish("B", false);
        verify(output).attendance(map(
                entry("A", Attendance.UNKNOWN),
                entry("B", Attendance.NOT_ATTENDING),
                entry("C", Attendance.PROBABLY_NOT_ATTENDING),
                entry("D", Attendance.PROBABLY_NOT_ATTENDING),
                entry("E", Attendance.PROBABLY_NOT_ATTENDING),
                entry("F", Attendance.PROBABLY_NOT_ATTENDING),
                entry("G", Attendance.UNKNOWN),
                entry("H", Attendance.PROBABLY_NOT_ATTENDING),
                entry("I", Attendance.NOT_ATTENDING),
                entry("J", Attendance.PROBABLY_NOT_ATTENDING),
                entry("K", Attendance.UNKNOWN)
        ));
    }

    @Test
    public void cyclicDependencyUnknown() {
        processFile("cyclic");
        input.publish("A", true);
        input.publish("G", true);
        verify(output).attendance(map(
                entry("A", Attendance.ATTENDING),
                entry("B", Attendance.UNKNOWN),
                entry("C", Attendance.UNKNOWN),
                entry("D", Attendance.UNKNOWN),
                entry("E", Attendance.UNKNOWN),
                entry("F", Attendance.UNKNOWN),
                entry("G", Attendance.ATTENDING),
                entry("H", Attendance.UNKNOWN),
                entry("I", Attendance.UNKNOWN),
                entry("J", Attendance.UNKNOWN),
                entry("K", Attendance.UNKNOWN)
        ));
    }

    @Test
    public void cyclicAllDefinite() {
        processFile("cyclic");
        input.publish("A", true);
        input.publish("B", false);
        input.publish("C", true);
        input.publish("D", false);
        input.publish("E", true);
        input.publish("F", false);
        input.publish("G", false);
        input.publish("H", true);
        input.publish("I", false);
        input.publish("J", true);
        input.publish("K", true);
        verify(output).attendance(map(
                entry("A", Attendance.ATTENDING),
                entry("B", Attendance.NOT_ATTENDING),
                entry("C", Attendance.ATTENDING),
                entry("D", Attendance.NOT_ATTENDING),
                entry("E", Attendance.ATTENDING),
                entry("F", Attendance.NOT_ATTENDING),
                entry("G", Attendance.NOT_ATTENDING),
                entry("H", Attendance.ATTENDING),
                entry("I", Attendance.NOT_ATTENDING),
                entry("J", Attendance.ATTENDING),
                entry("K", Attendance.ATTENDING)
        ));
    }

    @Test
    public void cyclicProbablyYes() {
        processFile("cyclic");
        input.publish("A", false);
        input.publish("E", true);
        input.publish("K", true);
        verify(output).attendance(map(
                entry("A", Attendance.NOT_ATTENDING),
                entry("B", Attendance.PROBABLY_NOT_ATTENDING),
                entry("C", Attendance.PROBABLY_ATTENDING),
                entry("D", Attendance.PROBABLY_ATTENDING),
                entry("E", Attendance.ATTENDING),
                entry("F", Attendance.PROBABLY_ATTENDING),
                entry("G", Attendance.PROBABLY_ATTENDING),
                entry("H", Attendance.UNKNOWN),
                entry("I", Attendance.UNKNOWN),
                entry("J", Attendance.UNKNOWN),
                entry("K", Attendance.ATTENDING)
        ));
    }

    @Test
    public void cyclicProbablyNo() {
        processFile("cyclic");
        input.publish("A", false);
        input.publish("K", true);
        input.publish("J", false);
        verify(output).attendance(map(
                entry("A", Attendance.NOT_ATTENDING),
                entry("B", Attendance.PROBABLY_NOT_ATTENDING),
                entry("C", Attendance.PROBABLY_NOT_ATTENDING),
                entry("D", Attendance.PROBABLY_NOT_ATTENDING),
                entry("E", Attendance.PROBABLY_NOT_ATTENDING),
                entry("F", Attendance.PROBABLY_NOT_ATTENDING),
                entry("G", Attendance.PROBABLY_ATTENDING),
                entry("H", Attendance.PROBABLY_NOT_ATTENDING),
                entry("I", Attendance.PROBABLY_NOT_ATTENDING),
                entry("J", Attendance.NOT_ATTENDING),
                entry("K", Attendance.ATTENDING)
        ));
    }

    @Test
    public void cyclicRemainUnknown() {
        processFile("cyclic");
        input.publish("A", true);
        input.publish("K", false);
        input.publish("H", false);
        verify(output).attendance(map(
                entry("A", Attendance.ATTENDING),
                entry("B", Attendance.UNKNOWN),
                entry("C", Attendance.UNKNOWN),
                entry("D", Attendance.UNKNOWN),
                entry("E", Attendance.UNKNOWN),
                entry("F", Attendance.PROBABLY_NOT_ATTENDING),
                entry("G", Attendance.PROBABLY_NOT_ATTENDING),
                entry("H", Attendance.NOT_ATTENDING),
                entry("I", Attendance.PROBABLY_NOT_ATTENDING),
                entry("J", Attendance.PROBABLY_NOT_ATTENDING),
                entry("K", Attendance.NOT_ATTENDING)
        ));
    }
    @Ignore
    @Test
    public void cyclicChangeYesToNo() {
        processFile("cyclic");
        input.publish("A", true);
        input.publish("B", true);
        verify(output).attendance(map(
                entry("A", Attendance.UNKNOWN),
                entry("B", Attendance.UNKNOWN),
                entry("C", Attendance.UNKNOWN),
                entry("D", Attendance.UNKNOWN),
                entry("E", Attendance.UNKNOWN),
                entry("F", Attendance.UNKNOWN),
                entry("G", Attendance.UNKNOWN),
                entry("H", Attendance.UNKNOWN),
                entry("I", Attendance.UNKNOWN),
                entry("J", Attendance.UNKNOWN),
                entry("K", Attendance.UNKNOWN)
        ));

        input.publish("B", false);
        verify(output).attendance(map(
                entry("A", Attendance.UNKNOWN),
                entry("B", Attendance.UNKNOWN),
                entry("C", Attendance.UNKNOWN),
                entry("D", Attendance.UNKNOWN),
                entry("E", Attendance.UNKNOWN),
                entry("F", Attendance.UNKNOWN),
                entry("G", Attendance.UNKNOWN),
                entry("H", Attendance.UNKNOWN),
                entry("I", Attendance.UNKNOWN),
                entry("J", Attendance.UNKNOWN),
                entry("K", Attendance.UNKNOWN)
        ));

        input.publish("A", false);
        verify(output).attendance(map(
                entry("A", Attendance.UNKNOWN),
                entry("B", Attendance.UNKNOWN),
                entry("C", Attendance.UNKNOWN),
                entry("D", Attendance.UNKNOWN),
                entry("E", Attendance.UNKNOWN),
                entry("F", Attendance.UNKNOWN),
                entry("G", Attendance.UNKNOWN),
                entry("H", Attendance.UNKNOWN),
                entry("I", Attendance.UNKNOWN),
                entry("J", Attendance.UNKNOWN),
                entry("K", Attendance.UNKNOWN)
        ));


    }
    @Ignore
    @Test
    public void cyclicChangeNoToYes() {
        processFile("cyclic");
        input.publish("A", false);
        input.publish("B", false);
        verify(output).attendance(map(
                entry("A", Attendance.UNKNOWN),
                entry("B", Attendance.UNKNOWN),
                entry("C", Attendance.UNKNOWN),
                entry("D", Attendance.UNKNOWN),
                entry("E", Attendance.UNKNOWN),
                entry("F", Attendance.UNKNOWN),
                entry("G", Attendance.UNKNOWN),
                entry("H", Attendance.UNKNOWN),
                entry("I", Attendance.UNKNOWN),
                entry("J", Attendance.UNKNOWN),
                entry("K", Attendance.UNKNOWN)
        ));

        input.publish("B", true);
        verify(output).attendance(map(
                entry("A", Attendance.UNKNOWN),
                entry("B", Attendance.UNKNOWN),
                entry("C", Attendance.UNKNOWN),
                entry("D", Attendance.UNKNOWN),
                entry("E", Attendance.UNKNOWN),
                entry("F", Attendance.UNKNOWN),
                entry("G", Attendance.UNKNOWN),
                entry("H", Attendance.UNKNOWN),
                entry("I", Attendance.UNKNOWN),
                entry("J", Attendance.UNKNOWN),
                entry("K", Attendance.UNKNOWN)
        ));

        input.publish("A", true);
        verify(output).attendance(map(
                entry("A", Attendance.UNKNOWN),
                entry("B", Attendance.UNKNOWN),
                entry("C", Attendance.UNKNOWN),
                entry("D", Attendance.UNKNOWN),
                entry("E", Attendance.UNKNOWN),
                entry("F", Attendance.UNKNOWN),
                entry("G", Attendance.UNKNOWN),
                entry("H", Attendance.UNKNOWN),
                entry("I", Attendance.UNKNOWN),
                entry("J", Attendance.UNKNOWN),
                entry("K", Attendance.UNKNOWN)
        ));
    }
    @Ignore
    @Test
    public void cyclicChangeYesToUnknown() {
        processFile("cyclic");
        input.publish("A", true);
        input.publish("B", true);
        verify(output).attendance(map(
                entry("A", Attendance.UNKNOWN),
                entry("B", Attendance.UNKNOWN),
                entry("C", Attendance.UNKNOWN),
                entry("D", Attendance.UNKNOWN),
                entry("E", Attendance.UNKNOWN),
                entry("F", Attendance.UNKNOWN),
                entry("G", Attendance.UNKNOWN),
                entry("H", Attendance.UNKNOWN),
                entry("I", Attendance.UNKNOWN),
                entry("J", Attendance.UNKNOWN),
                entry("K", Attendance.UNKNOWN)
        ));

        input.publish("B", null);
        verify(output).attendance(map(
                entry("A", Attendance.UNKNOWN),
                entry("B", Attendance.UNKNOWN),
                entry("C", Attendance.UNKNOWN),
                entry("D", Attendance.UNKNOWN),
                entry("E", Attendance.UNKNOWN),
                entry("F", Attendance.UNKNOWN),
                entry("G", Attendance.UNKNOWN),
                entry("H", Attendance.UNKNOWN),
                entry("I", Attendance.UNKNOWN),
                entry("J", Attendance.UNKNOWN),
                entry("K", Attendance.UNKNOWN)
        ));

        input.publish("A", null);
        verify(output).attendance(map(
                entry("A", Attendance.UNKNOWN),
                entry("B", Attendance.UNKNOWN),
                entry("C", Attendance.UNKNOWN),
                entry("D", Attendance.UNKNOWN),
                entry("E", Attendance.UNKNOWN),
                entry("F", Attendance.UNKNOWN),
                entry("G", Attendance.UNKNOWN),
                entry("H", Attendance.UNKNOWN),
                entry("I", Attendance.UNKNOWN),
                entry("J", Attendance.UNKNOWN),
                entry("K", Attendance.UNKNOWN)
        ));


    }
    @Ignore
    @Test
    public void cyclicChangeNoToUnknown() {
        processFile("cyclic");
        input.publish("A", false);
        input.publish("B", false);
        verify(output).attendance(map(
                entry("A", Attendance.UNKNOWN),
                entry("B", Attendance.UNKNOWN),
                entry("C", Attendance.UNKNOWN),
                entry("D", Attendance.UNKNOWN),
                entry("E", Attendance.UNKNOWN),
                entry("F", Attendance.UNKNOWN),
                entry("G", Attendance.UNKNOWN),
                entry("H", Attendance.UNKNOWN),
                entry("I", Attendance.UNKNOWN),
                entry("J", Attendance.UNKNOWN),
                entry("K", Attendance.UNKNOWN)
        ));

        input.publish("B", null);
        verify(output).attendance(map(
                entry("A", Attendance.UNKNOWN),
                entry("B", Attendance.UNKNOWN),
                entry("C", Attendance.UNKNOWN),
                entry("D", Attendance.UNKNOWN),
                entry("E", Attendance.UNKNOWN),
                entry("F", Attendance.UNKNOWN),
                entry("G", Attendance.UNKNOWN),
                entry("H", Attendance.UNKNOWN),
                entry("I", Attendance.UNKNOWN),
                entry("J", Attendance.UNKNOWN),
                entry("K", Attendance.UNKNOWN)
        ));

        input.publish("A", null);
        verify(output).attendance(map(
                entry("A", Attendance.UNKNOWN),
                entry("B", Attendance.UNKNOWN),
                entry("C", Attendance.UNKNOWN),
                entry("D", Attendance.UNKNOWN),
                entry("E", Attendance.UNKNOWN),
                entry("F", Attendance.UNKNOWN),
                entry("G", Attendance.UNKNOWN),
                entry("H", Attendance.UNKNOWN),
                entry("I", Attendance.UNKNOWN),
                entry("J", Attendance.UNKNOWN),
                entry("K", Attendance.UNKNOWN)
        ));
    }




}
