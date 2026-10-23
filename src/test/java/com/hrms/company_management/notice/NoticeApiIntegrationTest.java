package com.hrms.company_management.notice;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.company_management.config.ModelMapperConfig;
import com.hrms.company_management.config.NoticeConfig;
import com.hrms.company_management.controller.NoticeAdminController;
import com.hrms.company_management.controller.NoticeController;
import com.hrms.company_management.repository.NoticeRepo;
import com.hrms.company_management.service.NoticeService;
import com.hrms.company_management.utility.JwtRoleExtractor;
import com.hrms.company_management.utility.NoticeAccessGuard;
import com.hrms.company_management.utility.NoticeMapper;
import com.hrms.company_management.utility.TenantRegistry;

/**
 * HTTP -> controller -> service -> JPA -> H2, including the real TenantFilter.
 * The full application context is not used because MultiTenantConfiguration fetches tenant DBs over HTTP at startup.
 */
@WebMvcTest(controllers = {NoticeAdminController.class, NoticeController.class})
@AutoConfigureDataJpa
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({NoticeService.class, NoticeMapper.class, NoticeAccessGuard.class, TenantRegistry.class,
        JwtRoleExtractor.class, ModelMapperConfig.class, NoticeConfig.class})
@ActiveProfiles("test")
class NoticeApiIntegrationTest {

    private static final String ADMIN = TestTokens.bearer("admin-1", "hr-admin");
    private static final String EMPLOYEE = TestTokens.bearer("emp-1", "employee");

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private NoticeRepo noticeRepo;

    @BeforeEach
    void cleanDatabase() {
        noticeRepo.deleteAll();
    }

    private static MockHttpServletRequestBuilder as(MockHttpServletRequestBuilder request, String token) {
        return request.header("X-Tenant-Id", "tomato").header(HttpHeaders.AUTHORIZATION, token);
    }

    private static String body(String extraFields) {
        return "{\"title\":\"Office closed\",\"description\":\"Diwali break\",\"noticeType\":\"GENERAL\"" + extraFields + "}";
    }

    private long create(String extraFields, String expectedStatus) throws Exception {
        String json = mvc.perform(as(post("/admin/notice"), ADMIN)
                        .contentType(MediaType.APPLICATION_JSON).content(body(extraFields)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(expectedStatus))
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(json);
        return node.get("id").asLong();
    }

    @Test
    void securityAndTenantChecks() throws Exception {
        mvc.perform(post("/admin/notice").header("X-Tenant-Id", "tomato")
                        .contentType(MediaType.APPLICATION_JSON).content(body("")))
                .andExpect(status().isUnauthorized());
        mvc.perform(as(post("/admin/notice"), EMPLOYEE).contentType(MediaType.APPLICATION_JSON).content(body("")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/notices").header(HttpHeaders.AUTHORIZATION, EMPLOYEE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("X-Tenant-Id header is required"));
        mvc.perform(as(get("/admin/notice"), EMPLOYEE)).andExpect(status().isForbidden());
    }

    @Test
    void validationErrorsAreReportedPerField() throws Exception {
        mvc.perform(as(post("/admin/notice"), ADMIN).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\" \",\"noticeType\":\"GENERAL\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").value("title is required"))
                .andExpect(jsonPath("$.errors.description").value("description is required"));

        Instant publishAt = Instant.now().plusSeconds(3600);
        mvc.perform(as(post("/admin/notice"), ADMIN).contentType(MediaType.APPLICATION_JSON)
                        .content(body(",\"publishAt\":\"" + publishAt + "\",\"expiresAt\":\"" + publishAt.minusSeconds(60) + "\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("expiresAt must be after publishAt"));

        mvc.perform(as(get("/admin/notice").param("status", "NOPE"), ADMIN)).andExpect(status().isBadRequest());
    }

    @Test
    void fullLifecycle() throws Exception {
        long id = create(",\"saveAsDraft\":true", "DRAFT");

        // Drafts are invisible to employees.
        mvc.perform(as(get("/notices"), EMPLOYEE)).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(as(get("/notices/" + id), EMPLOYEE)).andExpect(status().isNotFound());

        mvc.perform(as(put("/admin/notice/" + id), ADMIN).contentType(MediaType.APPLICATION_JSON)
                        .content(body("").replace("Office closed", "Office closed Monday")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Office closed Monday"))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        mvc.perform(as(post("/admin/notice/" + id + "/publish"), ADMIN))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PUBLISHED"));

        mvc.perform(as(get("/notices"), EMPLOYEE))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].createdBy").doesNotExist());

        mvc.perform(as(get("/notices/" + id), EMPLOYEE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Office closed Monday"));

        // Published notices are frozen: no edit, no delete; archive instead.
        mvc.perform(as(put("/admin/notice/" + id), ADMIN).contentType(MediaType.APPLICATION_JSON).content(body("")))
                .andExpect(status().isConflict());
        mvc.perform(as(delete("/admin/notice/" + id), ADMIN)).andExpect(status().isConflict());
        mvc.perform(as(post("/admin/notice/" + id + "/archive"), ADMIN))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ARCHIVED"));

        mvc.perform(as(get("/notices"), EMPLOYEE)).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(as(get("/admin/notice").param("status", "ARCHIVED"), ADMIN)).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void scheduledNoticeIsHiddenUntilPublishedNow() throws Exception {
        long id = create(",\"publishAt\":\"" + Instant.now().plusSeconds(3600) + "\"", "SCHEDULED");
        mvc.perform(as(get("/notices"), EMPLOYEE)).andExpect(jsonPath("$", hasSize(0)));

        mvc.perform(as(post("/admin/notice/" + id + "/publish"), ADMIN))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
        mvc.perform(as(get("/notices"), EMPLOYEE)).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void onlyDraftsCanBeDeleted() throws Exception {
        long id = create(",\"saveAsDraft\":true", "DRAFT");
        mvc.perform(as(delete("/admin/notice/" + id), ADMIN)).andExpect(status().isNoContent());
        mvc.perform(as(get("/admin/notice/" + id), ADMIN)).andExpect(status().isNotFound());
    }
}
