"""Interest estimate shown in reminders. Day count 30/360: every month has 30 days, the year 360."""


def interest_so_far(principal: float, annual_rate_percent: float, days: int) -> float:
    return round(principal * annual_rate_percent / 100 * days / 360, 2)
