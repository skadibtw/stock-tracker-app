from __future__ import annotations

import math
from pathlib import Path
from xml.sax.saxutils import escape

from reportlab.lib import colors
from reportlab.lib.styles import ParagraphStyle
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.pdfgen import canvas
from reportlab.platypus import Paragraph


ROOT = Path(__file__).resolve().parents[2]
DIAGRAMS_DIR = ROOT / "docs" / "diagrams"


FONT_REGULAR = "TimesNewRoman"
FONT_BOLD = "ArialBold"


def register_fonts() -> None:
    pdfmetrics.registerFont(
        TTFont(FONT_REGULAR, "/System/Library/Fonts/Supplemental/Times New Roman.ttf")
    )
    pdfmetrics.registerFont(
        TTFont(FONT_BOLD, "/System/Library/Fonts/Supplemental/Arial Bold.ttf")
    )


def paragraph_style(
    *,
    font_name: str = FONT_REGULAR,
    font_size: int = 12,
    leading: int | None = None,
    color: colors.Color = colors.HexColor("#1F2937"),
    alignment: int = 1,
) -> ParagraphStyle:
    return ParagraphStyle(
        name=f"{font_name}-{font_size}-{alignment}",
        fontName=font_name,
        fontSize=font_size,
        leading=leading or int(font_size * 1.25),
        textColor=color,
        alignment=alignment,
    )


def draw_paragraph(
    pdf: canvas.Canvas,
    x: float,
    y_top: float,
    width: float,
    text: str,
    *,
    style: ParagraphStyle,
) -> float:
    paragraph = Paragraph(escape(text).replace("\n", "<br/>"), style)
    _, height = paragraph.wrap(width, 1000)
    paragraph.drawOn(pdf, x, y_top - height)
    return height


def draw_group(
    pdf: canvas.Canvas,
    x: float,
    y: float,
    width: float,
    height: float,
    title: str,
) -> None:
    pdf.saveState()
    pdf.setFillColor(colors.HexColor("#F8FAFC"))
    pdf.setStrokeColor(colors.HexColor("#CBD5E1"))
    pdf.setLineWidth(1)
    pdf.roundRect(x, y, width, height, 10, stroke=1, fill=1)
    pdf.setFillColor(colors.HexColor("#334155"))
    pdf.setFont(FONT_BOLD, 12)
    pdf.drawString(x + 14, y + height - 20, title)
    pdf.restoreState()


def draw_box(
    pdf: canvas.Canvas,
    x: float,
    y: float,
    width: float,
    height: float,
    title: str,
    subtitle: str = "",
    *,
    fill: colors.Color = colors.HexColor("#EEF4FF"),
    stroke: colors.Color = colors.HexColor("#5B6B82"),
) -> None:
    pdf.saveState()
    pdf.setFillColor(fill)
    pdf.setStrokeColor(stroke)
    pdf.setLineWidth(1.2)
    pdf.roundRect(x, y, width, height, 8, stroke=1, fill=1)
    pdf.restoreState()

    title_style = paragraph_style(font_name=FONT_BOLD, font_size=12, alignment=1)
    body_style = paragraph_style(font_name=FONT_REGULAR, font_size=10, alignment=1)

    title_height = draw_paragraph(
        pdf,
        x + 10,
        y + height - 10,
        width - 20,
        title,
        style=title_style,
    )

    if subtitle:
        draw_paragraph(
            pdf,
            x + 10,
            y + height - 18 - title_height,
            width - 20,
            subtitle,
            style=body_style,
        )


def draw_label(
    pdf: canvas.Canvas,
    x_center: float,
    y_center: float,
    text: str,
    *,
    width: float = 150,
    font_size: int = 9,
) -> None:
    style = paragraph_style(font_name=FONT_REGULAR, font_size=font_size, alignment=1)
    paragraph = Paragraph(escape(text).replace("\n", "<br/>"), style)
    w, h = paragraph.wrap(width, 1000)
    x = x_center - w / 2
    y = y_center - h / 2
    paragraph.drawOn(pdf, x, y)


