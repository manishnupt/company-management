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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.company_management.config.ModelMapperConfig;
import com.hrms.company_management.config.NoticeConfig;
import com.hrms.company_management.controller.NoticeController;
import com.hrms.company_management.repository.NoticeRepo;
import com.hrms.company_management.service.NoticeService;
import com.hrms.company_management.utility.NoticeMapper;
import com.hrms.company_management.utility.TenantRegistry;

/**
 * HTTP -> controller -> service -> JPA -> H2.
 * The full application context is not used because MultiTenantConfiguration fetches tenant DBs over HTTP at startup.
 */
@WebMvcTest(controllers = NoticeController.class)
@AutoConfigureDataJpa
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({NoticeService.class, NoticeMapper.class, TenantRegistry.class,
        ModelMapperConfig.class, NoticeConfig.class})
@ActiveProfiles("test")
class NoticeApiIntegrationTest {

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

    private static String body(String extraFields) {
        return "{\"title\":\"Office closed\",\"description\":\"Diwali break\",\"noticeType\":\"GENERAL\"" + extraFields + "}";
    }

    private long create(String extraFields, String expectedStatus) throws Exception {
        String json = mvc.perform(post("/admin/notice")
                        .contentType(MediaType.APPLICATION_JSON).content(body(extraFields)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(expectedStatus))
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(json);
        return node.get("id").asLong();
    }

    @Test
    void endpointsNeedNoTokenOrTenantHeader() throws Exception {
        mvc.perform(post("/admin/notice").contentType(MediaType.APPLICATION_JSON).content(body("")))
                .andExpect(status().isCreated());
        mvc.perform(get("/notices")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void validationErrorsAreReportedPerField() throws Exception {
        mvc.perform(post("/admin/notice").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\" \",\"noticeType\":\"GENERAL\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").value("title is required"))
                .andExpect(jsonPath("$.errors.description").value("description is required"));

        Instant publishAt = Instant.now().plusSeconds(3600);
        mvc.perform(post("/admin/notice").contentType(MediaType.APPLICATION_JSON)
                        .content(body(",\"publishAt\":\"" + publishAt + "\",\"expiresAt\":\"" + publishAt.minusSeconds(60) + "\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("expiresAt must be after publishAt"));

        mvc.perform(get("/admin/notice").param("status", "NOPE")).andExpect(status().isBadRequest());
    }

    @Test
    void fullLifecycle() throws Exception {
        long id = create(",\"saveAsDraft\":true", "DRAFT");

        // Drafts are invisible to employees.
        mvc.perform(get("/notices")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/notices/" + id)).andExpect(status().isNotFound());

        mvc.perform(put("/admin/notice/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(body("").replace("Office closed", "Office closed Monday")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Office closed Monday"))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        mvc.perform(post("/admin/notice/" + id + "/publish"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PUBLISHED"));

        mvc.perform(get("/notices"))
                .andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(get("/notices/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Office closed Monday"));

        // Published notices are frozen: no edit, no delete; archive instead.
        mvc.perform(put("/admin/notice/" + id).contentType(MediaType.APPLICATION_JSON).content(body("")))
                .andExpect(status().isConflict());
        mvc.perform(delete("/admin/notice/" + id)).andExpect(status().isConflict());
        mvc.perform(post("/admin/notice/" + id + "/archive"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ARCHIVED"));

        mvc.perform(get("/notices")).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/admin/notice").param("status", "ARCHIVED")).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void scheduledNoticeIsHiddenUntilPublishedNow() throws Exception {
        long id = create(",\"publishAt\":\"" + Instant.now().plusSeconds(3600) + "\"", "SCHEDULED");
        mvc.perform(get("/notices")).andExpect(jsonPath("$", hasSize(0)));

        mvc.perform(post("/admin/notice/" + id + "/publish"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
        mvc.perform(get("/notices")).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void onlyDraftsCanBeDeleted() throws Exception {
        long id = create(",\"saveAsDraft\":true", "DRAFT");
        mvc.perform(delete("/admin/notice/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/admin/notice/" + id)).andExpect(status().isNotFound());
    }
}
