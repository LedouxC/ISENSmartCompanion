package fr.isen.clement.isensmartcompanion.screens

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import fr.isen.clement.isensmartcompanion.TabBarItem
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip



@Composable
fun TabView(tabBarItems: List<TabBarItem>, navController: NavController) {
    var selectedTabIndex by rememberSaveable {
        mutableStateOf(0)
    }

    NavigationBar(containerColor = Color(0xFF0A84FF)) {
        tabBarItems.forEachIndexed { index, tabBarItem ->
            NavigationBarItem(
                modifier = Modifier.clip(CircleShape),
                selected = selectedTabIndex == index,
                onClick = {
                    selectedTabIndex = index
                    navController.navigate(tabBarItem.title)
                },
                icon = {
                    Icon(
                        imageVector = if (selectedTabIndex == index) tabBarItem.selectedIcon else tabBarItem.unselectedIcon,
                        contentDescription = tabBarItem.title,
                        tint = if (selectedTabIndex == index) Color.White else Color.Black
                    )
                },
                label = {
                    Text(
                        tabBarItem.title,
                        color = if (selectedTabIndex == index) Color.White else Color.Black
                    )
                }
            )
        }
    }

    @Composable
    fun TabBarIconView(
        isSelected: Boolean,
        selectedIcon: ImageVector,
        unselectedIcon: ImageVector,
        title: String,
        badgeAmount: Int? = null
    ) {
        Icon(
            imageVector = if (isSelected) {
                selectedIcon
            } else {
                unselectedIcon
            },
            contentDescription = title
        )
    }
}