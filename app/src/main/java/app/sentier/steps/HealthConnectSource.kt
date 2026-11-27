/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.steps

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import app.sentier.domain.HealthSession
import java.time.Instant
import java.time.LocalDate
import java.time.Period

/** Lecture du sommeil et du sport (voir HealthSync), remplaçable dans les tests. */
interface HealthReader {
    suspend fun hasHealthPermission(): Boolean
    suspend fun readSleep(from: Instant, to: Instant): List<HealthSession>
    suspend fun readExercise(from: Instant, to: Instant): List<HealthSession>
}

/** Lecture des pas, du sommeil et du sport dans Health Connect (Android 14+ intégré, sinon appli à installer). */
class HealthConnectSource(private val context: Context) : HealthReader {

    enum class Status { AVAILABLE, NEEDS_UPDATE, UNAVAILABLE }

    fun status(): Status = when (runCatching { HealthConnectClient.getSdkStatus(context) }.getOrDefault(HealthConnectClient.SDK_UNAVAILABLE)) {
        HealthConnectClient.SDK_AVAILABLE -> Status.AVAILABLE
        HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> Status.NEEDS_UPDATE
        else -> Status.UNAVAILABLE
    }

    private val client: HealthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    suspend fun hasPermission(): Boolean = status() == Status.AVAILABLE &&
        runCatching { client.permissionController.getGrantedPermissions().containsAll(PERMISSIONS) }.getOrDefault(false)

    /** Pas par jour, du jour [from] au jour [to] inclus. */
    suspend fun readDailySteps(from: LocalDate, to: LocalDate): Map<LocalDate, Long> {
        val groups = client.aggregateGroupByPeriod(
            AggregateGroupByPeriodRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(from.atStartOfDay(), to.plusDays(1).atStartOfDay()),
                timeRangeSlicer = Period.ofDays(1),
            )
        )
        return groups.associate { g -> g.startTime.toLocalDate() to (g.result[StepsRecord.COUNT_TOTAL] ?: 0L) }
    }

    override suspend fun hasHealthPermission(): Boolean = status() == Status.AVAILABLE &&
        runCatching { client.permissionController.getGrantedPermissions().containsAll(HEALTH_PERMISSIONS) }.getOrDefault(false)

    /** Nuits de sommeil qui finissent entre [from] et [to], sans les moments éveillés notés par la montre. */
    override suspend fun readSleep(from: Instant, to: Instant): List<HealthSession> =
        client.readRecords(ReadRecordsRequest(SleepSessionRecord::class, timeRangeFilter = TimeRangeFilter.between(from, to)))
            .records.map { r ->
                val awake = r.stages
                    .filter { it.stage == SleepSessionRecord.STAGE_TYPE_AWAKE || it.stage == SleepSessionRecord.STAGE_TYPE_OUT_OF_BED }
                    .sumOf { (it.endTime.epochSecond - it.startTime.epochSecond) / 60 }
                HealthSession(r.startTime, r.endTime, awake)
            }

    /** Séances de sport entre [from] et [to]. */
    override suspend fun readExercise(from: Instant, to: Instant): List<HealthSession> =
        client.readRecords(ReadRecordsRequest(ExerciseSessionRecord::class, timeRangeFilter = TimeRangeFilter.between(from, to)))
            .records.map { HealthSession(it.startTime, it.endTime) }

    companion object {
        val PERMISSIONS: Set<String> = setOf(HealthPermission.getReadPermission(StepsRecord::class))

        /** Sommeil et séances de sport, pour cocher les habitudes toutes seules. */
        val HEALTH_PERMISSIONS: Set<String> = setOf(
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        )
        const val PROVIDER_PACKAGE = "com.google.android.apps.healthdata"
    }
}
