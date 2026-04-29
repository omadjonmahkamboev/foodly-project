package com.example.fooddeliveryapp.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.HeadsetMic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fooddeliveryapp.ui.screens.AuthScreen
import com.example.fooddeliveryapp.ui.screens.AddPaymentCardScreen
import com.example.fooddeliveryapp.ui.screens.AddressListScreen
import com.example.fooddeliveryapp.ui.screens.AdminDashboardScreen
import com.example.fooddeliveryapp.ui.screens.AiAssistantScreen
import com.example.fooddeliveryapp.ui.screens.CartScreen
import com.example.fooddeliveryapp.ui.screens.CouponsScreen
import com.example.fooddeliveryapp.ui.screens.CourierAuthScreen
import com.example.fooddeliveryapp.ui.screens.CourierChatScreen
import com.example.fooddeliveryapp.ui.screens.CourierEditProfileScreen
import com.example.fooddeliveryapp.ui.screens.CourierHomeScreen
import com.example.fooddeliveryapp.ui.screens.CourierMessagesScreen
import com.example.fooddeliveryapp.ui.screens.CourierOrderDetailsScreen
import com.example.fooddeliveryapp.ui.screens.CourierProfileScreen
import com.example.fooddeliveryapp.ui.screens.CourierReviewsScreen
import com.example.fooddeliveryapp.ui.screens.CourierSettingsScreen
import com.example.fooddeliveryapp.ui.screens.CustomerOrderChatScreen
import com.example.fooddeliveryapp.ui.screens.EditProfileScreen
import com.example.fooddeliveryapp.ui.screens.FaqScreen
import com.example.fooddeliveryapp.ui.screens.FoodDetailsScreen
import com.example.fooddeliveryapp.ui.screens.FavoriteScreen
import com.example.fooddeliveryapp.ui.screens.HomeScreen
import com.example.fooddeliveryapp.ui.screens.LocationIntroScreen
import com.example.fooddeliveryapp.ui.screens.MapPickerScreen
import com.example.fooddeliveryapp.ui.screens.NotificationsScreen
import com.example.fooddeliveryapp.ui.screens.OnboardingScreen
import com.example.fooddeliveryapp.ui.screens.OrdersScreen
import com.example.fooddeliveryapp.ui.screens.PaymentScreen
import com.example.fooddeliveryapp.ui.screens.PersonalInfoScreen
import com.example.fooddeliveryapp.ui.screens.ProfileScreen
import com.example.fooddeliveryapp.ui.screens.RestaurantScreen
import com.example.fooddeliveryapp.ui.screens.SearchScreen
import com.example.fooddeliveryapp.ui.screens.SettingsScreen
import com.example.fooddeliveryapp.ui.screens.SplashScreen
import com.example.fooddeliveryapp.ui.screens.SuccessScreen
import com.example.fooddeliveryapp.ui.screens.TrackOrderScreen
import com.example.fooddeliveryapp.ui.data.MenuItem
import com.example.fooddeliveryapp.ui.theme.Orange
import com.example.fooddeliveryapp.ui.theme.CardWhite
import com.example.fooddeliveryapp.ui.theme.FoodDeliveryAppTheme
import kotlinx.coroutines.launch

