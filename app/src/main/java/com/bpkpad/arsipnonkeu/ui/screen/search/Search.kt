//package com.bpkpad.arsipnonkeu.ui.screen.search
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.BasicTextField
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.SolidColor
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.bpkpad.arsipnonkeu.ui.theme.*
//import com.bpkpad.arsipnonkeu.ui.component.TopBar
//
//data class SearchResult(
//    val id: String,
//    val title: String,
//    val subtitle: String,
//    val status: String, // "di Gudang" or "di Pinjam"
//    val building: String,
//    val rack: String,
//    val box: String
//)
//
//@Composable
//fun SearchScreen(
//    onBackClick: () -> Unit = {},
//    onResultClick: (SearchResult) -> Unit = {}
//) {
//    var searchQuery by remember { mutableStateOf("") }
//
//    val results = listOf(
//        SearchResult(
//            id = "BNK-123456-DPPA-2022",
//            title = "Lorem Ipsum dot silor",
//            subtitle = "Berkas Non Keuangan",
//            status = "di Gudang",
//            building = "G1",
//            rack = "C-42",
//            box = "10B"
//        ),
//        SearchResult(
//            id = "BNK-123456-DPPA-2022",
//            title = "Lorem Ipsum dot silor",
//            subtitle = "Berkas Non Keuangan",
//            status = "di Pinjam",
//            building = "G2",
//            rack = "C-42",
//            box = "109"
//        ),
//        SearchResult(
//            id = "BNK-123456-DPPA-2022",
//            title = "Lorem Ipsum dot silor",
//            subtitle = "Berkas Non Keuangan",
//            status = "di Pinjam",
//            building = "G2",
//            rack = "C-42",
//            box = "109"
//        ),
//        SearchResult(
//            id = "BNK-123456-DPPA-2022",
//            title = "Lorem Ipsum dot silor",
//            subtitle = "Berkas Non Keuangan",
//            status = "di Gudang",
//            building = "G1",
//            rack = "C-42",
//            box = "10B"
//        )
//    )
//
//    Scaffold(
//        topBar = {
//            TopBar(
//                title = "Pencarian",
//                onProfileClick = {},
//                navigationIcon = {
//                    IconButton(onClick = onBackClick) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
//                    }
//                }
//            )
//        },
//        containerColor = BackgroundGray
//    ) { innerPadding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(innerPadding)
//                .padding(horizontal = 16.dp, vertical = 12.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            // Search Bar
//            SearchBarField(
//                value = searchQuery,
//                onValueChange = { searchQuery = it }
//            )
//
//            // Filter Chips
//            FilterChipsRow()
//
//            // Result Header
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = "Hasil Pencarian",
//                    fontSize = 14.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = TextPrimary
//                )
//                Text(
//                    text = "${results.size} berkas ditemukan",
//                    fontSize = 12.sp,
//                    color = TextSecondary
//                )
//            }
//
//            // Results List
//            LazyColumn(
//                verticalArrangement = Arrangement.spacedBy(12.dp),
//                contentPadding = PaddingValues(bottom = 16.dp)
//            ) {
//                items(results) { result ->
//                    SearchResultCard(result = result, onClick = { onResultClick(result) })
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun SearchBarField(value: String, onValueChange: (String) -> Unit) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clip(RoundedCornerShape(20.dp))
//            .background(Color.White)
//            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
//            .padding(horizontal = 14.dp, vertical = 10.dp),
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.spacedBy(10.dp)
//    ) {
//        Text(text = "🔍", fontSize = 18.sp)
//        BasicTextField(
//            value = value,
//            onValueChange = onValueChange,
//            modifier = Modifier.weight(1f),
//            textStyle = TextStyle(fontSize = 14.sp, color = TextPrimary),
//            cursorBrush = SolidColor(PrimaryGreen),
//            decorationBox = { innerTextField ->
//                if (value.isEmpty()) {
//                    Text(
//                        text = "Lorem Ipsum dot silor amet",
//                        fontSize = 14.sp,
//                        color = Color(0xFF9CA3AF)
//                    )
//                }
//                innerTextField()
//            }
//        )
//        if (value.isNotEmpty()) {
//            Text(
//                text = "✕",
//                modifier = Modifier.clickable { onValueChange("") },
//                fontSize = 14.sp,
//                color = Color(0xFF9CA3AF)
//            )
//        }
//    }
//}
//
//@Composable
//private fun FilterChipsRow() {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.spacedBy(8.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        // Settings icon placeholder
//        Text(text = "≡", fontSize = 18.sp, color = TextSecondary)
//
//        // Tahun: 2022 X
//        Row(
//            modifier = Modifier
//                .clip(RoundedCornerShape(20.dp))
//                .background(PrimaryGreen)
//                .padding(horizontal = 12.dp, vertical = 6.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(6.dp)
//        ) {
//            Text(text = "Tahun:2022", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
//            Text(text = "✕", color = Color.White, fontSize = 12.sp)
//        }
//
//        // Tipe Dok ▾
//        FilterChip(label = "Tipe Dok")
//
//        // Gudang ▾
//        FilterChip(label = "Gudang")
//    }
//}
//
//@Composable
//private fun FilterChip(label: String) {
//    Row(
//        modifier = Modifier
//            .clip(RoundedCornerShape(20.dp))
//            .background(Color(0xFFE0F2FE))
//            .padding(horizontal = 12.dp, vertical = 6.dp),
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.spacedBy(4.dp)
//    ) {
//        Text(text = label, color = Color(0xFF0369A1), fontSize = 12.sp, fontWeight = FontWeight.Medium)
//        Text(text = "▾", color = Color(0xFF0369A1), fontSize = 12.sp)
//    }
//}
//
//@Composable
//private fun SearchResultCard(result: SearchResult, onClick: () -> Unit) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clip(RoundedCornerShape(16.dp))
//            .background(Color.White)
//            .clickable(onClick = onClick)
//            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.Top
//        ) {
//            Column(modifier = Modifier.weight(1f)) {
//                Text(
//                    text = result.title,
//                    fontSize = 14.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = TextPrimary
//                )
//                Text(
//                    text = result.subtitle,
//                    fontSize = 13.sp,
//                    color = TextSecondary
//                )
//                Text(
//                    text = "#${result.id}",
//                    fontSize = 12.sp,
//                    color = TextSecondary,
//                    modifier = Modifier.padding(top = 4.dp)
//                )
//            }
//
//            // Status Badge
//            val isGudang = result.status == "di Gudang"
//            Row(
//                modifier = Modifier
//                    .clip(RoundedCornerShape(20.dp))
//                    .background(if (isGudang) Color(0xFFDCFCE7) else Color(0xFFE0F2FE))
//                    .padding(horizontal = 10.dp, vertical = 4.dp),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(4.dp)
//            ) {
//                Box(
//                    modifier = Modifier
//                        .size(6.dp)
//                        .clip(CircleShape)
//                        .background(if (isGudang) Color(0xFF22C55E) else Color(0xFF0EA5E9))
//                )
//                Text(
//                    text = result.status,
//                    fontSize = 11.sp,
//                    fontWeight = FontWeight.Medium,
//                    color = if (isGudang) Color(0xFF166534) else Color(0xFF0369A1)
//                )
//            }
//        }
//
//        // Location Info (Bottom Part)
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(Color(0xFFF0F9FF))
//                .padding(horizontal = 16.dp, vertical = 10.dp),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            LocationItem(label = "BUILDING", value = result.building, icon = "🏙")
//            LocationItem(label = "RACK", value = result.rack)
//            LocationItem(label = "BOX", value = result.box)
//        }
//    }
//}
//
//@Composable
//private fun LocationItem(label: String, value: String, icon: String? = null) {
//    Column {
//        Text(text = label, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
//        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
//            if (icon != null) Text(text = icon, fontSize = 12.sp)
//            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
//        }
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun SearchScreenPreview() {
//    SearchScreen()
//}
