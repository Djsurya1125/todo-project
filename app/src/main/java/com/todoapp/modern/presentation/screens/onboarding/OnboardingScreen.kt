package com.todoapp.modern.presentation.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingPage(
                page = onboardingPages[page],
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Bottom navigation
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(onboardingPages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip button
                AnimatedVisibility(
                    visible = pagerState.currentPage < onboardingPages.size - 1,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    TextButton(
                        onClick = onComplete
                    ) {
                        Text("Skip")
                    }
                }
                
                if (pagerState.currentPage == onboardingPages.size - 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
                
                // Next/Get Started button
                Button(
                    onClick = {
                        if (pagerState.currentPage < onboardingPages.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onComplete()
                        }
                    },
                    modifier = Modifier
                        .height(48.dp)
                        .let { 
                            if (pagerState.currentPage == onboardingPages.size - 1) 
                                it.fillMaxWidth() 
                            else it
                        },
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage < onboardingPages.size - 1) 
                            "Next" 
                        else 
                            "Get Started",
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (pagerState.currentPage < onboardingPages.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPage(
    page: OnboardingPageData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        Card(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = page.iconBackgroundColor
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = page.iconColor
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Title
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
        )
    }
}

private data class OnboardingPageData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color,
    val iconBackgroundColor: Color
)

private val onboardingPages = listOf(
    OnboardingPageData(
        title = "Welcome to Modern Todo",
        description = "Stay organized and boost your productivity with our intuitive task management app. Create, organize, and track your tasks effortlessly.",
        icon = Icons.Default.CheckCircle,
        iconColor = Color(0xFF4CAF50),
        iconBackgroundColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
    ),
    OnboardingPageData(
        title = "Smart Organization",
        description = "Categorize your tasks by work, personal, shopping, and more. Set priorities and due dates to never miss important deadlines.",
        icon = Icons.Default.Category,
        iconColor = Color(0xFF2196F3),
        iconBackgroundColor = Color(0xFF2196F3).copy(alpha = 0.1f)
    ),
    OnboardingPageData(
        title = "Never Forget Again",
        description = "Set reminders for your important tasks and get notifications when they're due. Stay on top of your schedule with smart alerts.",
        icon = Icons.Default.Notifications,
        iconColor = Color(0xFFFF9800),
        iconBackgroundColor = Color(0xFFFF9800).copy(alpha = 0.1f)
    ),
    OnboardingPageData(
        title = "Quick Access Widget",
        description = "Add our home screen widget to quickly view and manage your tasks without opening the app. Perfect for busy schedules!",
        icon = Icons.Default.Dashboard,
        iconColor = Color(0xFF9C27B0),
        iconBackgroundColor = Color(0xFF9C27B0).copy(alpha = 0.1f)
    )
)
