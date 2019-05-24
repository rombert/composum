package com.composum.sling.core.servlet;

import com.composum.sling.core.util.LoggerFormat;
import com.composum.sling.core.util.ResponseUtil;
import com.composum.sling.cpnl.CpnlElFunctions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static javax.servlet.http.HttpServletResponse.SC_ACCEPTED;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_OK;

/**
 * the standardised answer object of a servlet request to fill the response output
 */
public class Status {

    public static final String STATUS = "status";
    public static final String SUCCESS = "success";
    public static final String WARNING = "warning";

    public static final String TITLE = "title";
    public static final String MESSAGES = "messages";
    public static final String LEVEL = "level";
    public static final String CONTEXT = "context";
    public static final String TARGET = "target";
    public static final String TEXT = "text";

    public static final String DATA = "data";

    public enum Level {info, warn, error}

    public class Message {

        public final Level level;
        public final String context;
        public final String target;
        public final String text;

        public Message(@Nonnull final Level level, @Nonnull final String text) {
            this(level, null, null, text);
        }

        public Message(@Nonnull final Level level,
                       @Nullable final String context, @Nullable final String target,
                       @Nonnull final String text) {
            this.level = level;
            this.context = context;
            this.target = target;
            this.text = text;
        }

        public void toJson(JsonWriter writer) throws IOException {
            writer.beginObject();
            writer.name(LEVEL).value(level.name());
            if (StringUtils.isNotBlank(context)) {
                writer.name(CONTEXT).value(context);
            }
            if (StringUtils.isNotBlank(target)) {
                writer.name(TARGET).value(target);
            }
            writer.name(TEXT).value(text);
            writer.endObject();
        }
    }

    protected final Gson gson;
    protected final SlingHttpServletRequest request;
    protected final SlingHttpServletResponse response;

    protected int status = SC_OK;
    protected boolean success = true;
    protected boolean warning = false;

    protected String title;
    protected final List<Message> messages;
    protected final Map<String, Map<String, Object>> data;

    public Status(@Nonnull final SlingHttpServletRequest request, @Nonnull final SlingHttpServletResponse response) {
        this(new GsonBuilder().create(), request, response);
    }

    public Status(@Nonnull final Gson gson,
                  @Nonnull final SlingHttpServletRequest request, @Nonnull final SlingHttpServletResponse response) {
        this.gson = gson;
        this.request = request;
        this.response = response;
        data = new HashMap<>();
        messages = new ArrayList<>();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
        if (status < 200 || status >= 300) {
            success = false;
        }
    }

    public String getTitle() {
        return hasTitle() ? title : CpnlElFunctions.i18n(request, "Result");
    }

    public boolean hasTitle() {
        return StringUtils.isNotBlank(title);
    }

    public void setTitle(String title) {
        this.title = title != null ? CpnlElFunctions.i18n(request, title) : null;
    }

    public boolean isValid() {
        return isSuccess();
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isWarning() {
        return warning;
    }

    public boolean isError() {
        return !isSuccess();
    }

    public void info(@Nonnull final String text, Object... args) {
        addMessage(Level.info, text, args);
    }

    public void warn(@Nonnull final String text, Object... args) {
        addMessage(Level.warn, text, args);
    }

    public void warn(@Nonnull final String target, @Nonnull final String text, Object... args) {
        addMessage(Level.warn, null, target, text, args);
    }

    public void warn(@Nonnull final String context, @Nonnull final String target,
                     @Nonnull final String text, Object... args) {
        addMessage(Level.warn, context, target, text);
    }

    public void error(@Nonnull final String text, Object... args) {
        addMessage(Level.error, text);
    }

    public void error(@Nonnull final String target, @Nonnull final String text, Object... args) {
        addMessage(Level.error, null, target, text);
    }

    public void error(@Nonnull final String context, @Nonnull final String target,
                      @Nonnull final String text, Object... args) {
        addMessage(Level.error, target, context, text);
    }

    /**
     * @param name the key of the data element
     * @return a new Map for the data values
     */
    @Nonnull
    public Map<String, Object> data(@Nonnull final String name) {
        Map<String, Object> object = new LinkedHashMap<>();
        data.put(name, object);
        return object;
    }

    public void addMessage(@Nonnull final Level level, @Nonnull final String text, Object... args) {
        addMessage(level, null, null, text, args);
    }

    public void addMessage(@Nonnull final Level level, @Nullable final String context, @Nullable final String target,
                           @Nonnull final String text, Object... args) {
        if (level == Level.error) {
            status = SC_BAD_REQUEST;
            success = false;
            if (!hasTitle()) {
                setTitle("Error");
            }
        } else if (level == Level.warn && status == SC_OK) {
            status = SC_ACCEPTED; // 202 - accepted but there is a warning
            warning = true;
            if (!hasTitle()) {
                setTitle("Warning");
            }
        }
        String message = CpnlElFunctions.i18n(request, text);
        if (args != null && args.length > 0) {
            message = LoggerFormat.format(message, args);
        }
        messages.add(new Message(level, context != null ? CpnlElFunctions.i18n(request, context) : null,
                target != null ? CpnlElFunctions.i18n(request, target) : null, message));
    }

    public void toJson(@Nonnull final JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name(STATUS).value(getStatus());
        writer.name(SUCCESS).value(isSuccess());
        writer.name(WARNING).value(isWarning());
        writer.name(TITLE).value(getTitle());
        writer.name(MESSAGES).beginArray();
        for (Message message : messages) {
            message.toJson(writer);
        }
        writer.endArray();
        for (Map.Entry<String, Map<String, Object>> entry : data.entrySet()) {
            writer.name(entry.getKey());
            gson.toJson(entry.getValue(), Map.class, writer);
        }
        writer.endObject();
    }

    public void sendJson() throws IOException {
        JsonWriter writer = ResponseUtil.getJsonWriter(response);
        response.setStatus(getStatus());
        response.setContentType("application/json; charset=UTF-8");
        toJson(writer);
    }
}