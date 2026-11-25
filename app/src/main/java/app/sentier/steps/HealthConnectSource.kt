/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.steps

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.LocalDate
import java.time.Period

/** Lecture des pas dans Health Connect (Android 14+ intégré, sinon appli à installer). */
class HealthConnectSource(private val context: Context) {

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

    companion object {
        val PERMISSIONS: Set<String> = setOf(HealthPermission.getReadPermission(StepsRecord::class))
        const val PROVIDER_PACKAGE = "com.google.android.apps.healthdata"
    }
}
