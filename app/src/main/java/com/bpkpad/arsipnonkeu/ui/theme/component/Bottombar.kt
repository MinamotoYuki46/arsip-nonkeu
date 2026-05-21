package com.bpkpad.arsipnonkeu.ui.theme.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PostAdd
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Menggunakan Font Poppins (Pastikan PoppinsFont sudah didefinisikan di Theme/Type.kt)
// Jika belum, gunakan FontFamily.Default untuk sementara
val PoppinsFont = FontFamily.Default

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun BottomBar(
    selectedRoute: String = "dashboard",
    onItemSelected: (String) -> Unit = {}
) {
    val items = listOf(
        BottomNavItem("Main", Icons.Outlined.GridView, "dashboard"),
        BottomNavItem("Docs", Icons.Outlined.Inventory2, "archive"),
        BottomNavItem("Search", Icons.Outlined.Search, "search"),
        BottomNavItem("New", Icons.Outlined.PostAdd, "new_record"),
        BottomNavItem("Profile", Icons.Outlined.Person, "profile")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE6F6FF)) // Latar belakang biru muda sesuai foto
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val isSelected = item.route == selectedRoute
            BottomNavItemView(
                item = item,
                isSelected = isSelected,
                onSelected = { onItemSelected(item.route) }
            )
        }
    }
}

@Composable
private fun BottomNavItemView(
    item: BottomNavItem,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    // Warna Background Kapsul jika Aktif
    val backgroundColor = if (isSelected) Color(0xFF2E7D32) else Color.Transparent
    // Warna Konten (Icon & Text)
    val contentColor = if (isSelected) Color.White else Color(0xFF333333)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(32.dp)) // Membuat bentuk pill/kapsul
            .background(backgroundColor)
            .clickable(
                onClick = onSelected
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                modifier = Modifier.size(24.dp),
                tint = contentColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.label,
                color = contentColor,
                fontSize = 12.sp,
                fontFamily = PoppinsFont,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BottomBarPreview() {
    BottomBar(selectedRoute = "dashboard")
}