def draw_arrow(
    pdf: canvas.Canvas,
    x1: float,
    y1: float,
    x2: float,
    y2: float,
    *,
    label: str | None = None,
    dashed: bool = False,
    color: colors.Color = colors.HexColor("#475569"),
    label_dx: float = 0,
    label_dy: float = 0,
    label_width: float = 170,
) -> None:
    pdf.saveState()
    pdf.setStrokeColor(color)
    pdf.setFillColor(color)
    pdf.setLineWidth(1.4)
    if dashed:
        pdf.setDash(4, 3)
    pdf.line(x1, y1, x2, y2)

    angle = math.atan2(y2 - y1, x2 - x1)
    arrow_length = 10
    arrow_angle = math.pi / 8
    x3 = x2 - arrow_length * math.cos(angle - arrow_angle)
    y3 = y2 - arrow_length * math.sin(angle - arrow_angle)
    x4 = x2 - arrow_length * math.cos(angle + arrow_angle)
    y4 = y2 - arrow_length * math.sin(angle + arrow_angle)
    pdf.line(x2, y2, x3, y3)
    pdf.line(x2, y2, x4, y4)
    pdf.restoreState()

    if label:
        draw_label(
            pdf,
            (x1 + x2) / 2 + label_dx,
            (y1 + y2) / 2 + label_dy,
            label,
            width=label_width,
        )


def draw_lifeline(pdf: canvas.Canvas, x: float, top: float, bottom: float, title: str) -> None:
    draw_box(
        pdf,
        x - 60,
        top - 56,
        120,
        54,
        title,
        fill=colors.HexColor("#F1F5F9"),
        stroke=colors.HexColor("#64748B"),
    )
    pdf.saveState()
    pdf.setStrokeColor(colors.HexColor("#94A3B8"))
    pdf.setDash(4, 3)
    pdf.line(x, top - 58, x, bottom)
    pdf.restoreState()


def draw_note(pdf: canvas.Canvas, x: float, y: float, width: float, height: float, text: str) -> None:
    pdf.saveState()
    pdf.setFillColor(colors.HexColor("#FFF7E6"))
    pdf.setStrokeColor(colors.HexColor("#D4A72C"))
    pdf.roundRect(x, y, width, height, 6, stroke=1, fill=1)
    pdf.restoreState()
    draw_paragraph(
        pdf,
        x + 8,
        y + height - 8,
        width - 16,
        text,
        style=paragraph_style(font_name=FONT_REGULAR, font_size=9, alignment=1),
    )


