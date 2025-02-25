/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.feature.fxsuggest

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import mozilla.appservices.suggest.Suggestion
import mozilla.appservices.suggest.SuggestionQuery
import mozilla.components.support.test.any
import mozilla.components.support.test.eq
import mozilla.components.support.test.mock
import mozilla.components.support.test.robolectric.testContext
import mozilla.components.support.test.whenever
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class FxSuggestSuggestionProviderTest {
    private lateinit var storage: FxSuggestStorage

    @Before
    fun setUp() {
        storage = mock()
        GlobalFxSuggestDependencyProvider.storage = storage
    }

    @After
    fun tearDown() {
        GlobalFxSuggestDependencyProvider.storage = null
    }

    @Test
    fun inputEmpty() = runTest {
        whenever(storage.query(any())).thenReturn(
            listOf(
                Suggestion.Wikipedia(
                    title = "Las Vegas",
                    url = "https://wikipedia.org/wiki/Las_Vegas",
                    icon = null,
                    fullKeyword = "las",
                ),
            ),
        )

        val provider = FxSuggestSuggestionProvider(
            resources = testContext.resources,
            loadUrlUseCase = mock(),
            includeNonSponsoredSuggestions = true,
            includeSponsoredSuggestions = true,
        )

        val suggestions = provider.onInputChanged("")

        verify(storage, never()).query(any())
        assertTrue(suggestions.isEmpty())
    }

    @Test
    fun inputNotEmpty() = runTest {
        whenever(storage.query(any())).thenReturn(
            listOf(
                Suggestion.Amp(
                    title = "Lasagna Come Out Tomorrow",
                    url = "https://www.lasagna.restaurant",
                    icon = listOf(
                        137u, 80u, 78u, 71u, 13u, 10u, 26u, 10u, 0u, 0u, 0u, 13u, 73u, 72u, 68u, 82u,
                        0u, 0u, 0u, 1u, 0u, 0u, 0u, 1u, 1u, 3u, 0u, 0u, 0u, 37u, 219u, 86u, 202u, 0u,
                        0u, 0u, 3u, 80u, 76u, 84u, 69u, 0u, 0u, 0u, 167u, 122u, 61u, 218u, 0u, 0u, 0u,
                        1u, 116u, 82u, 78u, 83u, 0u, 64u, 230u, 216u, 102u, 0u, 0u, 0u, 10u, 73u, 68u,
                        65u, 84u, 8u, 215u, 99u, 96u, 0u, 0u, 0u, 2u, 0u, 1u, 226u, 33u, 188u, 51u, 0u,
                        0u, 0u, 0u, 73u, 69u, 78u, 68u, 174u, 66u, 96u, 130u,
                    ),
                    fullKeyword = "lasagna",
                    blockId = 0,
                    advertiser = "Good Place Eats",
                    iabCategory = "8 - Food & Drink",
                    impressionUrl = "https://example.com/impression_url",
                    clickUrl = "https://example.com/click_url",
                ),
                Suggestion.Wikipedia(
                    title = "Las Vegas",
                    url = "https://wikipedia.org/wiki/Las_Vegas",
                    icon = null,
                    fullKeyword = "las",
                ),
            ),
        )

        val provider = FxSuggestSuggestionProvider(
            resources = testContext.resources,
            loadUrlUseCase = mock(),
            includeNonSponsoredSuggestions = true,
            includeSponsoredSuggestions = true,
        )

        val suggestions = provider.onInputChanged("la")

        verify(storage).query(
            eq(
                SuggestionQuery(
                    keyword = "la",
                    includeSponsored = true,
                    includeNonSponsored = true,
                ),
            ),
        )
        assertEquals(2, suggestions.size)
        assertEquals("lasagna — Lasagna Come Out Tomorrow", suggestions[0].title)
        assertEquals(testContext.resources.getString(R.string.sponsored_suggestion_description), suggestions[0].description)
        assertNotNull(suggestions[0].icon)
        assertEquals("las — Las Vegas", suggestions[1].title)
        assertNull(suggestions[1].description)
        assertNull(suggestions[1].icon)
    }

    @Test
    fun inputCancelled() = runTest {
        doNothing().`when`(storage).cancelReads()

        val provider = FxSuggestSuggestionProvider(
            resources = testContext.resources,
            loadUrlUseCase = mock(),
            includeNonSponsoredSuggestions = true,
            includeSponsoredSuggestions = true,
        )

        provider.onInputCancelled()

        verify(storage).cancelReads()
    }

    @Test
    fun includeNonSponsoredSuggestionsOnly() = runTest {
        whenever(storage.query(any())).thenReturn(
            listOf(
                Suggestion.Wikipedia(
                    title = "Las Vegas",
                    url = "https://wikipedia.org/wiki/Las_Vegas",
                    icon = null,
                    fullKeyword = "las",
                ),
            ),
        )

        val provider = FxSuggestSuggestionProvider(
            resources = testContext.resources,
            loadUrlUseCase = mock(),
            includeNonSponsoredSuggestions = true,
            includeSponsoredSuggestions = false,
        )

        val suggestions = provider.onInputChanged("la")

        verify(storage).query(
            eq(
                SuggestionQuery(
                    keyword = "la",
                    includeSponsored = false,
                    includeNonSponsored = true,
                ),
            ),
        )
        assertEquals(1, suggestions.size)
        assertEquals("las — Las Vegas", suggestions.first().title)
        assertNull(suggestions.first().description)
        assertNull(suggestions.first().icon)
    }

    @Test
    fun includeSponsoredSuggestionsOnly() = runTest {
        whenever(storage.query(any())).thenReturn(
            listOf(
                Suggestion.Amp(
                    title = "Lasagna Come Out Tomorrow",
                    url = "https://www.lasagna.restaurant",
                    icon = null,
                    fullKeyword = "lasagna",
                    blockId = 0,
                    advertiser = "Good Place Eats",
                    iabCategory = "8 - Food & Drink",
                    impressionUrl = "https://example.com/impression_url",
                    clickUrl = "https://example.com/click_url",
                ),
            ),
        )

        val provider = FxSuggestSuggestionProvider(
            resources = testContext.resources,
            loadUrlUseCase = mock(),
            includeNonSponsoredSuggestions = false,
            includeSponsoredSuggestions = true,
        )

        val suggestions = provider.onInputChanged("la")

        verify(storage).query(
            eq(
                SuggestionQuery(
                    keyword = "la",
                    includeSponsored = true,
                    includeNonSponsored = false,
                ),
            ),
        )
        assertEquals(1, suggestions.size)
        assertEquals("lasagna — Lasagna Come Out Tomorrow", suggestions.first().title)
        assertEquals(testContext.resources.getString(R.string.sponsored_suggestion_description), suggestions.first().description)
    }
}
