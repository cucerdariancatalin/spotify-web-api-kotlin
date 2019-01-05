/* Created by Adam Ratzman (2018) */
package com.adamratzman.spotify.endpoints.client

import com.adamratzman.spotify.endpoints.public.FollowingAPI
import com.adamratzman.spotify.main.SpotifyAPI
import com.adamratzman.spotify.main.SpotifyClientAPI
import com.adamratzman.spotify.main.SpotifyRestAction
import com.adamratzman.spotify.main.SpotifyRestActionPaging
import com.adamratzman.spotify.utils.Artist
import com.adamratzman.spotify.utils.ArtistURI
import com.adamratzman.spotify.utils.CursorBasedPagingObject
import com.adamratzman.spotify.utils.EndpointBuilder
import com.adamratzman.spotify.utils.PlaylistURI
import com.adamratzman.spotify.utils.UserURI
import com.adamratzman.spotify.utils.encode
import com.adamratzman.spotify.utils.toArray
import com.adamratzman.spotify.utils.toCursorBasedPagingObject
import java.util.function.Supplier

/**
 * These endpoints allow you manage the artists, users and playlists that a Spotify user follows.
 */
class ClientFollowingAPI(api: SpotifyAPI) : FollowingAPI(api) {
    /**
     * Check to see if the current user is following another Spotify users.
     *
     * @param user user id or uri to check.
     *
     * @throws BadRequestException if [user] is a non-existing id
     */
    fun isFollowingUser(user: String): SpotifyRestAction<Boolean> {
        return toAction(Supplier {
            isFollowingUsers(user).complete()[0]
        })
    }

    /**
     * Check to see if the logged-in Spotify user is following the specified playlist.
     *
     * @param playlistOwner id or uri of the creator of the playlist
     * @param playlistId playlist id or uri
     *
     * @return booleans representing whether the user follows the playlist. User IDs **not** found will return false
     *
     * @throws [BadRequestException] if the playlist is not found
     */
    fun isFollowingPlaylist(playlistOwner: String, playlistId: String): SpotifyRestAction<Boolean> {
        return toAction(Supplier {
            isFollowingPlaylist(
                playlistOwner,
                playlistId,
                (api as SpotifyClientAPI).userId
            ).complete()
        })
    }

    /**
     * Check to see if the current user is following one or more other Spotify users.
     *
     * @param users List of the user Spotify IDs to check. Max 50
     *
     * @throws BadRequestException if [users] contains a non-existing id
     */
    fun isFollowingUsers(vararg users: String): SpotifyRestAction<List<Boolean>> {
        return toAction(Supplier {
            get(
                EndpointBuilder("/me/following/contains").with("type", "user")
                    .with("ids", users.joinToString(",") { UserURI(it).id.encode() }).toString()
            ).toArray<Boolean>(api)
        })
    }

    /**
     * Check to see if the current user is following a Spotify artist.
     *
     * @param artist artist id to check.
     *
     * @throws BadRequestException if [artist] is a non-existing id
     */
    fun isFollowingArtist(artist: String): SpotifyRestAction<Boolean> {
        return toAction(Supplier {
            isFollowingArtists(artist).complete()[0]
        })
    }

    /**
     * Check to see if the current user is following one or more artists.
     *
     * @param artists List of the artist ids or uris to check. Max 50
     *
     * @throws BadRequestException if [artists] contains a non-existing id
     */
    fun isFollowingArtists(vararg artists: String): SpotifyRestAction<List<Boolean>> {
        return toAction(Supplier {
            get(
                EndpointBuilder("/me/following/contains").with("type", "artist")
                    .with("ids", artists.joinToString(",") { ArtistURI(it).id.encode() }).toString()
            ).toArray<Boolean>(api)
        })
    }

    /**
     * Get the current user’s followed artists.
     *
     * @return [CursorBasedPagingObject] ([Information about them](https://github.com/adamint/spotify-web-api-kotlin/blob/master/README.md#the-benefits-of-linkedresults-pagingobjects-and-cursor-based-paging-objects)
     * with full [Artist] objects
     */
    fun getFollowedArtists(
        limit: Int? = null,
        after: String? = null
    ): SpotifyRestActionPaging<Artist, CursorBasedPagingObject<Artist>> {
        return toActionPaging(Supplier {
            get(
                EndpointBuilder("/me/following").with("type", "artist").with("limit", limit).with(
                    "after",
                    after
                ).toString()
            ).toCursorBasedPagingObject<Artist>("artists", this)
        })
    }

