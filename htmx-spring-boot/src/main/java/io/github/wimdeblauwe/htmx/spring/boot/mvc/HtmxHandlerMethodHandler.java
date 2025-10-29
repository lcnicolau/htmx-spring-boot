package io.github.wimdeblauwe.htmx.spring.boot.mvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * A handler for processing {@link HtmxResponse} and annotations present on handler methods.
 *
 * @since 3.6.2
 */
class HtmxHandlerMethodHandler {

    private final ObjectMapper objectMapper;

    public HtmxHandlerMethodHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void handleMethodArgument(HttpServletRequest request, HttpServletResponse response) {

        HtmxResponse htmxResponse = RequestContextUtils.getHtmxResponse(request);
        if (htmxResponse != null) {
            addHxTriggerHeaders(response, HtmxResponseHeader.HX_TRIGGER, htmxResponse.getTriggers());
            addHxTriggerHeaders(response, HtmxResponseHeader.HX_TRIGGER_AFTER_SETTLE, htmxResponse.getTriggersAfterSettle());
            addHxTriggerHeaders(response, HtmxResponseHeader.HX_TRIGGER_AFTER_SWAP, htmxResponse.getTriggersAfterSwap());

            if (htmxResponse.getReplaceUrl() != null) {
                response.setHeader(HtmxResponseHeader.HX_REPLACE_URL.getValue(), RequestContextUtils.createUrl(request, htmxResponse.getReplaceUrl(), htmxResponse.isContextRelative()));
            }
            if (htmxResponse.getPushUrl() != null) {
                response.setHeader(HtmxResponseHeader.HX_PUSH_URL.getValue(), RequestContextUtils.createUrl(request, htmxResponse.getPushUrl(), htmxResponse.isContextRelative()));
            }
            if (htmxResponse.getRetarget() != null) {
                response.setHeader(HtmxResponseHeader.HX_RETARGET.getValue(), htmxResponse.getRetarget());
            }
            if (htmxResponse.getReselect() != null) {
                response.setHeader(HtmxResponseHeader.HX_RESELECT.getValue(), htmxResponse.getReselect());
            }
            if (htmxResponse.getReswap() != null) {
                response.setHeader(HtmxResponseHeader.HX_RESWAP.getValue(), htmxResponse.getReswap().toHeaderValue());
            }
        }
    }

    public void handleMethodAnnotations(Method method, HttpServletRequest request, HttpServletResponse response) {

        setHxPushUrl(request, response, method);
        setHxReplaceUrl(request, response, method);
        setHxReswap(response, method);
        setHxRetarget(response, method);
        setHxReselect(response, method);
        setHxTrigger(response, method);
        setHxTriggerAfterSettle(response, method);
        setHxTriggerAfterSwap(response, method);
    }

    private void addHxTriggerHeaders(HttpServletResponse response, HtmxResponseHeader headerName, Collection<HtmxTrigger> triggers) {
        if (triggers.isEmpty()) {
            return;
        }

        // separate event names by commas if no additional details are available
        if (triggers.stream().allMatch(t -> t.getEventDetail() == null)) {
            String value = triggers.stream()
                                   .map(HtmxTrigger::getEventName)
                                   .collect(Collectors.joining(","));

            response.setHeader(headerName.getValue(), value);
            return;
        }

        // multiple events with or without details
        var triggerMap = new HashMap<String, Object>();
        for (HtmxTrigger trigger : triggers) {
            triggerMap.put(trigger.getEventName(), trigger.getEventDetail());
        }
        setHeaderJsonValue(response, headerName, triggerMap);
    }

