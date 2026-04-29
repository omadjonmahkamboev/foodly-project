package com.example.fooddeliveryapp.backend

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.fooddeliveryapp.BuildConfig
import com.example.fooddeliveryapp.ui.data.DeliveryAddress
import com.example.fooddeliveryapp.ui.data.GeoPoint
import com.example.fooddeliveryapp.ui.data.OrderStatus
import com.example.fooddeliveryapp.ui.data.OrderSummary
import com.example.fooddeliveryapp.ui.data.PaymentCard
import com.example.fooddeliveryapp.ui.data.PaymentMethod
import com.example.fooddeliveryapp.ui.data.cardBrandName
import com.example.fooddeliveryapp.ui.data.detectPaymentMethod
import java.security.SecureRandom
import java.util.Locale
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.delay

data class UserSession(
    val id: String,
    val name: String,
    val email: String,
    val token: String,
    val welcomeCouponExpiresAtMillis: Long? = null,
    val showsWelcomeCouponOffer: Boolean = false,
)

data class AuthRequest(
    val name: String = "",
    val email: String,
    val password: String,
)

data class GoogleAuthRequest(
    val name: String,
    val email: String,
    val googleSubject: String?,
)

data class PasswordResetChallenge(
    val email: String,
    val expiresAtMillis: Long,
)

data class EmailVerificationChallenge(
    val email: String,
    val expiresAtMillis: Long,
    val debugCode: String? = null,
)

data class AddPaymentCardRequest(
    val token: String,
    val number: String,
    val holderName: String,
    val expiry: String,
    val cvv: String,
)

data class CreateOrderRequest(
    val token: String,
    val restaurantId: String,
    val restaurantName: String,
    val restaurantPoint: GeoPoint,
    val address: DeliveryAddress,
    val itemsLabel: String,
    val total: Int,
    val paymentMethod: PaymentMethod,
    val paymentCard: PaymentCard?,
)

data class DeliveryQuote(
    val distanceKm: Double,
    val etaMinutes: Int,
    val deliveryFee: Int,
)

sealed interface BackendResult<out T> {
    data class Success<T>(val data: T) : BackendResult<T>
    data class Error(val message: String) : BackendResult<Nothing>
}

interface FoodBackend {
    suspend fun restoreSession(email: String): BackendResult<UserSession>
    suspend fun requestRegistrationVerification(email: String): BackendResult<EmailVerificationChallenge>
    suspend fun verifyRegistrationCode(email: String, code: String): BackendResult<Unit>
    suspend fun register(request: AuthRequest): BackendResult<UserSession>
    suspend fun login(request: AuthRequest): BackendResult<UserSession>
    suspend fun googleSignIn(request: GoogleAuthRequest): BackendResult<UserSession>
    suspend fun requestPasswordReset(email: String): BackendResult<PasswordResetChallenge>
    suspend fun resetPassword(email: String, code: String, newPassword: String): BackendResult<UserSession>
    fun updateUserProfile(token: String, name: String, email: String): BackendResult<UserSession>
    suspend fun paymentCards(token: String): BackendResult<List<PaymentCard>>
    suspend fun addPaymentCard(request: AddPaymentCardRequest): BackendResult<PaymentCard>
    suspend fun favoriteItemIds(token: String): BackendResult<List<String>>
    suspend fun replaceFavoriteItemIds(token: String, itemIds: List<String>): BackendResult<Unit>
    suspend fun deliveryQuote(restaurantPoint: GeoPoint, addressPoint: GeoPoint): BackendResult<DeliveryQuote>
    suspend fun createOrder(request: CreateOrderRequest): BackendResult<OrderSummary>
    suspend fun redeemWelcomeCoupon(token: String, code: String): BackendResult<Unit>
}

