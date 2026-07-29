package com.restaurant.waitlist.backend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingsPayload {

    @Builder.Default
    @JsonProperty("guestNotifications")
    private GuestNotifications guestNotifications = new GuestNotifications();

    @Builder.Default
    @JsonProperty("messageTemplates")
    private MessageTemplates messageTemplates = new MessageTemplates();

    @Builder.Default
    @JsonProperty("staffNotifications")
    private StaffNotifications staffNotifications = new StaffNotifications();

    public static NotificationSettingsPayload defaults() {
        return NotificationSettingsPayload.builder()
                .guestNotifications(GuestNotifications.builder().build())
                .messageTemplates(MessageTemplates.builder().build())
                .staffNotifications(StaffNotifications.builder().build())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuestNotifications {
        @Builder.Default
        @JsonProperty("notifysmsenabled")
        private Boolean notifysmsenabled = true;

        @Builder.Default
        @JsonProperty("notifycallenabled")
        private Boolean notifycallenabled = true;

        @Builder.Default
        @JsonProperty("approvesmsenabled")
        private Boolean approvesmsenabled = true;

        @Builder.Default
        @JsonProperty("joinedwaitlistsmsenabled")
        private Boolean joinedwaitlistsmsenabled = true;

        @Builder.Default
        @JsonProperty("autoRemoveNoShowEnabled")
        private Boolean autoRemoveNoShowEnabled = false;

        @Builder.Default
        @JsonProperty("autoRemoveMinutes")
        private Integer autoRemoveMinutes = 15;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageTemplates {
        @Builder.Default
        @JsonProperty("smsTemplateId")
        private Long smsTemplateId = 1L;

        @Builder.Default
        @JsonProperty("smsTemplateName")
        private String smsTemplateName = "joinedwaitlistsms";

        @Builder.Default
        @JsonProperty("smsTemplatePreview")
        private String smsTemplatePreview = "Thank you for joining the waitlist. We will notify you when your table is ready.";

        @Builder.Default
        @JsonProperty("approveSmsTemplateId")
        private Long approveSmsTemplateId = 2L;

        @Builder.Default
        @JsonProperty("approveSmsTemplateName")
        private String approveSmsTemplateName = "approvewaitlistsms";

        @Builder.Default
        @JsonProperty("approveSmsTemplatePreview")
        private String approveSmsTemplatePreview = "Your waitlist request has been approved. Your position is {position} and the estimated wait time is {estimatedWait} minutes.";

        @Builder.Default
        @JsonProperty("notifySmsTemplateId")
        private Long notifySmsTemplateId = 3L;

        @Builder.Default
        @JsonProperty("notifySmsTemplateName")
        private String notifySmsTemplateName = "notifyguestsms";

        @Builder.Default
        @JsonProperty("notifySmsTemplatePreview")
        private String notifySmsTemplatePreview = "Hi {guestName}, this is {restaurantName}. Your table is ready. Please head to the host stand within the next ten minutes. We look forward to seeing you!";

        @Builder.Default
        @JsonProperty("voiceTemplateId")
        private Long voiceTemplateId = 1L;

        @Builder.Default
        @JsonProperty("voiceTemplateName")
        private String voiceTemplateName = "call guest sms";

        @Builder.Default
        @JsonProperty("voiceTemplatePreview")
        private String voiceTemplatePreview = "Hi {guestName}, this is {restaurantName}. Your table is ready. Please head to the host stand within the next ten minutes. We look forward to seeing you!";

        @Builder.Default
        @JsonProperty("voice")
        private String voice = "male";
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StaffNotifications {
        @Builder.Default
        @JsonProperty("partyWaitingTooLong")
        private Boolean partyWaitingTooLong = true;

        @Builder.Default
        @JsonProperty("tableOccupiedTooLong")
        private Boolean tableOccupiedTooLong = false;
    }
}
