package cs.technion.ac.il.sd.app;

import com.google.inject.AbstractModule;

public class PartyModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(PartyApp.class).to(FakeParty.class);
  }
}