    private void setHxPushUrl(HttpServletRequest request, HttpServletResponse response, Method method) {
        HxPushUrl methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, HxPushUrl.class);
        if (methodAnnotation != null) {
            if (HtmxValue.TRUE.equals(methodAnnotation.value())) {
                setHeader(response, HtmxResponseHeader.HX_PUSH_URL, getRequestUrl(request));
            } else {
                setHeader(response, HtmxResponseHeader.HX_PUSH_URL, RequestContextUtils.createUrl(request, methodAnnotation.value(), methodAnnotation.contextRelative()));
            }
        }
    }

    private void setHxReplaceUrl(HttpServletRequest request, HttpServletResponse response, Method method) {
        HxReplaceUrl methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, HxReplaceUrl.class);
        if (methodAnnotation != null) {
            if (HtmxValue.TRUE.equals(methodAnnotation.value())) {
                setHeader(response, HtmxResponseHeader.HX_REPLACE_URL, getRequestUrl(request));
            } else {
                setHeader(response, HtmxResponseHeader.HX_REPLACE_URL, RequestContextUtils.createUrl(request, methodAnnotation.value(), methodAnnotation.contextRelative()));
            }
        }
    }

    private void setHxReswap(HttpServletResponse response, Method method) {
        HxReswap methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, HxReswap.class);
        if (methodAnnotation != null) {
            setHeader(response, HtmxResponseHeader.HX_RESWAP, convertToReswap(methodAnnotation));
        }
    }

    private void setHxRetarget(HttpServletResponse response, Method method) {
        HxRetarget methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, HxRetarget.class);
        if (methodAnnotation != null) {
            setHeader(response, HtmxResponseHeader.HX_RETARGET, methodAnnotation.value());
        }
    }

    private void setHxReselect(HttpServletResponse response, Method method) {
        HxReselect methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, HxReselect.class);
        if (methodAnnotation != null) {
            setHeader(response, HtmxResponseHeader.HX_RESELECT, methodAnnotation.value());
        }
    }

    private void setHxTrigger(HttpServletResponse response, Method method) {
        HxTrigger methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, HxTrigger.class);
        if (methodAnnotation != null) {
            setHeader(response, HtmxResponseHeader.HX_TRIGGER, methodAnnotation.value());
        }
    }

    private void setHxTriggerAfterSettle(HttpServletResponse response, Method method) {
        HxTriggerAfterSettle methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, HxTriggerAfterSettle.class);
        if (methodAnnotation != null) {
            setHeader(response, HtmxResponseHeader.HX_TRIGGER_AFTER_SETTLE, methodAnnotation.value());
        }
    }

    private void setHxTriggerAfterSwap(HttpServletResponse response, Method method) {
        HxTriggerAfterSwap methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, HxTriggerAfterSwap.class);
        if (methodAnnotation != null) {
            setHeader(response, HtmxResponseHeader.HX_TRIGGER_AFTER_SWAP, methodAnnotation.value());
        }
    }

    private void setHeader(HttpServletResponse response, HtmxResponseHeader header, String value) {
        response.setHeader(header.getValue(), value);
    }

    private void setHeader(HttpServletResponse response, HtmxResponseHeader header, String[] values) {
        response.setHeader(header.getValue(), String.join(",", values));
    }

    private String convertToReswap(HxReswap annotation) {

        var reswap = new HtmxReswap(annotation.value());
        if (annotation.swap() != -1) {
            reswap.swap(Duration.ofMillis(annotation.swap()));
        }
        if (annotation.settle() != -1) {
            reswap.swap(Duration.ofMillis(annotation.settle()));
        }
        if (annotation.transition()) {
            reswap.transition();
        }
        if (annotation.focusScroll() != HxReswap.FocusScroll.UNDEFINED) {
            reswap.focusScroll(annotation.focusScroll() == HxReswap.FocusScroll.TRUE);
        }
        if (annotation.show() != HxReswap.Position.UNDEFINED) {
            reswap.show(convertToPosition(annotation.show()));
            if (!annotation.showTarget().isEmpty()) {
                reswap.scrollTarget(annotation.showTarget());
            }
        }
        if (annotation.scroll() != HxReswap.Position.UNDEFINED) {
            reswap.scroll(convertToPosition(annotation.scroll()));
            if (!annotation.scrollTarget().isEmpty()) {
                reswap.scrollTarget(annotation.scrollTarget());
            }
        }

        return reswap.toString();
    }

    private HtmxReswap.Position convertToPosition(HxReswap.Position position) {
        return switch (position) {
            case TOP -> HtmxReswap.Position.TOP;
            case BOTTOM -> HtmxReswap.Position.BOTTOM;
            default -> throw new IllegalStateException("Unexpected value: " + position);
        };
    }

    private String getRequestUrl(HttpServletRequest request) {
        String path = request.getRequestURI();
        String queryString = request.getQueryString();

        if (queryString != null && !queryString.isEmpty()) {
            path += "?" + queryString;
        }
        return path;
    }

    private void setHeaderJsonValue(HttpServletResponse response, HtmxResponseHeader header, Object value) {
        try {
            response.setHeader(header.getValue(), objectMapper.writeValueAsString(value));
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Unable to set header " + header.getValue() + " to " + value, e);
        }
    }

}
