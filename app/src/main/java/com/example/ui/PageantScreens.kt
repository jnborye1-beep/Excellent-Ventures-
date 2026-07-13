package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.Contestant
import com.example.data.VoteTransaction
import com.example.ui.theme.DarkSatinBg
import com.example.ui.theme.DeepSurface
import com.example.ui.theme.MetallicGold
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SunlitGold
import com.example.ui.theme.VelvetOrchid
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun getDrawableId(name: String): Int {
    val context = LocalContext.current
    return remember(name) {
        val id = context.resources.getIdentifier(name, "drawable", context.packageName)
        if (id == 0) R.drawable.ic_launcher_foreground else id
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageantMainScreen(viewModel: PageantViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedContestant by viewModel.selectedContestant.collectAsStateWithLifecycle()
    val votingContestant by viewModel.votingContestant.collectAsStateWithLifecycle()
    val paymentUiState by viewModel.paymentState.collectAsStateWithLifecycle()

    val contestants by viewModel.contestants.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = "Crown",
                            tint = SunlitGold,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CROWN VOTE",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkSatinBg,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DeepSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = currentTab == PageantTab.CONTESTANTS,
                    onClick = { viewModel.selectTab(PageantTab.CONTESTANTS) },
                    icon = { Icon(Icons.Default.People, contentDescription = "Contestants") },
                    label = { Text("Candidates") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SunlitGold,
                        selectedTextColor = SunlitGold,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f),
                        indicatorColor = RoyalPurple
                    )
                )
                NavigationBarItem(
                    selected = currentTab == PageantTab.LEADERBOARD,
                    onClick = { viewModel.selectTab(PageantTab.LEADERBOARD) },
                    icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Leaderboard") },
                    label = { Text("Leaderboard") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SunlitGold,
                        selectedTextColor = SunlitGold,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f),
                        indicatorColor = RoyalPurple
                    )
                )
                NavigationBarItem(
                    selected = currentTab == PageantTab.HISTORY,
                    onClick = { viewModel.selectTab(PageantTab.HISTORY) },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Votes Ledger") },
                    label = { Text("Ledger") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SunlitGold,
                        selectedTextColor = SunlitGold,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f),
                        indicatorColor = RoyalPurple
                    )
                )
            }
        },
        containerColor = DarkSatinBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen switching with transition
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "TabTransition"
            ) { tab ->
                when (tab) {
                    PageantTab.CONTESTANTS -> ContestantsScreen(
                        contestants = contestants,
                        onContestantClick = { viewModel.viewContestantDetails(it) },
                        onVoteClick = { viewModel.startVotingFlow(it) }
                    )
                    PageantTab.LEADERBOARD -> LeaderboardScreen(
                        contestants = contestants,
                        onVoteClick = { viewModel.startVotingFlow(it) }
                    )
                    PageantTab.HISTORY -> HistoryScreen(
                        transactions = transactions
                    )
                }
            }

            // Contestant details full screen sheet
            selectedContestant?.let { contestant ->
                ContestantDetailsDialog(
                    contestant = contestant,
                    onDismiss = { viewModel.viewContestantDetails(null) },
                    onVoteClick = {
                        viewModel.viewContestantDetails(null)
                        viewModel.startVotingFlow(contestant)
                    },
                    onUpdateContestant = { updated ->
                        viewModel.updateContestant(updated)
                    }
                )
            }

            // Secure simulated payment process
            votingContestant?.let { contestant ->
                SecurePaymentDialog(
                    contestant = contestant,
                    paymentUiState = paymentUiState,
                    onInitialize = { votes, phone, provider ->
                        viewModel.initializePayment(contestant, votes, phone, provider)
                    },
                    onConfirmPin = { viewModel.confirmPin(it) },
                    onVerifyOtp = { viewModel.verifyOtp(it) },
                    onCancel = { viewModel.failOrCancelTransaction() },
                    onSuccessDismiss = { viewModel.dismissSuccess() }
                )
            }
        }
    }
}

