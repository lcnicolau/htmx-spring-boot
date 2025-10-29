package io.github.wimdeblauwe.htmx.spring.boot.mvc;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;


@WebMvcTest(HtmxHandlerMethodArgumentResolverTest.TestController.class)
@ContextConfiguration(classes = HtmxHandlerMethodArgumentResolverTest.TestController.class)
@WithMockUser
class HtmxHandlerMethodArgumentResolverTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private TestService service;

    @Test
    void testIfNonHtmxRequest() throws Exception {
        mockMvc.perform(get("/method-arg-resolver"));

        ArgumentCaptor<HtmxRequest> captor = ArgumentCaptor.forClass(HtmxRequest.class);
        verify(service).doSomething(captor.capture());

        HtmxRequest request = captor.getValue();
        assertThat(request).isNotNull();
        assertThat(request.isHtmxRequest()).isFalse();
    }

    @Test
    void testIfHtmxRequest() throws Exception {
        mockMvc.perform(get("/method-arg-resolver")
                                .header("HX-Request", "true"));

        ArgumentCaptor<HtmxRequest> captor = ArgumentCaptor.forClass(HtmxRequest.class);
        verify(service).doSomething(captor.capture());

        HtmxRequest request = captor.getValue();
        assertThat(request).isNotNull();
        assertThat(request.isHtmxRequest()).isTrue();
        assertThat(request.isBoosted()).isFalse();
    }

    @Test
    void testHxBoosted() throws Exception {
        mockMvc.perform(get("/method-arg-resolver")
                                .header("HX-Request", "true")
                                .header("HX-Boosted", "true"));

        ArgumentCaptor<HtmxRequest> captor = ArgumentCaptor.forClass(HtmxRequest.class);
        verify(service).doSomething(captor.capture());

        HtmxRequest request = captor.getValue();
        assertThat(request).isNotNull();
        assertThat(request.isHtmxRequest()).isTrue();
        assertThat(request.isBoosted()).isTrue();
    }

    @Test
    void testHxCurrentUrl() throws Exception {
        mockMvc.perform(get("/method-arg-resolver")
                                .header("HX-Request", "true")
                                .header("HX-Current-URL", "http://localhost:8080/"));

        ArgumentCaptor<HtmxRequest> captor = ArgumentCaptor.forClass(HtmxRequest.class);
        verify(service).doSomething(captor.capture());

        HtmxRequest request = captor.getValue();
        assertThat(request).isNotNull();
        assertThat(request.isHtmxRequest()).isTrue();
        assertThat(request.isBoosted()).isFalse();
        assertThat(request.getCurrentUrl()).isEqualTo("http://localhost:8080/");
    }

    @Test
    void testHxHistoryRestoreRequest() throws Exception {
        mockMvc.perform(get("/method-arg-resolver")
                                .header("HX-Request", "true")
                                .header("HX-History-Restore-Request", "true"));

        ArgumentCaptor<HtmxRequest> captor = ArgumentCaptor.forClass(HtmxRequest.class);
        verify(service).doSomething(captor.capture());

        HtmxRequest request = captor.getValue();
        assertThat(request).isNotNull();
        assertThat(request.isHtmxRequest()).isTrue();
        assertThat(request.isBoosted()).isFalse();
        assertThat(request.isHistoryRestoreRequest()).isTrue();
    }

    @Test
    void testHxPrompt() throws Exception {
        mockMvc.perform(get("/method-arg-resolver")
                                .header("HX-Request", "true")
                                .header("HX-Prompt", "Yes"));

        ArgumentCaptor<HtmxRequest> captor = ArgumentCaptor.forClass(HtmxRequest.class);
        verify(service).doSomething(captor.capture());

        HtmxRequest request = captor.getValue();
        assertThat(request).isNotNull();
        assertThat(request.isHtmxRequest()).isTrue();
        assertThat(request.isBoosted()).isFalse();
        assertThat(request.getPromptResponse()).isEqualTo("Yes");
    }

    @Test
    void testHxTarget() throws Exception {
        mockMvc.perform(get("/method-arg-resolver")
                                .header("HX-Request", "true")
                                .header("HX-Target", "button-1"));

        ArgumentCaptor<HtmxRequest> captor = ArgumentCaptor.forClass(HtmxRequest.class);
        verify(service).doSomething(captor.capture());

        HtmxRequest request = captor.getValue();
        assertThat(request).isNotNull();
        assertThat(request.isHtmxRequest()).isTrue();
        assertThat(request.isBoosted()).isFalse();
        assertThat(request.getTarget()).isEqualTo("button-1");
    }

    @Test
    void testHxTriggerName() throws Exception {
        mockMvc.perform(get("/method-arg-resolver")
                                .header("HX-Request", "true")
                                .header("HX-Trigger-Name", "myTriggerName"));

        ArgumentCaptor<HtmxRequest> captor = ArgumentCaptor.forClass(HtmxRequest.class);
        verify(service).doSomething(captor.capture());

        HtmxRequest request = captor.getValue();
        assertThat(request).isNotNull();
        assertThat(request.isHtmxRequest()).isTrue();
        assertThat(request.isBoosted()).isFalse();
        assertThat(request.getTriggerName()).isEqualTo("myTriggerName");
    }

    @Test
    void testHxTriggerId() throws Exception {
        mockMvc.perform(get("/method-arg-resolver")
                                .header("HX-Request", "true")
                                .header("HX-Trigger", "myTriggerId"));

        ArgumentCaptor<HtmxRequest> captor = ArgumentCaptor.forClass(HtmxRequest.class);
        verify(service).doSomething(captor.capture());

        HtmxRequest request = captor.getValue();
        assertThat(request).isNotNull();
        assertThat(request.isHtmxRequest()).isTrue();
        assertThat(request.isBoosted()).isFalse();
        assertThat(request.getTriggerId()).isEqualTo("myTriggerId");
    }


    @Test
    void testHxRequestAnnotation() throws Exception {
        mockMvc.perform(get("/method-arg-resolver/users")
                                .header("HX-Request", "true"))
               .andExpect(view().name("users :: list"));
    }

    @Test
    void testHxRequestAnnotationInheritance() throws Exception {
        mockMvc.perform(get("/method-arg-resolver/users/inherited")
                                .header("HX-Request", "true"))
               .andExpect(view().name("users :: list"));
    }

    @Test
    void testHxRequestSameUrlNoAnnotation() throws Exception {
        mockMvc.perform(get("/method-arg-resolver/users"))
               .andExpect(view().name("users"));
    }

    @Controller
    @RequestMapping("/method-arg-resolver")
    static class TestController {

        @Autowired
        private TestService service;

        @GetMapping
        @ResponseBody
        public String htmxRequestDetails(HtmxRequest details) {
            service.doSomething(details);

            return "";
        }

        @GetMapping("/users")
        @HxRequest
        public String htmxRequest(HtmxRequest details) {
            service.doSomething(details);

            return "users :: list";
        }

        @GetMapping("/users")
        public String normalRequest(HtmxRequest details) {
            service.doSomething(details);

            return "users";
        }

        @HxGetMapping("/users/inherited")
        public String htmxRequestInheritance(HtmxRequest details) {
            service.doSomething(details);

            return "users :: list";
        }

        @GetMapping("/users/inherited")
        public String normalRequestInheritance(HtmxRequest details) {
            service.doSomething(details);

            return "users";
        }
    }

    @Service
    public class TestService {
        void doSomething(HtmxRequest details) {
        }
    }

}
