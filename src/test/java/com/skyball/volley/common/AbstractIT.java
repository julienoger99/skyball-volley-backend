package com.skyball.volley.common;

import tools.jackson.databind.ObjectMapper;
import com.skyball.volley.championship.persistence.ChampionshipRepository;
import com.skyball.volley.club.domain.Club;
import com.skyball.volley.club.domain.ClubMembership;
import com.skyball.volley.club.domain.ClubMembershipId;
import com.skyball.volley.club.persistence.ClubMembershipRepository;
import com.skyball.volley.club.persistence.ClubRepository;
import com.skyball.volley.match.persistence.MatchRepository;
import com.skyball.volley.team.persistence.TeamMembershipRepository;
import com.skyball.volley.team.persistence.TeamRepository;
import com.skyball.volley.user.domain.AppUser;
import com.skyball.volley.user.domain.Role;
import com.skyball.volley.user.persistence.AppUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootTest
@Import(TestDbConfig.class)
@TestPropertySource(properties = {
        "management.health.mail.enabled=false",
        "JWT_SECRET_KEY=test-jwt-secret-key-for-testing-only-xxxxxxxxxx"
})
public abstract class AbstractIT {

    @Autowired private WebApplicationContext wac;
    @Autowired protected ObjectMapper mapper;
    @Autowired protected AppUserRepository userRepository;
    @Autowired protected ClubRepository clubRepository;
    @Autowired protected ClubMembershipRepository clubMembershipRepository;
    @Autowired protected PasswordEncoder passwordEncoder;
    @Autowired private MatchRepository matchRepository;
    @Autowired private TeamMembershipRepository teamMembershipRepository;
    @Autowired private TeamRepository teamRepository;
    @Autowired private ChampionshipRepository championshipRepository;
    @MockitoBean @SuppressWarnings("unused") JavaMailSender mailSender;

    protected MockMvc mvc;

    @BeforeEach
    void setUpMockMvc() {
        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @AfterEach
    void cleanUp() {
        matchRepository.deleteAll();
        teamMembershipRepository.deleteAll();
        teamRepository.deleteAll();
        championshipRepository.deleteAll();
        clubMembershipRepository.deleteAll();
        clubRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected AppUser savedUser(String username, boolean superAdmin) {
        return userRepository.save(AppUser.builder()
                .username(username)
                .email(username + "@test.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.PLAYER)
                .superAdmin(superAdmin)
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .build());
    }

    protected AppUser savedUnverifiedUser(String username) {
        return userRepository.save(AppUser.builder()
                .username(username)
                .email(username + "@test.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.PLAYER)
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .build());
    }

    protected Club savedClub(String name) {
        return clubRepository.save(Club.builder()
                .name(name)
                .city("Paris")
                .createdAt(LocalDate.now())
                .build());
    }

    protected void addToClub(AppUser user, Club club, MembershipRole role) {
        clubMembershipRepository.save(ClubMembership.builder()
                .id(new ClubMembershipId(user.getId(), club.getId()))
                .user(user)
                .club(club)
                .role(role)
                .build());
    }

    protected static RequestPostProcessor asUser(String username) {
        return SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.subject(username));
    }
}
