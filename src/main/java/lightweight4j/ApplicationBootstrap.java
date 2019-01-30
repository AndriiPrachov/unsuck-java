package lightweight4j;

import lightweight4j.lib.commands.Eventually;
import lightweight4j.lib.commands.Future;
import lightweight4j.lib.commands.Now;
import lightweight4j.app.membership.BecomeAMember;
import lightweight4j.app.permissions.CreatePermission;
import lightweight4j.app.permissions.GrantPermission;
import lightweight4j.app.permissions.ListPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.function.Consumer;

@SpringBootApplication
public class ApplicationBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ApplicationBootstrap.class);

    private final Now now;

    private final Future async;

    private final Eventually eventually;

    public ApplicationBootstrap(Now now, Future async, Eventually eventually) {
        this.now = now;
        this.async = async;
        this.eventually = eventually;
    }

    @Override
    public void run(String... args) {

        var createPermission = new CreatePermission("Superpowers");
        var permissionId = createPermission.execute(now);

        var becomeAMember = new BecomeAMember("alan@devternity.com");
        var memberId = becomeAMember.execute(now);

        var grantPermission = new GrantPermission(memberId, permissionId);
        grantPermission.execute(now);

        var listPermissions = new ListPermissions(memberId);
        listPermissions.execute(async).thenAccept(logPermissions());

    }

    private Consumer<ListPermissions.PermissionNames> logPermissions() {
        return permissionNames -> log.info("Alan has permissions {}", permissionNames);
    }

    public static void main(String[] args) {
        SpringApplication.run(ApplicationBootstrap.class, args);
    }
}