class DatabaseFoodBackend(context: Context) : FoodBackend {
    private val database = FoodBackendDatabase(context.applicationContext)
    private val usersByToken = mutableMapOf<String, UserRecord>()
    private val ordersByUser = mutableMapOf<String, MutableList<OrderSummary>>()
    private val passwordResetChallenges = mutableMapOf<String, PasswordResetRecord>()
    private val registrationChallenges = mutableMapOf<String, VerificationRecord>()
    private val verifiedRegistrationEmails = mutableSetOf<String>()
    private val emailSender: VerificationEmailSender = LogcatVerificationEmailSender
    private val random = SecureRandom()

    init {
        database.ensureDemoUser()
    }

    override suspend fun restoreSession(email: String): BackendResult<UserSession> {
        val normalizedEmail = email.trim().lowercase(Locale.US)
        val user = database.userByEmail(normalizedEmail)
            ?: return BackendResult.Error("Saved session expired. Please sign in again")
        val activeCouponExpiresAtMillis = user.welcomeCouponExpiresAtMillis
            ?.takeIf { expiresAt -> !user.welcomeCouponRedeemed && expiresAt > System.currentTimeMillis() }
        return BackendResult.Success(
            createSession(
                user = user,
                welcomeCouponExpiresAtMillis = activeCouponExpiresAtMillis,
                showsWelcomeCouponOffer = false,
            ),
        )
    }

    override suspend fun requestRegistrationVerification(email: String): BackendResult<EmailVerificationChallenge> {
        delay(NetworkDelayMs)
        val normalizedEmail = email.trim().lowercase(Locale.US)
        if (!normalizedEmail.contains("@")) return BackendResult.Error("Проверь email")
        if (database.userByEmail(normalizedEmail) != null) {
            return BackendResult.Error("Аккаунт с таким email уже есть")
        }

        val code = nextVerificationCode()
        val expiresAtMillis = System.currentTimeMillis() + VerificationCodeLifetimeMs
        registrationChallenges[normalizedEmail] = VerificationRecord(code, expiresAtMillis)
        if (!emailSender.sendRegistrationCode(normalizedEmail, code)) {
            return BackendResult.Error("Email sender is not configured. Connect SMTP or an email API on the backend.")
        }
        return BackendResult.Success(
            EmailVerificationChallenge(
                email = normalizedEmail,
                expiresAtMillis = expiresAtMillis,
                debugCode = code.takeIf { BuildConfig.DEBUG },
            ),
        )
    }

    override suspend fun verifyRegistrationCode(email: String, code: String): BackendResult<Unit> {
        delay(NetworkDelayMs)
        val normalizedEmail = email.trim().lowercase(Locale.US)
        val challenge = registrationChallenges[normalizedEmail]
            ?: return BackendResult.Error("Код истек. Запроси новый код.")
        if (challenge.expiresAtMillis <= System.currentTimeMillis()) {
            registrationChallenges.remove(normalizedEmail)
            return BackendResult.Error("Код истек. Запроси новый код.")
        }
        if (challenge.code != code.filter(Char::isDigit)) {
            return BackendResult.Error("Неверный код")
        }

        registrationChallenges.remove(normalizedEmail)
        verifiedRegistrationEmails.add(normalizedEmail)
        return BackendResult.Success(Unit)
    }

    override suspend fun register(request: AuthRequest): BackendResult<UserSession> {
        delay(NetworkDelayMs)
        val email = request.email.trim().lowercase(Locale.US)
        val name = request.name.trim()

        if (name.length < 2) return BackendResult.Error("Укажи имя минимум из 2 символов")
        if (!email.contains("@")) return BackendResult.Error("Проверь email")
        if (request.password.length < 8) return BackendResult.Error("Пароль должен быть не короче 8 символов")
        if (database.userByEmail(email) != null) return BackendResult.Error("Аккаунт с таким email уже есть")

        if (!verifiedRegistrationEmails.remove(email)) return BackendResult.Error("Сначала подтвердите email")

        val user = UserRecord(
            id = "user_${UUID.randomUUID()}",
            name = name,
            email = email,
            password = request.password,
            createdAtMillis = System.currentTimeMillis(),
            welcomeCouponClaimed = false,
            welcomeCouponExpiresAtMillis = System.currentTimeMillis() + WelcomeCouponLifetimeMs,
            welcomeCouponRedeemed = false,
        )
        database.insertUser(user)
        ordersByUser[user.id] = mutableListOf()

        return BackendResult.Success(createSession(user, welcomeCouponExpiresAtMillis = null, showsWelcomeCouponOffer = false))
    }

