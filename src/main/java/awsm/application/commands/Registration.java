package awsm.application.commands;

import an.awesome.pipelinr.Command;
import awsm.domain.registration.Email;
import awsm.domain.registration.EmailBlacklist;
import awsm.domain.registration.EmailBlacklistedException;
import awsm.domain.registration.Member;
import awsm.domain.registration.Members;
import awsm.domain.registration.Name;
import awsm.infra.pipeline.ExecutableCommand;
import javax.validation.constraints.NotEmpty;
import org.hashids.Hashids;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

class Registration extends ExecutableCommand<String> {

  @NotEmpty
  private final String email;

  @NotEmpty
  private final String firstName;

  @NotEmpty
  private final String lastName;

  public Registration(String email, String firstName, String lastName) {
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
  }

  @RestController
  static class HttpEntryPoint {

    @PostMapping("/members")
    String accept(@RequestBody Registration command) {
      return command.execute();
    }
  }

  @Component
  static class Handler implements Command.Handler<Registration, String> {

    private final Members members;

    private final EmailBlacklist blacklist;

    private final Hashids hashids;

    Handler(Members members, EmailBlacklist blacklist, Hashids hashids) {
      this.members = members;
      this.blacklist = blacklist;
      this.hashids = hashids;
    }

    @Override
    public String handle(Registration cmd) {
      var email = new Email(cmd.email);
      throwIfBlacklisted(email);

      var name = new Name(cmd.firstName, cmd.lastName);
      var member = new Member(name, email);
      members.save(member);
      return hashids.encode(member.id());
    }

    private void throwIfBlacklisted(Email email) {
      if (blacklist.contains(email)) {
        throw new EmailBlacklistedException(email);
      }
    }

  }

}