// --- CONTESTANTS SCREEN ---
@Composable
fun ContestantsScreen(
    contestants: List<Contestant>,
    onContestantClick: (Contestant) -> Unit,
    onVoteClick: (Contestant) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("contestants_screen"),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Luxury Pageant Banner Hero
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_pageant_banner),
                    contentDescription = "CrownVote Pageant Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    DarkSatinBg.copy(alpha = 0.95f)
                                )
                            )
                        )
                )
                // Text overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("ANNUAL CORONATION", color = SunlitGold, fontWeight = FontWeight.Bold) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = RoyalPurple.copy(alpha = 0.8f)),
                        border = BorderStroke(1.dp, SunlitGold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Global Voices of Impact",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Vote securely via Mobile Money to support your favorite advocate.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }
        }

        // Contestants Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Official Candidates",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "${contestants.size} Active",
                    fontSize = 12.sp,
                    color = SunlitGold,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Contestants List (Elegant, full width cards for impact)
        items(contestants) { contestant ->
            ContestantCard(
                contestant = contestant,
                onClick = { onContestantClick(contestant) },
                onVoteClick = { onVoteClick(contestant) }
            )
        }
    }
}

@Composable
fun ContestantCard(
    contestant: Contestant,
    onClick: () -> Unit,
    onVoteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() }
            .testTag("contestant_card_${contestant.candidateNumber}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepSurface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image with round border
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f))
            ) {
                Image(
                    painter = painterResource(id = getDrawableId(contestant.imageResName)),
                    contentDescription = contestant.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Candidate Number badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .background(
                            color = VelvetOrchid,
                            shape = RoundedCornerShape(bottomEnd = 8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "#${contestant.candidateNumber}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Main Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = contestant.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${contestant.age} yrs • ${contestant.region}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Platform description
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalActivity,
                        contentDescription = "Platform",
                        tint = SunlitGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = contestant.platform,
                        fontSize = 11.sp,
                        color = SunlitGold,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                // Votes indicator
                Text(
                    text = "%,d Votes".format(contestant.votes),
                    fontSize = 13.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Fast vote action button
            IconButton(
                onClick = onVoteClick,
                modifier = Modifier
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(RoyalPurple, VelvetOrchid)
                        ),
                        shape = CircleShape
                    )
                    .size(44.dp)
                    .testTag("vote_btn_${contestant.candidateNumber}"),
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
            ) {
                Icon(
                    imageVector = Icons.Default.HowToVote,
                    contentDescription = "Vote for ${contestant.name}",
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// --- CONTESTANT DETAILS DIALOG ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContestantDetailsDialog(
    contestant: Contestant,
    onDismiss: () -> Unit,
    onVoteClick: () -> Unit,
    onUpdateContestant: (Contestant) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        EditContestantDialog(
            contestant = contestant,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                onUpdateContestant(updated)
                showEditDialog = false
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkSatinBg),
            color = DarkSatinBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Image Header with full dismiss button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    Image(
                        painter = painterResource(id = getDrawableId(contestant.imageResName)),
                        contentDescription = contestant.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Custom Back Button Overlay
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    // Custom Edit Button Overlay
                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .testTag("edit_contestant_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = Color.White
                        )
                    }

                    // Floating Card overlay for basic bio details
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                            .background(
                                color = DeepSurface.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "CANDIDATE #${contestant.candidateNumber}",
                                color = SunlitGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = contestant.name,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Age ${contestant.age} • Representing ${contestant.region}",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Body content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Votes & Standing Highlights
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DeepSurface),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "LIVE TOTAL COUNT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "%,d Votes".format(contestant.votes),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SunlitGold
                                )
                            }
                            Button(
                                onClick = onVoteClick,
                                colors = ButtonDefaults.buttonColors(containerColor = VelvetOrchid),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                            ) {
                                Icon(Icons.Default.HowToVote, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("VOTE NOW", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Platform Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = SunlitGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Platform Statement",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = contestant.platform,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SunlitGold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Detailed Bio text
                    Text(
                        text = "Full Biography",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = contestant.biography,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContestantDialog(
    contestant: Contestant,
    onDismiss: () -> Unit,
    onSave: (Contestant) -> Unit
) {
    var name by remember { mutableStateOf(contestant.name) }
    var candidateNumber by remember { mutableStateOf(contestant.candidateNumber) }
    var ageString by remember { mutableStateOf(contestant.age.toString()) }
    var region by remember { mutableStateOf(contestant.region) }
    var platform by remember { mutableStateOf(contestant.platform) }
    var biography by remember { mutableStateOf(contestant.biography) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var candidateNumberError by remember { mutableStateOf<String?>(null) }
    var ageError by remember { mutableStateOf<String?>(null) }
    var regionError by remember { mutableStateOf<String?>(null) }
    var platformError by remember { mutableStateOf<String?>(null) }
    var biographyError by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkSatinBg),
            color = DarkSatinBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Candidate Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = SunlitGold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = if (it.isBlank()) "Name cannot be empty" else null
                    },
                    label = { Text("Full Name") },
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth().testTag("edit_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SunlitGold,
                        focusedLabelColor = SunlitGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Row for Candidate Number and Age
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = candidateNumber,
                        onValueChange = {
                            candidateNumber = it
                            candidateNumberError = if (it.isBlank()) "Required" else null
                        },
                        label = { Text("Candidate #") },
                        isError = candidateNumberError != null,
                        supportingText = { candidateNumberError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.weight(1f).testTag("edit_number_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SunlitGold,
                            focusedLabelColor = SunlitGold,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = ageString,
                        onValueChange = {
                            ageString = it
                            val parsed = it.toIntOrNull()
                            ageError = when {
                                it.isBlank() -> "Required"
                                parsed == null || parsed <= 0 -> "Invalid age"
                                else -> null
                            }
                        },
                        label = { Text("Age") },
                        isError = ageError != null,
                        supportingText = { ageError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("edit_age_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SunlitGold,
                            focusedLabelColor = SunlitGold,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Region field
                OutlinedTextField(
                    value = region,
                    onValueChange = {
                        region = it
                        regionError = if (it.isBlank()) "Region cannot be empty" else null
                    },
                    label = { Text("Representing Region/Country") },
                    isError = regionError != null,
                    supportingText = { regionError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth().testTag("edit_region_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SunlitGold,
                        focusedLabelColor = SunlitGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Platform statement
                OutlinedTextField(
                    value = platform,
                    onValueChange = {
                        platform = it
                        platformError = if (it.isBlank()) "Platform cannot be empty" else null
                    },
                    label = { Text("Platform Statement") },
                    isError = platformError != null,
                    supportingText = { platformError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth().testTag("edit_platform_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SunlitGold,
                        focusedLabelColor = SunlitGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Biography field (multiline)
                OutlinedTextField(
                    value = biography,
                    onValueChange = {
                        biography = it
                        biographyError = if (it.isBlank()) "Biography cannot be empty" else null
                    },
                    label = { Text("Biography") },
                    isError = biographyError != null,
                    supportingText = { biographyError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp).testTag("edit_bio_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SunlitGold,
                        focusedLabelColor = SunlitGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    maxLines = 10
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Action buttons: Cancel & Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CANCEL", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val finalAge = ageString.toIntOrNull() ?: contestant.age
                            if (name.isNotBlank() && candidateNumber.isNotBlank() && finalAge > 0 && region.isNotBlank() && platform.isNotBlank() && biography.isNotBlank()) {
                                onSave(
                                    contestant.copy(
                                        name = name.trim(),
                                        candidateNumber = candidateNumber.trim(),
                                        age = finalAge,
                                        region = region.trim(),
                                        platform = platform.trim(),
                                        biography = biography.trim()
                                    )
                                )
                            } else {
                                if (name.isBlank()) nameError = "Name is required"
                                if (candidateNumber.isBlank()) candidateNumberError = "Required"
                                if (ageString.isBlank() || finalAge <= 0) ageError = "Invalid age"
                                if (region.isBlank()) regionError = "Region is required"
                                if (platform.isBlank()) platformError = "Platform is required"
                                if (biography.isBlank()) biographyError = "Biography is required"
                            }
                        },
                        modifier = Modifier.weight(1.5f).height(50.dp).testTag("save_contestant_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = SunlitGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("SAVE CHANGES", fontWeight = FontWeight.Bold, color = DarkSatinBg)
                    }
                }
            }
        }
    }
}

// --- LEADERBOARD SCREEN ---
@Composable
fun LeaderboardScreen(
    contestants: List<Contestant>,
    onVoteClick: (Contestant) -> Unit
) {
    val totalVotes = remember(contestants) { contestants.sumOf { it.votes }.coerceAtLeast(1) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("leaderboard_screen"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Podium Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(RoyalPurple.copy(alpha = 0.4f), Color.Transparent)
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "LIVE LEADERBOARD",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SunlitGold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "The Race to the Crown",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Podium Layout
                    if (contestants.size >= 3) {
                        PodiumLayout(
                            first = contestants.getOrNull(0),
                            second = contestants.getOrNull(1),
                            third = contestants.getOrNull(2)
                        )
                    }
                }
            }
        }

        // Leaderboard List Title
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Global Standing",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "%,d Votes Cast".format(totalVotes),
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Standing lists
        items(contestants.sortedByDescending { it.votes }) { contestant ->
            val position = contestants.sortedByDescending { it.votes }.indexOf(contestant) + 1
            val percentage = (contestant.votes.toDouble() / totalVotes * 100).coerceIn(0.0, 100.0)

            LeaderboardRow(
                position = position,
                contestant = contestant,
                percentage = percentage,
                onVoteClick = { onVoteClick(contestant) }
            )
        }
    }
}

@Composable
fun PodiumLayout(
    first: Contestant?,
    second: Contestant?,
    third: Contestant?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        // Second Place (Left)
        second?.let {
            PodiumColumn(
                contestant = it,
                rank = 2,
                podiumHeight = 110.dp,
                badgeColor = Color(0xFFC0C0C0) // Silver
            )
        }

        // First Place (Center, Tallest)
        first?.let {
            PodiumColumn(
                contestant = it,
                rank = 1,
                podiumHeight = 140.dp,
                badgeColor = SunlitGold // Gold
            )
        }

        // Third Place (Right)
        third?.let {
            PodiumColumn(
                contestant = it,
                rank = 3,
                podiumHeight = 90.dp,
                badgeColor = Color(0xFFCD7F32) // Bronze
            )
        }
    }
}

@Composable
fun PodiumColumn(
    contestant: Contestant,
    rank: Int,
    podiumHeight: androidx.compose.ui.unit.Dp,
    badgeColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        // Avatar circle
        Box(
            modifier = Modifier
                .size(if (rank == 1) 70.dp else 56.dp)
                .border(2.dp, badgeColor, CircleShape)
                .padding(3.dp)
                .clip(CircleShape)
        ) {
            Image(
                painter = painterResource(id = getDrawableId(contestant.imageResName)),
                contentDescription = contestant.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Candidate Name
        Text(
            text = contestant.name.split(" ").firstOrNull() ?: contestant.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Vote total
        Text(
            text = "%,d".format(contestant.votes),
            fontSize = 11.sp,
            color = SunlitGold,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Actual Podium Block
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(podiumHeight),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (rank == 1) RoyalPurple else DeepSurface
            ),
            border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$rank",
                        fontSize = if (rank == 1) 32.sp else 24.sp,
                        fontWeight = FontWeight.Black,
                        color = badgeColor
                    )
                    Text(
                        text = if (rank == 1) "CROWN" else "RUNNER",
                        fontSize = 8.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun LeaderboardRow(
    position: Int,
    contestant: Contestant,
    percentage: Double,
    onVoteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = DeepSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placement rank
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = when (position) {
                            1 -> SunlitGold.copy(alpha = 0.2f)
                            2 -> Color.LightGray.copy(alpha = 0.2f)
                            3 -> Color(0xFFCD7F32).copy(alpha = 0.2f)
                            else -> Color.White.copy(alpha = 0.05f)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$position",
                    fontWeight = FontWeight.Bold,
                    color = when (position) {
                        1 -> SunlitGold
                        2 -> Color.LightGray
                        3 -> Color(0xFFCD7F32)
                        else -> Color.White
                    },
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Candidate Avatar
            Image(
                painter = painterResource(id = getDrawableId(contestant.imageResName)),
                contentDescription = contestant.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Candidate Name & Progress
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = contestant.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Custom Live Progress Bar
                LinearProgressIndicator(
                    progress = { (percentage / 100.0).toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (position == 1) SunlitGold else VelvetOrchid,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "%,d votes".format(contestant.votes),
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "%.1f%%".format(percentage),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (position == 1) SunlitGold else VelvetOrchid
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Fast Vote icon
            IconButton(
                onClick = onVoteClick,
                modifier = Modifier
                    .background(RoyalPurple.copy(alpha = 0.2f), CircleShape)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Quick vote",
                    tint = SunlitGold,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// --- SECURE PAYMENT PROCESS DIALOG ---
@Composable
fun SecurePaymentDialog(
    contestant: Contestant,
    paymentUiState: PaymentUiState,
    onInitialize: (votes: Int, phone: String, provider: String) -> Unit,
    onConfirmPin: (String) -> Unit,
    onVerifyOtp: (String) -> Unit,
    onCancel: () -> Unit,
    onSuccessDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = {
            if (paymentUiState is PaymentUiState.Idle || paymentUiState is PaymentUiState.Success || paymentUiState is PaymentUiState.Error) {
                onCancel()
            }
        },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("secure_payment_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = DeepSurface,
            tonalElevation = 16.dp,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Header showing secure layer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF4CAF50).copy(alpha = 0.15f), CircleShape)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Https,
                            contentDescription = "Secure Payment",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SECURE VOTING GATEWAY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Mobile Money Checkout",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Divider(
                    color = Color.White.copy(alpha = 0.08f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // State content routing
                when (paymentUiState) {
                    is PaymentUiState.Idle -> {
                        InitializePaymentLayout(
                            contestant = contestant,
                            onInitialize = onInitialize,
                            onCancel = onCancel
                        )
                    }
                    is PaymentUiState.Initiated -> {
                        PaymentProcessingLayout(status = "Initializing secure payment channels...")
                    }
                    is PaymentUiState.Processing -> {
                        PaymentProcessingLayout(status = paymentUiState.statusMsg)
                    }
                    is PaymentUiState.AwaitingPin -> {
                        AwaitingPinLayout(
                            state = paymentUiState,
                            onConfirmPin = onConfirmPin,
                            onCancel = onCancel
                        )
                    }
                    is PaymentUiState.AwaitingOtp -> {
                        AwaitingOtpLayout(
                            state = paymentUiState,
                            onVerifyOtp = onVerifyOtp,
                            onCancel = onCancel
                        )
                    }
                    is PaymentUiState.Success -> {
                        PaymentSuccessLayout(
                            txn = paymentUiState.transaction,
                            onDismiss = onSuccessDismiss
                        )
                    }
                    is PaymentUiState.Error -> {
                        PaymentErrorLayout(
                            message = paymentUiState.message,
                            onDismiss = onCancel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InitializePaymentLayout(
    contestant: Contestant,
    onInitialize: (votes: Int, phone: String, provider: String) -> Unit,
    onCancel: () -> Unit
) {
    var votesToBuy by remember { mutableStateOf(10) }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf("MTN MoMo") }
    var hasError by remember { mutableStateOf(false) }

    val providers = listOf(
        Pair("MTN MoMo", Color(0xFFFFCC00)),    // Yellow
        Pair("M-Pesa", Color(0xFF4CAF50)),      // Green
        Pair("Orange Money", Color(0xFFFF6600)),// Orange
        Pair("Airtel Money", Color(0xFFE50914)) // Red
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Supporter Vote Allocation:",
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Preset Votes Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf(5, 10, 50, 100).forEach { value ->
                val isSelected = votesToBuy == value
                val amountText = "$%,.2f".format(value * 0.50)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .clickable { votesToBuy = value },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) RoyalPurple else Color.White.copy(alpha = 0.04f)
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) SunlitGold else Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$value",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            text = amountText,
                            fontSize = 10.sp,
                            color = if (isSelected) SunlitGold else Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Provider grid
        Text(
            text = "Select Mobile Money Operator:",
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            providers.forEach { (name, brandColor) ->
                val isSelected = selectedProvider == name
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .background(
                            color = if (isSelected) brandColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) brandColor else Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedProvider = name }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.split(" ").first(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) brandColor else Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Phone Number Input
        Text(
            text = "Wallet Phone Number:",
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = {
                if (it.all { char -> char.isDigit() } && it.length <= 10) {
                    phoneNumber = it
                    hasError = false
                }
            },
            placeholder = { Text("e.g. 0541234567", color = Color.White.copy(alpha = 0.3f)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("payment_phone_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = RoyalPurple,
                unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
            ),
            isError = hasError
        )
        if (hasError) {
            Text(
                text = "Please enter a valid 10-digit mobile wallet number.",
                color = Color.Red,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Cost details
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Secure Payment:",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Text(
                    text = "USD \$%,.2f".format(votesToBuy * 0.50),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SunlitGold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cancel / Submit Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onCancel,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.6f))
            ) {
                Text("CANCEL")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = {
                    if (phoneNumber.length < 9) {
                        hasError = true
                    } else {
                        onInitialize(votesToBuy, phoneNumber, selectedProvider)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VelvetOrchid),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("submit_payment_btn")
            ) {
                Text("VOTE NOW", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PaymentProcessingLayout(status: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = SunlitGold,
            strokeWidth = 3.dp,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = status,
            fontSize = 14.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Do not close or leave this screen.",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun AwaitingPinLayout(
    state: PaymentUiState.AwaitingPin,
    onConfirmPin: (String) -> Unit,
    onCancel: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    val maxPinLength = 4

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Provider simulated USSD prompt style
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.provider.uppercase(),
                    color = SunlitGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Authorize transaction to: CROWN VOTE LTD",
                    fontSize = 13.sp,
                    color = Color.White
                )
                Text(
                    text = "Amount: USD \$%,.2f".format(state.amount),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SunlitGold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Enter 4-Digit Wallet PIN:",
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Clean Masked PIN Entry
        OutlinedTextField(
            value = pin,
            onValueChange = {
                if (it.all { char -> char.isDigit() } && it.length <= maxPinLength) {
                    pin = it
                }
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier
                .width(130.dp)
                .testTag("pin_input_field"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = SunlitGold,
                unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
            ),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                letterSpacing = 6.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onCancel) {
                Text("CANCEL TRANSACTION", color = Color.Red.copy(alpha = 0.8f))
            }
            Button(
                onClick = { if (pin.length == maxPinLength) onConfirmPin(pin) },
                enabled = pin.length == maxPinLength,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("pin_confirm_btn")
            ) {
                Text("AUTHORIZE", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AwaitingOtpLayout(
    state: PaymentUiState.AwaitingOtp,
    onVerifyOtp: (String) -> Unit,
    onCancel: () -> Unit
) {
    var otpInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Simulated SMS header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = "Simulated SMS",
                        tint = SunlitGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SIMULATED SECURITY CODE SMS",
                        fontSize = 10.sp,
                        color = SunlitGold,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "A secure verification code of 2FA is sent to ${state.phoneNumber}: Your CrownVote security validation OTP is ${state.otpCode}.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Enter OTP to Complete Secure Payment:",
            fontSize = 13.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = otpInput,
            onValueChange = {
                if (it.all { char -> char.isDigit() } && it.length <= 4) {
                    otpInput = it
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text("xxxx", color = Color.White.copy(alpha = 0.2f)) },
            modifier = Modifier
                .width(130.dp)
                .testTag("otp_input_field"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = SunlitGold,
                unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
            ),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                letterSpacing = 6.sp,
                fontWeight = FontWeight.Bold
            )
        )

        state.errorMsg?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onCancel) {
                Text("CANCEL", color = Color.Red.copy(alpha = 0.8f))
            }
            Button(
                onClick = { if (otpInput.length == 4) onVerifyOtp(otpInput) },
                enabled = otpInput.length == 4,
                colors = ButtonDefaults.buttonColors(containerColor = VelvetOrchid),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("otp_verify_btn")
            ) {
                Text("VERIFY & PAY", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PaymentSuccessLayout(
    txn: VoteTransaction,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Success checkmark with scaling animation
        Box(
            modifier = Modifier
                .background(Color(0xFF4CAF50).copy(alpha = 0.12f), CircleShape)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Transaction Successful!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Your votes have been submitted to the live ledger.",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Receipt Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                ReceiptRow(label = "Reference ID", value = txn.transactionId)
                ReceiptRow(label = "To Candidate", value = txn.contestantName)
                ReceiptRow(label = "Votes Added", value = "+${txn.votesCount}")
                ReceiptRow(label = "Amount Charged", value = "USD \$%,.2f".format(txn.amount))
                ReceiptRow(label = "Payment System", value = txn.provider)
                ReceiptRow(label = "Status", value = "SECURE / COMPLETED", valueColor = Color(0xFF4CAF50))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dismiss_success_btn")
        ) {
            Text("PROCEED TO LEADERBOARD", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ReceiptRow(
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun PaymentErrorLayout(
    message: String,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            tint = Color.Red,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Payment Failed",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("DISMISS", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// --- HISTORY LEDGER SCREEN ---
@Composable
fun HistoryScreen(
    transactions: List<VoteTransaction>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("history_screen")
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "SECURE AUDIT TRAILS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = SunlitGold,
            letterSpacing = 1.sp
        )
        Text(
            text = "Voting Ledger History",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "A complete, immutable timeline of transactions validated on the secure payment gateway.",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No votes recorded yet",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Initialize a simulated Mobile Money voting flow to see secure transaction ledgers here.",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(transactions) { txn ->
                    TransactionItemRow(txn = txn)
                }
            }
        }
    }
}

@Composable
fun TransactionItemRow(txn: VoteTransaction) {
    val dateString = remember(txn.timestamp) {
        val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        formatter.format(Date(txn.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DeepSurface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle icon indicating payment provider or status
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (txn.status == "Completed") Color(0xFF4CAF50).copy(alpha = 0.1f)
                        else Color.Red.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (txn.status == "Completed") Icons.Default.HowToVote else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (txn.status == "Completed") Color(0xFF4CAF50) else Color.Red,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Transaction Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${txn.votesCount} Votes to ${txn.contestantName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
                Text(
                    text = "Ref: ${txn.transactionId} • ${txn.provider}",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Text(
                    text = dateString,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }

            // Amount & Status
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "\$${"%,.2f".format(txn.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SunlitGold
                )
                Box(
                    modifier = Modifier
                        .background(
                            color = if (txn.status == "Completed") Color(0xFF4CAF50).copy(alpha = 0.15f)
                            else Color.Red.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = txn.status.uppercase(),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = if (txn.status == "Completed") Color(0xFF4CAF50) else Color.Red
                    )
                }
            }
        }
    }
}