    override suspend fun login(request: AuthRequest): BackendResult<UserSession> {
        delay(NetworkDelayMs)
        val email = request.email.trim().lowercase(Locale.US)
        val user = database.userByEmail(email) ?: return BackendResult.Error("Аккаунт не найден")
        if (user.password != request.password) return BackendResult.Error("Неверный пароль")

        val activeCouponExpiresAtMillis = user.welcomeCouponExpiresAtMillis
            ?.takeIf { expiresAt -> !user.welcomeCouponRedeemed && expiresAt > System.currentTimeMillis() }
        val showsWelcomeCouponOffer = activeCouponExpiresAtMillis != null && !user.welcomeCouponClaimed
        if (showsWelcomeCouponOffer) {
            database.markWelcomeCouponClaimed(user.id)
        }

        return BackendResult.Success(
            createSession(
                user = user.copy(welcomeCouponClaimed = user.welcomeCouponClaimed || showsWelcomeCouponOffer),
                welcomeCouponExpiresAtMillis = activeCouponExpiresAtMillis,
                showsWelcomeCouponOffer = showsWelcomeCouponOffer,
            ),
        )
    }

    override suspend fun googleSignIn(request: GoogleAuthRequest): BackendResult<UserSession> {
        delay(NetworkDelayMs)
        val email = request.email.trim().lowercase(Locale.US)
        if (!email.contains("@")) return BackendResult.Error("Google account did not return a valid email")

        val existingUser = database.userByEmail(email)
        val user = if (existingUser != null) {
            existingUser
        } else {
            val displayName = request.name.trim().ifBlank { email.substringBefore("@") }
            UserRecord(
                id = "user_${UUID.randomUUID()}",
                name = displayName,
                email = email,
                password = "google:${request.googleSubject ?: UUID.randomUUID()}",
                createdAtMillis = System.currentTimeMillis(),
                welcomeCouponClaimed = false,
                welcomeCouponExpiresAtMillis = System.currentTimeMillis() + WelcomeCouponLifetimeMs,
                welcomeCouponRedeemed = false,
            ).also { newUser ->
                database.insertUser(newUser)
                ordersByUser[newUser.id] = mutableListOf()
            }
        }

        return BackendResult.Success(createLoginSession(user))
    }

    override suspend fun requestPasswordReset(email: String): BackendResult<PasswordResetChallenge> {
        delay(NetworkDelayMs)
        val normalizedEmail = email.trim().lowercase(Locale.US)
        database.userByEmail(normalizedEmail) ?: return BackendResult.Error("Account not found")

        val code = nextVerificationCode()
        val expiresAtMillis = System.currentTimeMillis() + VerificationCodeLifetimeMs
        passwordResetChallenges[normalizedEmail] = PasswordResetRecord(code, expiresAtMillis)
        if (!emailSender.sendPasswordResetCode(normalizedEmail, code)) {
            return BackendResult.Error("Email sender is not configured. Connect SMTP or an email API on the backend.")
        }

        return BackendResult.Success(
            PasswordResetChallenge(
                email = normalizedEmail,
                expiresAtMillis = expiresAtMillis,
            ),
        )
    }

