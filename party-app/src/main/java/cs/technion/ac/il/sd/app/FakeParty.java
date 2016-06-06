package cs.technion.ac.il.sd.app;

import com.google.inject.Inject;
import cs.technion.ac.il.sd.Attendance;
import cs.technion.ac.il.sd.Input;
import cs.technion.ac.il.sd.Output;

import java.io.File;
import java.util.HashMap;

public class FakeParty implements PartyApp {
  private final Input input;
  private final Output output;

  @Inject
  public FakeParty(Input input, Output output) {
    this.input = input;
    this.output = output;
  }

  @Override
  public void processFile(File file) {
    switch (file.getName()) {
      case "simple.txt":
        output.attendance(new HashMap<String, Attendance>() {{ put("Jerry", Attendance.UNKNOWN);}});
        break;
      case "dependency1.txt":
        output.attendance(new HashMap<String, Attendance>() {{
          put("Jerry", Attendance.PROBABLY_ATTENDING);
          put("Kramer", Attendance.ATTENDING);
          put("George", Attendance.ATTENDING);
          put("Elaine", Attendance.ATTENDING);
        }});
        break;
      case "dependency2.txt":
        output.attendance(new HashMap<String, Attendance>() {{
          put("Jerry", Attendance.PROBABLY_NOT_ATTENDING);
          put("Kramer", Attendance.UNKNOWN);
          put("George", Attendance.NOT_ATTENDING);
          put("Elaine", Attendance.ATTENDING);
        }});
        break;
      case "change.txt":
        output.attendance(new HashMap<String, Attendance>() {{
          put("Freddie", Attendance.ATTENDING);
          put("Brian", Attendance.PROBABLY_ATTENDING);
        }});
        output.attendance(new HashMap<String, Attendance>() {{
          put("Freddie", Attendance.NOT_ATTENDING);
          put("Brian", Attendance.PROBABLY_NOT_ATTENDING);
        }});
        output.attendance(new HashMap<String, Attendance>() {{
          put("Freddie", Attendance.UNKNOWN);
          put("Brian", Attendance.UNKNOWN);
        }});
        break;
      default:
        throw new UnsupportedOperationException("http://i.imgflip.com/112boa.jpg");
    }
  }
}