@Composable
fun FoodDeliveryApp() {
    val navController = rememberNavController()
    val appState = rememberFoodAppState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AppRoute.Splash
    val strings = remember(appState.language) { AppStrings(appState.language) }
    val soundPlayer = rememberAppSoundPlayer()
    val useDarkTheme = appState.themeMode == AppThemeMode.Dark && currentRoute != AppRoute.AdminDashboard
    val addToCartWithFeedback: (MenuItem) -> Unit = { item ->
        appState.addToCart(item)
        soundPlayer.playAddToCart()
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = strings.addedToCart(item.title),
                actionLabel = strings.openCartAction,
                withDismissAction = true,
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) {
                navController.navigate(AppRoute.Cart) {
                    launchSingleTop = true
                }
            }
        }
    }
    val toggleFavoriteWithFeedback: (MenuItem) -> Unit = appState::toggleFavorite

    LaunchedEffect(appState) {
        appState.restoreSavedSession()
    }

    LaunchedEffect(appState.lastFavoriteAddedEventId) {
        if (appState.lastFavoriteAddedEventId != null) {
            soundPlayer.playFavoriteAdded()
        }
    }

    LaunchedEffect(appState.lastNotificationEventId) {
        if (appState.lastNotificationEventId != null) {
            soundPlayer.playNotification()
        }
    }

    LaunchedEffect(appState.lastDeliveredOrderEventId) {
        if (appState.lastDeliveredOrderEventId != null) {
            soundPlayer.playDeliveryArrived()
        }
    }

    FoodDeliveryAppTheme(
        darkTheme = useDarkTheme,
        dynamicColor = false,
    ) {
        CompositionLocalProvider(
            LocalAppLanguage provides appState.language,
            LocalAppStrings provides strings,
        ) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    when {
                        currentRoute in AppRoute.bottomRoutes -> {
                            MainBottomBar(
                                currentRoute = currentRoute,
                                strings = strings,
                                onNavigate = { route -> navigateToTopLevel(navController, route) },
                            )
                        }
                        currentRoute in AppRoute.courierBottomRoutes -> {
                            CourierBottomBar(
                                currentRoute = currentRoute,
                                strings = strings,
                                onNavigate = { route -> navigateToCourierTopLevel(navController, route) },
                            )
                        }
                    }
                },
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = AppRoute.Splash,
                    modifier = Modifier.padding(innerPadding),
                    enterTransition = { appEnterTransition() },
                    exitTransition = { appExitTransition() },
                    popEnterTransition = { appPopEnterTransition() },
                    popExitTransition = { appPopExitTransition() },
                ) {
                composable(AppRoute.Splash) {
                    SplashScreen(
                        isReady = appState.isStartStateReady,
                        returningUser = appState.currentUser != null,
                        onFinished = {
                            navController.navigate(startRouteAfterSplash(appState)) {
                                popUpTo(AppRoute.Splash) { inclusive = true }
                            }
                        },
                    )
                }
                composable(AppRoute.Onboarding) {
                    OnboardingScreen(
                        slides = appState.onboarding,
                        onFinish = {
                            navController.navigate(AppRoute.Auth) {
                                popUpTo(AppRoute.Onboarding) { inclusive = true }
                            }
                        },
                    )
                }
                composable(AppRoute.AdminDashboard) {
                    AdminDashboardScreen(
                        appState = appState,
                        onBackToLogin = {
                            navController.navigate(AppRoute.Auth) {
                                popUpTo(AppRoute.AdminDashboard) { inclusive = true }
                            }
                        },
                    )
                }
                composable(AppRoute.Auth) {
                    AuthScreen(
                        onLogin = appState::login,
                        onRegister = appState::register,
                        onRequestRegistrationVerification = appState::requestRegistrationVerificationCode,
                        onVerifyRegistrationCode = appState::verifyRegistrationCode,
                        onAdminLogin = appState::adminLogin,
                        onRequestPasswordReset = appState::requestPasswordResetCode,
                        onResetPassword = appState::resetPassword,
                        onGoogleSignIn = appState::loginWithGoogle,
                        onAuthenticated = {
                            navController.navigate(routeAfterAuthentication(appState)) {
                                popUpTo(AppRoute.Auth) { inclusive = true }
                            }
                        },
                        onAdminAuthenticated = {
                            navController.navigate(AppRoute.AdminDashboard) {
                                popUpTo(AppRoute.Auth) { inclusive = true }
                            }
                        },
                        onOpenCourier = { navController.navigate(AppRoute.CourierAuth) },
                    )
                }
                composable(AppRoute.CourierAuth) {
                    CourierAuthScreen(
                        onLogin = appState::courierLogin,
                        onRegister = appState::courierRegister,
                        onAuthenticated = {
                            navController.navigate(AppRoute.CourierHome) {
                                popUpTo(AppRoute.CourierAuth) { inclusive = true }
                            }
                        },
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(AppRoute.CourierHome) {
                    CourierHomeScreen(
                        appState = appState,
                        onOpenOrder = { orderId ->
                            appState.selectCourierOrder(orderId)
                            navController.navigate(AppRoute.CourierOrderDetails)
                        },
                        onOpenChat = { orderId ->
                            appState.selectCourierOrder(orderId)
                            navController.navigate(AppRoute.CourierChat)
                        },
                        onOpenProfile = { navController.navigate(AppRoute.CourierProfile) },
                    )
                }
                composable(AppRoute.CourierOrderDetails) {
                    CourierOrderDetailsScreen(
                        appState = appState,
                        onBack = { navController.popBackStack() },
                        onOpenChat = { orderId ->
                            appState.selectCourierOrder(orderId)
                            navController.navigate(AppRoute.CourierChat)
                        },
                    )
                }
                composable(AppRoute.CourierChat) {
                    CourierChatScreen(
                        appState = appState,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(AppRoute.CourierMessages) {
                    CourierMessagesScreen(
                        appState = appState,
                        onOpenChat = { orderId ->
                            appState.selectCourierOrder(orderId)
                            navController.navigate(AppRoute.CourierChat)
                        },
                    )
                }
                composable(AppRoute.CourierReviews) {
                    CourierReviewsScreen(appState = appState)
                }
                composable(AppRoute.CourierProfile) {
                    CourierProfileScreen(
                        appState = appState,
                        onBack = { navController.popBackStack() },
                        onEdit = { navController.navigate(AppRoute.CourierEditProfile) },
                        onOpenSettings = { navController.navigate(AppRoute.CourierSettings) },
                        onLogOut = {
                            appState.logOutCourier()
                            navController.navigate(AppRoute.Auth) {
                                popUpTo(AppRoute.CourierHome) { inclusive = true }
                            }
                        },
                    )
                }
                composable(AppRoute.CourierSettings) {
                    CourierSettingsScreen(
                        appState = appState,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(AppRoute.CourierEditProfile) {
                    CourierEditProfileScreen(
                        appState = appState,
                        onBack = { navController.popBackStack() },
                        onSaved = { navController.popBackStack() },
                    )
                }
                composable(AppRoute.LocationIntro) {
                    LocationIntroScreen(
                        onLocationConfirmed = { address ->
                            appState.updateAddress(address)
                            navController.navigate(AppRoute.Home) {
                                popUpTo(AppRoute.LocationIntro) { inclusive = true }
                            }
                        },
                        onPickManually = {
                            appState.beginAddAddress()
                            navController.navigate(AppRoute.MapPicker)
                        },
                    )
                }
                composable(AppRoute.MapPicker) {
                    MapPickerScreen(
                        currentAddress = appState.addressDraft,
                        onBack = {
                            appState.clearAddressDraft()
                            navController.popBackStack()
                        },
                        onConfirm = { address ->
                            appState.updateAddress(address)
                            if (navController.previousBackStackEntry?.destination?.route == AppRoute.LocationIntro) {
                                navController.navigate(AppRoute.Home) {
                                    popUpTo(AppRoute.LocationIntro) { inclusive = true }
                                }
                            } else {
                                navController.popBackStack()
                            }
                        },
                    )
                }
                composable(AppRoute.Home) {
                    HomeScreen(
                        appState = appState,
                        onOpenSearch = { navController.navigate(AppRoute.Search) },
                        onOpenRestaurant = { restaurantId ->
                            appState.selectRestaurant(restaurantId)
                            navController.navigate(AppRoute.Restaurant)
                        },
                        onOpenCart = { navController.navigate(AppRoute.Cart) },
                        onEditAddress = {
                            appState.clearAddressDraft()
                            navController.navigate(AppRoute.MapPicker)
                        },
                    )
                }
                composable(AppRoute.Search) {
                    SearchScreen(
                        appState = appState,
                        onBack = { navController.popBackStack() },
                        onOpenCart = { navController.navigate(AppRoute.Cart) },
                        onOpenRestaurant = { restaurantId ->
                            appState.selectRestaurant(restaurantId)
                            navController.navigate(AppRoute.Restaurant)
                        },
                        onOpenFood = { item ->
                            appState.selectMenuItem(item.id)
                            navController.navigate(AppRoute.FoodDetails)
                        },
                        onAddToCart = addToCartWithFeedback,
                    )
                }
                composable(AppRoute.Restaurant) {
                    RestaurantScreen(
                        restaurant = appState.selectedRestaurant,
                        cartCount = appState.cartCount,
                        onBack = { navController.popBackStack() },
                        onOpenFood = { item ->
                            appState.selectMenuItem(item.id)
                            navController.navigate(AppRoute.FoodDetails)
                        },
                        onAddToCart = addToCartWithFeedback,
                        isFavorite = appState::isFavorite,
                        onToggleFavorite = toggleFavoriteWithFeedback,
                        onOpenCart = { navController.navigate(AppRoute.Cart) },
                    )
                }
                composable(AppRoute.FoodDetails) {
                    FoodDetailsScreen(
                        item = appState.selectedMenuItem,
                        restaurant = appState.selectedMenuRestaurant,
                        onBack = { navController.popBackStack() },
                        onAddToCart = addToCartWithFeedback,
                        onDecreaseCartQuantity = { itemId -> appState.changeQuantity(itemId, -1) },
                        cartQuantityForItem = appState::cartQuantity,
                        isFavorite = appState::isFavorite,
                        onToggleFavorite = toggleFavoriteWithFeedback,
                    )
                }
                composable(AppRoute.Cart) {
                    CartScreen(
                        appState = appState,
                        onBack = { navController.popBackStack() },
                        onEditAddress = {
                            appState.beginAddAddress()
                            navController.navigate(AppRoute.MapPicker)
                        },
                        onCheckout = { navController.navigate(AppRoute.CheckoutPayment) },
                        onContinueShopping = { navigateToTopLevel(navController, AppRoute.Home) },
                    )
                }
                composable(AppRoute.Payment) {
                    PaymentScreen(
                        appState = appState,
                        checkoutMode = false,
                        onBack = { navController.popBackStack() },
                        onAddCard = { navController.navigate(AppRoute.AddPaymentCard) },
                        onPay = {},
                    )
                }
                composable(AppRoute.CheckoutPayment) {
                    PaymentScreen(
                        appState = appState,
                        checkoutMode = true,
                        onBack = { navController.popBackStack() },
                        onAddCard = { navController.navigate(AppRoute.AddPaymentCard) },
                        onPay = {
                            scope.launch {
                                if (appState.placeOrder()) {
                                    soundPlayer.playOrderPlaced()
                                    navController.navigate(AppRoute.Success) {
                                        popUpTo(AppRoute.CheckoutPayment) { inclusive = true }
                                    }
                                }
                            }
                        },
                    )
                }
                composable(AppRoute.Success) {
                    val order = appState.lastPlacedOrder ?: appState.trackedOrder
                    if (order != null) {
                        SuccessScreen(
                            order = order,
                            onTrack = { navController.navigate(AppRoute.Track) },
                            onGoHome = { navigateHomeClearingStack(navController) },
                        )
                    }
                }
                composable(AppRoute.Orders) {
                    OrdersScreen(
                        appState = appState,
                        onBack = { navigateToTopLevel(navController, AppRoute.Home) },
                        onTrack = { orderId ->
                            appState.track(orderId)
                            navController.navigate(AppRoute.Track)
                        },
                        onOpenChat = { orderId ->
                            appState.track(orderId)
                            navController.navigate(AppRoute.OrderChat)
                        },
                        onReorder = { order ->
                            appState.reorder(order)
                            navController.navigate(AppRoute.Cart)
                        },
                    )
                }
                composable(AppRoute.OrderChat) {
                    CustomerOrderChatScreen(
                        appState = appState,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(AppRoute.Track) {
                    val order = appState.trackedOrder
                    if (order != null) {
                        val courierOrder = appState.courierOrderForOrder(order.id)
                        TrackOrderScreen(
                            order = order,
                            restaurant = appState.restaurantForOrder(order),
                            address = courierOrder?.customerAddress ?: appState.selectedAddress,
                            courierOrder = courierOrder,
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
                composable(AppRoute.Profile) {
                    ProfileScreen(
                        user = appState.currentUser,
                        profile = appState.profileDetails,
                        address = appState.selectedAddress,
                        paymentCardsCount = appState.savedPaymentCards.size,
                        couponCount = appState.activeCoupons.size,
                        favoriteCount = appState.favoriteItemIds.size,
                        notificationCount = appState.unreadNotificationCount,
                        language = appState.language,
                        onBack = { navController.popBackStack() },
                        onOpenPersonalInfo = { navController.navigate(AppRoute.PersonalInfo) },
                        onEditAddress = { navController.navigate(AppRoute.Addresses) },
                        onEditProfile = { navController.navigate(AppRoute.EditProfile) },
                        onOpenCart = { navController.navigate(AppRoute.Cart) },
                        onOpenFavorites = { navController.navigate(AppRoute.Favorites) },
                        onOpenCoupons = { navController.navigate(AppRoute.Coupons) },
                        onOpenNotifications = { navController.navigate(AppRoute.Notifications) },
                        onOpenPayment = { navController.navigate(AppRoute.Payment) },
                        onOpenFaqs = { navController.navigate(AppRoute.Faqs) },
                        onOpenSettings = { navController.navigate(AppRoute.Settings) },
                        onLogOut = {
                            appState.logOutCustomer()
                            navController.navigate(AppRoute.Auth) {
                                popUpTo(AppRoute.Home) { inclusive = true }
                            }
                        },
                    )
                }
                composable(AppRoute.Favorites) {
                    FavoriteScreen(
                        favorites = appState.favoriteItems,
                        restaurantForMenuItem = appState::restaurantForMenuItem,
                        onBack = { navController.popBackStack() },
                        onOpenFood = { item ->
                            appState.selectMenuItem(item.id)
                            navController.navigate(AppRoute.FoodDetails)
                        },
                        onAddToCart = { item ->
                            addToCartWithFeedback(item)
                            navController.navigate(AppRoute.Cart)
                        },
                        onRemoveFavorite = toggleFavoriteWithFeedback,
                    )
                }
                composable(AppRoute.AiAssistant) {
                    AiAssistantScreen(
                        appState = appState,
                        onBack = { navController.popBackStack() },
                        onOpenCart = { navController.navigate(AppRoute.Cart) },
                    )
                }
                composable(AppRoute.Coupons) {
                    LaunchedEffect(appState.currentUser?.id) {
                        appState.pruneExpiredCoupons()
                    }
                    CouponsScreen(
                        coupons = appState.activeCoupons,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(AppRoute.Notifications) {
                    LaunchedEffect(Unit) {
                        appState.markNotificationsRead()
                    }
                    NotificationsScreen(
                        notifications = appState.notifications,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(AppRoute.Faqs) {
                    FaqScreen(onBack = { navController.popBackStack() })
                }
                composable(AppRoute.AddPaymentCard) {
                    AddPaymentCardScreen(
                        appState = appState,
                        onClose = { navController.popBackStack() },
                        onSaved = { navController.popBackStack() },
                    )
                }
                composable(AppRoute.PersonalInfo) {
                    PersonalInfoScreen(
                        user = appState.currentUser,
                        profile = appState.profileDetails,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(AppRoute.Addresses) {
                    AddressListScreen(
                        addresses = appState.savedAddresses,
                        selectedAddress = appState.selectedAddress,
                        onBack = { navController.popBackStack() },
                        onSelectAddress = appState::selectSavedAddress,
                        onAddAddress = {
                            appState.beginAddAddress()
                            navController.navigate(AppRoute.MapPicker)
                        },
                        onEditAddress = { index ->
                            appState.beginEditAddress(index)
                            navController.navigate(AppRoute.MapPicker)
                        },
                        onDeleteAddress = appState::deleteSavedAddress,
                    )
                }
                composable(AppRoute.EditProfile) {
                    EditProfileScreen(
                        profile = appState.profileDetails,
                        language = appState.language,
                        onBack = { navController.popBackStack() },
                        onSave = { details ->
                            appState.updateProfile(details)
                            navController.popBackStack()
                        },
                    )
                }
                composable(AppRoute.Settings) {
                    SettingsScreen(
                        themeMode = appState.themeMode,
                        language = appState.language,
                        onBack = { navController.popBackStack() },
                        onThemeChange = appState::updateThemeMode,
                        onLanguageChange = appState::updateLanguage,
                    )
                }
                }
            }
        }
    }
}

private fun navigateToTopLevel(navController: NavHostController, route: String) {
    if (route == AppRoute.Home) {
        navigateHomeClearingStack(navController)
        return
    }

    navController.navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(AppRoute.Home) {
            saveState = true
        }
    }
}

private fun navigateHomeClearingStack(navController: NavHostController) {
    navController.navigate(AppRoute.Home) {
        launchSingleTop = true
        popUpTo(AppRoute.Home) {
            inclusive = true
            saveState = false
        }
    }
}

private fun navigateToCourierTopLevel(navController: NavHostController, route: String) {
    navController.navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(AppRoute.CourierHome) {
            saveState = true
        }
    }
}

private fun startRouteAfterSplash(appState: FoodAppState): String =
    if (appState.currentUser != null) {
        routeAfterAuthentication(appState)
    } else {
        AppRoute.Onboarding
    }

private fun routeAfterAuthentication(appState: FoodAppState): String =
    if (appState.hasConfirmedAddress) AppRoute.Home else AppRoute.LocationIntro

object AppRoute {
    const val Splash = "splash"
    const val Onboarding = "onboarding"
    const val Auth = "auth"
    const val AdminDashboard = "admin_dashboard"
    const val CourierAuth = "courier_auth"
    const val CourierHome = "courier_home"
    const val CourierOrderDetails = "courier_order_details"
    const val CourierChat = "courier_chat"
    const val CourierMessages = "courier_messages"
    const val CourierReviews = "courier_reviews"
    const val CourierProfile = "courier_profile"
    const val CourierSettings = "courier_settings"
    const val CourierEditProfile = "courier_edit_profile"
    const val LocationIntro = "location_intro"
    const val MapPicker = "map_picker"
    const val Home = "home"
    const val Search = "search"
    const val Restaurant = "restaurant"
    const val FoodDetails = "food_details"
    const val Cart = "cart"
    const val Payment = "payment"
    const val CheckoutPayment = "checkout_payment"
    const val Success = "success"
    const val Orders = "orders"
    const val OrderChat = "order_chat"
    const val Track = "track"
    const val Profile = "profile"
    const val PersonalInfo = "personal_info"
    const val Addresses = "addresses"
    const val EditProfile = "edit_profile"
    const val AiAssistant = "ai_assistant"
    const val Favorites = "favorites"
    const val Coupons = "coupons"
    const val Notifications = "notifications"
    const val Faqs = "faqs"
    const val AddPaymentCard = "add_payment_card"
    const val Settings = "settings"

    val bottomRoutes = setOf(Home, Search, AiAssistant, Orders, Profile)
    val courierBottomRoutes = setOf(CourierHome, CourierMessages, CourierReviews, CourierProfile)
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.appEnterTransition(): EnterTransition {
    val isBottomAnimation = initialState.destination.route in AppRoute.bottomRoutes &&
        targetState.destination.route in AppRoute.bottomRoutes
    val duration = if (isBottomAnimation) 380 else 280
    val direction = transitionDirection(initialState.destination.route, targetState.destination.route)
    return slideInHorizontally(
        animationSpec = tween(durationMillis = duration),
        initialOffsetX = { width -> (width / if (isBottomAnimation) 4 else 7) * direction },
    ) + fadeIn(animationSpec = tween(durationMillis = duration - 70))
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.appExitTransition(): ExitTransition {
    val isBottomAnimation = initialState.destination.route in AppRoute.bottomRoutes &&
        targetState.destination.route in AppRoute.bottomRoutes
    val duration = if (isBottomAnimation) 380 else 260
    val direction = transitionDirection(initialState.destination.route, targetState.destination.route)
    return slideOutHorizontally(
        animationSpec = tween(durationMillis = duration),
        targetOffsetX = { width -> -(width / if (isBottomAnimation) 5 else 9) * direction },
    ) + fadeOut(animationSpec = tween(durationMillis = duration - 90))
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.appPopEnterTransition(): EnterTransition =
    appEnterTransition()

private fun AnimatedContentTransitionScope<NavBackStackEntry>.appPopExitTransition(): ExitTransition =
    appExitTransition()

private fun transitionDirection(
    initialRoute: String?,
    targetRoute: String?,
): Int =
    if (bottomRouteIndex(targetRoute) >= bottomRouteIndex(initialRoute)) 1 else -1

private fun bottomRouteIndex(route: String?): Int = when (route) {
    AppRoute.Home -> 0
    AppRoute.Search -> 1
    AppRoute.AiAssistant -> 2
    AppRoute.Orders -> 3
    AppRoute.Profile -> 4
    AppRoute.CourierHome -> 0
    AppRoute.CourierMessages -> 1
    AppRoute.CourierReviews -> 2
    AppRoute.CourierProfile -> 3
    else -> 0
}

private data class BottomDestination(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit,
)

@Composable
private fun MainBottomBar(
    currentRoute: String,
    strings: AppStrings,
    onNavigate: (String) -> Unit,
) {
    val items = listOf(
        BottomDestination(AppRoute.Home, strings.home) { Icon(Icons.Default.Home, null) },
        BottomDestination(AppRoute.Search, strings.search) { Icon(Icons.Default.Search, null) },
        BottomDestination(AppRoute.AiAssistant, "AI") { AiNavigationIcon(selected = currentRoute == AppRoute.AiAssistant) },
        BottomDestination(AppRoute.Orders, strings.orders) { Icon(Icons.Default.LocalShipping, null) },
        BottomDestination(AppRoute.Profile, strings.profile) { Icon(Icons.Default.PersonOutline, null) },
    )

    Surface(
        tonalElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(item.route) },
                    icon = { item.icon() },
                    label = { Text(item.label) },
                )
            }
        }
    }
}

@Composable
private fun CourierBottomBar(
    currentRoute: String,
    strings: AppStrings,
    onNavigate: (String) -> Unit,
) {
    val items = listOf(
        BottomDestination(AppRoute.CourierHome, strings.orders) {
            AnimatedBottomIcon(selected = currentRoute == AppRoute.CourierHome) {
                Icon(Icons.Default.LocalShipping, null)
            }
        },
        BottomDestination(AppRoute.CourierMessages, strings.messages) {
            AnimatedBottomIcon(selected = currentRoute == AppRoute.CourierMessages) {
                Icon(Icons.Default.Chat, null)
            }
        },
        BottomDestination(AppRoute.CourierReviews, strings.reviews) {
            AnimatedBottomIcon(selected = currentRoute == AppRoute.CourierReviews) {
                Icon(Icons.Default.RateReview, null)
            }
        },
        BottomDestination(AppRoute.CourierProfile, strings.profile) {
            AnimatedBottomIcon(selected = currentRoute == AppRoute.CourierProfile) {
                Icon(Icons.Default.PersonOutline, null)
            }
        },
    )

    Surface(
        tonalElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(item.route) },
                    icon = { item.icon() },
                    label = { Text(item.label) },
                )
            }
        }
    }
}

@Composable
private fun AnimatedBottomIcon(
    selected: Boolean,
    content: @Composable () -> Unit,
) {
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.18f else 1f,
        animationSpec = tween(durationMillis = 220),
        label = "bottomIconScale",
    )
    Box(
        modifier = Modifier.scale(iconScale),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun AiNavigationIcon(
    selected: Boolean,
) {
    val accent = if (selected) Orange else MaterialTheme.colorScheme.onSurfaceVariant
    val container = if (selected) Orange else Orange.copy(alpha = 0.12f)
    val content = if (selected) CardWhite else accent

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(container),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Outlined.HeadsetMic, null, tint = content)
    }
}

private fun AppLanguage.homeLabel(): String = when (this) {
    AppLanguage.English -> "Home"
    AppLanguage.Russian -> "Главная"
    AppLanguage.Uzbek -> "Asosiy"
}

private fun AppLanguage.searchLabel(): String = when (this) {
    AppLanguage.English -> "Search"
    AppLanguage.Russian -> "Поиск"
    AppLanguage.Uzbek -> "Qidiruv"
}

private fun AppLanguage.ordersLabel(): String = when (this) {
    AppLanguage.English -> "Orders"
    AppLanguage.Russian -> "Заказы"
    AppLanguage.Uzbek -> "Buyurtmalar"
}

private fun AppLanguage.profileLabel(): String = when (this) {
    AppLanguage.English -> "Profile"
    AppLanguage.Russian -> "Профиль"
    AppLanguage.Uzbek -> "Profil"
}

private fun AppLanguage.cartLabel(): String = when (this) {
    AppLanguage.English -> "Cart"
    AppLanguage.Russian -> "Корзина"
    AppLanguage.Uzbek -> "Savat"
}

private fun AppLanguage.addedToCartMessage(itemTitle: String): String = when (this) {
    AppLanguage.English -> "$itemTitle added to cart"
    AppLanguage.Russian -> "$itemTitle добавлено в корзину"
    AppLanguage.Uzbek -> "$itemTitle savatga qo'shildi"
}

private fun AppLanguage.openCartAction(): String = when (this) {
    AppLanguage.English -> "Cart"
    AppLanguage.Russian -> "Корзина"
    AppLanguage.Uzbek -> "Savat"
}