    override suspend fun resetPassword(
        email: String,
        code: String,
        newPassword: String,
    ): BackendResult<UserSession> {
        delay(NetworkDelayMs)
        val normalizedEmail = email.trim().lowercase(Locale.US)
        val user = database.userByEmail(normalizedEmail) ?: return BackendResult.Error("Account not found")
        val challenge = passwordResetChallenges[normalizedEmail]
            ?: return BackendResult.Error("Reset code expired. Request a new code.")
        if (challenge.expiresAtMillis <= System.currentTimeMillis()) {
            passwordResetChallenges.remove(normalizedEmail)
            return BackendResult.Error("Reset code expired. Request a new code.")
        }
        if (challenge.code != code.filter(Char::isDigit)) {
            return BackendResult.Error("Wrong reset code")
        }
        if (newPassword.length < 8) {
            return BackendResult.Error("Password must be at least 8 characters")
        }

        database.updateUserPassword(user.id, newPassword)
        passwordResetChallenges.remove(normalizedEmail)
        val updatedUser = database.userByEmail(normalizedEmail) ?: user.copy(password = newPassword)
        return BackendResult.Success(createLoginSession(updatedUser))
    }

    private fun createLoginSession(user: UserRecord): UserSession {
        val activeCouponExpiresAtMillis = user.welcomeCouponExpiresAtMillis
            ?.takeIf { expiresAt -> !user.welcomeCouponRedeemed && expiresAt > System.currentTimeMillis() }
        val showsWelcomeCouponOffer = activeCouponExpiresAtMillis != null && !user.welcomeCouponClaimed
        if (showsWelcomeCouponOffer) {
            database.markWelcomeCouponClaimed(user.id)
        }

        return createSession(
            user = user.copy(welcomeCouponClaimed = user.welcomeCouponClaimed || showsWelcomeCouponOffer),
            welcomeCouponExpiresAtMillis = activeCouponExpiresAtMillis,
            showsWelcomeCouponOffer = showsWelcomeCouponOffer,
        )
    }

    override fun updateUserProfile(
        token: String,
        name: String,
        email: String,
    ): BackendResult<UserSession> {
        val user = userByToken(token) ?: return BackendResult.Error("Сессия истекла, войди снова")
        val cleanName = name.trim()
        val cleanEmail = email.trim().lowercase(Locale.US)

        if (cleanName.length < 2) return BackendResult.Error("Укажи имя минимум из 2 символов")
        if (!cleanEmail.contains("@")) return BackendResult.Error("Проверь email")

        val existingUser = database.userByEmail(cleanEmail)
        if (existingUser != null && existingUser.id != user.id) {
            return BackendResult.Error("Аккаунт с таким email уже есть")
        }

        val updatedUser = user.copy(name = cleanName, email = cleanEmail)
        database.updateUserProfile(
            userId = user.id,
            name = cleanName,
            email = cleanEmail,
        )
        usersByToken[token] = updatedUser

        return BackendResult.Success(
            UserSession(
                id = updatedUser.id,
                name = updatedUser.name,
                email = updatedUser.email,
                token = token,
            ),
        )
    }

    override suspend fun paymentCards(token: String): BackendResult<List<PaymentCard>> {
        delay(NetworkDelayMs)
        val user = userByToken(token) ?: return BackendResult.Error("Сессия истекла, войди снова")
        return BackendResult.Success(database.paymentCards(user.id))
    }

    override suspend fun addPaymentCard(request: AddPaymentCardRequest): BackendResult<PaymentCard> {
        delay(NetworkDelayMs)
        val user = userByToken(request.token) ?: return BackendResult.Error("Сессия истекла, войди снова")
        val digits = request.number.filter(Char::isDigit)
        val expiry = request.expiry.trim().normalizeCardExpiry()
        val paymentMethod = detectPaymentMethod(digits) ?: return BackendResult.Error("This card type is not supported")
        val requiresCvv = paymentMethod != PaymentMethod.HumoCard
        val cvv = request.cvv.filter(Char::isDigit).ifBlank {
            if (requiresCvv) "" else "000"
        }
        if (digits.length != 16) return BackendResult.Error("Card number must contain 16 digits")
        if (requiresCvv && cvv.length !in 3..4) return BackendResult.Error("CVV must contain 3-4 digits")
        if (!expiry.matches(Regex("""\d{2}/\d{2}"""))) return BackendResult.Error("Срок карты укажи как MM/YY")
        if (request.holderName.trim().length < 2) return BackendResult.Error("Укажи имя владельца")

        val card = PaymentCard(
            id = "card_${UUID.randomUUID()}",
            brand = paymentMethod.cardBrandName(),
            last4 = digits.takeLast(4),
            holderName = request.holderName.trim().uppercase(Locale.US),
            expiry = expiry,
        )
        database.insertPaymentCard(user.id, card)
        return BackendResult.Success(card)
    }

