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
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.function.BiConsumer;

import static org.mockito.Mockito.verify;

public class ExampleTest {
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
    return new SimpleEntry<>(name, a);
  }

  @Test
  public void testSimple() {
    processFile("simple");
    input.publish("Jerry", null);
    verify(output).attendance(map(entry("Jerry", Attendance.UNKNOWN)));
  }

  @Test
  public void dependencyAttending() {
    processFile("dependency1");
    input.publish("George", true);
    input.publish("Kramer", true);
    input.publish("Elaine", true);
    verify(output).attendance(map(entry("Jerry", Attendance.PROBABLY_ATTENDING),
        entry("George", Attendance.ATTENDING),
        entry("Elaine", Attendance.ATTENDING),
        entry("Kramer", Attendance.ATTENDING)));
  }

  @Test
  public void dependencyNotAttending() {
    processFile("dependency2");
    input.publish("George", false);
    input.publish("Elaine", true);
    verify(output).attendance(map(entry("Jerry", Attendance.PROBABLY_NOT_ATTENDING),
        entry("George", Attendance.NOT_ATTENDING),
        entry("Elaine", Attendance.ATTENDING),
        entry("Kramer", Attendance.UNKNOWN)));
  }

  @Test
  public void change() {
    processFile("change");
    InOrder inOrder = Mockito.inOrder(output);
    input.publish("Freddie", true);
    inOrder.verify(output).attendance(map(entry("Freddie", Attendance.ATTENDING),
        entry("Brian", Attendance.PROBABLY_ATTENDING)));
    input.publish("Freddie", false);
    inOrder.verify(output).attendance(map(entry("Freddie", Attendance.NOT_ATTENDING),
        entry("Brian", Attendance.PROBABLY_NOT_ATTENDING)));
    input.publish("Freddie", null);
    inOrder.verify(output).attendance(map(entry("Freddie", Attendance.UNKNOWN),
        entry("Brian", Attendance.UNKNOWN)));
  }
}
