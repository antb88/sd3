/**
 * Created by Nati on 6/11/2016.
 */

import cs.technion.ac.il.sd.app.Configuration;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

/**
 * Tests for {@link Configuration}
 */
public class ConfigurationTest {


    private Configuration $;


    private Configuration parseFile(String name) {
        $ = Configuration.fromFile(new File(getClass().getResource(name + ".txt").getFile()));
        return $;
    }


    private boolean isInviteeExist(String name) {return $.getInvitees().contains(name);}
    private boolean depends(String who, String onWhom) {
        return isInviteeExist(who) && isInviteeExist(onWhom) && $.getDependenciesOf(who).contains(onWhom);
    }

    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    @Test
    public void smallContainsAllInvitees() {
        parseFile("small");
        Assert.assertEquals(new HashSet<>(Arrays.asList("Jerry", "George", "Elaine"
                , "Kramer", "soup nazi", "Newman", "Forever alone")), $.getInvitees());
    }


    @Test
    public void smallDependenciesAreCorrect() {
        parseFile("small");
        Assert.assertEquals(new HashSet<>(Arrays.asList("George", "Elaine")), $.getDependenciesOf("Jerry"));
        Assert.assertEquals(Collections.EMPTY_SET, $.getDependenciesOf("George"));
        Assert.assertEquals(Collections.EMPTY_SET, $.getDependenciesOf("Elaine"));
        Assert.assertEquals(new HashSet<>(Arrays.asList("soup nazi", "Newman")), $.getDependenciesOf("Kramer"));
        Assert.assertEquals(Collections.EMPTY_SET, $.getDependenciesOf("soup nazi"));
        Assert.assertEquals(Collections.EMPTY_SET, $.getDependenciesOf("Newman"));
        Assert.assertEquals(Collections.EMPTY_SET, $.getDependenciesOf("Forever alone"));

    }


    @Test
    public void complexContainsAllInvitees() {
        parseFile("complex");
        Assert.assertEquals(new HashSet<>(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h")), $.getInvitees());
    }


    @Test
    public void complexDependenciesAreCorrect() {
        parseFile("complex");
        Assert.assertEquals(new HashSet<>(Arrays.asList("c", "e"))
                , $.getDependenciesOf("a"));
        Assert.assertEquals(new HashSet<>(Arrays.asList("e"))
                , $.getDependenciesOf("b"));
        Assert.assertEquals(new HashSet<>(Arrays.asList("a", "e"))
                , $.getDependenciesOf("c"));
        Assert.assertEquals(new HashSet<>(Arrays.asList("a", "b", "c", "f", "g", "h"))
                , $.getDependenciesOf("d"));
        Assert.assertEquals(Collections.EMPTY_SET
                , $.getDependenciesOf("e"));
        Assert.assertEquals(Collections.EMPTY_SET
                , $.getDependenciesOf("f"));
        Assert.assertEquals(new HashSet<>(Arrays.asList("d"))
                , $.getDependenciesOf("g"));
        Assert.assertEquals(Collections.EMPTY_SET
                , $.getDependenciesOf("h"));
    }

    @Test
    public void emptyContainsNoInvitees() {
        parseFile("empty");
        Assert.assertEquals($.getInvitees(), Collections.EMPTY_SET);
    }


}

