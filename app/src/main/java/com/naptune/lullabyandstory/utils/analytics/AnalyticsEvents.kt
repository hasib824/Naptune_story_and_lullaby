package com.naptune.lullabyandstory.utils.analytics

/**
 * Firebase Analytics Event Names
 *
 * All custom event names used throughout the app.
 * Follow Firebase naming conventions:
 * - Use lowercase with underscores
 * - Max 40 characters
 * - Start with letter
 * - No spaces or special characters except underscore
 */
object AnalyticsEvents {

    // ====================================================================================
    // LULLABY EVENTS
    // ====================================================================================
    const val LULLABY_SELECTED = "lullaby_selected"
    const val LULLABY_PLAY_STARTED = "lullaby_play_started"
    const val LULLABY_PLAY_COMPLETED = "lullaby_play_completed"

    // ====================================================================================
    // STORY EVENTS
    // ====================================================================================
    const val STORY_SELECTED = "story_selected"
    const val STORY_LISTEN_STARTED = "story_listen_started"
    const val STORY_READ_STARTED = "story_read_started"
    const val STORY_COMPLETED = "story_completed"

    // ====================================================================================
    // FAVORITE EVENTS
    // ====================================================================================
    const val ADD_TO_FAVOURITES = "add_to_favourites"
    const val REMOVE_FROM_FAVOURITES = "remove_from_favourites"
    const val CONTENT_PLAYED_FROM_FAVOURITES = "content_played_from_favourites" // ⭐ NEW
    const val FAVOURITES_SCREEN_VIEWED = "favourites_screen_viewed" // ⭐ NEW

    // ====================================================================================
    // DOWNLOAD EVENTS
    // ====================================================================================
    const val DOWNLOAD_STARTED = "download_started" // ⭐ NEW
    const val DOWNLOAD_COMPLETED = "download_completed" // ⭐ NEW
    const val CONTENT_DOWNLOADED = "content_downloaded"
    const val DOWNLOAD_FAILED = "download_failed"
    const val DOWNLOADED_CONTENT_PLAYED = "downloaded_content_played" // ⭐ NEW (offline usage)

    // ====================================================================================
    // AUDIO PLAYER EVENTS
    // ====================================================================================
    const val AUDIO_PLAY = "audio_play"
    const val AUDIO_PAUSE = "audio_pause"
    const val AUDIO_SEEK = "audio_seek"
    const val AUDIO_PLAYBACK_SPEED_CHANGED = "audio_playback_speed_changed"
    const val AUDIO_SKIP = "audio_skip" // ⭐ NEW
    const val AUDIO_LOOP_TOGGLED = "audio_loop_toggled" // ⭐ NEW
    const val AUDIO_VOLUME_CHANGED = "audio_volume_changed" // ⭐ NEW

    // ====================================================================================
    // TIMER EVENTS
    // ====================================================================================
    const val TIMER_SET = "timer_set"
    const val TIMER_MODIFIED = "timer_modified" // ⭐ NEW
    const val TIMER_CANCELLED = "timer_cancelled" // ⭐ NEW
    const val TIMER_COMPLETED = "timer_completed"

    // ====================================================================================
    // MONETIZATION EVENTS
    // ====================================================================================
    const val PREMIUM_VIEWED = "premium_viewed"
    const val SUBSCRIPTION_INITIATED = "subscription_initiated"
    const val SUBSCRIPTION_COMPLETED = "subscription_completed"
    const val SUBSCRIPTION_CANCELLED = "subscription_cancelled"
    const val AD_VIEWED = "ad_viewed"
    const val REWARDED_AD_WATCHED = "rewarded_ad_watched"
    const val AD_CLICKED = "ad_clicked"

    // Session Unlock (Rewarded Ads) ⭐⭐⭐
    const val REWARDED_AD_REQUESTED = "rewarded_ad_requested"
    const val REWARDED_AD_LOADED = "rewarded_ad_loaded"
    const val REWARDED_AD_LOAD_FAILED = "rewarded_ad_load_failed"
    const val REWARDED_AD_STARTED = "rewarded_ad_started"
    const val REWARDED_AD_COMPLETED = "rewarded_ad_completed"
    const val REWARDED_AD_CLOSED_EARLY = "rewarded_ad_closed_early"
    const val CONTENT_UNLOCKED_VIA_AD = "content_unlocked_via_ad"
    const val SESSION_UNLOCK_USED = "session_unlock_used"

    // ====================================================================================
    // ONBOARDING & SETTINGS EVENTS
    // ====================================================================================
    const val APP_FIRST_LAUNCH = "app_first_launch"
    const val LANGUAGE_CHANGED = "language_changed"
    const val THEME_CHANGED = "theme_changed"
    const val SETTINGS_CHANGED = "settings_changed"