    /**
     * Add the current user as a follower of another user
     *
     * @throws BadRequestException if an invalid id is provided
     */
    fun followUser(user: String): SpotifyRestAction<Unit> {
        return toAction(Supplier {
            followUsers(user).complete()
        })
    }

    /**
     * Add the current user as a follower of other users
     *
     * @throws BadRequestException if an invalid id is provided
     */
    fun followUsers(vararg users: String): SpotifyRestAction<Unit> {
        return toAction(Supplier {
            put(
                EndpointBuilder("/me/following").with("type", "user")
                    .with("ids", users.joinToString(",") { UserURI(it).id.encode() }).toString()
            )
            Unit
        })
    }

    /**
     * Add the current user as a follower of an artist
     *
     * @throws BadRequestException if an invalid id is provided
     */
    fun followArtist(artistId: String): SpotifyRestAction<Unit> {
        return toAction(Supplier {
            followArtists(artistId).complete()
        })
    }

    /**
     * Add the current user as a follower of other artists
     *
     * @throws BadRequestException if an invalid id is provided
     */
    fun followArtists(vararg artists: String): SpotifyRestAction<Unit> {
        return toAction(Supplier {
            put(
                EndpointBuilder("/me/following").with("type", "artist")
                    .with("ids", artists.joinToString(",") { ArtistURI(it).id.encode() }).toString()
            )
            Unit
        })
    }

    /**
     * Add the current user as a follower of a playlist.
     *
     * @param playlist the spotify id or uri of the playlist. Any playlist can be followed, regardless of its
     * public/private status, as long as you know its playlist ID.
     * @param followPublicly Defaults to true. If true the playlist will be included in user’s public playlists,
     * if false it will remain private. To be able to follow playlists privately, the user must have granted the playlist-modify-private scope.
     *
     * @throws BadRequestException if the playlist is not found
     */
    fun followPlaylist(playlist: String, followPublicly: Boolean = true): SpotifyRestAction<Unit> {
        return toAction(Supplier {
            put(
                EndpointBuilder("/playlists/${PlaylistURI(playlist).id}/followers").toString(),
                "{\"public\": $followPublicly}"
            )
            Unit
        })
    }

    /**
     * Remove the current user as a follower of another user
     *
     * @param user The user to be unfollowed from
     *
     * @throws BadRequestException if [user] is not found
     */
    fun unfollowUser(user: String): SpotifyRestAction<Unit> {
        return toAction(Supplier {
            unfollowUsers(user).complete()
        })
    }

    /**
     * Remove the current user as a follower of other users
     *
     * @param users The users to be unfollowed from
     *
     * @throws BadRequestException if an invalid id is provided
     */
    fun unfollowUsers(vararg users: String): SpotifyRestAction<Unit> {
        return toAction(Supplier {
            delete(
                EndpointBuilder("/me/following").with("type", "user")
                    .with("ids", users.joinToString(",") { UserURI(it).id.encode() }).toString()
            )
            Unit
        })
    }

    /**
     * Remove the current user as a follower of an artist
     *
     * @param artist The artist to be unfollowed from
     *
     * @throws BadRequestException if an invalid id is provided
     */
    fun unfollowArtist(artist: String): SpotifyRestAction<Unit> {
        return toAction(Supplier {
            unfollowArtists(artist).complete()
        })
    }

    /**
     * Remove the current user as a follower of artists
     *
     * @param artists The artists to be unfollowed from
     *
     * @throws BadRequestException if an invalid id is provided
     */
    fun unfollowArtists(vararg artists: String): SpotifyRestAction<Unit> {
        return toAction(Supplier {
            delete(
                EndpointBuilder("/me/following").with("type", "artist")
                    .with("ids", artists.joinToString(",") { ArtistURI(it).id.encode() }).toString()
            )
            Unit
        })
    }

    /**
     * Remove the current user as a follower of a playlist.
     *
     * @param playlist the spotify id or uri of the playlist that is to be no longer followed.
     *
     * @throws BadRequestException if the playlist is not found
     */
    fun unfollowPlaylist(playlist: String): SpotifyRestAction<Unit> {
        return toAction(Supplier {
            delete(EndpointBuilder("/playlists/${PlaylistURI(playlist).id}/followers").toString())
            Unit
        })
    }
}