    override suspend fun favoriteItemIds(token: String): BackendResult<List<String>> {
        delay(35)
        val user = userByToken(token) ?: return BackendResult.Error("Session expired. Sign in again")
        return BackendResult.Success(database.favoriteItemIds(user.id))
    }

    override suspend fun replaceFavoriteItemIds(
        token: String,
        itemIds: List<String>,
    ): BackendResult<Unit> {
        delay(35)
        val user = userByToken(token) ?: return BackendResult.Error("Session expired. Sign in again")
        database.replaceFavoriteItemIds(
            userId = user.id,
            itemIds = itemIds.map { it.trim() }.filter { it.isNotBlank() }.distinct(),
        )
        return BackendResult.Success(Unit)
    }

    override suspend fun deliveryQuote(
        restaurantPoint: GeoPoint,
        addressPoint: GeoPoint,
    ): BackendResult<DeliveryQuote> {
        when (val matrix = YandexRoutingApi.distance(restaurantPoint, addressPoint)) {
            is BackendResult.Success -> {
                val distance = matrix.data.distanceMeters / 1_000.0
                val eta = (matrix.data.durationSeconds / 60.0).roundToInt().coerceAtLeast(10)
                val fee = deliveryFeeForDistance(distance)
                return BackendResult.Success(
                    DeliveryQuote(
                        distanceKm = (distance * 10).roundToInt() / 10.0,
                        etaMinutes = eta,
                        deliveryFee = fee,
                    ),
                )
            }
            is BackendResult.Error -> Unit
        }

        val distance = haversineKm(restaurantPoint, addressPoint)
        val eta = (8 + distance * 3.2).roundToInt().coerceAtLeast(12)
        val fee = deliveryFeeForDistance(distance)
        return BackendResult.Success(
            DeliveryQuote(
                distanceKm = (distance * 10).roundToInt() / 10.0,
                etaMinutes = eta,
                deliveryFee = fee,
            ),
        )
    }

    override suspend fun createOrder(request: CreateOrderRequest): BackendResult<OrderSummary> {
        delay(NetworkDelayMs)
        val user = userByToken(request.token) ?: return BackendResult.Error("Сессия истекла, войди снова")
        if (request.itemsLabel.isBlank()) return BackendResult.Error("Корзина пустая")

        val quote = when (val result = deliveryQuote(request.restaurantPoint, request.address.point)) {
            is BackendResult.Success -> result.data
            is BackendResult.Error -> return result
        }
        val paymentLabel = request.paymentCard?.let { "${it.brand} •••• ${it.last4}" }
            ?: request.paymentMethod.paymentLabel()
        val order = OrderSummary(
            id = "ord_${System.currentTimeMillis()}",
            restaurantId = request.restaurantId,
            restaurantName = request.restaurantName,
            itemsLabel = request.itemsLabel,
            total = request.total,
            eta = "Курьер будет через ${quote.etaMinutes} мин",
            status = OrderStatus.Preparing,
            paymentLabel = paymentLabel,
            deliveryDistanceKm = quote.distanceKm,
            createdAtMillis = System.currentTimeMillis(),
        )

        ordersByUser.getOrPut(user.id) { mutableListOf() }.add(0, order)
        return BackendResult.Success(order)
    }