    // ====================================================================================
    // ERROR EVENTS
    // ====================================================================================
    const val PLAYBACK_ERROR = "playback_error"

    // ====================================================================================
    // SEARCH & BROWSE EVENTS
    // ====================================================================================
    const val SEARCH_PERFORMED = "search_performed"
    const val CATEGORY_BROWSED = "category_browsed"

    // ====================================================================================
    // 🆕 FCM NOTIFICATION EVENTS
    // ====================================================================================
    const val NOTIFICATION_RECEIVED = "notification_received"
    const val NOTIFICATION_OPENED = "notification_opened"
    const val FCM_TOKEN_GENERATED = "fcm_token_generated"
    const val FCM_TOKEN_REGISTERED = "fcm_token_registered"
    const val FCM_TOKEN_REFRESHED = "fcm_token_refreshed"
    const val DEEP_LINK_NAVIGATED = "deep_link_navigated"
    const val NOTIFICATION_PERMISSION_REQUESTED = "notification_permission_requested"
    const val NOTIFICATION_PERMISSION_RESULT = "notification_permission_result"
}

/**
 * Firebase Analytics Parameter Names
 *
 * All custom parameter names used in events.
 * Follow Firebase naming conventions:
 * - Use lowercase with underscores
 * - Max 40 characters
 * - Start with letter
 * - No spaces or special characters except underscore
 */
object AnalyticsParams {

    // ====================================================================================
    // CONTENT IDENTIFIERS
    // ====================================================================================
    const val LULLABY_ID = "lullaby_id"
    const val LULLABY_NAME = "lullaby_name"
    const val STORY_ID = "story_id"
    const val STORY_NAME = "story_name"
    const val CONTENT_ID = "content_id"
    const val CONTENT_NAME = "content_name"
    const val CONTENT_TYPE = "content_type"

    // ====================================================================================
    // CONTENT ATTRIBUTES
    // ====================================================================================
    const val CATEGORY = "category"
    const val IS_PREMIUM = "is_premium"
    const val IS_DOWNLOADED = "is_downloaded"
    const val DURATION_SECONDS = "duration_seconds"
    const val FILE_SIZE_MB = "file_size_mb"
    const val PAGE_COUNT = "page_count"

    // ====================================================================================
    // USER INTERACTION
    // ====================================================================================
    const val SOURCE_SCREEN = "source_screen"
    const val INTERACTION_TYPE = "interaction_type"
    const val PLAYBACK_METHOD = "playback_method"
    const val COMPLETION_TYPE = "completion_type"
    const val TRIGGER = "trigger"
    const val METHOD = "method"

    // ====================================================================================
    // PLAYBACK METRICS
    // ====================================================================================
    const val PLAYBACK_POSITION_MS = "playback_position_ms"
    const val SESSION_DURATION_MS = "session_duration_ms"
    const val LISTEN_DURATION_SECONDS = "listen_duration_seconds"
    const val TIME_SPENT_SECONDS = "time_spent_seconds"
    const val COMPLETION_PERCENTAGE = "completion_percentage"
    const val FROM_POSITION_MS = "from_position_ms"
    const val TO_POSITION_MS = "to_position_ms"

    // ====================================================================================
    // TIMER
    // ====================================================================================
    const val DURATION_MINUTES = "duration_minutes"
    const val REMAINING_MINUTES = "remaining_minutes" // ⭐ NEW
    const val TIMER_TYPE = "timer_type" // ⭐ NEW (fixed_duration or end_of_content)
    const val DID_STOP_PLAYBACK = "did_stop_playback" // ⭐ NEW
    const val CONTENT_PLAYING = "content_playing"
    const val STOPPED_CONTENT_ID = "stopped_content_id"

    // ====================================================================================
    // MONETIZATION
    // ====================================================================================
    const val AD_UNIT_ID = "ad_unit_id"
    const val AD_TYPE = "ad_type"
    const val PLACEMENT = "placement"
    const val REWARD_TYPE = "reward_type"
    const val REWARD_EARNED = "reward_earned"
    const val REWARD_AMOUNT = "reward_amount"
    const val WATCH_DURATION_SECONDS = "watch_duration_seconds"
    const val WATCHED_SECONDS = "watched_seconds"
    const val REQUIRED_SECONDS = "required_seconds"
    const val SESSION_UNLOCK_COUNT = "session_unlock_count"
    const val MINUTES_SINCE_UNLOCK = "minutes_since_unlock"
    const val LOAD_TIME_MS = "load_time_ms"
    const val PLAN_TYPE = "plan_type"
    const val PRICE = "price"
    const val CURRENCY = "currency"
    const val SUCCESS = "success"
    const val DAYS_ACTIVE = "days_active"
    const val REASON = "reason"

