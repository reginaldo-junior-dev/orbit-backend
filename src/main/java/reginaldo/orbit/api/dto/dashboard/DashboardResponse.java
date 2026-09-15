package reginaldo.orbit.api.dto.dashboard;

public record DashboardResponse(
        TaskSummary tasks,
        ProjectSummary projects,
        GoalSummary goals
) {
    public record TaskSummary(
            long total,
            long todo,
            long inProgress,
            long done
    ) {
    }

    public record ProjectSummary(
            long total,
            long active,
            long completed,
            long archived
    ) {
    }

    public record GoalSummary(
            long total,
            long active,
            long completed,
            long archived
    ) {
    }
}
