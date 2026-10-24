package com.civicpulse.service;

import com.civicpulse.dto.response.*;

import java.util.List;

public interface AnalyticsService {
    AnalyticsSummaryResponse getSummary(Long departmentId, String range);
    List<TrendDataPoint> getTrend(String range, Long departmentId);
    List<CategoryDistributionResponse> getByCategory(Long departmentId);
    List<DepartmentPerformanceResponse> getByDepartment();
    List<LeaderboardItemResponse> getLeaderboard();
    List<HeatmapPointResponse> getHeatmap(Long departmentId);
    List<HotspotPredictionResponse> getPredictions();
    byte[] exportData(String format, Long departmentId);
    PublicStatsResponse getPublicStats();
}