    // ====================================================================================
    // LANGUAGE & LOCALIZATION
    // ====================================================================================
    const val DEVICE_LANGUAGE = "device_language"
    const val FROM_LANGUAGE = "from_language"
    const val TO_LANGUAGE = "to_language"

    // ====================================================================================
    // DOWNLOAD METRICS
    // ====================================================================================
    const val DOWNLOAD_TIME_MS = "download_time_ms" // ⭐ NEW

    // ====================================================================================
    // ERRORS
    // ====================================================================================
    const val ERROR_CODE = "error_code"
    const val ERROR_MESSAGE = "error_message"
    const val NETWORK_TYPE = "network_type"

    // ====================================================================================
    // SEARCH & BROWSE
    // ====================================================================================
    const val SEARCH_TERM = "search_term"
    const val RESULTS_COUNT = "results_count"
    const val ITEMS_COUNT = "items_count"

    // ====================================================================================
    // 🆕 FCM & NOTIFICATIONS
    // ====================================================================================
    const val NOTIFICATION_TITLE = "notification_title"
    const val SCREEN_ROUTE = "screen_route"
    const val HAS_IMAGE = "has_image"
    const val TOKEN_LENGTH = "token_length"
    const val DEVICE_ID = "device_id"
    const val IS_SUCCESS = "is_success"
    const val IS_GRANTED = "is_granted"
    const val REFRESH_REASON = "refresh_reason"
    const val SOURCE = "source"

    // ====================================================================================
    // MISC
    // ====================================================================================
    const val TIMESTAMP = "timestamp"
    const val OLD_SPEED = "old_speed"
    const val NEW_SPEED = "new_speed"
    const val OLD_VOLUME = "old_volume"
    const val NEW_VOLUME = "new_volume"
    const val CHANGE_AMOUNT = "change_amount"
    const val SKIP_DIRECTION = "skip_direction"
    const val SKIP_PERCENTAGE = "skip_percentage"
    const val CONTENT_DURATION_MS = "content_duration_ms"
    const val LOOP_ENABLED = "loop_enabled"
    const val SETTING_NAME = "setting_name"
    const val OLD_VALUE = "old_value"
    const val NEW_VALUE = "new_value"
}

/**
 * Firebase Analytics User Property Names
 *
 * User properties for segmentation and analysis.
 * Follow Firebase naming conventions:
 * - Use lowercase with underscores
 * - Max 24 characters
 * - Start with letter
 */
object AnalyticsUserProperties {
    const val USER_TYPE = "user_type"
    const val PREFERRED_LANGUAGE = "preferred_language"
    const val CONTENT_PREFERENCE = "content_preference"
    const val DOWNLOAD_COUNT = "download_count"
    const val DAYS_SINCE_INSTALL = "days_since_install"
    const val ENGAGEMENT_LEVEL = "engagement_level"
    const val APP_VERSION = "app_version"
    const val DEVICE_TYPE = "device_type"
}

/**
 * Predefined Values for Analytics Parameters
 * Use these constants to ensure consistency
 */
object AnalyticsValues {

    // Content Types
    object ContentType {
        const val LULLABY = "lullaby"
        const val STORY = "story"
        const val SLEEP_SOUND = "sleep_sound"
    }

    // Interaction Types
    object InteractionType {
        const val LISTEN = "listen"
        const val READ = "read"
        const val WATCH = "watch"
    }

    // Playback Methods
    object PlaybackMethod {
        const val STREAM = "stream"
        const val DOWNLOAD = "download"
        const val OFFLINE = "offline"
    }

    // Source Screens
    object SourceScreen {
        const val MAIN = "main"
        const val LULLABY_BROWSE = "lullaby_browse"
        const val STORY_BROWSE = "story_browse"
        const val FAVOURITES = "favourites"
        const val EXPLORE = "explore"
        const val SEARCH = "search"
    }

    // Ad Types
    object AdType {
        const val BANNER = "banner"
        const val INTERSTITIAL = "interstitial"
        const val REWARDED = "rewarded"
        const val NATIVE = "native"
    }

    // User Types
    object UserType {
        const val FREE = "free"
        const val PREMIUM = "premium"
        const val TRIAL = "trial"
    }

    // Engagement Levels
    object EngagementLevel {
        const val LOW = "low"           // < 5 min/day
        const val MEDIUM = "medium"     // 5-15 min/day
        const val HIGH = "high"         // > 15 min/day
    }

    // Triggers
    object Trigger {
        const val BUTTON = "button"
        const val CONTENT_LOCKED = "content_locked"
        const val BANNER = "banner"
        const val POPUP = "popup"
    }

    // Methods
    object Method {
        const val MANUAL = "manual"
        const val AUTO = "auto"
    }

    // Network Types
    object NetworkType {
        const val WIFI = "wifi"
        const val MOBILE = "mobile"
        const val OFFLINE = "offline"
        const val UNKNOWN = "unknown"
    }
}
