package com.example.fooddeliveryapp.ui.components

import com.example.fooddeliveryapp.ui.data.SampleData
import org.junit.Assert.assertTrue
import org.junit.Test

class FoodDrawableRegistryTest {
    @Test
    fun everyBuiltInSampleImageResolves() {
        val builtInImages = SampleData.restaurants
            .asSequence()
            .flatMap { restaurant ->
                sequenceOf(restaurant.imageUrl) +
                    restaurant.imageUrls.asSequence() +
                    restaurant.menu.asSequence().map { it.imageUrl }
            }
            .filter { it.startsWith("android.resource://") }
            .toSet()

        assertTrue("Expected built-in sample images to be present", builtInImages.isNotEmpty())

        val unresolved = builtInImages.filterNot { model ->
            FoodDrawableRegistry.resolve(model) != null
        }

        assertTrue("Unresolved built-in images: $unresolved", unresolved.isEmpty())
    }
}
