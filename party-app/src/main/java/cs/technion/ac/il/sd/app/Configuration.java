package cs.technion.ac.il.sd.app;

import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Nati on 6/11/2016.
 */
public class Configuration {

    private HashSet<String> invitees;
    private HashMap<String, Set<String>> nameToDepNames;

    private Configuration() {
        this.invitees = new HashSet<>();
        this.nameToDepNames = new HashMap<>();

    }

    public static Configuration fromFile(File file) {

        Configuration c = new Configuration();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.lines().forEach(l -> {
                String trm = l.trim();
                if (!trm.equals(""))
                    c.parseLine(trm);
            });
        } catch (IOException e) {
            throw new AssertionError();
        }
        return c;
    }

    private void parseLine(String line) {
        String[] args = line.split(",");
        String name = args[0].trim();
        Set<String> deps = new HashSet<>(args.length > 1 ?
                Lists.newArrayList(Arrays.copyOfRange(args, 1, args.length)).stream().map(String::trim).collect(Collectors.toList()) : new ArrayList<>());
        invitees.add(name);

        for (String d : deps) {
            if(!d.isEmpty())
            {
                invitees.add(d);
                nameToDepNames.putIfAbsent(d, new HashSet<>());
            }
        }
        nameToDepNames.put(name, deps);
    }

    public Set<String> getInvitees() {
        return invitees;
    }

    public Set<String> getDependenciesOf(String name) {
        return nameToDepNames.get(name);
    }
}
