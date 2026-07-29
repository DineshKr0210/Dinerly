package com.restaurant.waitlist.backend.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.restaurant.waitlist.backend.dto.request.AdvancedSettingsRequest;
import com.restaurant.waitlist.backend.dto.request.HolidayHourRequest;
import com.restaurant.waitlist.backend.dto.request.UpdateHolidayHourRequest;
import com.restaurant.waitlist.backend.dto.request.UpdateRestaurantSettingsRequest;
import com.restaurant.waitlist.backend.dto.request.UpdateWaitlistSettingsRequest;
import com.restaurant.waitlist.backend.dto.response.AdvancedSettingsResponse;
import com.restaurant.waitlist.backend.dto.response.HolidayHourResponse;
import com.restaurant.waitlist.backend.dto.response.HolidayHoursResponse;
import com.restaurant.waitlist.backend.dto.response.RestaurantSettingsResponse;
import com.restaurant.waitlist.backend.dto.response.SettingsProfileResponse;
import com.restaurant.waitlist.backend.dto.response.WaitlistSettingsResponse;
import com.restaurant.waitlist.backend.entity.AdvancedSettingsPayload;
import com.restaurant.waitlist.backend.entity.HolidayHourPayload;
import com.restaurant.waitlist.backend.entity.HolidayHoursPayload;
import com.restaurant.waitlist.backend.entity.NotificationSettingsPayload;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.entity.RestaurantSettings;
import com.restaurant.waitlist.backend.entity.WaitlistSettingsPayload;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.RestaurantSettingsRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
public class SettingsService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private WaitlistRepository waitlistRepository;

    @Autowired
    private SmsService smsService;

    @Autowired
    private RestaurantSettingsRepository restaurantSettingsRepository;

    public SettingsProfileResponse getProfileSettings(Long restaurantId, Integer year, Integer month) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        int targetYear = year != null ? year : LocalDate.now().getYear();
        int targetMonth = month != null ? month : LocalDate.now().getMonthValue();

        long smsSentThisMonth = waitlistRepository.countSentSmsThisMonth(restaurantId, targetYear, targetMonth);
        double smsChargesThisMonth = smsService.getCurrentMonthEstimatedCharge("sms");
        double callChargesThisMonth = smsService.getCurrentMonthEstimatedCharge("call");
        double totalChargesThisMonth = smsChargesThisMonth + callChargesThisMonth;;

        YearMonth renewalMonth = YearMonth.of(targetYear, targetMonth).plusMonths(1);

        SettingsProfileResponse.ProfileResponse profileResponse = SettingsProfileResponse.ProfileResponse.builder()
                .restaurant(SettingsProfileResponse.RestaurantProfileResponse.builder()
                        .id(restaurant.getId())
                        .name(restaurant.getName())
                        .email(restaurant.getEmail())
                        .phone(restaurant.getPhone())
                        .address(restaurant.getAddress())
                        .hours(SettingsProfileResponse.HoursResponse.builder()
                                .open(restaurant.getOpenTime())
                                .close(restaurant.getCloseTime())
                                .build())
                        .build())
                .plan(SettingsProfileResponse.PlanResponse.builder()
                        .name("Basic")
                        .smssentthismonth((int) smsSentThisMonth)
                        .marketingsmssentthismonth(0)
                        .smsChargesThisMonth(smsChargesThisMonth)
                        .callChargesThisMonth(callChargesThisMonth)
                        .totalChargesThisMonth(totalChargesThisMonth)
                        .nextRenewal(renewalMonth.atDay(1).toString())
                        .build())
                .build();

        return SettingsProfileResponse.builder().profile(profileResponse).build();
    }

    public SettingsProfileResponse updateProfileSettings(Long restaurantId, com.restaurant.waitlist.backend.dto.request.UpdateSettingsProfileRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        if (request.getName() != null) {
            restaurant.setName(request.getName());
        }
        if (request.getEmail() != null) {
            restaurant.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            restaurant.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            restaurant.setAddress(request.getAddress());
        }
        if (request.getHours() != null) {
            if (request.getHours().getOpen() != null) {
                restaurant.setOpenTime(request.getHours().getOpen());
            }
            if (request.getHours().getClose() != null) {
                restaurant.setCloseTime(request.getHours().getClose());
            }
        }

        restaurantRepository.save(restaurant);
        return getProfileSettings(restaurantId, null, null);
    }

    public RestaurantSettingsResponse getRestaurantSettings(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        RestaurantSettings settings = restaurantSettingsRepository.findByRestaurantId(restaurantId)
                .orElseGet(() -> createDefaultSettings(restaurant));

        return RestaurantSettingsResponse.fromSettings(settings);
    }

    public RestaurantSettingsResponse updateRestaurantSettings(Long restaurantId, UpdateRestaurantSettingsRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        RestaurantSettings settings = restaurantSettingsRepository.findByRestaurantId(restaurantId)
                .orElseGet(() -> createDefaultSettings(restaurant));

        if (request.getSendSmsNotifications() != null) {
            settings.setSendSmsNotifications(request.getSendSmsNotifications());
        }
        if (request.getSendEmailNotifications() != null) {
            settings.setSendEmailNotifications(request.getSendEmailNotifications());
        }
        if (request.getNightlySummaryEmail() != null) {
            settings.setNightlySummaryEmail(request.getNightlySummaryEmail());
        }
        if (request.getAverageServiceTime() != null) {
            settings.setAverageServiceTime(request.getAverageServiceTime());
        }
        if (request.getBufferTime() != null) {
            settings.setBufferTime(request.getBufferTime());
        }
        if (request.getOperatingHours() != null) {
            settings.setOperatingHours(request.getOperatingHours());
        }
        if (request.getMaxWaitlistSize() != null) {
            settings.setMaxWaitlistSize(request.getMaxWaitlistSize());
        }
        if (request.getNotificationSettings() != null) {
            NotificationSettingsPayload payload = request.getNotificationSettings();
            settings.setNotificationSettings(payload);
        }

        restaurantSettingsRepository.save(settings);
        return RestaurantSettingsResponse.fromSettings(settings);
    }

    public AdvancedSettingsResponse getAdvancedSettings(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        RestaurantSettings settings = restaurantSettingsRepository.findByRestaurantId(restaurantId)
                .orElseGet(() -> createDefaultSettings(restaurant));

        return AdvancedSettingsResponse.fromPayload(settings.getAdvancedSettings());
    }

    public AdvancedSettingsResponse updateAdvancedSettings(Long restaurantId, AdvancedSettingsRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        RestaurantSettings settings = restaurantSettingsRepository.findByRestaurantId(restaurantId)
                .orElseGet(() -> createDefaultSettings(restaurant));

        AdvancedSettingsPayload payload = settings.getAdvancedSettings();
        if (request.getDarkMode() != null) {
            payload.setDarkMode(request.getDarkMode());
        }
        if (request.getDesktopNotifications() != null) {
            payload.setDesktopNotifications(request.getDesktopNotifications());
        }
        if (request.getKeepSignedIn() != null) {
            payload.setKeepSignedIn(request.getKeepSignedIn());
        }
        if (request.getLanguage() != null) {
            payload.setLanguage(request.getLanguage());
        }
        if (request.getTimezone() != null) {
            payload.setTimezone(request.getTimezone());
        }

        settings.setAdvancedSettings(payload);
        restaurantSettingsRepository.save(settings);
        return AdvancedSettingsResponse.fromPayload(payload);
    }

    public WaitlistSettingsResponse getWaitlistSettings(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        RestaurantSettings settings = restaurantSettingsRepository.findByRestaurantId(restaurantId)
                .orElseGet(() -> createDefaultSettings(restaurant));

        return WaitlistSettingsResponse.fromSettings(settings);
    }

    public WaitlistSettingsResponse updateWaitlistSettings(Long restaurantId, UpdateWaitlistSettingsRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        RestaurantSettings settings = restaurantSettingsRepository.findByRestaurantId(restaurantId)
                .orElseGet(() -> createDefaultSettings(restaurant));

        WaitlistSettingsPayload payload = settings.getWaitlistSettings();
        if (request.getMaxPartySize() != null) {
            payload.setMaxPartySize(request.getMaxPartySize());
        }
        if (request.getTableReadyResponseMinutes() != null) {
            payload.setTableReadyResponseMinutes(request.getTableReadyResponseMinutes());
        }
        if (request.getWalkInsOnly() != null) {
            payload.setWalkInsOnly(request.getWalkInsOnly());
        }
        if (request.getPauseNewJoinsAfterClosing() != null) {
            payload.setPauseNewJoinsAfterClosing(request.getPauseNewJoinsAfterClosing());
        }
        if (request.getAllowGoogleJoin() != null) {
            payload.setAllowGoogleJoin(request.getAllowGoogleJoin());
        }
        if (request.getAcceptOnlineJoin() != null) {
            payload.setAcceptOnlineJoin(request.getAcceptOnlineJoin());
        }

        settings.setWaitlistSettings(payload);
        restaurantSettingsRepository.save(settings);
        return WaitlistSettingsResponse.fromSettings(settings);
    }

    public HolidayHoursResponse getHolidayHours(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        RestaurantSettings settings = restaurantSettingsRepository.findByRestaurantId(restaurantId)
                .orElseGet(() -> createDefaultSettings(restaurant));

        return HolidayHoursResponse.fromPayload(settings.getHolidayHours());
    }

    public HolidayHourResponse addHolidayHour(Long restaurantId, HolidayHourRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        RestaurantSettings settings = restaurantSettingsRepository.findByRestaurantId(restaurantId)
                .orElseGet(() -> createDefaultSettings(restaurant));

        HolidayHourPayload holidayHour = HolidayHourPayload.builder()
                .date(request.getDate())
                .title(request.getTitle())
                .openTime(request.getOpenTime())
                .closeTime(request.getCloseTime())
                .notes(request.getNotes())
                .closed(request.getClosed() != null ? request.getClosed() : false)
                .build();

        HolidayHoursPayload holidayHours = settings.getHolidayHours();
        holidayHours.getHolidayHours().add(holidayHour);
        settings.setHolidayHours(holidayHours);
        restaurantSettingsRepository.save(settings);

        return HolidayHourResponse.fromPayload(holidayHour);
    }

    public HolidayHourResponse updateHolidayHour(Long restaurantId, String holidayHourId, UpdateHolidayHourRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        RestaurantSettings settings = restaurantSettingsRepository.findByRestaurantId(restaurantId)
                .orElseGet(() -> createDefaultSettings(restaurant));

        HolidayHoursPayload holidayHours = settings.getHolidayHours();
        HolidayHourPayload holidayHour = holidayHours.getHolidayHours().stream()
                .filter(item -> item.getId().equals(holidayHourId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Holiday hour entry not found"));

        if (request.getDate() != null) {
            holidayHour.setDate(request.getDate());
        }
        if (request.getTitle() != null) {
            holidayHour.setTitle(request.getTitle());
        }
        if (request.getOpenTime() != null) {
            holidayHour.setOpenTime(request.getOpenTime());
        }
        if (request.getCloseTime() != null) {
            holidayHour.setCloseTime(request.getCloseTime());
        }
        if (request.getNotes() != null) {
            holidayHour.setNotes(request.getNotes());
        }
        if (request.getClosed() != null) {
            holidayHour.setClosed(request.getClosed());
        }

        settings.setHolidayHours(holidayHours);
        restaurantSettingsRepository.save(settings);

        return HolidayHourResponse.fromPayload(holidayHour);
    }

    public byte[] getOrCreateQrCodeImage(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        String qrUrl = getStoredQrUrl(restaurantId);
        if (qrUrl == null) {
            qrUrl = createQrUrl(restaurant);
            saveQrUrl(restaurantId, qrUrl);
        }

        return generateQrCodeImage(qrUrl);
    }

    public Map<String, Object> createQrCode(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        String existingQrUrl = getStoredQrUrl(restaurantId);
        if (existingQrUrl != null) {
            throw new RuntimeException("QR code already exists for this restaurant");
        }

        String qrUrl = createQrUrl(restaurant);
        saveQrUrl(restaurantId, qrUrl);

        Map<String, Object> response = new HashMap<>();
        response.put("restaurantId", restaurantId);
        response.put("qrUrl", qrUrl);
        return response;
    }

    public Map<String, Object> deleteQrCode(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        Path propertiesPath = resolveApplicationPropertiesPath();
        if (propertiesPath == null || !Files.exists(propertiesPath)) {
            throw new RuntimeException("QR code not found for this restaurant");
        }

        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(propertiesPath)) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read application properties", e);
        }

        String key = "restaurant.qr." + restaurantId + ".url";
        if (!properties.containsKey(key)) {
            throw new RuntimeException("QR code not found for this restaurant");
        }

        properties.remove(key);
        try (OutputStream outputStream = Files.newOutputStream(propertiesPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            properties.store(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), "Generated QR URLs");
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete QR URL from application properties", e);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("restaurantId", restaurantId);
        response.put("restaurantName", restaurant.getName());
        response.put("deleted", true);
        return response;
    }

    private String createQrUrl(Restaurant restaurant) {
        String slug = restaurant.getName() == null ? "restaurant" : restaurant.getName()
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        return "https://dev.dinerly.ca/" + restaurant.getId() + "/join/" + slug;
    }

    private byte[] generateQrCodeImage(String content) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 300, 300);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    private String getStoredQrUrl(Long restaurantId) {
        Path propertiesPath = resolveApplicationPropertiesPath();
        if (propertiesPath == null || !Files.exists(propertiesPath)) {
            return null;
        }

        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(propertiesPath)) {
            properties.load(inputStream);
        } catch (IOException e) {
            return null;
        }

        return properties.getProperty("restaurant.qr." + restaurantId + ".url");
    }

    private void saveQrUrl(Long restaurantId, String qrUrl) {
        Path propertiesPath = resolveApplicationPropertiesPath();
        if (propertiesPath == null) {
            return;
        }

        Properties properties = new Properties();
        if (Files.exists(propertiesPath)) {
            try (InputStream inputStream = Files.newInputStream(propertiesPath)) {
                properties.load(inputStream);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read application properties", e);
            }
        }

        properties.setProperty("restaurant.qr." + restaurantId + ".url", qrUrl);
        try (OutputStream outputStream = Files.newOutputStream(propertiesPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            properties.store(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), "Generated QR URLs");
        } catch (IOException e) {
            throw new RuntimeException("Failed to save QR URL to application properties", e);
        }
    }

    private Path resolveApplicationPropertiesPath() {
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        Path candidate = currentDir.resolve("src/main/resources/application.properties");
        if (Files.exists(candidate)) {
            return candidate;
        }
        Path fallback = currentDir.resolve("application.properties");
        return Files.exists(fallback) ? fallback : null;
    }

    private RestaurantSettings createDefaultSettings(Restaurant restaurant) {
        RestaurantSettings settings = RestaurantSettings.builder()
                .restaurant(restaurant)
                .notificationSettings(NotificationSettingsPayload.defaults())
                .waitlistSettings(WaitlistSettingsPayload.defaults())
                .holidayHours(HolidayHoursPayload.defaults())
                .build();
        return restaurantSettingsRepository.save(settings);
    }
}
