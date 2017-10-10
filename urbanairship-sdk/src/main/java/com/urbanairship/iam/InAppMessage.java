/* Copyright 2017 Urban Airship and Contributors */

package com.urbanairship.iam;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import com.urbanairship.Logger;
import com.urbanairship.iam.banner.BannerDisplayContent;
import com.urbanairship.iam.custom.CustomDisplayContent;
import com.urbanairship.json.JsonException;
import com.urbanairship.json.JsonMap;
import com.urbanairship.json.JsonSerializable;
import com.urbanairship.json.JsonValue;
import com.urbanairship.util.Checks;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Defines an in-app message.
 */
public class InAppMessage implements Parcelable, JsonSerializable {

    // JSON keys
    private static final String MESSAGE_ID_KEY = "message_id";
    private static final String DISPLAY_TYPE_KEY = "display_type";
    private static final String DISPLAY_CONTENT_KEY = "display";
    private static final String EXTRA_KEY = "extra";


    @StringDef({ TYPE_BANNER, TYPE_CUSTOM })
    @Retention(RetentionPolicy.SOURCE)
    @interface DisplayType {}

    /**
     * Banner in-app message.
     */
    public static final String TYPE_BANNER = "banner";

    /**
     * Custom in-app message.
     */
    public static final String TYPE_CUSTOM = "custom";



    @DisplayType
    private final String type;
    private final JsonMap extras;
    private final String id;
    private final JsonSerializable content;

    /**
     * Default constructor.
     *
     * @param builder An InAppMessage builder instance.
     */
    private InAppMessage(Builder builder) {
        this.type = builder.type;
        this.content = builder.content;
        this.id = builder.id;
        this.extras = builder.extras == null ? JsonMap.EMPTY_MAP : builder.extras;
    }

    /**
     * Gets the in-app message type.
     *
     * @return The in-app message type.
     */
    @DisplayType
    public String getType() {
        return type;
    }

    /**
     * Returns the display content.
     * <p>
     * The return type depends on the in-app message type:
     * {@link #TYPE_BANNER}: a {@link BannerDisplayContent} will be returned
     * {@link #TYPE_CUSTOM}: a {@link CustomDisplayContent} will be returned
     *
     * @param <T> The expected content type.
     * @return The display content.
     */
    public <T extends DisplayContent> T getDisplayContent() {
        if (content == null) {
            return null;
        }
        try {
            return (T) content;
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Gets the message ID.
     *
     * @return The message ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the extras.
     *
     * @return The extras.
     */
    @NonNull
    public JsonMap getExtras() {
        return extras;
    }

    @Override
    public JsonValue toJsonValue() {
        return JsonMap.newBuilder()
                      .put(MESSAGE_ID_KEY, id)
                      .putOpt(EXTRA_KEY, extras)
                      .putOpt(DISPLAY_CONTENT_KEY, content)
                      .putOpt(DISPLAY_TYPE_KEY, type)
                      .build().toJsonValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InAppMessage message = (InAppMessage) o;

        if (type != null ? !type.equals(message.type) : message.type != null) {
            return false;
        }
        if (extras != null ? !extras.equals(message.extras) : message.extras != null) {
            return false;
        }

        if (id != null ? !id.equals(message.id) : message.id != null) {
            return false;
        }
        return content != null ? content.equals(message.content) : message.content == null;

    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (extras != null ? extras.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }

    /**
     * Parses a json value.
     *
     * @param jsonValue The json value.
     * @return The parsed InAppMessage.
     * @throws JsonException If the json is invalid.
     */
    static InAppMessage fromJson(JsonValue jsonValue) throws JsonException {
        String type = jsonValue.optMap().opt(DISPLAY_TYPE_KEY).getString("");
        JsonValue content = jsonValue.optMap().opt(DISPLAY_CONTENT_KEY);


        try {
            return InAppMessage.newBuilder()
                               .setId(jsonValue.optMap().opt(MESSAGE_ID_KEY).getString())
                               .setExtras(jsonValue.optMap().opt(EXTRA_KEY).optMap())
                               .setDisplayContent(type, content)
                               .build();
        } catch (IllegalArgumentException e) {
            throw new JsonException("Invalid InAppMessage json.", e);
        }
    }


    /**
     * Creates a new builder.
     *
     * @return A new in-app message builder.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Creator for parcelable interface.
     *
     * @hide
     */
    public static final Creator<InAppMessage> CREATOR = new Creator<InAppMessage>() {
        @Override
        public InAppMessage createFromParcel(Parcel in) {
            String id = in.readString();
            String type = in.readString();
            String contentPayload = in.readString();
            String extraPayload = in.readString();

            InAppMessage.Builder builder = InAppMessage.newBuilder()
                                                       .setId(id);

            try {
                builder.setExtras(JsonValue.parseString(extraPayload).optMap());
            } catch (JsonException e) {
                Logger.error("InAppMessage - Invalid extras from parcel: " + extraPayload);
            }

            try {
                builder.setDisplayContent(type, JsonValue.parseString(contentPayload));
            } catch (JsonException e) {
                Logger.error("InAppMessage - Invalid extras from parcel: " + extraPayload);
            }

            return builder.build();
        }

        @Override
        public InAppMessage[] newArray(int size) {
            return new InAppMessage[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(type);
        dest.writeString(content.toJsonValue().toString());
        dest.writeString(extras.toString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return toJsonValue().toString();
    }

    /**
     * In-app message builder.
     */
    public static class Builder {
        @DisplayType
        private String type;
        private JsonMap extras;
        private String id;
        private JsonSerializable content;

        private Builder() {}

        /**
         * Sets the display content to the parsed type.
         *
         * @param type The type.
         * @param content The display content as a json value.
         * @return The builder object.
         */
        private Builder setDisplayContent(String type, JsonValue content) throws JsonException {
            switch (type) {
                case TYPE_BANNER:
                    this.setDisplayContent(BannerDisplayContent.parseJson(content));
                    break;

                case TYPE_CUSTOM:
                    this.setDisplayContent(CustomDisplayContent.parseJson(content));
                    break;
            }

            return this;
        }

        /**
         * Sets the banner display content and type.
         *
         * @param displayContent The banner display content.
         * @return The builder.
         */
        public Builder setDisplayContent(BannerDisplayContent displayContent) {
            this.type = TYPE_BANNER;
            this.content = displayContent;
            return this;
        }

        /**
         * Sets the custom display content and type.
         *
         * @param displayContent The custom display content.
         * @return The builder.
         */
        public Builder setDisplayContent(CustomDisplayContent displayContent) {
            this.type = TYPE_CUSTOM;
            this.content = displayContent;
            return this;
        }

        /**
         * Sets the extras.
         *
         * @param extras An extra json map.
         * @return The builder.
         */
        public Builder setExtras(JsonMap extras) {
            this.extras = extras;
            return this;
        }

        /**
         * Sets the in-app message ID.
         *
         * @param id The message ID.
         * @return The builder.
         */
        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        /**
         * Builds the in-app message.
         *
         * @return The built in-app message.
         */
        public InAppMessage build() {
            Checks.checkNotNull(id, "Missing ID.");
            Checks.checkNotNull(type, "Missing type.");
            Checks.checkNotNull(content, "Missing content.");
            return new InAppMessage(this);
        }
    }
}