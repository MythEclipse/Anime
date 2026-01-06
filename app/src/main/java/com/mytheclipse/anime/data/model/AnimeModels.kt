package com.mytheclipse.anime.data.model

import com.google.gson.annotations.SerializedName

// ==================== ANIME RESPONSE ====================

data class AnimeResponse(
    val status: String,
    val data: AnimeData
)

data class AnimeData(
    @SerializedName("ongoing_anime") val ongoingAnime: List<OngoingAnimeItem>,
    @SerializedName("complete_anime") val completeAnime: List<CompleteAnimeItem>
)

data class OngoingAnimeItem(
    val title: String,
    val slug: String,
    val poster: String,
    @SerializedName("current_episode") val currentEpisode: String,
    @SerializedName("anime_url") val animeUrl: String
)

data class CompleteAnimeItem(
    val title: String,
    val slug: String,
    val poster: String,
    @SerializedName("episode_count") val episodeCount: String,
    @SerializedName("anime_url") val animeUrl: String
)

// ==================== ONGOING ANIME RESPONSE ====================

data class OngoingAnimeResponse(
    val status: String,
    val data: List<OngoingAnimeItem>,
    val pagination: Pagination
)

// ==================== SEARCH RESPONSE ====================

data class SearchResponse(
    val status: String,
    val data: List<AnimeItem>,
    val pagination: Pagination
)

data class AnimeItem(
    val title: String,
    val slug: String,
    val poster: String,
    val episode: String,
    @SerializedName("anime_url") val animeUrl: String,
    val genres: List<String>,
    val status: String,
    val rating: String
)

// ==================== DETAIL RESPONSE ====================

data class DetailResponse(
    val status: String? = null,
    val data: AnimeDetailData
)

data class AnimeDetailData(
    val title: String,
    @SerializedName("alternative_title") val alternativeTitle: String,
    val poster: String,
    @SerializedName("release_date") val releaseDate: String,
    val studio: String,
    val synopsis: String,
    val status: String? = null,
    val type: String? = null,
    val genres: List<Genre>? = null,
    val producers: List<String>? = null,
    @SerializedName("episode_lists") val episodeLists: List<EpisodeList>,
    val batch: List<EpisodeList>? = null,
    val recommendations: List<Recommendation>
)

data class Genre(
    val name: String,
    val slug: String,
    @SerializedName("anime_url") val animeUrl: String
)

data class EpisodeList(
    val episode: String,
    val slug: String
)

data class Recommendation(
    val title: String,
    val slug: String,
    val poster: String,
    val status: String? = null,
    val type: String? = null
)

// ==================== FULL RESPONSE ====================

data class FullResponse(
    val status: String,
    val data: AnimeFullData
)

data class AnimeFullData(
    val episode: String,
    @SerializedName("episode_number") val episodeNumber: String,
    val anime: AnimeInfo,
    @SerializedName("has_next_episode") val hasNextEpisode: Boolean,
    @SerializedName("has_previous_episode") val hasPreviousEpisode: Boolean,
    @SerializedName("stream_url") val streamUrl: String,
    @SerializedName("download_urls") val downloadUrls: Map<String, List<DownloadLink>>,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("next_episode") val nextEpisode: EpisodeInfo? = null,
    @SerializedName("previous_episode") val previousEpisode: EpisodeInfo? = null
)

data class AnimeInfo(
    val slug: String
)

data class EpisodeInfo(
    val slug: String
)

data class DownloadLink(
    val server: String,
    val url: String
)

// ==================== PAGINATION ====================

data class Pagination(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("last_visible_page") val lastVisiblePage: Int,
    @SerializedName("has_next_page") val hasNextPage: Boolean,
    @SerializedName("has_previous_page") val hasPreviousPage: Boolean,
    @SerializedName("next_page") val nextPage: Int? = null,
    @SerializedName("previous_page") val previousPage: Int? = null
)
