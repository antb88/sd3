import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import cs.technion.ac.il.sd.Attendance;
import cs.technion.ac.il.sd.Input;
import cs.technion.ac.il.sd.Output;
import cs.technion.ac.il.sd.app.PartyApp;
import cs.technion.ac.il.sd.app.PartyModule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.mockito.Mockito;

import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

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

    private Map<String, Attendance> mapFromList(List<Map.Entry<String, Attendance>> entryList) {
        Map<String, Attendance> map = new HashMap<>();
        for (Map.Entry<String, Attendance> e : entryList)
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
        input.publish("Tyrion Lannister", null);
        verify(output).attendance(map(
                entry("Tyrion Lannister", Attendance.UNKNOWN),
                entry("Daenerys Targaryen", Attendance.UNKNOWN),
                entry("Cersei Lannister", Attendance.UNKNOWN),
                entry("Jaime Lannister", Attendance.UNKNOWN),
                entry("Tommen Baratheon", Attendance.UNKNOWN),
                entry("Khal Drogo", Attendance.UNKNOWN),
                entry("Ramsay Bolton", Attendance.UNKNOWN),
                entry("Catelyn Stark", Attendance.UNKNOWN),
                entry("Missandei", Attendance.UNKNOWN),
                entry("Stannis Baratheon", Attendance.UNKNOWN),
                entry("Varys", Attendance.UNKNOWN)
        ));
    }

    @Test
    public void cyclicDependencyProbablyYes() {
        processFile("cyclic");
        input.publish("Missandei", true);
        input.publish("Daenerys Targaryen", true);
        verify(output).attendance(map(
                entry("Tyrion Lannister", Attendance.UNKNOWN),
                entry("Daenerys Targaryen", Attendance.ATTENDING),
                entry("Cersei Lannister", Attendance.PROBABLY_ATTENDING),
                entry("Jaime Lannister", Attendance.PROBABLY_ATTENDING),
                entry("Tommen Baratheon", Attendance.PROBABLY_ATTENDING),
                entry("Khal Drogo", Attendance.UNKNOWN),
                entry("Ramsay Bolton", Attendance.UNKNOWN),
                entry("Catelyn Stark", Attendance.PROBABLY_ATTENDING),
                entry("Missandei", Attendance.ATTENDING),
                entry("Stannis Baratheon", Attendance.PROBABLY_ATTENDING),
                entry("Varys", Attendance.UNKNOWN)
        ));
    }

    @Test
    public void cyclicDependencyProbablyNo() {
        processFile("cyclic");
        input.publish("Missandei", false);
        input.publish("Daenerys Targaryen", false);
        verify(output).attendance(map(
                entry("Tyrion Lannister", Attendance.UNKNOWN),
                entry("Daenerys Targaryen", Attendance.NOT_ATTENDING),
                entry("Cersei Lannister", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Jaime Lannister", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Tommen Baratheon", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Khal Drogo", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Ramsay Bolton", Attendance.UNKNOWN),
                entry("Catelyn Stark", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Missandei", Attendance.NOT_ATTENDING),
                entry("Stannis Baratheon", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Varys", Attendance.UNKNOWN)
        ));
    }

    @Test
    public void cyclicDependencyUnknown() {
        processFile("cyclic");
        input.publish("Tyrion Lannister", true);
        input.publish("Ramsay Bolton", true);
        verify(output).attendance(map(
                entry("Tyrion Lannister", Attendance.ATTENDING),
                entry("Daenerys Targaryen", Attendance.UNKNOWN),
                entry("Cersei Lannister", Attendance.UNKNOWN),
                entry("Jaime Lannister", Attendance.UNKNOWN),
                entry("Tommen Baratheon", Attendance.UNKNOWN),
                entry("Khal Drogo", Attendance.UNKNOWN),
                entry("Ramsay Bolton", Attendance.ATTENDING),
                entry("Catelyn Stark", Attendance.UNKNOWN),
                entry("Missandei", Attendance.UNKNOWN),
                entry("Stannis Baratheon", Attendance.UNKNOWN),
                entry("Varys", Attendance.UNKNOWN)
        ));
    }

    @Test
    public void cyclicAllDefinite() {
        processFile("cyclic");
        input.publish("Tyrion Lannister", true);
        input.publish("Daenerys Targaryen", false);
        input.publish("Cersei Lannister", true);
        input.publish("Jaime Lannister", false);
        input.publish("Tommen Baratheon", true);
        input.publish("Khal Drogo", false);
        input.publish("Ramsay Bolton", false);
        input.publish("Catelyn Stark", true);
        input.publish("Missandei", false);
        input.publish("Stannis Baratheon", true);
        input.publish("Varys", true);
        verify(output).attendance(map(
                entry("Tyrion Lannister", Attendance.ATTENDING),
                entry("Daenerys Targaryen", Attendance.NOT_ATTENDING),
                entry("Cersei Lannister", Attendance.ATTENDING),
                entry("Jaime Lannister", Attendance.NOT_ATTENDING),
                entry("Tommen Baratheon", Attendance.ATTENDING),
                entry("Khal Drogo", Attendance.NOT_ATTENDING),
                entry("Ramsay Bolton", Attendance.NOT_ATTENDING),
                entry("Catelyn Stark", Attendance.ATTENDING),
                entry("Missandei", Attendance.NOT_ATTENDING),
                entry("Stannis Baratheon", Attendance.ATTENDING),
                entry("Varys", Attendance.ATTENDING)
        ));
    }

    @Test
    public void cyclicProbablyYes() {
        processFile("cyclic");
        input.publish("Tyrion Lannister", false);
        input.publish("Tommen Baratheon", true);
        input.publish("Varys", true);
        verify(output).attendance(map(
                entry("Tyrion Lannister", Attendance.NOT_ATTENDING),
                entry("Daenerys Targaryen", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Cersei Lannister", Attendance.PROBABLY_ATTENDING),
                entry("Jaime Lannister", Attendance.PROBABLY_ATTENDING),
                entry("Tommen Baratheon", Attendance.ATTENDING),
                entry("Khal Drogo", Attendance.PROBABLY_ATTENDING),
                entry("Ramsay Bolton", Attendance.PROBABLY_ATTENDING),
                entry("Catelyn Stark", Attendance.UNKNOWN),
                entry("Missandei", Attendance.UNKNOWN),
                entry("Stannis Baratheon", Attendance.UNKNOWN),
                entry("Varys", Attendance.ATTENDING)
        ));
    }

    @Test
    public void cyclicProbablyNo() {
        processFile("cyclic");
        input.publish("Tyrion Lannister", false);
        input.publish("Varys", true);
        input.publish("Stannis Baratheon", false);
        verify(output).attendance(map(
                entry("Tyrion Lannister", Attendance.NOT_ATTENDING),
                entry("Daenerys Targaryen", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Cersei Lannister", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Jaime Lannister", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Tommen Baratheon", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Khal Drogo", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Ramsay Bolton", Attendance.PROBABLY_ATTENDING),
                entry("Catelyn Stark", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Missandei", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Stannis Baratheon", Attendance.NOT_ATTENDING),
                entry("Varys", Attendance.ATTENDING)
        ));
    }

    @Test
    public void cyclicRemainUnknown() {
        processFile("cyclic");
        input.publish("Tyrion Lannister", true);
        input.publish("Varys", false);
        input.publish("Catelyn Stark", false);
        verify(output).attendance(map(
                entry("Tyrion Lannister", Attendance.ATTENDING),
                entry("Daenerys Targaryen", Attendance.UNKNOWN),
                entry("Cersei Lannister", Attendance.UNKNOWN),
                entry("Jaime Lannister", Attendance.UNKNOWN),
                entry("Tommen Baratheon", Attendance.UNKNOWN),
                entry("Khal Drogo", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Ramsay Bolton", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Catelyn Stark", Attendance.NOT_ATTENDING),
                entry("Missandei", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Stannis Baratheon", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Varys", Attendance.NOT_ATTENDING)
        ));
    }

    @Test
    public void cyclicChangeYesToNo() {
        processFile("cyclic");
        input.publish("Daenerys Targaryen", true);
        input.publish("Missandei", true);
        verify(output).attendance(map(
                entry("Tyrion Lannister", Attendance.UNKNOWN),
                entry("Daenerys Targaryen", Attendance.ATTENDING),
                entry("Cersei Lannister", Attendance.PROBABLY_ATTENDING),
                entry("Jaime Lannister", Attendance.PROBABLY_ATTENDING),
                entry("Tommen Baratheon", Attendance.PROBABLY_ATTENDING),
                entry("Khal Drogo", Attendance.UNKNOWN),
                entry("Ramsay Bolton", Attendance.UNKNOWN),
                entry("Catelyn Stark", Attendance.PROBABLY_ATTENDING),
                entry("Missandei", Attendance.ATTENDING),
                entry("Stannis Baratheon", Attendance.PROBABLY_ATTENDING),
                entry("Varys", Attendance.UNKNOWN)
        ));

        input.publish("Daenerys Targaryen", false);
        verify(output).attendance(map(
                entry("Tyrion Lannister", Attendance.UNKNOWN),
                entry("Daenerys Targaryen", Attendance.NOT_ATTENDING),
                entry("Cersei Lannister", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Jaime Lannister", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Tommen Baratheon", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Khal Drogo", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Ramsay Bolton", Attendance.UNKNOWN),
                entry("Catelyn Stark", Attendance.PROBABLY_ATTENDING),
                entry("Missandei", Attendance.ATTENDING),
                entry("Stannis Baratheon", Attendance.PROBABLY_ATTENDING),
                entry("Varys", Attendance.UNKNOWN)
        ));

        input.publish("Missandei", false);
        verify(output).attendance(map(
                entry("Tyrion Lannister", Attendance.UNKNOWN),
                entry("Daenerys Targaryen", Attendance.NOT_ATTENDING),
                entry("Cersei Lannister", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Jaime Lannister", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Tommen Baratheon", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Khal Drogo", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Ramsay Bolton", Attendance.UNKNOWN),
                entry("Catelyn Stark", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Missandei", Attendance.NOT_ATTENDING),
                entry("Stannis Baratheon", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Varys", Attendance.UNKNOWN)
        ));


    }

    @Test
    public void cyclicChangeNoToYes() {
        processFile("cyclic");
        input.publish("Tyrion Lannister", false);
        input.publish("Varys", true);
        input.publish("Stannis Baratheon", false);
        verify(output).attendance(map(
                entry("Tyrion Lannister", Attendance.NOT_ATTENDING),
                entry("Daenerys Targaryen", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Cersei Lannister", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Jaime Lannister", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Tommen Baratheon", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Khal Drogo", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Ramsay Bolton", Attendance.PROBABLY_ATTENDING),
                entry("Catelyn Stark", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Missandei", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Stannis Baratheon", Attendance.NOT_ATTENDING),
                entry("Varys", Attendance.ATTENDING)
        ));

        input.publish("Stannis Baratheon", true);
        verify(output).attendance(map(
                entry("Tyrion Lannister", Attendance.NOT_ATTENDING),
                entry("Daenerys Targaryen", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Cersei Lannister", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Jaime Lannister", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Tommen Baratheon", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Khal Drogo", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Ramsay Bolton", Attendance.PROBABLY_ATTENDING),
                entry("Catelyn Stark", Attendance.PROBABLY_ATTENDING),
                entry("Missandei", Attendance.PROBABLY_ATTENDING),
                entry("Stannis Baratheon", Attendance.ATTENDING),
                entry("Varys", Attendance.ATTENDING)
        ));

        input.publish("Tyrion Lannister", true);
        verify(output).attendance(map(
                entry("Tyrion Lannister", Attendance.ATTENDING),
                entry("Daenerys Targaryen", Attendance.UNKNOWN),
                entry("Cersei Lannister", Attendance.UNKNOWN),
                entry("Jaime Lannister", Attendance.UNKNOWN),
                entry("Tommen Baratheon", Attendance.UNKNOWN),
                entry("Khal Drogo", Attendance.UNKNOWN),
                entry("Ramsay Bolton", Attendance.PROBABLY_ATTENDING),
                entry("Catelyn Stark", Attendance.PROBABLY_ATTENDING),
                entry("Missandei", Attendance.PROBABLY_ATTENDING),
                entry("Stannis Baratheon", Attendance.ATTENDING),
                entry("Varys", Attendance.ATTENDING)
        ));
    }

    @Test
    public void cyclicChangeYesToUnknown() {
        processFile("cyclic");
        input.publish("Tyrion Lannister", true);
        input.publish("Tommen Baratheon", true);
        input.publish("Varys", true);
        verify(output).attendance(map(
                entry("Tyrion Lannister", Attendance.ATTENDING),
                entry("Daenerys Targaryen", Attendance.PROBABLY_ATTENDING),
                entry("Cersei Lannister", Attendance.PROBABLY_ATTENDING),
                entry("Jaime Lannister", Attendance.PROBABLY_ATTENDING),
                entry("Tommen Baratheon", Attendance.ATTENDING),
                entry("Khal Drogo", Attendance.PROBABLY_ATTENDING),
                entry("Ramsay Bolton", Attendance.PROBABLY_ATTENDING),
                entry("Catelyn Stark", Attendance.UNKNOWN),
                entry("Missandei", Attendance.UNKNOWN),
                entry("Stannis Baratheon", Attendance.UNKNOWN),
                entry("Varys", Attendance.ATTENDING)
        ));

        input.publish("Tyrion Lannister", null);
        verify(output).attendance(map(
                entry("Tyrion Lannister", Attendance.UNKNOWN),
                entry("Daenerys Targaryen", Attendance.UNKNOWN),
                entry("Cersei Lannister", Attendance.PROBABLY_ATTENDING),
                entry("Jaime Lannister", Attendance.PROBABLY_ATTENDING),
                entry("Tommen Baratheon", Attendance.ATTENDING),
                entry("Khal Drogo", Attendance.PROBABLY_ATTENDING),
                entry("Ramsay Bolton", Attendance.PROBABLY_ATTENDING),
                entry("Catelyn Stark", Attendance.UNKNOWN),
                entry("Missandei", Attendance.UNKNOWN),
                entry("Stannis Baratheon", Attendance.UNKNOWN),
                entry("Varys", Attendance.ATTENDING)
        ));

        input.publish("Tommen Baratheon", null);
        verify(output).attendance(map(
                entry("Tyrion Lannister", Attendance.UNKNOWN),
                entry("Daenerys Targaryen", Attendance.UNKNOWN),
                entry("Cersei Lannister", Attendance.UNKNOWN),
                entry("Jaime Lannister", Attendance.UNKNOWN),
                entry("Tommen Baratheon", Attendance.UNKNOWN),
                entry("Khal Drogo", Attendance.UNKNOWN),
                entry("Ramsay Bolton", Attendance.PROBABLY_ATTENDING),
                entry("Catelyn Stark", Attendance.UNKNOWN),
                entry("Missandei", Attendance.UNKNOWN),
                entry("Stannis Baratheon", Attendance.UNKNOWN),
                entry("Varys", Attendance.ATTENDING)
        ));

        input.publish("Varys", null);
        verify(output).attendance(map(
                entry("Tyrion Lannister", Attendance.UNKNOWN),
                entry("Daenerys Targaryen", Attendance.UNKNOWN),
                entry("Cersei Lannister", Attendance.UNKNOWN),
                entry("Jaime Lannister", Attendance.UNKNOWN),
                entry("Tommen Baratheon", Attendance.UNKNOWN),
                entry("Khal Drogo", Attendance.UNKNOWN),
                entry("Ramsay Bolton", Attendance.UNKNOWN),
                entry("Catelyn Stark", Attendance.UNKNOWN),
                entry("Missandei", Attendance.UNKNOWN),
                entry("Stannis Baratheon", Attendance.UNKNOWN),
                entry("Varys", Attendance.UNKNOWN)
        ));


    }

    @Test
    public void cyclicChangeNoToUnknown() {
        processFile("cyclic");
        input.publish("Daenerys Targaryen", false);
        input.publish("Missandei", false);
        verify(output).attendance(map(
                entry("Tyrion Lannister", Attendance.UNKNOWN),
                entry("Daenerys Targaryen", Attendance.NOT_ATTENDING),
                entry("Cersei Lannister", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Jaime Lannister", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Tommen Baratheon", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Khal Drogo", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Ramsay Bolton", Attendance.UNKNOWN),
                entry("Catelyn Stark", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Missandei", Attendance.NOT_ATTENDING),
                entry("Stannis Baratheon", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Varys", Attendance.UNKNOWN)
        ));

        input.publish("Daenerys Targaryen", null);
        verify(output).attendance(map(
                entry("Tyrion Lannister", Attendance.UNKNOWN),
                entry("Daenerys Targaryen", Attendance.UNKNOWN),
                entry("Cersei Lannister", Attendance.UNKNOWN),
                entry("Jaime Lannister", Attendance.UNKNOWN),
                entry("Tommen Baratheon", Attendance.UNKNOWN),
                entry("Khal Drogo", Attendance.UNKNOWN),
                entry("Ramsay Bolton", Attendance.UNKNOWN),
                entry("Catelyn Stark", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Missandei", Attendance.NOT_ATTENDING),
                entry("Stannis Baratheon", Attendance.PROBABLY_NOT_ATTENDING),
                entry("Varys", Attendance.UNKNOWN)
        ));

        input.publish("Missandei", null);
        verify(output).attendance(map(
                entry("Tyrion Lannister", Attendance.UNKNOWN),
                entry("Daenerys Targaryen", Attendance.UNKNOWN),
                entry("Cersei Lannister", Attendance.UNKNOWN),
                entry("Jaime Lannister", Attendance.UNKNOWN),
                entry("Tommen Baratheon", Attendance.UNKNOWN),
                entry("Khal Drogo", Attendance.UNKNOWN),
                entry("Ramsay Bolton", Attendance.UNKNOWN),
                entry("Catelyn Stark", Attendance.UNKNOWN),
                entry("Missandei", Attendance.UNKNOWN),
                entry("Stannis Baratheon", Attendance.UNKNOWN),
                entry("Varys", Attendance.UNKNOWN)
        ));
    }

    @Test
    public void largeInitAllToUnknown() {
        processFile("large");
        input.publish("0", null);
        List<Map.Entry<String, Attendance>> entries = new ArrayList<>();
        for(int i=0 ; i<500; i++)
        {
            entries.add(entry(String.valueOf(i), Attendance.UNKNOWN));
        }
        verify(output).attendance(mapFromList(entries));

    }

    @Test
    public void largeDependencyIsCorrect() {
        processFile("large");
        input.publish("0", true);
        List<Map.Entry<String, Attendance>> entries = new ArrayList<>();
        entries.add(entry(String.valueOf(0), Attendance.ATTENDING));
        for(int i=1 ; i<500; i++)
        {
            entries.add(entry(String.valueOf(i), Attendance.PROBABLY_ATTENDING));
        }
        verify(output).attendance(mapFromList(entries));

        input.publish("0", null);
        input.publish("1", false);
        input.publish("2", true);
        entries.clear();
        entries.add(entry(String.valueOf(0), Attendance.UNKNOWN));
        entries.add(entry(String.valueOf(1), Attendance.NOT_ATTENDING));
        entries.add(entry(String.valueOf(2), Attendance.ATTENDING));
        for(int i=3 ; i<500; i++)
        {
            switch (i%3)
            {
                case 0:
                    entries.add(entry(String.valueOf(i), Attendance.UNKNOWN));
                    break;
                case 1:
                    entries.add(entry(String.valueOf(i), Attendance.PROBABLY_NOT_ATTENDING));
                    break;
                case 2:
                    entries.add(entry(String.valueOf(i), Attendance.PROBABLY_ATTENDING));
                    break;
            }
        }
        verify(output).attendance(mapFromList(entries));
    }

    @Test
    public void largeRootChange() {
        processFile("large");
        input.publish("0", true);
        List<Map.Entry<String, Attendance>> entries = new ArrayList<>();
        entries.add(entry(String.valueOf(0), Attendance.ATTENDING));
        for(int i=1 ; i<500; i++)
        {
            entries.add(entry(String.valueOf(i), Attendance.PROBABLY_ATTENDING));
        }
        verify(output).attendance(mapFromList(entries));

        input.publish("0", false);
        entries.clear();
        entries.add(entry(String.valueOf(0), Attendance.NOT_ATTENDING));
        for(int i=1 ; i<500; i++)
        {
            entries.add(entry(String.valueOf(i), Attendance.PROBABLY_NOT_ATTENDING));
        }
        verify(output).attendance(mapFromList(entries));

        input.publish("0", null);
        entries.clear();
        for(int i=0 ; i<500; i++)
        {
            entries.add(entry(String.valueOf(i), Attendance.UNKNOWN));
        }
        verify(output).attendance(mapFromList(entries));

    }

    @Test
    public void largeInnerVertexChange() {
        processFile("large");
        List<Map.Entry<String, Attendance>> entries = new ArrayList<>();
        input.publish("1", false);
        input.publish("2", true);
        entries.add(entry(String.valueOf(0), Attendance.UNKNOWN));
        entries.add(entry(String.valueOf(1), Attendance.NOT_ATTENDING));
        entries.add(entry(String.valueOf(2), Attendance.ATTENDING));
        for(int i=3 ; i<500; i++)
        {
            switch (i%3)
            {
                case 0:
                    entries.add(entry(String.valueOf(i), Attendance.UNKNOWN));
                    break;
                case 1:
                    entries.add(entry(String.valueOf(i), Attendance.PROBABLY_NOT_ATTENDING));
                    break;
                case 2:
                    entries.add(entry(String.valueOf(i), Attendance.PROBABLY_ATTENDING));
                    break;
            }
        }
        verify(output).attendance(mapFromList(entries));

        input.publish("3", false);
        input.publish("1", true);
        input.publish("2", null);
        entries.clear();
        entries.add(entry(String.valueOf(0), Attendance.UNKNOWN));
        entries.add(entry(String.valueOf(1), Attendance.ATTENDING));
        entries.add(entry(String.valueOf(2), Attendance.UNKNOWN));
        entries.add(entry(String.valueOf(3), Attendance.NOT_ATTENDING));
        for(int i=4 ; i<500; i++)
        {
            switch (i%3)
            {
                case 0:
                    entries.add(entry(String.valueOf(i), Attendance.PROBABLY_NOT_ATTENDING));
                    break;
                case 1:
                    entries.add(entry(String.valueOf(i), Attendance.PROBABLY_ATTENDING));
                    break;
                case 2:
                    entries.add(entry(String.valueOf(i), Attendance.UNKNOWN));
                    break;
            }
        }
        verify(output).attendance(mapFromList(entries));
    }
}
