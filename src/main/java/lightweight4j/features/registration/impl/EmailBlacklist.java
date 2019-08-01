package lightweight4j.features.registration.impl;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
class EmailBlacklist {

    private static final List<String> BAD_DOMAINS = List.of("pornhub.com", "rotten.com");

    boolean contains(Email email) {
        return BAD_DOMAINS
                .stream()
                .anyMatch(domain -> email.toString().contains(domain));
    }

}
