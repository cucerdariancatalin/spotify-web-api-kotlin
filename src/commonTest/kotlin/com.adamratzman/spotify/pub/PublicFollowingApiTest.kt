/* Spotify Web API, Kotlin Wrapper; MIT License, 2017-2021; Original author: Adam Ratzman */
package com.adamratzman.spotify.pub

import com.adamratzman.spotify.AbstractTest
import com.adamratzman.spotify.GenericSpotifyApi
import com.adamratzman.spotify.SpotifyException
import com.adamratzman.spotify.runBlockingTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PublicFollowingApiTest : AbstractTest<GenericSpotifyApi>() {
    @Test
    fun testUsersFollowingPlaylist() {
        return runBlockingTest {
            super.build<GenericSpotifyApi>()
            if (!testPrereq()) return@runBlockingTest else api!!

            assertFailsWith<SpotifyException.BadRequestException> {
                api!!.following.areFollowingPlaylist(
                    "37i9dQZF1DXcBWIGoYBM5M",
                    "udontexist89"
                )[0]
            }
            assertFailsWith<SpotifyException.BadRequestException> {
                api!!.following.areFollowingPlaylist("37i9dQZF1DXcBWIGoYBM5M")
            }
            assertFailsWith<SpotifyException.BadRequestException> {
                api!!.following.areFollowingPlaylist("asdkfjajksdfjkasdf", "adamratzman1")
            }
            assertEquals(
                listOf(true, false),
                api!!.following.areFollowingPlaylist("37i9dQZF1DXcBWIGoYBM5M", "adamratzman1", "adamratzman")
            )
            assertFailsWith<SpotifyException.BadRequestException> {
                api!!.following.areFollowingPlaylist("37i9dQZF1DXcBWIGoYBM5M", "udontexist89", "adamratzman1")
            }
        }
    }
}
