package cs.technion.ac.il.sd.app;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import cs.technion.ac.il.sd.Attendance;
import cs.technion.ac.il.sd.Input;
import cs.technion.ac.il.sd.Output;
import library.graph.Graph;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * PartyApp implementation
 */
public class SimplePartyApp implements PartyApp{

    private final Input input;
    private final Output output;
    private final Graph<String> graph;
    private  Map<String, Attendance> attendance;
    private Configuration configuration;

    @Inject
    public SimplePartyApp(Input input, Output output) {
        this.input = input;
        this.output = output;
        this.graph = new Graph<>();
        this.attendance = new HashMap<>();
    }

    @Override
    public void processFile(File file) {
        loadConfiguration(file)
                .buildGraph()
                .initAttendance()
                .beginListening();
    }

    private SimplePartyApp beginListening() {
        input.listen(this::onAttendanceEvent);
        return this;
    }

    private SimplePartyApp loadConfiguration(File file) {
        configuration = Configuration.fromFile(file);
        return this;
    }

    private SimplePartyApp initAttendance() {
        attendance = configuration.getInvitees()
                .stream()
                .collect(Collectors.toMap(s->s, s->Attendance.UNKNOWN));
        return this;
    }

    private SimplePartyApp buildGraph() {
        Set<String> invitees = configuration.getInvitees();
        invitees.forEach(graph::addNode);
        invitees.forEach(this::addDependenciesToGraph);
        return this;
    }

    private void addDependenciesToGraph(String invitee) {
        configuration.getDependenciesOf(invitee)
                .forEach(dependency -> graph.addEdge(dependency,invitee));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private SimplePartyApp updateAttendance(String name, Optional<Boolean> attendance_) {
        if (attendance_.isPresent()) {
            graph.removeIncomingEdgesOf(name);
        } else {
            if (alreadyDeclaredAttendance(name)){
                graph.addEdgesTo(name, configuration.getDependenciesOf(name));
            }
        }
        attendance.put(name,toAttendance(attendance_));
        return this;
    }

    private boolean alreadyDeclaredAttendance(String name) {
        return attendance.get(name) == Attendance.ATTENDING || attendance.get(name) == Attendance.NOT_ATTENDING;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void onAttendanceEvent(String name, Optional<Boolean> attendance) {
        updateAttendance(name, attendance)
                .computeAll()
                .outputResult();
    }

    private SimplePartyApp outputResult() {
        output.attendance(Maps.newHashMap(attendance));
        return this;
    }

    private SimplePartyApp computeAll() {
        attendance.replaceAll(this::defaultAttendance);
        graph.topologicSort(this::calculateAttendance);
        return this;
    }

    private void calculateAttendance(String invitee) {
        attendance.compute(invitee, this::computeByDependencies);
        if (attendance.get(invitee) == Attendance.NOT_ATTENDING) {
            graph.getAllReachableFrom(invitee).stream()
                    .filter(x -> !alreadyDeclaredAttendance(x))
                    .forEach(x -> attendance.put(x, Attendance.PROBABLY_NOT_ATTENDING));
        }
    }

    private Attendance computeByDependencies(String invitee, Attendance attendance) {
        if (alreadyDeclaredAttendance(invitee))
            return attendance;

        Set<String> dependencies = configuration.getDependenciesOf(invitee);

        if (!dependencies.isEmpty() && dependencies.stream().allMatch(this::attendingOrProbablyAttending))
            return Attendance.PROBABLY_ATTENDING;

        else if (dependencies.stream().anyMatch(this::notAttendingOrProbablyNotAttending))
            return Attendance.PROBABLY_NOT_ATTENDING;

        else
            return Attendance.UNKNOWN;

    }

    private boolean notAttendingOrProbablyNotAttending(String s) {
        return attendance.get(s) == Attendance.NOT_ATTENDING || attendance.get(s) == Attendance.PROBABLY_NOT_ATTENDING;
    }

    private boolean attendingOrProbablyAttending(String s) {
        return attendance.get(s) == Attendance.ATTENDING || attendance.get(s) == Attendance.PROBABLY_ATTENDING;
    }

    private Attendance defaultAttendance(String invitee, Attendance attendance) {
        return alreadyDeclaredAttendance(invitee) ? attendance : Attendance.UNKNOWN;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static  Attendance toAttendance(Optional<Boolean> attendance) {
        if (!attendance.isPresent()) {
            return Attendance.UNKNOWN;
        }
        return attendance.get() ? Attendance.ATTENDING : Attendance.NOT_ATTENDING;
    }


}
