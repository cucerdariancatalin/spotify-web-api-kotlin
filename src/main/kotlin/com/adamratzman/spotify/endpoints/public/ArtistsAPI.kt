/* Created by Adam Ratzman (2018) */
package com.adamratzman.spotify.endpoints.public

import com.adamratzman.spotify.main.SpotifyAPI
import com.adamratzman.spotify.main.SpotifyRestAction
import com.adamratzman.spotify.main.SpotifyRestActionPaging
import com.adamratzman.spotify.utils.Artist
import com.adamratzman.spotify.utils.ArtistList
import com.adamratzman.spotify.utils.ArtistURI
import com.adamratzman.spotify.utils.BadRequestException
import com.adamratzman.spotify.utils.CursorBasedPagingObject
import com.adamratzman.spotify.utils.EndpointBuilder
import com.adamratzman.spotify.utils.Market
import com.adamratzman.spotify.utils.PagingObject
import com.adamratzman.spotify.utils.SimpleAlbum
import com.adamratzman.spotify.utils.SpotifyEndpoint
import com.adamratzman.spotify.utils.Track
import com.adamratzman.spotify.utils.catch
import com.adamratzman.spotify.utils.encode
import com.adamratzman.spotify.utils.toInnerArray
import com.adamratzman.spotify.utils.toObject
import com.adamratzman.spotify.utils.toPagingObject
import java.util.function.Supplier

/**
 * Endpoints for retrieving information about one or more artists from the Spotify catalog.
 */
class ArtistsAPI(api: SpotifyAPI) : SpotifyEndpoint(api) {
    /**
     * Get Spotify catalog information for a single artist identified by their unique Spotify ID.
     * @param artist the spotify id or uri for the artist.
     *
     * @return [Artist] if valid artist id is provided, otherwise null
     */
    fun getArtist(artist: String): SpotifyRestAction<Artist?> {
        return toAction(Supplier {
            catch {
                get(EndpointBuilder("/artists/${ArtistURI(artist).id.encode()}").toString())
                    .toObject<Artist>(api)
            }
        })
    }

    /**
     * Get Spotify catalog information for several artists based on their Spotify IDs. **Artists not found are returned as null inside the ordered list**
     * @param artists the spotify ids or uris representing the artists.
     */
    fun getArtists(vararg artists: String): SpotifyRestAction<List<Artist?>> {
        return toAction(Supplier {
            get(
                EndpointBuilder("/artists").with(
                    "ids",
                    artists.joinToString(",") { ArtistURI(it).id.encode() }).toString()
            ).toObject<ArtistList>(api).artists
        })
    }

    /**
     * Get Spotify catalog information about an artist’s albums.
     * @param artist artist id or uri
     * @param market Supply this parameter to limit the response to one particular geographical market.
     * @param limit The number of objects to return. Default: 20. Minimum: 1. Maximum: 50.
     * @param offset The index of the first item to return. Default: 0. Use with limit to get the next set of items
     * @param include List of keywords that will be used to filter the response. If not supplied, all album groups will be returned.
     *
     * @throws BadRequestException if [artist] is not found, or filter parameters are illegal
     */
    fun getArtistAlbums(
        artist: String,
        limit: Int? = null,
        offset: Int? = null,
        market: Market? = null,
        vararg include: AlbumInclusionStrategy
    ): SpotifyRestActionPaging<SimpleAlbum, PagingObject<SimpleAlbum>> {
        return toActionPaging(Supplier {
            get(
                EndpointBuilder("/artists/${ArtistURI(artist).id.encode()}/albums").with("limit", limit).with(
                    "offset",
                    offset
                ).with("market", market?.code)
                    .with("include_groups", include.joinToString(",") { it.keyword }).toString()
            ).toPagingObject<SimpleAlbum>(null, this)
        })
    }

    /**
     * Describes object types to include when finding albums
     *
     * @param keyword the spotify id of the strategy
     */
    enum class AlbumInclusionStrategy(val keyword: String) {
        ALBUM("album"), SINGLE("single"), APPEARS_ON("appears_on"), COMPILATION("compilation")
    }

    /**
     * Get Spotify catalog information about an artist’s top tracks **by country**. Contains only up to **10** tracks with no
     * [CursorBasedPagingObject] to go between top track pages. Unfortunately, this isn't likely to change soon
     *
     * @param artist the spotify id or uri for the artist.
     * @param market The country ([Market]) to search. Unlike endpoints with optional Track Relinking, the Market is **not** optional.
     *
     * @throws BadRequestException if tracks are not available in the specified [Market] or the [artist] is not found
     */
    fun getArtistTopTracks(artist: String, market: Market = Market.US): SpotifyRestAction<List<Track>> {
        return toAction(Supplier {
            get(
                EndpointBuilder("/artists/${ArtistURI(artist).id.encode()}/top-tracks").with(
                    "country",
                    market.code
                ).toString()
            ).toInnerArray<Track>("tracks", api)
        })
    }

    /**
     * Get Spotify catalog information about artists similar to a given artist.
     * Similarity is based on analysis of the Spotify community’s listening history.
     *
     * @param artist the spotify id or uri for the artist.
     *
     * @return List of *never-null*, but possibly empty Artist objects representing similar artists
     * @throws BadRequestException if the [artist] is not found
     */
    fun getRelatedArtists(artist: String): SpotifyRestAction<List<Artist>> {
        return toAction(Supplier {
            get(EndpointBuilder("/artists/${ArtistURI(artist).id.encode()}/related-artists").toString())
                .toObject<ArtistList>(api).artists.filterNotNull()
        })
    }
}