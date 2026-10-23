package com.allst.dynamic.scenario;

import com.allst.dynamic.DynamicApplication;
import com.allst.dynamic.controller.CityController;
import com.allst.dynamic.scenario.common.RequestRouteCleanupFilter;
import com.allst.dynamic.scenario.common.ScenarioHeaders;
import com.allst.dynamic.scenario.common.DataSourceKeys;
import com.allst.dynamic.scenario.gray.FeatureService;
import com.allst.dynamic.scenario.gray.GrayPolicyCommand;
import com.allst.dynamic.scenario.shard.ShardDirectory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import com.allst.dynamic.scenario.common.RequestRouteCleanupFilter;
import com.allst.dynamic.scenario.tenant.TenantContextFilter;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = DynamicApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
class ScenarioFlowTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RequestRouteCleanupFilter requestRouteCleanupFilter;

    @Autowired
    private TenantContextFilter tenantContextFilter;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CityController cityController;

    @Autowired
    private FeatureService featureService;

    @Autowired
    private ShardDirectory shardDirectory;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(requestRouteCleanupFilter, tenantContextFilter)
                .build();
    }

    @Test
    void catalogAndLegacyControllerStayAvailable() throws Exception {
        assertNotNull(cityController);
        mockMvc.perform(get("/scenario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()").value(6));
        mockMvc.perform(get("/scenario/datasources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dataSources").isArray())
                .andExpect(jsonPath("$.data.groups[?(@ == 'rwSlave')]").exists());
    }

    @Test
    void readWriteSplitsAndSticksToMasterAfterWrite() throws Exception {
        String title = "only-on-master";
        mockMvc.perform(post("/scenario/rw/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(article(title, "ada"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dataSource").value("rwMaster"))
                .andExpect(jsonPath("$.data.database").value(org.hamcrest.Matchers.containsStringIgnoringCase("rw_master")));

        mockMvc.perform(get("/scenario/rw/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dataSource").value(org.hamcrest.Matchers.startsWith("rwSlave_")))
                .andExpect(jsonPath("$.data.records[?(@.title == '" + title + "')]").doesNotExist());

        mockMvc.perform(get("/scenario/rw/articles").header(ScenarioHeaders.FORCE_MASTER, "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dataSource").value("rwMaster"))
                .andExpect(jsonPath("$.data.records[?(@.title == '" + title + "')]").exists());

        mockMvc.perform(post("/scenario/rw/articles/read-after-write")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(article("sticky-" + title, "ada"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.written.dataSource").value("rwMaster"))
                .andExpect(jsonPath("$.data.read.dataSource").value("rwMaster"))
                .andExpect(jsonPath("$.data.read.records[?(@.title == 'sticky-" + title + "')]").exists());

        Set<String> slaves = new HashSet<>();
        for (int i = 0; i < 6; i++) {
            MvcResult result = mockMvc.perform(get("/scenario/rw/articles"))
                    .andExpect(status().isOk())
                    .andReturn();
            String selected = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.data.dataSource");
            slaves.add(selected);
        }
        assertTrue(slaves.contains(DataSourceKeys.RW_SLAVE_1), slaves.toString());
        assertTrue(slaves.contains(DataSourceKeys.RW_SLAVE_2), slaves.toString());
    }

    @Test
    void ordersAreStoredOnTheRoutedShard() throws Exception {
        mockMvc.perform(get("/scenario/sharding/route").param("userId", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dataSource").value("orderShard0"))
                .andExpect(jsonPath("$.data.table").value("t_order_0"));
        mockMvc.perform(get("/scenario/sharding/route").param("userId", "1"))
                .andExpect(jsonPath("$.data.dataSource").value("orderShard1"))
                .andExpect(jsonPath("$.data.table").value("t_order_0"));

        mockMvc.perform(post("/scenario/sharding/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":0,\"orderNo\":\"ORD-USER-0\",\"amount\":18.50}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dataSource").value("orderShard0"))
                .andExpect(jsonPath("$.data.table").value("t_order_0"))
                .andExpect(jsonPath("$.data.database").value(org.hamcrest.Matchers.containsStringIgnoringCase("order_0")));

        mockMvc.perform(get("/scenario/sharding/orders").param("userId", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.table").value("t_order_0"))
                .andExpect(jsonPath("$.data.records[?(@.orderNo == 'ORD-USER-0')]").exists());

        mockMvc.perform(get("/scenario/sharding/orders/scatter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(4));
    }

    @Test
    void tenantsStayApartAndTransferRollsBackTogether() throws Exception {
        mockMvc.perform(get("/scenario/tenant/accounts"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("X-Tenant-Id")));

        mockMvc.perform(post("/scenario/tenant/accounts")
                        .header(ScenarioHeaders.TENANT_ID, "a")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountName\":\"alpha\",\"levelName\":\"basic\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dataSource").value("tenantA"))
                .andExpect(jsonPath("$.data.database").value(org.hamcrest.Matchers.containsStringIgnoringCase("tenant_a")));

        mockMvc.perform(get("/scenario/tenant/accounts").header(ScenarioHeaders.TENANT_ID, "b"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dataSource").value("tenantB"))
                .andExpect(jsonPath("$.data.records[?(@.accountName == 'alpha')]").doesNotExist());

        mockMvc.perform(post("/scenario/tenant/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromTenant\":\"a\",\"toTenant\":\"b\",\"amount\":9.90,\"bizNo\":\"rollback-1\",\"failAfterDebit\":true}"))
                .andExpect(status().isConflict());
        mockMvc.perform(get("/scenario/tenant/ledgers").header(ScenarioHeaders.TENANT_ID, "a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[?(@.bizNo == 'rollback-1')]").doesNotExist());

        mockMvc.perform(post("/scenario/tenant/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromTenant\":\"a\",\"toTenant\":\"b\",\"amount\":9.90,\"bizNo\":\"ok-1\",\"failAfterDebit\":false}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/scenario/tenant/ledgers").header(ScenarioHeaders.TENANT_ID, "a"))
                .andExpect(jsonPath("$.data.records[?(@.bizNo == 'ok-1' && @.direction == 'DEBIT')]").exists());
        mockMvc.perform(get("/scenario/tenant/ledgers").header(ScenarioHeaders.TENANT_ID, "b"))
                .andExpect(jsonPath("$.data.records[?(@.bizNo == 'ok-1' && @.direction == 'CREDIT')]").exists());
    }

    @Test
    void runtimeTenantCanBeRegisteredAndRemoved() throws Exception {
        String url = "jdbc:h2:mem:dyn_tenant_c;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;"
                + "DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;INIT=RUNSCRIPT FROM 'classpath:sql/h2/tenant.sql'";
        String body = objectMapper.writeValueAsString(new java.util.LinkedHashMap<String, String>() {{
            put("tenantId", "c");
            put("url", url);
            put("username", "sa");
            put("password", "");
            put("driverClassName", "org.h2.Driver");
        }});
        try {
            mockMvc.perform(post("/scenario/tenant/datasources")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value("rtTenantc"));
            mockMvc.perform(post("/scenario/tenant/accounts")
                            .header(ScenarioHeaders.TENANT_ID, "c")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"accountName\":\"gamma\",\"levelName\":\"pro\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.dataSource").value("rtTenantc"));
        } finally {
            mockMvc.perform(delete("/scenario/tenant/datasources/c")).andExpect(status().isOk());
        }
        mockMvc.perform(get("/scenario/tenant/accounts").header(ScenarioHeaders.TENANT_ID, "c"))
                .andExpect(status().isNotFound());
    }

    @Test
    void grayReleaseChoosesStableOrGrayDatabase() throws Exception {
        mockMvc.perform(post("/scenario/gray/seed")).andExpect(status().isOk());
        mockMvc.perform(get("/scenario/gray/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dataSource").value("releaseStable"))
                .andExpect(jsonPath("$.data.records[0].versionTag").value("stable"));
        mockMvc.perform(get("/scenario/gray/config").header(ScenarioHeaders.GRAY_VERSION, "gray"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dataSource").value("releaseGray"))
                .andExpect(jsonPath("$.data.records[0].versionTag").value("gray"));

        GrayPolicyCommand command = new GrayPolicyCommand();
        command.setPercent(0);
        command.setAllowUsers(List.of("gray-user"));
        try {
            mockMvc.perform(put("/scenario/gray/policy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isOk());
            mockMvc.perform(get("/scenario/gray/config").header(ScenarioHeaders.USER_ID, "gray-user"))
                    .andExpect(jsonPath("$.data.dataSource").value("releaseGray"));
        } finally {
            featureService.updatePolicy(emptyPolicy());
        }
    }

    @Test
    void dataShardFollowsTheDirectory() throws Exception {
        String shardKey = "device-flow";
        int slot = shardDirectory.slotOf(shardKey);
        String original = shardDirectory.dataSourceOf(shardKey);
        try {
            mockMvc.perform(post("/scenario/shard/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"shardKey\":\"device-flow\",\"eventType\":\"online\",\"payload\":\"up\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.dataSource").value(original))
                    .andExpect(jsonPath("$.data.table").value("t_event"));
            mockMvc.perform(get("/scenario/shard/events").param("shardKey", shardKey))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.dataSource").value(original))
                    .andExpect(jsonPath("$.data.records[?(@.eventType == 'online')]").exists());

            String other = "dataShard0".equals(original) ? "dataShard1" : "dataShard0";
            mockMvc.perform(post("/scenario/shard/slots")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"dataSource\":\"" + other + "\",\"slots\":[" + slot + "]}"))
                    .andExpect(status().isOk());
            mockMvc.perform(get("/scenario/shard/locate").param("shardKey", shardKey))
                    .andExpect(jsonPath("$.data.dataSource").value(other))
                    .andExpect(jsonPath("$.data.slot").value(slot));
        } finally {
            shardDirectory.move(original, List.of(slot));
        }
        assertEquals(original, shardDirectory.dataSourceOf(shardKey));
    }

    private GrayPolicyCommand emptyPolicy() {
        GrayPolicyCommand command = new GrayPolicyCommand();
        command.setPercent(0);
        command.setAllowUsers(List.of());
        return command;
    }

    private java.util.Map<String, String> article(String title, String author) {
        java.util.Map<String, String> body = new java.util.LinkedHashMap<>();
        body.put("title", title);
        body.put("author", author);
        return body;
    }
}
