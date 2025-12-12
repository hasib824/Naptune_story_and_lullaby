package com.naptune.lullabyandstory.utils

/**
 * 🔥 Firebase Analytics Event Names
 * Centralized constants for all analytics events in the app
 */
object AnalyticsEvents {
    
    // ============== SCREEN VIEWS ==============
    const val SCREEN_SPLASH = "splash_screen"
    const val SCREEN_HOME = "home_screen"
    const val SCREEN_LULLABY = "lullaby_screen"
    const val SCREEN_STORY = "story_screen"
    const val SCREEN_SLEEP_SOUNDS = "sleep_sounds_screen"
    const val SCREEN_FAVOURITE = "favourite_screen"
    const val SCREEN_PROFILE = "profile_screen"
    const val SCREEN_PREMIUM = "premium_screen"
    const val SCREEN_SETTINGS = "settings_screen"
    const val SCREEN_AUDIO_PLAYER = "audio_player_screen"
    const val SCREEN_STORY_MANAGER = "story_manager_screen"
    const val SCREEN_STORY_READER = "story_reader_screen"
    
    // ============== USER ACTIONS ==============
    // Content Interaction
    const val LULLABY_PLAY = "lullaby_play"
    const val LULLABY_PAUSE = "lullaby_pause"
    const val LULLABY_DOWNLOAD = "lullaby_download"
    const val LULLABY_FAVOURITE_ADD = "lullaby_favourite_add"
    const val LULLABY_FAVOURITE_REMOVE = "lullaby_favourite_remove"
    
    const val STORY_PLAY = "story_play"
    const val STORY_PAUSE = "story_pause"
    const val STORY_FAVOURITE_ADD = "story_favourite_add"
    const val STORY_FAVOURITE_REMOVE = "story_favourite_remove"
    const val STORY_READ = "story_read"
    const val STORY_LISTEN = "story_listen"
    
    // Player Controls
    const val PLAYER_NEXT = "player_next"
    const val PLAYER_PREVIOUS = "player_previous"
    const val PLAYER_SEEK = "player_seek"
    const val PLAYER_REPEAT_TOGGLE = "player_repeat_toggle"
    const val PLAYER_SHUFFLE_TOGGLE = "player_shuffle_toggle"
    
    // Timer Events
    const val TIMER_SET = "timer_set"
    const val TIMER_CANCEL = "timer_cancel"
    const val TIMER_COMPLETE = "timer_complete"
    
    // Navigation
    const val NAV_TO_LULLABY = "navigate_to_lullaby"
    const val NAV_TO_STORY = "navigate_to_story"
    const val NAV_TO_SLEEP_SOUNDS = "navigate_to_sleep_sounds"
    const val NAV_TO_PREMIUM = "navigate_to_premium"
    const val NAV_TO_SETTINGS = "navigate_to_settings"
    
    // Premium & Monetization
    const val PREMIUM_BUTTON_CLICK = "premium_button_click"
    const val PREMIUM_PLAN_SELECT = "premium_plan_select"
    const val PREMIUM_PURCHASE_INITIATE = "premium_purchase_initiate"
    const val PREMIUM_PURCHASE_SUCCESS = "premium_purchase_success"
    const val PREMIUM_PURCHASE_CANCEL = "premium_purchase_cancel"
    const val PREMIUM_PURCHASE_ERROR = "premium_purchase_error"
    const val PREMIUM_RESTORE = "premium_restore"
    
    // Ads
    const val AD_REWARDED_SHOW = "ad_rewarded_show"
    const val AD_REWARDED_COMPLETE = "ad_rewarded_complete"
    const val AD_REWARDED_SKIP = "ad_rewarded_skip"
    const val AD_BANNER_LOAD = "ad_banner_load"
    const val AD_BANNER_ERROR = "ad_banner_error"
    
    // Session Unlock (Rewarded Ad Unlock)
    const val SESSION_UNLOCK_REQUEST = "session_unlock_request"
    const val SESSION_UNLOCK_SUCCESS = "session_unlock_success"
    const val SESSION_UNLOCK_FAIL = "session_unlock_fail"
    
