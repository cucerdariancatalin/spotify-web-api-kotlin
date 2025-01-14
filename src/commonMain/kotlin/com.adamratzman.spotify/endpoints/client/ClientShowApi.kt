/* Spotify Web API, Kotlin Wrapper; MIT License, 2017-2021; Original author: Adam Ratzman */
package com.adamratzman.spotify.endpoints.client

import com.adamratzman.spotify.GenericSpotifyApi
import com.adamratzman.spotify.SpotifyException.BadRequestException
import com.adamratzman.spotify.SpotifyRestAction
import com.adamratzman.spotify.SpotifyScope
import com.adamratzman.spotify.endpoints.pub.ShowApi
import com.adamratzman.spotify.models.PagingObject
import com.adamratzman.spotify.models.Show
import com.adamratzman.spotify.models.ShowList
import com.adamratzman.spotify.models.ShowUri
import com.adamratzman.spotify.models.SimpleEpisode
import com.adamratzman.spotify.models.SimpleShow
import com.adamratzman.spotify.models.serialization.toNonNullablePagingObject
import com.adamratzman.spotify.models.serialization.toObject
import com.adamratzman.spotify.utils.Market
import com.adamratzman.spotify.utils.catch
import com.adamratzman.spotify.utils.encodeUrl

/**
 * Endpoints for retrieving information about one or more shows and their episodes from the Spotify catalog.
 *
 * **[Api Reference](https://developer.spotify.com/documentation/web-api/reference/shows/)**
 */
public class ClientShowApi(api: GenericSpotifyApi) : ShowApi(api) {
    /**
     * Get Spotify catalog information for a single show identified by its unique Spotify ID. The [Market] associated with
     * the user account will be used.
     *
     * **Reading the user’s resume points on episode objects requires the [SpotifyScope.USER_READ_PLAYBACK_POSITION] scope**
     *
     * **[Api Reference](https://developer.spotify.com/documentation/web-api/reference/tracks/get-track/)**
     *
     * @param id The Spotify ID for the show.
     *
     * @return possibly-null Show. This behavior is *not the same* as in [getShows]
     */
    public suspend fun getShow(id: String): Show? {
        return catch {
            get(
                endpointBuilder("/shows/${ShowUri(id).id.encodeUrl()}").toString()
            ).toObject(Show.serializer(), api, json)
        }
    }

    /**
     * Get Spotify catalog information for a single show identified by its unique Spotify ID. The [Market] associated with
     * the user account will be used.
     *
     * **Reading the user’s resume points on episode objects requires the [SpotifyScope.USER_READ_PLAYBACK_POSITION] scope**
     *
     * **[Api Reference](https://developer.spotify.com/documentation/web-api/reference/tracks/get-track/)**
     *
     * @param id The Spotify ID for the show.
     *
     * @return possibly-null Show. This behavior is *not the same* as in [getShows]
     */
    public fun getShowRestAction(id: String): SpotifyRestAction<Show?> = SpotifyRestAction { getShow(id) }

    /**
     * Get Spotify catalog information for multiple shows based on their Spotify IDs. The [Market] associated with
     * the user account will be used.
     *
     * **Invalid show ids will result in a [BadRequestException]
     *
     * **Reading the user’s resume points on episode objects requires the [SpotifyScope.USER_READ_PLAYBACK_POSITION] scope**
     *
     * **[Api Reference](https://developer.spotify.com/documentation/web-api/reference/shows/get-several-shows/)**
     *
     * @param ids The id or uri for the shows. Maximum **50**.
     *
     * @return List of possibly-null [SimpleShow] objects.
     * @throws BadRequestException If any invalid show id is provided
     */
    public suspend fun getShows(vararg ids: String): List<SimpleShow?> {
        checkBulkRequesting(50, ids.size)
        return bulkStatelessRequest(50, ids.toList()) { chunk ->
            get(
                endpointBuilder("/shows")
                    .with("ids", chunk.joinToString(",") { ShowUri(it).id.encodeUrl() })
                    .toString()
            ).toObject(ShowList.serializer(), api, json).shows
        }.flatten()
    }

    /**
     * Get Spotify catalog information for multiple shows based on their Spotify IDs. The [Market] associated with
     * the user account will be used.
     *
     * **Invalid show ids will result in a [BadRequestException]
     *
     * **Reading the user’s resume points on episode objects requires the [SpotifyScope.USER_READ_PLAYBACK_POSITION] scope**
     *
     * **[Api Reference](https://developer.spotify.com/documentation/web-api/reference/shows/get-several-shows/)**
     *
     * @param ids The id or uri for the shows. Maximum **50**.
     *
     * @return List of possibly-null [SimpleShow] objects.
     * @throws BadRequestException If any invalid show id is provided
     */
    public fun getShowsRestAction(vararg ids: String): SpotifyRestAction<List<SimpleShow?>> =
        SpotifyRestAction { getShows(*ids) }

    /**
     * Get Spotify catalog information about an show’s episodes. The [Market] associated with
     * the user account will be used.
     *
     * **Reading the user’s resume points on episode objects requires the [SpotifyScope.USER_READ_PLAYBACK_POSITION] scope**
     *
     * **[Api Reference](https://developer.spotify.com/documentation/web-api/reference/shows/get-shows-episodes/)**
     *
     * @param id The Spotify ID for the show.
     * @param limit The number of objects to return. Default: 20 (or api limit). Minimum: 1. Maximum: 50.
     * @param offset The index of the first item to return. Default: 0. Use with limit to get the next set of items
     *
     * @throws BadRequestException if the playlist cannot be found
     */
    public suspend fun getShowEpisodes(
        id: String,
        limit: Int? = null,
        offset: Int? = null
    ): PagingObject<SimpleEpisode> = get(
        endpointBuilder("/shows/${ShowUri(id).id.encodeUrl()}/episodes").with("limit", limit)
            .with("offset", offset).toString()
    ).toNonNullablePagingObject(SimpleEpisode.serializer(), null, api, json)

    /**
     * Get Spotify catalog information about an show’s episodes. The [Market] associated with
     * the user account will be used.
     *
     * **Reading the user’s resume points on episode objects requires the [SpotifyScope.USER_READ_PLAYBACK_POSITION] scope**
     *
     * **[Api Reference](https://developer.spotify.com/documentation/web-api/reference/shows/get-shows-episodes/)**
     *
     * @param id The Spotify ID for the show.
     * @param limit The number of objects to return. Default: 20 (or api limit). Minimum: 1. Maximum: 50.
     * @param offset The index of the first item to return. Default: 0. Use with limit to get the next set of items
     *
     * @throws BadRequestException if the playlist cannot be found
     */
    public fun getShowEpisodesRestAction(
        id: String,
        limit: Int? = null,
        offset: Int? = null
    ): SpotifyRestAction<PagingObject<SimpleEpisode>> = SpotifyRestAction { getShowEpisodes(id, limit, offset) }
}