    override suspend fun redeemWelcomeCoupon(token: String, code: String): BackendResult<Unit> {
        delay(35)
        val user = userByToken(token) ?: return BackendResult.Error("Сессия истекла, войди снова")
        if (code.equals(WelcomeCouponCode, ignoreCase = true)) {
            database.markWelcomeCouponRedeemed(user.id)
            usersByToken[token] = user.copy(welcomeCouponRedeemed = true)
        }
        return BackendResult.Success(Unit)
    }

    private fun createSession(
        user: UserRecord,
        welcomeCouponExpiresAtMillis: Long?,
        showsWelcomeCouponOffer: Boolean,
    ): UserSession {
        val session = UserSession(
            id = user.id,
            name = user.name,
            email = user.email,
            token = "token_${UUID.randomUUID()}",
            welcomeCouponExpiresAtMillis = welcomeCouponExpiresAtMillis,
            showsWelcomeCouponOffer = showsWelcomeCouponOffer,
        )
        usersByToken[session.token] = user
        return session
    }

    private fun userByToken(token: String): UserRecord? = usersByToken[token]

    private fun PaymentMethod.paymentLabel(): String = when (this) {
        PaymentMethod.Cash -> "Cash"
        PaymentMethod.Visa -> "Visa"
        PaymentMethod.MasterCard -> "Mastercard"
        PaymentMethod.Uzcard -> "Uzcard"
        PaymentMethod.HumoCard -> "HumoCard"
    }

