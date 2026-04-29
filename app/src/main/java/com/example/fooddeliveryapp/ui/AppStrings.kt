package com.example.fooddeliveryapp.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.fooddeliveryapp.ui.data.CourierOrderStatus
import com.example.fooddeliveryapp.ui.data.OrderStatus
import com.example.fooddeliveryapp.ui.data.PaymentMethod

val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.Russian }
val LocalAppStrings = staticCompositionLocalOf { AppStrings(AppLanguage.Russian) }

@Immutable
data class OnboardingText(
    val title: String,
    val description: String,
)

@Immutable
class AppStrings(
    private val language: AppLanguage,
) {
    val currentLanguage: AppLanguage get() = language

    val appTagline: String get() = when (language) {
        AppLanguage.English -> "A fresh taste of delivery"
        AppLanguage.Russian -> "Новый вкус доставки"
        AppLanguage.Uzbek -> "Yetkazib berishning yangi ta'mi"
    }

    val skip: String get() = when (language) {
        AppLanguage.English -> "Skip"
        AppLanguage.Russian -> "Пропустить"
        AppLanguage.Uzbek -> "O'tkazib yuborish"
    }

    val next: String get() = when (language) {
        AppLanguage.English -> "Next"
        AppLanguage.Russian -> "Далее"
        AppLanguage.Uzbek -> "Keyingi"
    }

    val start: String get() = when (language) {
        AppLanguage.English -> "Start"
        AppLanguage.Russian -> "Начать"
        AppLanguage.Uzbek -> "Boshlash"
    }

    val onboarding: List<OnboardingText> get() = when (language) {
        AppLanguage.English -> listOf(
            OnboardingText(
                "All favorite dishes in one place",
                "Burgers, pizza, wok and desserts near you. A clean catalog and a fast order flow without extra steps.",
            ),
            OnboardingText(
                "Choose a restaurant and add to cart in seconds",
                "Search, menus, payment and order tracking are gathered in one simple app.",
            ),
            OnboardingText(
                "Set the delivery point on a Yandex map",
                "Tap the map, confirm the address and get accurate delivery without extra calls.",
            ),
        )
        AppLanguage.Russian -> listOf(
            OnboardingText(
                "Все любимые блюда в одном месте",
                "Бургеры, пицца, wok и десерты рядом с тобой. Красивый каталог и быстрый заказ без лишних шагов.",
            ),
            OnboardingText(
                "Выбирай ресторан и добавляй в корзину за секунды",
                "Поиск, меню, оплата и отслеживание заказа собраны в одном удобном приложении.",
            ),
            OnboardingText(
                "Укажи точку доставки через Yandex карту",
                "Тапни по карте, подтверди адрес и получай точную доставку без звонков и путаницы.",
            ),
        )
        AppLanguage.Uzbek -> listOf(
            OnboardingText(
                "Sevimli taomlar bitta joyda",
                "Burger, pizza, wok va desertlar yoningizda. Chiroyli katalog va ortiqcha bosqichlarsiz tez buyurtma.",
            ),
            OnboardingText(
                "Restoranni tanlang va savatga soniyalarda qo'shing",
                "Qidiruv, menyu, to'lov va buyurtmani kuzatish bitta qulay ilovada.",
            ),
            OnboardingText(
                "Yetkazish nuqtasini Yandex xaritada belgilang",
                "Xaritaga bosing, manzilni tasdiqlang va aniq yetkazib berishni oling.",
            ),
        )
    }

    val loginTitle: String get() = when (language) {
        AppLanguage.English -> "Sign in and continue\nyour order"
        AppLanguage.Russian -> "Войди и продолжи\nсвой заказ"
        AppLanguage.Uzbek -> "Kiring va buyurtmani\ndavom ettiring"
    }

    val signUpTitle: String get() = when (language) {
        AppLanguage.English -> "Create an account\nand order beautifully"
        AppLanguage.Russian -> "Создай аккаунт\nи заказывай красиво"
        AppLanguage.Uzbek -> "Akkaunt yarating\nva qulay buyurtma qiling"
    }

    val authSubtitle: String get() = when (language) {
        AppLanguage.English -> "Authorization uses the local backend contract. After sign in, we will choose your delivery address."
        AppLanguage.Russian -> "Вход работает через backend-контракт. После авторизации сразу перейдём к выбору адреса."
        AppLanguage.Uzbek -> "Kirish local backend orqali ishlaydi. Kirgandan keyin yetkazish manzilini tanlaymiz."
    }

    val averageDelivery: String get() = when (language) {
        AppLanguage.English -> "avg delivery"
        AppLanguage.Russian -> "средняя доставка"
        AppLanguage.Uzbek -> "o'rtacha yetkazish"
    }

    val rating: String get() = when (language) {
        AppLanguage.English -> "rating"
        AppLanguage.Russian -> "рейтинг"
        AppLanguage.Uzbek -> "reyting"
    }

    val loginTab: String get() = when (language) {
        AppLanguage.English -> "Login"
        AppLanguage.Russian -> "Вход"
        AppLanguage.Uzbek -> "Kirish"
    }

    val signUpTab: String get() = when (language) {
        AppLanguage.English -> "Registration"
        AppLanguage.Russian -> "Регистрация"
        AppLanguage.Uzbek -> "Ro'yxatdan o'tish"
    }

    val nameLabel: String get() = when (language) {
        AppLanguage.English -> "Name"
        AppLanguage.Russian -> "Имя"
        AppLanguage.Uzbek -> "Ism"
    }

    val namePlaceholder: String get() = when (language) {
        AppLanguage.English -> "What is your name?"
        AppLanguage.Russian -> "Как тебя зовут?"
        AppLanguage.Uzbek -> "Ismingiz nima?"
    }

    val emailLabel: String get() = "Email"

    val emailPlaceholder: String get() = "example@mail.com"

    val passwordLabel: String get() = when (language) {
        AppLanguage.English -> "Password"
        AppLanguage.Russian -> "Пароль"
        AppLanguage.Uzbek -> "Parol"
    }

    val passwordPlaceholder: String get() = when (language) {
        AppLanguage.English -> "At least 8 characters"
        AppLanguage.Russian -> "Не менее 8 символов"
        AppLanguage.Uzbek -> "Kamida 8 ta belgi"
    }

    val creating: String get() = when (language) {
        AppLanguage.English -> "Creating..."
        AppLanguage.Russian -> "Создаём..."
        AppLanguage.Uzbek -> "Yaratilmoqda..."
    }

    val signingIn: String get() = when (language) {
        AppLanguage.English -> "Signing in..."
        AppLanguage.Russian -> "Входим..."
        AppLanguage.Uzbek -> "Kirilmoqda..."
    }

    val createAccount: String get() = when (language) {
        AppLanguage.English -> "Create account"
        AppLanguage.Russian -> "Создать аккаунт"
        AppLanguage.Uzbek -> "Akkaunt yaratish"
    }

    val signIn: String get() = when (language) {
        AppLanguage.English -> "Sign in"
        AppLanguage.Russian -> "Войти"
        AppLanguage.Uzbek -> "Kirish"
    }

    fun signUpSuccess(code: String): String = when (language) {
        AppLanguage.English -> "Account saved locally. Your $code coupon activates after sign in."
        AppLanguage.Russian -> "Аккаунт сохранён в базе. Твой код $code активируется после входа."
        AppLanguage.Uzbek -> "Akkaunt local bazada saqlandi. $code kuponingiz kirgandan keyin faollashadi."
    }

    val haveAccount: String get() = when (language) {
        AppLanguage.English -> "Already have an account? Sign in"
        AppLanguage.Russian -> "Уже есть аккаунт? Войти"
        AppLanguage.Uzbek -> "Akkauntingiz bormi? Kirish"
    }

    val needAccount: String get() = when (language) {
        AppLanguage.English -> "No account? Register"
        AppLanguage.Russian -> "Нет аккаунта? Зарегистрироваться"
        AppLanguage.Uzbek -> "Akkaunt yo'qmi? Ro'yxatdan o'tish"
    }

    val demoAccount: String get() = when (language) {
        AppLanguage.English -> "Demo account: hello@foodly.app / 12345678. New accounts are saved in the local backend database."
        AppLanguage.Russian -> "Демо-аккаунт: hello@foodly.app / 12345678. Новые аккаунты сохраняются в локальной базе backend."
        AppLanguage.Uzbek -> "Demo akkaunt: hello@foodly.app / 12345678. Yangi akkauntlar local backend bazasida saqlanadi."
    }

    val locationStatusRequest: String get() = when (language) {
        AppLanguage.English -> "Requesting location access..."
        AppLanguage.Russian -> "Запрашиваем доступ к геолокации..."
        AppLanguage.Uzbek -> "Geolokatsiyaga ruxsat so'ralmoqda..."
    }

    val locationFoundSaving: String get() = when (language) {
        AppLanguage.English -> "Location found. Saving address..."
        AppLanguage.Russian -> "Локация найдена. Сохраняем адрес..."
        AppLanguage.Uzbek -> "Joylashuv topildi. Manzil saqlanmoqda..."
    }

    val locationTitle: String get() = when (language) {
        AppLanguage.English -> "Let's find your address"
        AppLanguage.Russian -> "Найдём твой адрес"
        AppLanguage.Uzbek -> "Manzilingizni topamiz"
    }

    val locating: String get() = when (language) {
        AppLanguage.English -> "LOCATING..."
        AppLanguage.Russian -> "ИЩЕМ ЛОКАЦИЮ..."
        AppLanguage.Uzbek -> "JOYLASHUV QIDIRILMOQDA..."
    }

    val enableLocation: String get() = when (language) {
        AppLanguage.English -> "ENABLE LOCATION"
        AppLanguage.Russian -> "ВКЛЮЧИТЬ ЛОКАЦИЮ"
        AppLanguage.Uzbek -> "JOYLASHUVNI YOQISH"
    }

    val pickOnMap: String get() = when (language) {
        AppLanguage.English -> "Choose on map manually"
        AppLanguage.Russian -> "Выбрать на карте вручную"
        AppLanguage.Uzbek -> "Xaritadan qo'lda tanlash"
    }

    val mapTitle: String get() = when (language) {
        AppLanguage.English -> "Delivery point"
        AppLanguage.Russian -> "Точка доставки"
        AppLanguage.Uzbek -> "Yetkazish nuqtasi"
    }

    val mapDescription: String get() = when (language) {
        AppLanguage.English -> "We use Yandex MapKit. Tap the map to set a convenient pickup point."
        AppLanguage.Russian -> "Используем Yandex MapKit. Тапни по карте, чтобы поставить удобную точку получения заказа."
        AppLanguage.Uzbek -> "Yandex MapKit ishlatiladi. Buyurtmani olish uchun qulay nuqtani xaritada belgilang."
    }

    val yandexMap: String get() = when (language) {
        AppLanguage.English -> "Yandex map"
        AppLanguage.Russian -> "Yandex карта"
        AppLanguage.Uzbek -> "Yandex xarita"
    }

    val moveMap: String get() = when (language) {
        AppLanguage.English -> "Move map"
        AppLanguage.Russian -> "Двигай карту"
        AppLanguage.Uzbek -> "Xaritani suring"
    }

    val precisePin: String get() = when (language) {
        AppLanguage.English -> "Precise pin"
        AppLanguage.Russian -> "Точный пин"
        AppLanguage.Uzbek -> "Aniq belgi"
    }

    val addressData: String get() = when (language) {
        AppLanguage.English -> "Address details"
        AppLanguage.Russian -> "Данные адреса"
        AppLanguage.Uzbek -> "Manzil ma'lumotlari"
    }

    val label: String get() = when (language) {
        AppLanguage.English -> "Label"
        AppLanguage.Russian -> "Метка"
        AppLanguage.Uzbek -> "Belgi"
    }

    val address: String get() = when (language) {
        AppLanguage.English -> "Address"
        AppLanguage.Russian -> "Адрес"
        AppLanguage.Uzbek -> "Manzil"
    }

    val saveAddress: String get() = when (language) {
        AppLanguage.English -> "Save address"
        AppLanguage.Russian -> "Сохранить адрес"
        AppLanguage.Uzbek -> "Manzilni saqlash"
    }

    val homeAddress: String get() = when (language) {
        AppLanguage.English -> "Home"
        AppLanguage.Russian -> "Дом"
        AppLanguage.Uzbek -> "Uy"
    }

    val officeAddress: String get() = when (language) {
        AppLanguage.English -> "Office"
        AppLanguage.Russian -> "Офис"
        AppLanguage.Uzbek -> "Ofis"
    }

    val otherAddress: String get() = when (language) {
        AppLanguage.English -> "Other"
        AppLanguage.Russian -> "Другое"
        AppLanguage.Uzbek -> "Boshqa"
    }

    val selectedMapPoint: String get() = when (language) {
        AppLanguage.English -> "Selected point on map"
        AppLanguage.Russian -> "Выбранная точка на карте"
        AppLanguage.Uzbek -> "Xaritada tanlangan nuqta"
    }

    fun pointLabel(latitude: Double, longitude: Double): String = when (language) {
        AppLanguage.English -> "Point: %.5f, %.5f".format(latitude, longitude)
        AppLanguage.Russian -> "Точка: %.5f, %.5f".format(latitude, longitude)
        AppLanguage.Uzbek -> "Nuqta: %.5f, %.5f".format(latitude, longitude)
    }

    val deliverTo: String get() = when (language) {
        AppLanguage.English -> "DELIVER TO"
        AppLanguage.Russian -> "ДОСТАВКА"
        AppLanguage.Uzbek -> "YETKAZISH"
    }

    val defaultOffice: String get() = when (language) {
        AppLanguage.English -> "Halal Lab office"
        AppLanguage.Russian -> "Офис Halal Lab"
        AppLanguage.Uzbek -> "Halal Lab ofisi"
    }

    val searchPlaceholder: String get() = when (language) {
        AppLanguage.English -> "Search dishes, restaurants"
        AppLanguage.Russian -> "Ищи блюда, рестораны"
        AppLanguage.Uzbek -> "Taomlar va restoranlarni qidiring"
    }

    val allCategories: String get() = when (language) {
        AppLanguage.English -> "All Categories"
        AppLanguage.Russian -> "Все категории"
        AppLanguage.Uzbek -> "Barcha kategoriyalar"
    }

    val seeAll: String get() = when (language) {
        AppLanguage.English -> "See All"
        AppLanguage.Russian -> "Все"
        AppLanguage.Uzbek -> "Hammasi"
    }

    val starting: String get() = when (language) {
        AppLanguage.English -> "Starting"
        AppLanguage.Russian -> "От"
        AppLanguage.Uzbek -> "Boshlanishi"
    }

    val searchExample: String get() = when (language) {
        AppLanguage.English -> "Pizza"
        AppLanguage.Russian -> "Пицца"
        AppLanguage.Uzbek -> "Pizza"
    }

    val openRestaurants: String get() = when (language) {
        AppLanguage.English -> "Open Restaurants"
        AppLanguage.Russian -> "Открытые рестораны"
        AppLanguage.Uzbek -> "Ochiq restoranlar"
    }

    fun greetingForHour(hour: Int): String = when (language) {
        AppLanguage.English -> when (hour) {
            in 5..11 -> "Good Morning!"
            in 12..16 -> "Good Afternoon!"
            in 17..21 -> "Good Evening!"
            else -> "Good Night!"
        }
        AppLanguage.Russian -> when (hour) {
            in 5..11 -> "Доброе утро!"
            in 12..16 -> "Добрый день!"
            in 17..21 -> "Добрый вечер!"
            else -> "Доброй ночи!"
        }
        AppLanguage.Uzbek -> when (hour) {
            in 5..11 -> "Xayrli tong!"
            in 12..16 -> "Xayrli kun!"
            in 17..21 -> "Xayrli kech!"
            else -> "Xayrli tun!"
        }
    }

    fun hey(name: String): String = when (language) {
        AppLanguage.English -> "Hey $name, "
        AppLanguage.Russian -> "Привет, $name, "
        AppLanguage.Uzbek -> "Salom, $name, "
    }

    val search: String get() = when (language) {
        AppLanguage.English -> "Search"
        AppLanguage.Russian -> "Поиск"
        AppLanguage.Uzbek -> "Qidiruv"
    }

    val recentKeywords: String get() = when (language) {
        AppLanguage.English -> "Recent Keywords"
        AppLanguage.Russian -> "Недавние запросы"
        AppLanguage.Uzbek -> "So'nggi qidiruvlar"
    }

    val suggestedRestaurants: String get() = when (language) {
        AppLanguage.English -> "Suggested Restaurants"
        AppLanguage.Russian -> "Рекомендуемые рестораны"
        AppLanguage.Uzbek -> "Tavsiya etilgan restoranlar"
    }

    val popularFastFood: String get() = when (language) {
        AppLanguage.English -> "Popular Fast Food"
        AppLanguage.Russian -> "Популярный fast food"
        AppLanguage.Uzbek -> "Mashhur fast food"
    }

    fun popularCategory(title: String): String = when (language) {
        AppLanguage.English -> "Popular $title"
        AppLanguage.Russian -> "Популярное: $title"
        AppLanguage.Uzbek -> "Mashhur: $title"
    }

    val details: String get() = when (language) {
        AppLanguage.English -> "Details"
        AppLanguage.Russian -> "Детали"
        AppLanguage.Uzbek -> "Tafsilotlar"
    }

    val size: String get() = when (language) {
        AppLanguage.English -> "SIZE:"
        AppLanguage.Russian -> "РАЗМЕР:"
        AppLanguage.Uzbek -> "O'LCHAM:"
    }

    val ingredients: String get() = when (language) {
        AppLanguage.English -> "INGREDIENTS"
        AppLanguage.Russian -> "ИНГРЕДИЕНТЫ"
        AppLanguage.Uzbek -> "TARKIBI"
    }

    val cart: String get() = when (language) {
        AppLanguage.English -> "Cart"
        AppLanguage.Russian -> "Корзина"
        AppLanguage.Uzbek -> "Savat"
    }

    val home: String get() = when (language) {
        AppLanguage.English -> "Home"
        AppLanguage.Russian -> "Главная"
        AppLanguage.Uzbek -> "Asosiy"
    }

    val emptyCartTitle: String get() = when (language) {
        AppLanguage.English -> "Your cart is empty"
        AppLanguage.Russian -> "Корзина пока пустая"
        AppLanguage.Uzbek -> "Savat hozircha bo'sh"
    }

    val emptyCartDescription: String get() = when (language) {
        AppLanguage.English -> "Add a few dishes from the catalog and checkout will appear here."
        AppLanguage.Russian -> "Добавь несколько позиций из каталога, и здесь появится красивый checkout."
        AppLanguage.Uzbek -> "Katalogdan bir nechta taom qo'shing, checkout shu yerda ko'rinadi."
    }

    val goHome: String get() = when (language) {
        AppLanguage.English -> "Go home"
        AppLanguage.Russian -> "На главную"
        AppLanguage.Uzbek -> "Asosiy sahifaga"
    }

    val deliveryAddress: String get() = when (language) {
        AppLanguage.English -> "Delivery address"
        AppLanguage.Russian -> "Адрес доставки"
        AppLanguage.Uzbek -> "Yetkazish manzili"
    }

    val items: String get() = when (language) {
        AppLanguage.English -> "Items"
        AppLanguage.Russian -> "Товары"
        AppLanguage.Uzbek -> "Mahsulotlar"
    }

    val food: String get() = when (language) {
        AppLanguage.English -> "Food"
        AppLanguage.Russian -> "Еда"
        AppLanguage.Uzbek -> "Taom"
    }

    fun itemsCount(count: Int): String = when (language) {
        AppLanguage.English -> "%02d items".format(count)
        AppLanguage.Russian -> "%02d поз.".format(count)
        AppLanguage.Uzbek -> "%02d ta".format(count)
    }

    val delivery: String get() = when (language) {
        AppLanguage.English -> "Delivery"
        AppLanguage.Russian -> "Доставка"
        AppLanguage.Uzbek -> "Yetkazish"
    }

    val service: String get() = when (language) {
        AppLanguage.English -> "Service"
        AppLanguage.Russian -> "Сервис"
        AppLanguage.Uzbek -> "Servis"
    }

    val discount: String get() = when (language) {
        AppLanguage.English -> "Discount"
        AppLanguage.Russian -> "Скидка"
        AppLanguage.Uzbek -> "Chegirma"
    }

    val edit: String get() = when (language) {
        AppLanguage.English -> "Edit"
        AppLanguage.Russian -> "Изменить"
        AppLanguage.Uzbek -> "O'zgartirish"
    }

    fun checkoutToPay(total: String): String = when (language) {
        AppLanguage.English -> "Pay $total"
        AppLanguage.Russian -> "К оплате $total"
        AppLanguage.Uzbek -> "To'lash $total"
    }

    val payment: String get() = when (language) {
        AppLanguage.English -> "Payment"
        AppLanguage.Russian -> "Оплата"
        AppLanguage.Uzbek -> "To'lov"
    }

    val whereDeliver: String get() = when (language) {
        AppLanguage.English -> "Where to deliver"
        AppLanguage.Russian -> "Куда везём"
        AppLanguage.Uzbek -> "Qayerga yetkazamiz"
    }

    val chooseDeliveryAddress: String get() = when (language) {
        AppLanguage.English -> "Choose one of your saved delivery addresses."
        AppLanguage.Russian -> "Выбери один из сохранённых адресов доставки."
        AppLanguage.Uzbek -> "Saqlangan yetkazish manzillaridan birini tanlang."
    }

    val route: String get() = when (language) {
        AppLanguage.English -> "Route"
        AppLanguage.Russian -> "Маршрут"
        AppLanguage.Uzbek -> "Yo'nalish"
    }

    fun kmToAddress(km: Double): String = when (language) {
        AppLanguage.English -> "$km km to address"
        AppLanguage.Russian -> "$km км до адреса"
        AppLanguage.Uzbek -> "Manzilgacha $km km"
    }

    fun etaMinutes(minutes: Int): String = when (language) {
        AppLanguage.English -> "ETA: $minutes min"
        AppLanguage.Russian -> "Ориентир: $minutes мин"
        AppLanguage.Uzbek -> "Taxminan: $minutes daqiqa"
    }

    val couponApplied: String get() = when (language) {
        AppLanguage.English -> "Coupon applied"
        AppLanguage.Russian -> "Купон применён"
        AppLanguage.Uzbek -> "Kupon qo'llandi"
    }

    val couponTitle: String get() = when (language) {
        AppLanguage.English -> "Discount coupon"
        AppLanguage.Russian -> "Купон на скидку"
        AppLanguage.Uzbek -> "Chegirma kuponi"
    }

    val couponSubtitle: String get() = when (language) {
        AppLanguage.English -> "Enter a code and lower the payment amount"
        AppLanguage.Russian -> "Введи код и уменьши сумму оплаты"
        AppLanguage.Uzbek -> "Kodni kiriting va to'lov summasini kamaytiring"
    }

    val apply: String get() = when (language) {
        AppLanguage.English -> "Apply"
        AppLanguage.Russian -> "Применить"
        AppLanguage.Uzbek -> "Qo'llash"
    }

    val change: String get() = when (language) {
        AppLanguage.English -> "Change"
        AppLanguage.Russian -> "Сменить"
        AppLanguage.Uzbek -> "Almashtirish"
    }

    val removeCoupon: String get() = when (language) {
        AppLanguage.English -> "Remove coupon"
        AppLanguage.Russian -> "Убрать купон"
        AppLanguage.Uzbek -> "Kuponni olib tashlash"
    }

    val cashOnDelivery: String get() = when (language) {
        AppLanguage.English -> "Cash on delivery"
        AppLanguage.Russian -> "Наличными курьеру"
        AppLanguage.Uzbek -> "Yetkazilganda naqd"
    }

    val cashDescription: String get() = when (language) {
        AppLanguage.English -> "Pay the courier when your order arrives."
        AppLanguage.Russian -> "Оплати курьеру, когда заказ приедет."
        AppLanguage.Uzbek -> "Buyurtma kelganda kuryerga to'lang."
    }

    val addNew: String get() = when (language) {
        AppLanguage.English -> "ADD NEW"
        AppLanguage.Russian -> "ДОБАВИТЬ"
        AppLanguage.Uzbek -> "QO'SHISH"
    }

    val addCard: String get() = when (language) {
        AppLanguage.English -> "Add Card"
        AppLanguage.Russian -> "Добавить карту"
        AppLanguage.Uzbek -> "Karta qo'shish"
    }

    val cardHolderName: String get() = when (language) {
        AppLanguage.English -> "CARD HOLDER NAME"
        AppLanguage.Russian -> "ИМЯ ВЛАДЕЛЬЦА"
        AppLanguage.Uzbek -> "KARTA EGASI"
    }

    val cardNumber: String get() = when (language) {
        AppLanguage.English -> "CARD NUMBER"
        AppLanguage.Russian -> "НОМЕР КАРТЫ"
        AppLanguage.Uzbek -> "KARTA RAQAMI"
    }

    val expireDate: String get() = when (language) {
        AppLanguage.English -> "EXPIRE DATE"
        AppLanguage.Russian -> "СРОК ДЕЙСТВИЯ"
        AppLanguage.Uzbek -> "AMAL QILISH MUDDATI"
    }

    val saving: String get() = when (language) {
        AppLanguage.English -> "SAVING..."
        AppLanguage.Russian -> "СОХРАНЯЕМ..."
        AppLanguage.Uzbek -> "SAQLANMOQDA..."
    }

    val addMakePayment: String get() = when (language) {
        AppLanguage.English -> "ADD & MAKE PAYMENT"
        AppLanguage.Russian -> "ДОБАВИТЬ И ОПЛАТИТЬ"
        AppLanguage.Uzbek -> "QO'SHISH VA TO'LASH"
    }

    fun detected(label: String): String = when (language) {
        AppLanguage.English -> "$label detected"
        AppLanguage.Russian -> "$label определена"
        AppLanguage.Uzbek -> "$label aniqlandi"
    }

    val total: String get() = when (language) {
        AppLanguage.English -> "TOTAL:"
        AppLanguage.Russian -> "ИТОГО:"
        AppLanguage.Uzbek -> "JAMI:"
    }

    val processing: String get() = when (language) {
        AppLanguage.English -> "PROCESSING..."
        AppLanguage.Russian -> "ОФОРМЛЯЕМ..."
        AppLanguage.Uzbek -> "RASMIYLASHTIRILMOQDA..."
    }

    val payConfirm: String get() = when (language) {
        AppLanguage.English -> "PAY & CONFIRM"
        AppLanguage.Russian -> "ОПЛАТИТЬ И ПОДТВЕРДИТЬ"
        AppLanguage.Uzbek -> "TO'LASH VA TASDIQLASH"
    }

    val successTitle: String get() = when (language) {
        AppLanguage.English -> "Order placed"
        AppLanguage.Russian -> "Заказ оформлен"
        AppLanguage.Uzbek -> "Buyurtma rasmiylashtirildi"
    }

    val trackOrder: String get() = when (language) {
        AppLanguage.English -> "Track order"
        AppLanguage.Russian -> "Отследить заказ"
        AppLanguage.Uzbek -> "Buyurtmani kuzatish"
    }

    fun orderAt(date: String): String = when (language) {
        AppLanguage.English -> "Ordered at $date"
        AppLanguage.Russian -> "Заказ от $date"
        AppLanguage.Uzbek -> "$date dagi buyurtma"
    }

    fun refundedToAccount(amount: String): String = when (language) {
        AppLanguage.English -> "Refunded $amount to account"
        AppLanguage.Russian -> "$amount возвращено на счёт"
        AppLanguage.Uzbek -> "$amount hisobga qaytarildi"
    }

    val eta: String get() = when (language) {
        AppLanguage.English -> "ETA"
        AppLanguage.Russian -> "Время"
        AppLanguage.Uzbek -> "Yetib kelish"
    }

    val distance: String get() = when (language) {
        AppLanguage.English -> "Distance"
        AppLanguage.Russian -> "Расстояние"
        AppLanguage.Uzbek -> "Masofa"
    }

    fun kmByRoute(distance: Double): String = when (language) {
        AppLanguage.English -> "$distance km by route"
        AppLanguage.Russian -> "$distance км по маршруту"
        AppLanguage.Uzbek -> "$distance km marshrut bo'yicha"
    }

    val paymentLabel: String get() = when (language) {
        AppLanguage.English -> "Payment"
        AppLanguage.Russian -> "Оплата"
        AppLanguage.Uzbek -> "To'lov"
    }

    val liveTracker: String get() = when (language) {
        AppLanguage.English -> "Live tracker"
        AppLanguage.Russian -> "Живой трекер"
        AppLanguage.Uzbek -> "Jonli kuzatuv"
    }

    val kitchen: String get() = when (language) {
        AppLanguage.English -> "Kitchen"
        AppLanguage.Russian -> "Кухня"
        AppLanguage.Uzbek -> "Oshxona"
    }

    val courier: String get() = when (language) {
        AppLanguage.English -> "Courier"
        AppLanguage.Russian -> "Курьер"
        AppLanguage.Uzbek -> "Kuryer"
    }

    val door: String get() = when (language) {
        AppLanguage.English -> "Door"
        AppLanguage.Russian -> "Дверь"
        AppLanguage.Uzbek -> "Eshik"
    }

    val cardHolder: String get() = when (language) {
        AppLanguage.English -> "Card holder"
        AppLanguage.Russian -> "Владелец карты"
        AppLanguage.Uzbek -> "Karta egasi"
    }

    val deleteCard: String get() = when (language) {
        AppLanguage.English -> "Delete card"
        AppLanguage.Russian -> "Удалить карту"
        AppLanguage.Uzbek -> "Kartani o'chirish"
    }

    val orders: String get() = when (language) {
        AppLanguage.English -> "Orders"
        AppLanguage.Russian -> "Заказы"
        AppLanguage.Uzbek -> "Buyurtmalar"
    }

    val ongoing: String get() = when (language) {
        AppLanguage.English -> "Ongoing"
        AppLanguage.Russian -> "Активные"
        AppLanguage.Uzbek -> "Jarayonda"
    }

    val history: String get() = when (language) {
        AppLanguage.English -> "History"
        AppLanguage.Russian -> "История"
        AppLanguage.Uzbek -> "Tarix"
    }

    val rate: String get() = when (language) {
        AppLanguage.English -> "Rate"
        AppLanguage.Russian -> "Оценить"
        AppLanguage.Uzbek -> "Baholash"
    }

    val rated: String get() = when (language) {
        AppLanguage.English -> "Rated"
        AppLanguage.Russian -> "Оценен"
        AppLanguage.Uzbek -> "Baholangan"
    }

    val rateOrder: String get() = when (language) {
        AppLanguage.English -> "Rate order"
        AppLanguage.Russian -> "Оценить заказ"
        AppLanguage.Uzbek -> "Buyurtmani baholash"
    }

    val orderLabel: String get() = when (language) {
        AppLanguage.English -> "Order"
        AppLanguage.Russian -> "Заказ"
        AppLanguage.Uzbek -> "Buyurtma"
    }

    val reviewPlaceholder: String get() = when (language) {
        AppLanguage.English -> "Write a short review"
        AppLanguage.Russian -> "Напишите короткий отзыв"
        AppLanguage.Uzbek -> "Qisqa izoh yozing"
    }

    val chat: String get() = when (language) {
        AppLanguage.English -> "Chat"
        AppLanguage.Russian -> "Чат"
        AppLanguage.Uzbek -> "Chat"
    }

    val messages: String get() = when (language) {
        AppLanguage.English -> "Messages"
        AppLanguage.Russian -> "Сообщения"
        AppLanguage.Uzbek -> "Xabarlar"
    }

    val reviews: String get() = when (language) {
        AppLanguage.English -> "Reviews"
        AppLanguage.Russian -> "Отзывы"
        AppLanguage.Uzbek -> "Sharhlar"
    }

    val courierChat: String get() = when (language) {
        AppLanguage.English -> "Courier chat"
        AppLanguage.Russian -> "Чат с курьером"
        AppLanguage.Uzbek -> "Kuryer bilan chat"
    }

    val writeToCourier: String get() = when (language) {
        AppLanguage.English -> "Write to courier"
        AppLanguage.Russian -> "Напишите курьеру"
        AppLanguage.Uzbek -> "Kuryerga yozing"
    }

    val noOrderSelected: String get() = when (language) {
        AppLanguage.English -> "No order selected"
        AppLanguage.Russian -> "Заказ не выбран"
        AppLanguage.Uzbek -> "Buyurtma tanlanmagan"
    }

    val openChatFromOrder: String get() = when (language) {
        AppLanguage.English -> "Open chat from an ongoing order."
        AppLanguage.Russian -> "Откройте чат из активного заказа."
        AppLanguage.Uzbek -> "Chatni faol buyurtmadan oching."
    }

    val chatClosedAfterDelivery: String get() = when (language) {
        AppLanguage.English -> "Chat closes automatically after the order is delivered."
        AppLanguage.Russian -> "Чат автоматически закрывается после доставки заказа."
        AppLanguage.Uzbek -> "Buyurtma yetkazilgandan keyin chat avtomatik yopiladi."
    }

    val back: String get() = when (language) {
        AppLanguage.English -> "Back"
        AppLanguage.Russian -> "Назад"
        AppLanguage.Uzbek -> "Orqaga"
    }

    fun statusPrefix(value: String): String = when (language) {
        AppLanguage.English -> "Status: $value"
        AppLanguage.Russian -> "Статус: $value"
        AppLanguage.Uzbek -> "Holat: $value"
    }

    fun customerOrderStatus(status: OrderStatus): String = when (status) {
        OrderStatus.Preparing -> when (language) {
            AppLanguage.English -> "Preparing"
            AppLanguage.Russian -> "Готовится"
            AppLanguage.Uzbek -> "Tayyorlanmoqda"
        }
        OrderStatus.OnTheWay -> when (language) {
            AppLanguage.English -> "Delivering"
            AppLanguage.Russian -> "Доставляется"
            AppLanguage.Uzbek -> "Yetkazilmoqda"
        }
        OrderStatus.Delivered -> when (language) {
            AppLanguage.English -> "Delivered"
            AppLanguage.Russian -> "Доставлено"
            AppLanguage.Uzbek -> "Yetkazildi"
        }
        OrderStatus.Cancelled -> when (language) {
            AppLanguage.English -> "Cancelled"
            AppLanguage.Russian -> "Отменено"
            AppLanguage.Uzbek -> "Bekor qilindi"
        }
    }

    fun courierCustomerStatus(status: CourierOrderStatus): String = when (status) {
        CourierOrderStatus.Available -> when (language) {
            AppLanguage.English -> "Preparing"
            AppLanguage.Russian -> "Готовится"
            AppLanguage.Uzbek -> "Tayyorlanmoqda"
        }
        CourierOrderStatus.Accepted -> when (language) {
            AppLanguage.English -> "Courier accepted"
            AppLanguage.Russian -> "Курьер принял"
            AppLanguage.Uzbek -> "Kuryer qabul qildi"
        }
        CourierOrderStatus.ArrivedAtRestaurant -> when (language) {
            AppLanguage.English -> "Courier at restaurant"
            AppLanguage.Russian -> "Курьер у ресторана"
            AppLanguage.Uzbek -> "Kuryer restoranda"
        }
        CourierOrderStatus.PickedUp -> when (language) {
            AppLanguage.English -> "Courier picked up"
            AppLanguage.Russian -> "Курьер забрал"
            AppLanguage.Uzbek -> "Kuryer oldi"
        }
        CourierOrderStatus.OnTheWay -> when (language) {
            AppLanguage.English -> "Delivering"
            AppLanguage.Russian -> "Доставляется"
            AppLanguage.Uzbek -> "Yetkazilmoqda"
        }
        CourierOrderStatus.Delivered -> when (language) {
            AppLanguage.English -> "Delivered"
            AppLanguage.Russian -> "Доставлено"
            AppLanguage.Uzbek -> "Yetkazildi"
        }
    }

    val reorder: String get() = when (language) {
        AppLanguage.English -> "Re-order"
        AppLanguage.Russian -> "Повторить"
        AppLanguage.Uzbek -> "Qayta buyurtma"
    }

    val noActiveOrdersTitle: String get() = when (language) {
        AppLanguage.English -> "No active orders"
        AppLanguage.Russian -> "Активных заказов нет"
        AppLanguage.Uzbek -> "Faol buyurtmalar yo'q"
    }

    val noActiveOrdersDescription: String get() = when (language) {
        AppLanguage.English -> "When you place an order, it will stay here until delivery is complete."
        AppLanguage.Russian -> "Когда оформишь заказ, он появится здесь до завершения доставки."
        AppLanguage.Uzbek -> "Buyurtma berganingizda, u yetkazib berish tugaguncha shu yerda bo'ladi."
    }

    val emptyHistoryTitle: String get() = when (language) {
        AppLanguage.English -> "History is empty"
        AppLanguage.Russian -> "История пока пустая"
        AppLanguage.Uzbek -> "Tarix hozircha bo'sh"
    }

    val emptyHistoryDescription: String get() = when (language) {
        AppLanguage.English -> "Delivered and cancelled orders will appear here automatically."
        AppLanguage.Russian -> "Доставленные и отмененные заказы появятся здесь автоматически."
        AppLanguage.Uzbek -> "Yetkazilgan va bekor qilingan buyurtmalar shu yerda ko'rinadi."
    }

    val refundBalance: String get() = when (language) {
        AppLanguage.English -> "Refund balance"
        AppLanguage.Russian -> "Баланс возврата"
        AppLanguage.Uzbek -> "Qaytarilgan balans"
    }

    val returnedToAccount: String get() = when (language) {
        AppLanguage.English -> "Returned to account"
        AppLanguage.Russian -> "Вернули на счёт"
        AppLanguage.Uzbek -> "Hisobga qaytarildi"
    }

    fun cancelWindow(time: String): String = when (language) {
        AppLanguage.English -> "Cancel window: $time"
        AppLanguage.Russian -> "Отмена доступна: $time"
        AppLanguage.Uzbek -> "Bekor qilish vaqti: $time"
    }

    val cancellationLocked: String get() = when (language) {
        AppLanguage.English -> "Cancellation locked after 5 min"
        AppLanguage.Russian -> "Отмена закрывается через 5 мин"
        AppLanguage.Uzbek -> "Bekor qilish 5 daqiqadan keyin yopiladi"
    }

    val cancel: String get() = when (language) {
        AppLanguage.English -> "Cancel"
        AppLanguage.Russian -> "Отменить"
        AppLanguage.Uzbek -> "Bekor qilish"
    }

    val profile: String get() = when (language) {
        AppLanguage.English -> "Profile"
        AppLanguage.Russian -> "Профиль"
        AppLanguage.Uzbek -> "Profil"
    }

    val personalInfo: String get() = when (language) {
        AppLanguage.English -> "Personal Info"
        AppLanguage.Russian -> "Личная информация"
        AppLanguage.Uzbek -> "Shaxsiy ma'lumot"
    }

    val addresses: String get() = when (language) {
        AppLanguage.English -> "Addresses"
        AppLanguage.Russian -> "Адреса"
        AppLanguage.Uzbek -> "Manzillar"
    }

    fun favorites(count: Int): String = when (language) {
        AppLanguage.English -> "Favourite ($count)"
        AppLanguage.Russian -> "Избранное ($count)"
        AppLanguage.Uzbek -> "Sevimlilar ($count)"
    }

    fun coupons(count: Int): String = when (language) {
        AppLanguage.English -> "Coupons ($count)"
        AppLanguage.Russian -> "Купоны ($count)"
        AppLanguage.Uzbek -> "Kuponlar ($count)"
    }

    val couponsTitle: String get() = when (language) {
        AppLanguage.English -> "Coupons"
        AppLanguage.Russian -> "Купоны"
        AppLanguage.Uzbek -> "Kuponlar"
    }

    val activeDiscounts: String get() = when (language) {
        AppLanguage.English -> "Active discounts"
        AppLanguage.Russian -> "Активные скидки"
        AppLanguage.Uzbek -> "Faol chegirmalar"
    }

    val noCouponsTitle: String get() = when (language) {
        AppLanguage.English -> "No active coupons yet."
        AppLanguage.Russian -> "Пока что нет активных купонов."
        AppLanguage.Uzbek -> "Hozircha faol kuponlar yo'q."
    }

    val noCouponsDescription: String get() = when (language) {
        AppLanguage.English -> "New discounts will appear here automatically."
        AppLanguage.Russian -> "Новые скидки появятся здесь автоматически."
        AppLanguage.Uzbek -> "Yangi chegirmalar shu yerda avtomatik ko'rinadi."
    }

    fun couponValidUntil(date: String): String = when (language) {
        AppLanguage.English -> "Valid until $date"
        AppLanguage.Russian -> "Действует до $date"
        AppLanguage.Uzbek -> "$date gacha amal qiladi"
    }

    val copiedCode: String get() = when (language) {
        AppLanguage.English -> "Code copied"
        AppLanguage.Russian -> "Код скопирован"
        AppLanguage.Uzbek -> "Kod nusxalandi"
    }

    val copyCode: String get() = when (language) {
        AppLanguage.English -> "Copy code"
        AppLanguage.Russian -> "Скопировать код"
        AppLanguage.Uzbek -> "Kodni nusxalash"
    }

    val welcomeGiftTitle: String get() = when (language) {
        AppLanguage.English -> "Welcome gift"
        AppLanguage.Russian -> "Подарок новичку"
        AppLanguage.Uzbek -> "Yangi foydalanuvchi sovg'asi"
    }

    val tapCodeToCopy: String get() = when (language) {
        AppLanguage.English -> "Tap the code to copy"
        AppLanguage.Russian -> "Нажми на код, чтобы скопировать"
        AppLanguage.Uzbek -> "Nusxalash uchun kodni bosing"
    }

    val copyCodeUpper: String get() = when (language) {
        AppLanguage.English -> "COPY CODE"
        AppLanguage.Russian -> "СКОПИРОВАТЬ КОД"
        AppLanguage.Uzbek -> "KODNI NUSXALASH"
    }

    val copiedUpper: String get() = when (language) {
        AppLanguage.English -> "COPIED"
        AppLanguage.Russian -> "СКОПИРОВАНО"
        AppLanguage.Uzbek -> "NUSXALANDI"
    }

    val later: String get() = when (language) {
        AppLanguage.English -> "LATER"
        AppLanguage.Russian -> "ПОЗЖЕ"
        AppLanguage.Uzbek -> "KEYINROQ"
    }

    val notifications: String get() = when (language) {
        AppLanguage.English -> "Notifications"
        AppLanguage.Russian -> "Уведомления"
        AppLanguage.Uzbek -> "Bildirishnomalar"
    }

    fun paymentMethod(count: Int): String = when (language) {
        AppLanguage.English -> "Payment Method ($count)"
        AppLanguage.Russian -> "Способ оплаты ($count)"
        AppLanguage.Uzbek -> "To'lov usuli ($count)"
    }

    val faqs: String get() = when (language) {
        AppLanguage.English -> "FAQs"
        AppLanguage.Russian -> "Вопросы"
        AppLanguage.Uzbek -> "Savollar"
    }

    val faqItems: List<Pair<String, String>> get() = when (language) {
        AppLanguage.English -> listOf(
            "How do I add food to the cart?" to "Open a restaurant or category, tap + on a food card, then open Cart from Profile or the cart icon.",
            "How do favourites work?" to "Tap the heart on any food card. Saved dishes appear in Favourite, where you can open them or order again.",
            "Which payment methods are available?" to "You can pay by cash, Visa, Mastercard, Uzcard, or HumoCard.",
            "Can I cancel an order?" to "Cancellation is available during the first 5 minutes while the restaurant is preparing the order.",
            "Can I change the delivery address?" to "Yes. Open Addresses from Profile or edit the address before checkout.",
        )
        AppLanguage.Russian -> listOf(
            "Как добавить блюдо в корзину?" to "Открой ресторан или категорию, нажми + на карточке блюда, затем открой корзину через профиль или иконку корзины.",
            "Как работает избранное?" to "Нажми сердечко на карточке блюда. Сохранённые блюда появятся в избранном, где их можно открыть или заказать снова.",
            "Какие способы оплаты доступны?" to "Можно оплатить наличными, Visa, Mastercard, Uzcard или HumoCard.",
            "Можно ли отменить заказ?" to "Отмена доступна первые 5 минут, пока ресторан готовит заказ.",
            "Можно ли изменить адрес доставки?" to "Да. Открой адреса из профиля или измени адрес перед оплатой.",
        )
        AppLanguage.Uzbek -> listOf(
            "Taomni savatga qanday qo'shaman?" to "Restoran yoki kategoriyani oching, taom kartasidagi + ni bosing, keyin profil yoki savat ikonkasidan savatni oching.",
            "Sevimlilar qanday ishlaydi?" to "Taom kartasidagi yurakchani bosing. Saqlangan taomlar sevimlilarda ko'rinadi.",
            "Qaysi to'lov usullari bor?" to "Naqd, Visa, Mastercard, Uzcard yoki HumoCard orqali to'lash mumkin.",
            "Buyurtmani bekor qila olamanmi?" to "Restoran tayyorlayotgan birinchi 5 daqiqa ichida bekor qilish mumkin.",
            "Yetkazish manzilini o'zgartirish mumkinmi?" to "Ha. Profildagi manzillarni oching yoki to'lovdan oldin manzilni o'zgartiring.",
        )
    }

    val settings: String get() = when (language) {
        AppLanguage.English -> "Settings"
        AppLanguage.Russian -> "Настройки"
        AppLanguage.Uzbek -> "Sozlamalar"
    }

    val logOut: String get() = when (language) {
        AppLanguage.English -> "Log Out"
        AppLanguage.Russian -> "Выйти"
        AppLanguage.Uzbek -> "Chiqish"
    }

    val editProfile: String get() = when (language) {
        AppLanguage.English -> "Edit profile"
        AppLanguage.Russian -> "Изменить профиль"
        AppLanguage.Uzbek -> "Profilni o'zgartirish"
    }

    val defaultBio: String get() = when (language) {
        AppLanguage.English -> "I love fast food"
        AppLanguage.Russian -> "Люблю вкусную доставку"
        AppLanguage.Uzbek -> "Mazali yetkazishni yoqtiraman"
    }

    val favoriteTitle: String get() = when (language) {
        AppLanguage.English -> "Favourite"
        AppLanguage.Russian -> "Избранное"
        AppLanguage.Uzbek -> "Sevimlilar"
    }

    val noFavoriteTitle: String get() = when (language) {
        AppLanguage.English -> "No favourite food yet"
        AppLanguage.Russian -> "Избранных блюд пока нет"
        AppLanguage.Uzbek -> "Sevimli taomlar hali yo'q"
    }

    val noFavoriteDescription: String get() = when (language) {
        AppLanguage.English -> "Tap the heart on any food card. Saved dishes will appear here so you can order them again."
        AppLanguage.Russian -> "Нажми на сердечко у блюда. Сохранённые позиции появятся здесь, чтобы заказать их снова."
        AppLanguage.Uzbek -> "Taom kartasidagi yurakchani bosing. Saqlangan taomlarni shu yerdan yana buyurtma qilasiz."
    }

    fun savedItems(count: Int): String = when (language) {
        AppLanguage.English -> "$count saved item(s)"
        AppLanguage.Russian -> "$count избранных"
        AppLanguage.Uzbek -> "$count ta saqlangan"
    }

    val orderAgain: String get() = when (language) {
        AppLanguage.English -> "Order again"
        AppLanguage.Russian -> "Заказать снова"
        AppLanguage.Uzbek -> "Yana buyurtma"
    }

    val fullName: String get() = when (language) {
        AppLanguage.English -> "FULL NAME"
        AppLanguage.Russian -> "ПОЛНОЕ ИМЯ"
        AppLanguage.Uzbek -> "TO'LIQ ISM"
    }

    val phone: String get() = when (language) {
        AppLanguage.English -> "PHONE"
        AppLanguage.Russian -> "ТЕЛЕФОН"
        AppLanguage.Uzbek -> "TELEFON"
    }

    val bio: String get() = when (language) {
        AppLanguage.English -> "BIO"
        AppLanguage.Russian -> "О СЕБЕ"
        AppLanguage.Uzbek -> "BIO"
    }

    val myAddress: String get() = when (language) {
        AppLanguage.English -> "My Address"
        AppLanguage.Russian -> "Мой адрес"
        AppLanguage.Uzbek -> "Mening manzilim"
    }

    val addAddress: String get() = when (language) {
        AppLanguage.English -> "Add address"
        AppLanguage.Russian -> "Добавить адрес"
        AppLanguage.Uzbek -> "Manzil qo'shish"
    }

    val selected: String get() = when (language) {
        AppLanguage.English -> "Selected"
        AppLanguage.Russian -> "Выбран"
        AppLanguage.Uzbek -> "Tanlangan"
    }

    val choose: String get() = when (language) {
        AppLanguage.English -> "Choose"
        AppLanguage.Russian -> "Выбрать"
        AppLanguage.Uzbek -> "Tanlash"
    }

    val delete: String get() = when (language) {
        AppLanguage.English -> "Delete"
        AppLanguage.Russian -> "Удалить"
        AppLanguage.Uzbek -> "O'chirish"
    }

    val save: String get() = when (language) {
        AppLanguage.English -> "SAVE"
        AppLanguage.Russian -> "СОХРАНИТЬ"
        AppLanguage.Uzbek -> "SAQLASH"
    }

    val availableBonus: String get() = when (language) {
        AppLanguage.English -> "Available bonus"
        AppLanguage.Russian -> "Доступный бонус"
        AppLanguage.Uzbek -> "Mavjud bonus"
    }

    fun addedToCart(itemTitle: String): String = when (language) {
        AppLanguage.English -> "$itemTitle added to cart"
        AppLanguage.Russian -> "$itemTitle добавлено в корзину"
        AppLanguage.Uzbek -> "$itemTitle savatga qo'shildi"
    }

    val openCartAction: String get() = when (language) {
        AppLanguage.English -> "Cart"
        AppLanguage.Russian -> "Корзина"
        AppLanguage.Uzbek -> "Savat"
    }

    fun paymentMethodTitle(method: PaymentMethod): String = when (method) {
        PaymentMethod.Cash -> cashOnDelivery
        PaymentMethod.Visa -> "Visa"
        PaymentMethod.MasterCard -> "Mastercard"
        PaymentMethod.Uzcard -> "Uzcard"
        PaymentMethod.HumoCard -> "HumoCard"
    }

    fun noPaymentMethodAdded(methodTitle: String): String = when (language) {
        AppLanguage.English -> "No ${methodTitle.lowercase()} added"
        AppLanguage.Russian -> "$methodTitle не добавлена"
        AppLanguage.Uzbek -> "$methodTitle qo'shilmagan"
    }

    fun addPaymentMethodHint(methodTitle: String, checkoutMode: Boolean): String = when (language) {
        AppLanguage.English -> if (checkoutMode) {
            "Add a ${methodTitle.lowercase()} in Profile > Payment Method, then choose it here."
        } else {
            "You can add a ${methodTitle.lowercase()} and save it for later."
        }
        AppLanguage.Russian -> if (checkoutMode) {
            "Добавь $methodTitle в профиле, затем выбери её здесь."
        } else {
            "Можно добавить $methodTitle и сохранить её для следующих заказов."
        }
        AppLanguage.Uzbek -> if (checkoutMode) {
            "$methodTitle profil to'lov usullariga qo'shing, keyin shu yerda tanlang."
        } else {
            "$methodTitle qo'shib, keyingi buyurtmalar uchun saqlab qo'yishingiz mumkin."
        }
    }

    fun deliveryFeeLabel(fee: String): String {
        val isFree = fee.contains("free", ignoreCase = true) ||
            fee.contains("бесплат", ignoreCase = true) ||
            fee.contains("bepul", ignoreCase = true)
        return if (isFree) {
            when (language) {
                AppLanguage.English -> "Free"
                AppLanguage.Russian -> "Бесплатно"
                AppLanguage.Uzbek -> "Bepul"
            }
        } else {
            fee
        }
    }

    fun deliveryTimeLabel(time: String): String = when (language) {
        AppLanguage.English -> time.replace("мин", "min")
        AppLanguage.Russian -> time.replace("min", "мин").replace("daq", "мин")
        AppLanguage.Uzbek -> time.replace("мин", "daq").replace("min", "daq")
    }

    fun status(status: OrderStatus): String = when (status) {
        OrderStatus.Preparing -> when (language) {
            AppLanguage.English -> "Preparing"
            AppLanguage.Russian -> "Готовим"
            AppLanguage.Uzbek -> "Tayyorlanmoqda"
        }
        OrderStatus.OnTheWay -> when (language) {
            AppLanguage.English -> "On the way"
            AppLanguage.Russian -> "В пути"
            AppLanguage.Uzbek -> "Yo'lda"
        }
        OrderStatus.Delivered -> when (language) {
            AppLanguage.English -> "Delivered"
            AppLanguage.Russian -> "Доставлен"
            AppLanguage.Uzbek -> "Yetkazildi"
        }
        OrderStatus.Cancelled -> when (language) {
            AppLanguage.English -> "Cancelled"
            AppLanguage.Russian -> "Отменён"
            AppLanguage.Uzbek -> "Bekor qilindi"
        }
    }

    fun menuGroup(label: String): String = when (label) {
        "All", "Все" -> when (language) {
            AppLanguage.English -> "All"
            AppLanguage.Russian -> "Все"
            AppLanguage.Uzbek -> "Hammasi"
        }
        "Fast food", "Фастфуд" -> when (language) {
            AppLanguage.English -> "Fast food"
            AppLanguage.Russian -> "Фастфуд"
            AppLanguage.Uzbek -> "Fastfud"
        }
        "Суши", "Sushi", "Суши и роллы", "Sushi and rolls" -> when (language) {
            AppLanguage.English -> "Sushi"
            AppLanguage.Russian -> if (label == "Суши и роллы" || label == "Sushi and rolls") "Суши и роллы" else "Суши"
            AppLanguage.Uzbek -> "Sushi"
        }
        "Плов", "Plov" -> when (language) {
            AppLanguage.English -> "Plov"
            AppLanguage.Russian -> "Плов"
            AppLanguage.Uzbek -> "Osh"
        }
        "Национальная кухня", "National cuisine" -> when (language) {
            AppLanguage.English -> "National cuisine"
            AppLanguage.Russian -> "Национальная кухня"
            AppLanguage.Uzbek -> "Milliy oshxona"
        }
        "Азиатская кухня", "Asian cuisine" -> when (language) {
            AppLanguage.English -> "Asian"
            AppLanguage.Russian -> "Азиатская кухня"
            AppLanguage.Uzbek -> "Osiyo oshxonasi"
        }
        "Лапша", "Noodles" -> when (language) {
            AppLanguage.English -> "Noodles"
            AppLanguage.Russian -> "Лапша"
            AppLanguage.Uzbek -> "Lag'mon"
        }
        "Европейская кухня", "European cuisine" -> when (language) {
            AppLanguage.English -> "European"
            AppLanguage.Russian -> "Европейская кухня"
            AppLanguage.Uzbek -> "Yevropa oshxonasi"
        }
        "Курица", "Chicken" -> when (language) {
            AppLanguage.English -> "Chicken"
            AppLanguage.Russian -> "Курица"
            AppLanguage.Uzbek -> "Tovuq"
        }
        "Шаурма и донер", "Shawarma and doner" -> when (language) {
            AppLanguage.English -> "Shawarma"
            AppLanguage.Russian -> "Шаурма и донер"
            AppLanguage.Uzbek -> "Shaurma va doner"
        }
        "Салаты", "Salads" -> when (language) {
            AppLanguage.English -> "Salads"
            AppLanguage.Russian -> "Салаты"
            AppLanguage.Uzbek -> "Salatlar"
        }
        "Завтраки", "Breakfasts" -> when (language) {
            AppLanguage.English -> "Breakfasts"
            AppLanguage.Russian -> "Завтраки"
            AppLanguage.Uzbek -> "Nonushtalar"
        }
        "Десерты", "Desserts" -> when (language) {
            AppLanguage.English -> "Desserts"
            AppLanguage.Russian -> "Десерты"
            AppLanguage.Uzbek -> "Desertlar"
        }
        "Напитки", "Drinks" -> when (language) {
            AppLanguage.English -> "Drinks"
            AppLanguage.Russian -> "Напитки"
            AppLanguage.Uzbek -> "Ichimliklar"
        }
        "Вегетарианская еда", "Vegetarian food" -> when (language) {
            AppLanguage.English -> "Vegetarian"
            AppLanguage.Russian -> "Вегетарианская еда"
            AppLanguage.Uzbek -> "Vegetarian"
        }
        "Здоровое питание", "Healthy food" -> when (language) {
            AppLanguage.English -> "Healthy"
            AppLanguage.Russian -> "Здоровое питание"
            AppLanguage.Uzbek -> "Foydali taom"
        }
        "Семейные сеты", "Family sets" -> when (language) {
            AppLanguage.English -> "Family sets"
            AppLanguage.Russian -> "Семейные сеты"
            AppLanguage.Uzbek -> "Oilaviy setlar"
        }
        "Комбо", "Combo" -> when (language) {
            AppLanguage.English -> "Combo"
            AppLanguage.Russian -> "Комбо"
            AppLanguage.Uzbek -> "Kombo"
        }
        "Детское меню", "Kids menu" -> when (language) {
            AppLanguage.English -> "Kids menu"
            AppLanguage.Russian -> "Детское меню"
            AppLanguage.Uzbek -> "Bolalar menyusi"
        }
        "Выпечка", "Bakery" -> when (language) {
            AppLanguage.English -> "Bakery"
            AppLanguage.Russian -> "Выпечка"
            AppLanguage.Uzbek -> "Pishiriqlar"
        }
        "Морепродукты", "Seafood" -> when (language) {
            AppLanguage.English -> "Seafood"
            AppLanguage.Russian -> "Морепродукты"
            AppLanguage.Uzbek -> "Dengiz mahsulotlari"
        }
        "Milliy", "National", "Национальная кухня" -> when (language) {
            AppLanguage.English -> "Uzbek"
            AppLanguage.Russian -> "Milliy"
            AppLanguage.Uzbek -> "Milliy"
        }
        "Гриль", "Grill" -> when (language) {
            AppLanguage.English -> "Grill"
            AppLanguage.Russian -> "Гриль"
            AppLanguage.Uzbek -> "Gril"
        }
        "Супы", "Soup", "Soups" -> when (language) {
            AppLanguage.English -> "Soups"
            AppLanguage.Russian -> "Супы"
            AppLanguage.Uzbek -> "Sho'rvalar"
        }
        "Burger", "Burgers", "Бургеры" -> when (language) {
            AppLanguage.English -> "Burgers"
            AppLanguage.Russian -> "Бургеры"
            AppLanguage.Uzbek -> "Burgerlar"
        }
        "Cheeseburger", "Чизбургер" -> when (language) {
            AppLanguage.English -> "Cheeseburger"
            AppLanguage.Russian -> "Чизбургер"
            AppLanguage.Uzbek -> "Chizburger"
        }
        "Chicken Burger", "Chicken burger", "Чикенбургер" -> when (language) {
            AppLanguage.English -> "Chicken burger"
            AppLanguage.Russian -> "Чикенбургер"
            AppLanguage.Uzbek -> "Tovuqli burger"
        }
        "Menu" -> when (language) {
            AppLanguage.English -> "Menu"
            AppLanguage.Russian -> "Меню"
            AppLanguage.Uzbek -> "Menyu"
        }
        "Sandwich", "Sandwiches" -> when (language) {
            AppLanguage.English -> "Sandwich"
            AppLanguage.Russian -> "Сэндвичи"
            AppLanguage.Uzbek -> "Sendvich"
        }
        "Shawarma", "Шаурма" -> when (language) {
            AppLanguage.English -> "Shawarma"
            AppLanguage.Russian -> "Шаурма"
            AppLanguage.Uzbek -> "Shaurma"
        }
        "Pizza", "Пицца" -> when (language) {
            AppLanguage.English -> "Pizza"
            AppLanguage.Russian -> "Пицца"
            AppLanguage.Uzbek -> "Pizza"
        }
        "Wok" -> "Wok"
        "Wings", "Крылышки" -> when (language) {
            AppLanguage.English -> "Wings"
            AppLanguage.Russian -> "Крылышки"
            AppLanguage.Uzbek -> "Qanotchalar"
        }
        "Fries", "Фри" -> when (language) {
            AppLanguage.English -> "Fries"
            AppLanguage.Russian -> "Фри"
            AppLanguage.Uzbek -> "Fri"
        }
        "Snack", "Snacks", "Снеки" -> when (language) {
            AppLanguage.English -> "Snacks"
            AppLanguage.Russian -> "Снеки"
            AppLanguage.Uzbek -> "Sneklar"
        }
        "Hot Dog", "Hot Dogs", "Хот-доги" -> when (language) {
            AppLanguage.English -> "Hot dogs"
            AppLanguage.Russian -> "Хот-доги"
            AppLanguage.Uzbek -> "Hot-doglar"
        }
        "Healthy", "Полезное" -> when (language) {
            AppLanguage.English -> "Healthy"
            AppLanguage.Russian -> "Полезное"
            AppLanguage.Uzbek -> "Foydali"
        }
        "Dessert", "Десерты" -> when (language) {
            AppLanguage.English -> "Dessert"
            AppLanguage.Russian -> "Десерты"
            AppLanguage.Uzbek -> "Desertlar"
        }
        "Drink", "Напитки" -> when (language) {
            AppLanguage.English -> "Drink"
            AppLanguage.Russian -> "Напитки"
            AppLanguage.Uzbek -> "Ichimliklar"
        }
        else -> label
    }
}
