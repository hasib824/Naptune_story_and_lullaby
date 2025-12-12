package com.naptune.lullabyandstory.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.naptune.lullabyandstory.ui.theme.PrimaryColor
import com.naptune.lullabyandstory.ui.theme.SecondaryColor
import com.naptune.lullabyandstory.R
import com.naptune.lullabyandstory.ui.theme.AccentColor
import com.naptune.lullabyandstory.ui.theme.PrimarySurfaceColor

@Composable
fun NaptuneBottomNavigation(
    navController: NavController
) {
    val items = listOf(
        NavItem.Home,
        NavItem.Lullaby, // ✅ NEW: Lullaby tab
        NavItem.Story, // ✅ NEW: Story tab
        NavItem.Profile
      //  NavItem.Explore, // Commented out
      //  NavItem.Favourite, // Commented out
      //  NavItem.Debug // ✅ NEW: Temporary debug tab
    )
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(PrimarySurfaceColor)
        // Navigation bar padding directly on card
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEach { item ->
                BottomNavItemComponent(
                    item = item,
                    isSelected = currentRoute == item.route,
                    onClick = {
                        if (currentRoute != item.route) {
                            if (item.route == Screen.Main.route) {
                                // ✅ When clicking Home, pop back to it instead of navigating
                                navController.popBackStack(Screen.Main.route, inclusive = false)
                            } else {
                                // ✅ For other tabs, navigate with proper backstack management
                                navController.navigate(item.route) {
                                    popUpTo(Screen.Main.route) {
                                        saveState = true
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

// ✅ NEW: Bottom Navigation Items Definition
sealed class NavItem(
    val route: String,
    val titleRes: Int,
    val iconRes: Int
) {
    object Home : NavItem(
        route = Screen.Main.route,
        titleRes = R.string.nav_home,
        iconRes = R.drawable.homenavic // Using system icon temporarily
    )

    object Lullaby : NavItem(
        route = Screen.Lullaby.route,
        titleRes = R.string.nav_lullaby,
        iconRes = R.drawable.ic_lullaby_nav
    )

    object Story : NavItem(
        route = Screen.Story.route,
        titleRes = R.string.nav_story,
        iconRes = R.drawable.ic_story_nav
    )

    object Profile : NavItem(
        route = Screen.Profile.route,
        titleRes = R.string.nav_profile,
        iconRes = R.drawable.profilenavic
    )

    // Commented out items
//    object Explore : NavItem(
//        route = Screen.Explore.route,
//        titleRes = R.string.nav_explore,
//        iconRes = R.drawable.ic_explore_bottom_nav
//    )
//
//    object Favourite : NavItem(
//        route = Screen.Favourite.route,
//        titleRes = R.string.nav_favourite,
//        iconRes = R.drawable.favouritenavic
//    )
    
/*    object Debug : NavItem(
        route = Screen.Debug.route,
        title = "Debug",
        iconRes = android.R.drawable.ic_menu_info_details
    )*/
}

@Composable
fun BottomNavItemComponent(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier 
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        val titleText = stringResource(id = item.titleRes)
        Icon(
            painter = painterResource(id = item.iconRes),
            contentDescription = titleText,
            tint = if (isSelected) AccentColor else Color(0xFFB2B7BE), // White text on gradient background

        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = titleText,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) AccentColor else Color(0xFFD4D7DB)
        )


    }
}