    private fun haversineKm(start: GeoPoint, end: GeoPoint): Double {
        val earthRadiusKm = 6371.0
        val latDistance = Math.toRadians(end.latitude - start.latitude)
        val lonDistance = Math.toRadians(end.longitude - start.longitude)
        val startLat = Math.toRadians(start.latitude)
        val endLat = Math.toRadians(end.latitude)
        val a = sin(latDistance / 2) * sin(latDistance / 2) +
            cos(startLat) * cos(endLat) * sin(lonDistance / 2) * sin(lonDistance / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }

    private fun deliveryFeeForDistance(distanceKm: Double): Int = when {
        distanceKm <= 2.0 -> 0
        distanceKm <= 5.0 -> 2
        else -> 4
    }

    private data class UserRecord(
        val id: String,
        val name: String,
        val email: String,
        val password: String,
        val createdAtMillis: Long,
        val welcomeCouponClaimed: Boolean,
        val welcomeCouponExpiresAtMillis: Long?,
        val welcomeCouponRedeemed: Boolean,
    )

    private data class PasswordResetRecord(
        val code: String,
        val expiresAtMillis: Long,
    )

    private data class VerificationRecord(
        val code: String,
        val expiresAtMillis: Long,
    )

    private fun nextVerificationCode(): String =
        "%06d".format(Locale.US, random.nextInt(1_000_000))

    private interface VerificationEmailSender {
        fun sendRegistrationCode(email: String, code: String): Boolean
        fun sendPasswordResetCode(email: String, code: String): Boolean
    }

    private object LogcatVerificationEmailSender : VerificationEmailSender {
        override fun sendRegistrationCode(email: String, code: String): Boolean =
            send("Foodly registration code", email, code)

        override fun sendPasswordResetCode(email: String, code: String): Boolean =
            send("Foodly password reset code", email, code)

        private fun send(subject: String, email: String, code: String): Boolean {
            if (!BuildConfig.DEBUG) return false
            Log.i("FoodlyEmail", "$subject for $email: $code")
            return true
        }
    }

    private class FoodBackendDatabase(context: Context) :
        SQLiteOpenHelper(context, DatabaseName, null, DatabaseVersion) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE users (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    email TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    created_at_millis INTEGER NOT NULL,
                    welcome_coupon_claimed INTEGER NOT NULL DEFAULT 0,
                    welcome_coupon_expires_at_millis INTEGER,
                    welcome_coupon_redeemed INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE payment_cards (
                    id TEXT PRIMARY KEY,
                    user_id TEXT NOT NULL,
                    brand TEXT NOT NULL,
                    last4 TEXT NOT NULL,
                    holder_name TEXT NOT NULL,
                    expiry TEXT NOT NULL,
                    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX idx_payment_cards_user_id ON payment_cards(user_id)")
            createFavoritesTable(db)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            if (oldVersion < 2) {
                runCatching { db.execSQL("ALTER TABLE users ADD COLUMN welcome_coupon_expires_at_millis INTEGER") }
                runCatching { db.execSQL("ALTER TABLE users ADD COLUMN welcome_coupon_redeemed INTEGER NOT NULL DEFAULT 0") }
            }
            if (oldVersion < 3) {
                createFavoritesTable(db)
            }
        }

        private fun createFavoritesTable(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS favorite_items (
                    user_id TEXT NOT NULL,
                    item_id TEXT NOT NULL,
                    sort_order INTEGER NOT NULL,
                    PRIMARY KEY(user_id, item_id),
                    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_favorite_items_user_id ON favorite_items(user_id)")
        }

        fun ensureDemoUser() {
            if (userByEmail(DemoEmail) == null) {
                insertUser(
                    UserRecord(
                        id = DemoUserId,
                        name = "Vishal Khadok",
                        email = DemoEmail,
                        password = "12345678",
                        createdAtMillis = System.currentTimeMillis(),
                        welcomeCouponClaimed = true,
                        welcomeCouponExpiresAtMillis = null,
                        welcomeCouponRedeemed = true,
                    ),
                )
            }

            if (paymentCards(DemoUserId).none { it.id == DemoCardId }) {
                insertPaymentCard(
                    userId = DemoUserId,
                    card = PaymentCard(
                        id = DemoCardId,
                        brand = "Mastercard",
                        last4 = "4368",
                        holderName = "VISHAL KHADOK",
                        expiry = "12/28",
                    ),
                )
            }
        }

        fun userByEmail(email: String): UserRecord? =
            readableDatabase.query(
                UsersTable,
                null,
                "email = ?",
                arrayOf(email.trim().lowercase(Locale.US)),
                null,
                null,
                null,
                "1",
            ).use { cursor ->
                if (cursor.moveToFirst()) cursor.toUserRecord() else null
            }

        fun insertUser(user: UserRecord) {
            writableDatabase.insertOrThrow(
                UsersTable,
                null,
                ContentValues().apply {
                    put("id", user.id)
                    put("name", user.name)
                    put("email", user.email.trim().lowercase(Locale.US))
                    put("password", user.password)
                    put("created_at_millis", user.createdAtMillis)
                    put("welcome_coupon_claimed", if (user.welcomeCouponClaimed) 1 else 0)
                    user.welcomeCouponExpiresAtMillis?.let { put("welcome_coupon_expires_at_millis", it) }
                    put("welcome_coupon_redeemed", if (user.welcomeCouponRedeemed) 1 else 0)
                },
            )
        }

        fun markWelcomeCouponClaimed(userId: String) {
            writableDatabase.update(
                UsersTable,
                ContentValues().apply { put("welcome_coupon_claimed", 1) },
                "id = ?",
                arrayOf(userId),
            )
        }

        fun markWelcomeCouponRedeemed(userId: String) {
            writableDatabase.update(
                UsersTable,
                ContentValues().apply { put("welcome_coupon_redeemed", 1) },
                "id = ?",
                arrayOf(userId),
            )
        }

        fun updateUserPassword(userId: String, password: String) {
            writableDatabase.update(
                UsersTable,
                ContentValues().apply { put("password", password) },
                "id = ?",
                arrayOf(userId),
            )
        }

        fun updateUserProfile(
            userId: String,
            name: String,
            email: String,
        ) {
            writableDatabase.update(
                UsersTable,
                ContentValues().apply {
                    put("name", name)
                    put("email", email.trim().lowercase(Locale.US))
                },
                "id = ?",
                arrayOf(userId),
            )
        }

        fun paymentCards(userId: String): List<PaymentCard> =
            readableDatabase.query(
                PaymentCardsTable,
                null,
                "user_id = ?",
                arrayOf(userId),
                null,
                null,
                "rowid DESC",
            ).use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(cursor.toPaymentCard())
                    }
                }
            }