def render_architecture_diagram_option1(output: Path) -> None:
    pdf = canvas.Canvas(str(output), pagesize=(1100, 760))
    pdf.setTitle("Архитектура системы stock-tracker-app - вариант 1")

    draw_group(pdf, 30, 430, 290, 270, "Клиентский контур")
    draw_group(pdf, 350, 250, 420, 450, "Сервисный контур")
    draw_group(pdf, 350, 30, 420, 180, "Данные и события")
    draw_group(pdf, 800, 360, 270, 340, "Контур наблюдаемости")

    draw_box(pdf, 45, 560, 140, 72, "Пользователь")
    draw_box(pdf, 205, 540, 145, 100, "Android-клиент")

    draw_box(pdf, 390, 560, 150, 82, "mobile-api", "Внешний API Gateway")
    draw_box(pdf, 390, 385, 190, 102, "mobile-backend / portfolio-service", "Основной бизнес-сервис")
    draw_box(pdf, 605, 560, 145, 82, "quotes-service", "Go-сервис котировок")
    draw_box(pdf, 612, 655, 132, 48, "Linux C driver", "Источник котировок")

    draw_box(pdf, 390, 90, 150, 82, "PostgreSQL", "Транзакционное хранилище")
    draw_box(pdf, 575, 210, 170, 82, "Redis / KeyDB", "Кэш и брокер\nсообщений")
    draw_box(pdf, 575, 90, 170, 82, "ClickHouse", "История котировок")

    draw_box(pdf, 835, 500, 200, 108, "OpenTelemetry", "Логи и телеметрия")

    draw_arrow(pdf, 185, 596, 205, 596)
    draw_arrow(pdf, 350, 596, 390, 600, label="HTTP REST + токен", label_dy=18, label_width=120)
    draw_arrow(pdf, 465, 560, 485, 487, label="Проксирование HTTP-запросов", label_dx=38, label_dy=-20, label_width=165)
    draw_arrow(pdf, 485, 385, 465, 172, label="Транзакционные данные пользователей,\nпортфелей и сделок", label_dx=-92, label_width=210)
    draw_arrow(pdf, 530, 385, 610, 292, label="Публикация событий пользователей и сделок", label_dx=85, label_dy=10, label_width=210)
    draw_arrow(pdf, 580, 446, 605, 582, label="Запрос актуальных котировок", label_dx=82, label_dy=22, label_width=175)
    draw_arrow(pdf, 680, 560, 660, 292, label="Актуальные котировки и события", label_dx=100, label_width=170)
    draw_arrow(pdf, 650, 560, 650, 172, label="История котировок", label_dx=82, label_width=140)
    draw_arrow(pdf, 678, 655, 678, 642, label="Источник рыночных данных", label_dx=120, label_width=170)

    telemetry_color = colors.HexColor("#64748B")
    draw_arrow(pdf, 540, 610, 835, 555, dashed=True, color=telemetry_color)
    draw_arrow(pdf, 580, 455, 835, 555, label="Логи и телеметрия", dashed=True, color=telemetry_color, label_dx=40, label_dy=-28, label_width=140)
    draw_arrow(pdf, 750, 600, 835, 555, dashed=True, color=telemetry_color)

    pdf.save()


def render_architecture_diagram_option2(output: Path) -> None:
    pdf = canvas.Canvas(str(output), pagesize=(1100, 760))
    pdf.setTitle("Архитектура системы stock-tracker-app - вариант 2")

    draw_group(pdf, 30, 420, 255, 280, "Клиентский контур")
    draw_group(pdf, 315, 240, 470, 460, "Сервисный контур")
    draw_group(pdf, 315, 30, 470, 180, "Данные и события")
    draw_group(pdf, 820, 430, 250, 270, "Контур наблюдаемости")

    draw_box(pdf, 45, 560, 125, 72, "Пользователь")
    draw_box(pdf, 190, 535, 125, 98, "Android-клиент")

    draw_box(pdf, 350, 560, 150, 82, "mobile-api", "Внешний API Gateway")
    draw_box(pdf, 345, 390, 210, 108, "mobile-backend / portfolio-service", "Основной бизнес-сервис")
    draw_box(pdf, 610, 560, 145, 82, "quotes-service", "Go-сервис котировок")
    draw_box(pdf, 616, 655, 132, 48, "Linux C driver", "Источник котировок")

    draw_box(pdf, 360, 90, 150, 82, "PostgreSQL", "Транзакционное хранилище")
    draw_box(pdf, 600, 230, 155, 82, "Redis / KeyDB", "Кэш и брокер\nсообщений")
    draw_box(pdf, 600, 90, 155, 82, "ClickHouse", "История котировок")

    draw_box(pdf, 850, 545, 180, 96, "OpenTelemetry", "Логи и телеметрия")

    draw_arrow(pdf, 170, 596, 190, 596)
    draw_arrow(pdf, 315, 592, 350, 600, label="HTTP REST + токен", label_dy=18, label_width=120)
    draw_arrow(pdf, 425, 560, 448, 498, label="Проксирование запросов", label_dx=18, label_dy=-22, label_width=130)
    draw_arrow(pdf, 455, 390, 435, 172, label="Транзакционные данные пользователей,\nпортфелей и сделок", label_dx=-84, label_width=205)
    draw_arrow(pdf, 520, 390, 620, 312, label="Публикация событий пользователей и сделок", label_dx=78, label_dy=10, label_width=205)
    draw_arrow(pdf, 555, 445, 610, 582, label="Запрос актуальных котировок", label_dx=75, label_dy=22, label_width=170)
    draw_arrow(pdf, 682, 560, 678, 312, label="Актуальные котировки и события", label_dx=95, label_width=170)
    draw_arrow(pdf, 678, 560, 678, 172, label="История котировок", label_dx=82, label_width=135)
    draw_arrow(pdf, 682, 655, 682, 642, label="Источник рыночных данных", label_dx=118, label_width=168)

    telemetry_color = colors.HexColor("#64748B")
    draw_arrow(pdf, 500, 600, 850, 592, dashed=True, color=telemetry_color)
    draw_arrow(pdf, 555, 480, 850, 592, dashed=True, color=telemetry_color)
    draw_arrow(pdf, 755, 600, 850, 592, dashed=True, color=telemetry_color)
    draw_label(pdf, 800, 635, "Логи и телеметрия", width=140, font_size=10)

    pdf.save()


