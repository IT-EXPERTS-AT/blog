"""Structured output schema for the BI report.

The agent returns a BIReport (not free text), which makes the PDF and the
Prefect artifacts cleanly formatted.
"""

from pydantic import BaseModel, Field


class Metric(BaseModel):
    name: str = Field(description="KPI name, e.g. 'APAC revenue (2026-06-28)'")
    value: str = Field(description="Formatted value, e.g. '€9,800' or '-55%'")
    note: str = Field(default="", description="Optional short context")


class BIReport(BaseModel):
    title: str = Field(description="Report title")
    period: str = Field(description="Period covered, e.g. a date or date range")
    executive_summary: str = Field(description="2-4 sentence narrative summary")
    key_metrics: list[Metric] = Field(description="Headline KPIs for the period")
    insights: list[str] = Field(description="Notable changes, anomalies, trends")
    recommendations: list[str] = Field(description="A few actionable next steps")