        fun insertPaymentCard(userId: String, card: PaymentCard) {
            writableDatabase.insertOrThrow(
                PaymentCardsTable,
                null,
                ContentValues().apply {
                    put("id", card.id)
                    put("user_id", userId)
                    put("brand", card.brand)
                    put("last4", card.last4)
                    put("holder_name", card.holderName)
                    put("expiry", card.expiry)
                },
            )
        }

        fun favoriteItemIds(userId: String): List<String> =
            readableDatabase.query(
                FavoriteItemsTable,
                arrayOf("item_id"),
                "user_id = ?",
                arrayOf(userId),
                null,
                null,
                "sort_order ASC",
            ).use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(cursor.getString(cursor.getColumnIndexOrThrow("item_id")))
                    }
                }
            }

        fun replaceFavoriteItemIds(userId: String, itemIds: List<String>) {
            val db = writableDatabase
            db.beginTransaction()
            try {
                db.delete(FavoriteItemsTable, "user_id = ?", arrayOf(userId))
                itemIds.forEachIndexed { index, itemId ->
                    db.insertWithOnConflict(
                        FavoriteItemsTable,
                        null,
                        ContentValues().apply {
                            put("user_id", userId)
                            put("item_id", itemId)
                            put("sort_order", index)
                        },
                        SQLiteDatabase.CONFLICT_REPLACE,
                    )
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }

        private fun Cursor.toUserRecord(): UserRecord =
            UserRecord(
                id = getString(getColumnIndexOrThrow("id")),
                name = getString(getColumnIndexOrThrow("name")),
                email = getString(getColumnIndexOrThrow("email")),
                password = getString(getColumnIndexOrThrow("password")),
                createdAtMillis = getLong(getColumnIndexOrThrow("created_at_millis")),
                welcomeCouponClaimed = getInt(getColumnIndexOrThrow("welcome_coupon_claimed")) == 1,
                welcomeCouponExpiresAtMillis = nullableLong("welcome_coupon_expires_at_millis"),
                welcomeCouponRedeemed = getInt(getColumnIndexOrThrow("welcome_coupon_redeemed")) == 1,
            )

        private fun Cursor.toPaymentCard(): PaymentCard =
            PaymentCard(
                id = getString(getColumnIndexOrThrow("id")),
                brand = getString(getColumnIndexOrThrow("brand")),
                last4 = getString(getColumnIndexOrThrow("last4")),
                holderName = getString(getColumnIndexOrThrow("holder_name")),
                expiry = getString(getColumnIndexOrThrow("expiry")),
            )

        private fun Cursor.nullableLong(column: String): Long? {
            val index = getColumnIndexOrThrow(column)
            return if (isNull(index)) null else getLong(index)
        }
    }

    private companion object {
        const val NetworkDelayMs = 80L
        const val DatabaseName = "foodly_backend.db"
        const val DatabaseVersion = 3
        const val UsersTable = "users"
        const val PaymentCardsTable = "payment_cards"
        const val FavoriteItemsTable = "favorite_items"
        const val DemoUserId = "user_demo"
        const val DemoEmail = "hello@foodly.app"
        const val DemoCardId = "card_demo_mastercard"
        const val WelcomeCouponCode = "FOODLY25"
        const val WelcomeCouponLifetimeMs = 7 * 24 * 60 * 60 * 1_000L
        const val PasswordResetLifetimeMs = 5 * 60 * 1_000L
        const val VerificationCodeLifetimeMs = 5 * 60 * 1_000L
    }
}

private fun String.normalizeCardExpiry(): String =
    if (matches(Regex("""\d{2}/\d{4}"""))) {
        take(3) + takeLast(2)
    } else {
        this
    }