def render_architecture_diagram(output: Path) -> None:
    render_architecture_diagram_option1(output)


def draw_message(
    pdf: canvas.Canvas,
    x1: float,
    x2: float,
    y: float,
    text: str,
    *,
    dashed: bool = False,
    color: colors.Color = colors.HexColor("#334155"),
    label_width: float = 150,
    label_dy: float = 14,
) -> None:
    draw_arrow(
        pdf,
        x1,
        y,
        x2,
        y,
        label=text,
        dashed=dashed,
        color=color,
        label_dy=label_dy,
        label_width=label_width,
    )


def render_sequence_diagram(output: Path) -> None:
    pdf = canvas.Canvas(str(output), pagesize=(1050, 1600))
    pdf.setTitle("Последовательность основных пользовательских сценариев")

    lifelines = {
        "app": 90,
        "gateway": 260,
        "backend": 430,
        "db": 600,
        "quotes": 770,
        "redis": 930,
    }
    top = 1540
    bottom = 80

    sections = [
        (1240, 1520, "Вход пользователя через POST /auth/login"),
        (910, 1210, "Запрос котировки через GET /market/quotes/{symbol}"),
        (430, 860, "Покупка акции через POST /portfolio/stocks/buy"),
        (70, 350, "Получение статистики через GET /portfolio/statistics"),
    ]
    for y_bottom, y_top, title in sections:
        pdf.saveState()
        pdf.setFillColor(colors.HexColor("#F8FAFC"))
        pdf.setStrokeColor(colors.HexColor("#CBD5E1"))
        pdf.roundRect(35, y_bottom, 980, y_top - y_bottom, 8, stroke=1, fill=1)
        pdf.restoreState()
        pdf.setFont(FONT_BOLD, 11)
        pdf.setFillColor(colors.HexColor("#334155"))
        pdf.drawString(50, y_top - 18, title)

    for title, x in [
        ("Мобильное\nприложение", lifelines["app"]),
        ("API-шлюз\nmobile-api", lifelines["gateway"]),
        ("Сервис портфеля\nmobile-backend", lifelines["backend"]),
        ("PostgreSQL", lifelines["db"]),
        ("Сервис котировок\nquotes-service", lifelines["quotes"]),
        ("Redis", lifelines["redis"]),
    ]:
        draw_lifeline(pdf, x, top, bottom, title)

    draw_message(pdf, lifelines["app"], lifelines["gateway"], 1450, "POST /auth/login\nлогин, пароль", label_width=140)
    draw_message(pdf, lifelines["gateway"], lifelines["backend"], 1410, "Проксирование запроса", label_width=120)
    draw_message(pdf, lifelines["backend"], lifelines["db"], 1370, "Проверка пользователя\nи загрузка портфеля", label_width=150)
    draw_message(pdf, lifelines["db"], lifelines["backend"], 1330, "Данные пользователя", dashed=True, label_width=110)
    draw_message(pdf, lifelines["backend"], lifelines["gateway"], 1290, "200 OK + токен доступа", dashed=True, label_width=145)
    draw_message(pdf, lifelines["gateway"], lifelines["app"], 1250, "200 OK + токен доступа", dashed=True, label_width=145)

    draw_message(pdf, lifelines["app"], lifelines["gateway"], 1140, "GET /market/quotes/{symbol}", label_width=150)
    draw_message(pdf, lifelines["gateway"], lifelines["backend"], 1100, "Проксирование запроса", label_width=120)
    draw_message(pdf, lifelines["backend"], lifelines["quotes"], 1060, "Получение актуальной котировки", label_width=150)
    draw_message(pdf, lifelines["quotes"], lifelines["backend"], 1020, "Цена, валюта,\nвремя получения, источник", dashed=True, label_width=150)
    draw_message(pdf, lifelines["backend"], lifelines["gateway"], 980, "200 OK + котировка", dashed=True, label_width=110)
    draw_message(pdf, lifelines["gateway"], lifelines["app"], 940, "200 OK + котировка", dashed=True, label_width=110)

    draw_message(pdf, lifelines["app"], lifelines["gateway"], 790, "POST /portfolio/stocks/buy\nтокен доступа + параметры сделки", label_width=180)
    draw_message(pdf, lifelines["gateway"], lifelines["backend"], 754, "Проксирование запроса", label_width=120)
    draw_note(pdf, 468, 700, 120, 36, "Проверка токена\nдоступа")
    draw_message(pdf, lifelines["backend"], lifelines["quotes"], 682, "Запрос актуальной цены акции", label_width=150)
    draw_message(pdf, lifelines["quotes"], lifelines["backend"], 646, "Актуальная цена", dashed=True, label_width=100)
    draw_message(pdf, lifelines["backend"], lifelines["db"], 610, "Чтение портфеля и\nденежного баланса", label_width=140)
    draw_message(pdf, lifelines["db"], lifelines["backend"], 574, "Текущее состояние портфеля", dashed=True, label_width=150)
    draw_message(pdf, lifelines["backend"], lifelines["db"], 538, "Списание средств,\nдобавление lot,\nзапись trade history", label_width=145)
    draw_message(pdf, lifelines["db"], lifelines["backend"], 502, "Транзакция сохранена", dashed=True, label_width=120)
    draw_message(pdf, lifelines["backend"], lifelines["redis"], 466, "Публикация trade.executed", label_width=140)
    draw_message(pdf, lifelines["redis"], lifelines["backend"], 430, "ACK", dashed=True, label_width=60)
    draw_message(pdf, lifelines["backend"], lifelines["gateway"], 394, "201 Created + данные сделки", dashed=True, label_width=145)
    draw_message(pdf, lifelines["gateway"], lifelines["app"], 358, "201 Created + данные сделки", dashed=True, label_width=145)

    draw_message(pdf, lifelines["app"], lifelines["gateway"], 260, "GET /portfolio/statistics\nтокен доступа", label_width=145)
    draw_message(pdf, lifelines["gateway"], lifelines["backend"], 220, "Проксирование запроса", label_width=120)
    draw_note(pdf, 468, 176, 120, 36, "Проверка токена\nдоступа")
    draw_message(pdf, lifelines["backend"], lifelines["db"], 150, "Загрузка агрегированной\nстатистики", label_width=130)
    draw_message(pdf, lifelines["db"], lifelines["backend"], 110, "Агрегированная статистика\nи денежный баланс", dashed=True, label_width=150)
    draw_message(pdf, lifelines["backend"], lifelines["gateway"], 80, "200 OK + статистика", dashed=True, label_width=110)
    draw_message(pdf, lifelines["gateway"], lifelines["app"], 50, "200 OK + статистика", dashed=True, label_width=110)

    pdf.save()


def main() -> None:
    register_fonts()
    DIAGRAMS_DIR.mkdir(parents=True, exist_ok=True)
    render_architecture_diagram(DIAGRAMS_DIR / "system-architecture.pdf")
    render_architecture_diagram_option1(DIAGRAMS_DIR / "system-architecture-option1.pdf")
    render_architecture_diagram_option2(DIAGRAMS_DIR / "system-architecture-option2.pdf")
    render_sequence_diagram(DIAGRAMS_DIR / "mobile-flow-sequence.pdf")


if __name__ == "__main__":
    main()