    // Language
    const val LANGUAGE_CHANGE = "language_change"
    const val LANGUAGE_SELECT = "language_select"
    
    // Settings
    const val SETTINGS_PRIVACY_POLICY = "settings_privacy_policy"
    const val SETTINGS_ACKNOWLEDGEMENT = "settings_acknowledgement"
    const val SETTINGS_RESTORE_PURCHASE = "settings_restore_purchase"
    
    // Search & Filter
    const val SEARCH_QUERY = "search_query"
    const val FILTER_CATEGORY = "filter_category"
    
    // Onboarding
    const val APP_FIRST_OPEN = "app_first_open"
    const val SPLASH_COMPLETE = "splash_complete"
    
    // Errors
    const val ERROR_NETWORK = "error_network"
    const val ERROR_PLAYBACK = "error_playback"
    const val ERROR_DOWNLOAD = "error_download"
}

/**
 * 🏷️ Firebase Analytics Parameter Names
 * Centralized constants for all analytics parameters
 */
object AnalyticsParams {
    // Content Parameters
    const val ITEM_ID = "item_id"
    const val ITEM_NAME = "item_name"
    const val ITEM_TYPE = "item_type"
    const val ITEM_CATEGORY = "item_category"
    const val CONTENT_TYPE = "content_type"
    const val CONTENT_ID = "content_id"
    
    // Audio/Story Parameters
    const val AUDIO_DURATION = "audio_duration"
    const val AUDIO_SOURCE = "audio_source"
    const val STORY_LANGUAGE = "story_language"
    const val IS_PREMIUM = "is_premium"
    const val IS_DOWNLOADED = "is_downloaded"
    const val IS_FAVOURITE = "is_favourite"
    
    // User Interaction
    const val SOURCE_SCREEN = "source_screen"
    const val SOURCE_SECTION = "source_section"
    const val ACTION_TYPE = "action_type"
    const val POSITION = "position"
    
    // Timer Parameters
    const val TIMER_DURATION = "timer_duration"
    const val TIMER_MODE = "timer_mode"
    
    // Premium Parameters
    const val PLAN_TYPE = "plan_type"
    const val PLAN_PRICE = "plan_price"
    const val PLAN_CURRENCY = "plan_currency"
    const val PURCHASE_STATUS = "purchase_status"
    
    // Language Parameters
    const val LANGUAGE_FROM = "language_from"
    const val LANGUAGE_TO = "language_to"
    const val LANGUAGE_CODE = "language_code"
    
    // Error Parameters
    const val ERROR_MESSAGE = "error_message"
    const val ERROR_CODE = "error_code"
    const val ERROR_TYPE = "error_type"
    
    // Search Parameters
    const val SEARCH_TERM = "search_term"
    const val SEARCH_RESULTS = "search_results"
    
    // Player State
    const val PLAYER_STATE = "player_state"
    const val PLAYBACK_POSITION = "playback_position"
    const val REPEAT_MODE = "repeat_mode"
    const val SHUFFLE_MODE = "shuffle_mode"
    
    // Ad Parameters
    const val AD_TYPE = "ad_type"
    const val AD_PLACEMENT = "ad_placement"
    const val AD_REWARD_TYPE = "ad_reward_type"
    
    // Session Parameters
    const val SESSION_DURATION = "session_duration"
    const val UNLOCK_METHOD = "unlock_method"
}

/**
 * 📊 Content Type Constants
 */
object ContentType {
    const val LULLABY = "lullaby"
    const val STORY = "story"
    const val SLEEP_SOUND = "sleep_sound"
}

/**
 * 💳 Plan Type Constants
 */
object PlanType {
    const val MONTHLY = "monthly"
    const val YEARLY = "yearly"
    const val LIFETIME = "lifetime"
}

/**
 * 🎵 Player State Constants
 */
object PlayerState {
    const val PLAYING = "playing"
    const val PAUSED = "paused"
    const val STOPPED = "stopped"
    const val BUFFERING = "buffering"
}
