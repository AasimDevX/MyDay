package com.example.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.SoftGold
import com.example.ui.viewmodel.NavItem

@Composable
fun BottomNavBar(
    selectedItem: NavItem,
    onItemSelected: (NavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        val items = listOf(
            Triple(NavItem.HOME, Icons.Filled.Home, Icons.Outlined.Home),
            Triple(NavItem.ROUTINE, Icons.Filled.Schedule, Icons.Outlined.Schedule),
            Triple(NavItem.TASKS, Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle),
            Triple(NavItem.ANALYTICS, Icons.Filled.BarChart, Icons.Outlined.BarChart),
            Triple(NavItem.PROFILE, Icons.Filled.Person, Icons.Outlined.Person)
        )

        items.forEach { (item, filledIcon, outlinedIcon) ->
            val isSelected = selectedItem == item
            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemSelected(item) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) filledIcon else outlinedIcon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DeepNavy,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = SoftGold,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("${item.name.lowercase()}_nav_item")
            )
        }
    }
}
