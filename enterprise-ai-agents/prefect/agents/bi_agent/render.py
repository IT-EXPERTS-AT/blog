"""Render a BIReport to a clean PDF (reportlab) and to Markdown (Prefect UI).

Each run writes a uniquely named file — runs never overwrite each other.
"""

from datetime import datetime

from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import cm
from reportlab.platypus import (
    ListFlowable,
    ListItem,
    Paragraph,
    SimpleDocTemplate,
    Spacer,
    Table,
    TableStyle,
)

from agents.bi_agent.config import OUTPUT_DIR
from agents.bi_agent.schemas import BIReport


def render_report_as_pdf(report: BIReport) -> str:
    """Write the report to output/bi-summary_<timestamp>.pdf and return its path."""
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    path = OUTPUT_DIR / f"bi-summary_{datetime.now():%Y%m%d_%H%M%S}.pdf"

    styles = getSampleStyleSheet()
    cell = ParagraphStyle("Cell", parent=styles["BodyText"], fontSize=9, leading=12)
    header = ParagraphStyle(
        "Header", parent=cell, textColor=colors.white, fontName="Helvetica-Bold"
    )

    story = [
        Paragraph(report.title, styles["Title"]),
        Paragraph(f"Period: {report.period}", styles["Italic"]),
        Paragraph(f"Generated: {datetime.now():%Y-%m-%d %H:%M}", styles["Italic"]),
        Spacer(1, 0.6 * cm),
        Paragraph("Executive Summary", styles["Heading2"]),
        Paragraph(report.executive_summary, styles["BodyText"]),
        Spacer(1, 0.4 * cm),
        Paragraph("Key Metrics", styles["Heading2"]),
        _metrics_table(report, cell, header),
        Spacer(1, 0.5 * cm),
        Paragraph("Insights", styles["Heading2"]),
        _bullets(report.insights, styles),
        Spacer(1, 0.4 * cm),
        Paragraph("Recommendations", styles["Heading2"]),
        _bullets(report.recommendations, styles),
    ]

    doc = SimpleDocTemplate(
        str(path),
        pagesize=A4,
        title=report.title,
        leftMargin=2 * cm,
        rightMargin=2 * cm,
        topMargin=2 * cm,
        bottomMargin=2 * cm,
    )
    doc.build(story)
    return str(path)


def _metrics_table(report: BIReport, cell, header) -> Table:
    rows = [[Paragraph(h, header) for h in ("Metric", "Value", "Note")]]
    rows += [
        [Paragraph(m.name, cell), Paragraph(m.value, cell), Paragraph(m.note, cell)]
        for m in report.key_metrics
    ]
    table = Table(rows, colWidths=[6 * cm, 3.5 * cm, 7.5 * cm], repeatRows=1)
    table.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#2c3e50")),
                ("GRID", (0, 0), (-1, -1), 0.5, colors.grey),
                ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, colors.HexColor("#f2f4f6")]),
                ("VALIGN", (0, 0), (-1, -1), "TOP"),
                ("LEFTPADDING", (0, 0), (-1, -1), 6),
                ("RIGHTPADDING", (0, 0), (-1, -1), 6),
            ]
        )
    )
    return table


def _bullets(items: list[str], styles) -> ListFlowable:
    return ListFlowable(
        [ListItem(Paragraph(text, styles["BodyText"])) for text in items],
        bulletType="bullet",
    )


def render_report_as_markdown(report: BIReport, pdf_path: str) -> str:
    """Render the report as Markdown for the Prefect flow-run artifact."""
    lines = [
        f"# {report.title}",
        f"_Period: {report.period}_",
        "",
        "## Executive Summary",
        report.executive_summary,
        "",
        "## Key Metrics",
        "| Metric | Value | Note |",
        "| --- | --- | --- |",
        *[f"| {m.name} | {m.value} | {m.note} |" for m in report.key_metrics],
        "",
        "## Insights",
        *[f"- {item}" for item in report.insights],
        "",
        "## Recommendations",
        *[f"- {item}" for item in report.recommendations],
        "",
        f"_PDF: `{pdf_path}`_",
    ]
    return "\n".join(lines)
