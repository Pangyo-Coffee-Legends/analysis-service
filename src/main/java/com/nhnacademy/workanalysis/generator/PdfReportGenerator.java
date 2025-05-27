package com.nhnacademy.workanalysis.generator;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;

import com.nhnacademy.workanalysis.dto.report.AttendanceReportDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Map;

@Slf4j
@Component
public class PdfReportGenerator {

    /**
     * 근태 리포트를 PDF로 생성합니다.
     *
     * @param reportDto 통계 + 마크다운 요약이 포함된 DTO
     * @param mbNo 사원 번호
     * @param year 연도
     * @param month 월
     * @return PDF 바이트 배열
     */
    private static final String DEFAULT_FONT_PATH="src/main/resources/font/NotoSansKR-Regular.ttf";

    public byte[] generateAttendancePdf(AttendanceReportDto reportDto, String memberName, int year, int month) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.A4.rotate()); // 가로형 A4
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            // 📌 한글 지원용 폰트 설정 (명시적 BaseFont)
            BaseFont baseFont = BaseFont.createFont(
                    DEFAULT_FONT_PATH,
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED
            );
            Font titleFont = new Font(baseFont, 16, Font.BOLD);
            Font textFont = new Font(baseFont, 11);

            // ✅ 제목 설정
            String title = String.format("근태 리포트 %d년 %02d월 (%s 사원)", year, month, memberName);
            Paragraph titleParagraph = new Paragraph(title, titleFont);
            titleParagraph.setSpacingAfter(20);
            document.add(titleParagraph);

            // 📊 테이블 레이아웃 (2열: 그래프 + 텍스트)
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{2, 3});

            // ✅ 차트 이미지 생성
            Image chartImage = createChartImage(reportDto.getStatusCountMap(), baseFont);
            PdfPCell chartCell = new PdfPCell(chartImage, true);
            chartCell.setBorder(Rectangle.NO_BORDER);
            table.addCell(chartCell);

            // ✅ 요약 텍스트 삽입
            PdfPCell textCell = new PdfPCell(new Phrase(reportDto.getMarkdownSummary(), textFont));
            textCell.setBorder(Rectangle.NO_BORDER);
            table.addCell(textCell);

            document.add(table);
            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            log.error("❌ PDF 리포트 생성 실패", e);
            throw new RuntimeException("PDF 리포트 생성 중 오류 발생");
        }
    }

    /**
     * 상태별 출현 횟수를 막대그래프로 생성하여 이미지로 반환합니다.
     */
    private Image createChartImage(Map<Long, Long> codeCountMap, BaseFont baseFont) throws Exception {
        int width = 400;
        int height = 300;

        BufferedImage chart = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = chart.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // 한글 폰트 적용
        g.setFont(new java.awt.Font(baseFont.getPostscriptFontName(), java.awt.Font.PLAIN, 12));


        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int barWidth = 30;
        int maxHeight = 180;
        int x = 50;
        int yBase = height - 50;

        int maxCount = codeCountMap.
                values().
                stream().
                mapToInt(Long::intValue).
                max().
                orElse(1);

        // ✅ y축 눈금선 + 단위
        g.setColor(Color.GRAY);
        for (int i = 0; i <= maxCount; i++) {
            int y = yBase - (int)((i / (double)maxCount) * maxHeight);
            g.drawLine(40, y, width - 20, y);
            g.drawString(i + "회", 5, y + 5);
        }

        // ✅ 막대 그래프
        for (Map.Entry<Long, Long> entry : codeCountMap.entrySet()) {
            Long code = entry.getKey();
            Long count = entry.getValue();
            int barHeight = (int)((count / (double)maxCount) * maxHeight);

            g.setColor(Color.BLUE);
            g.fillRect(x, yBase - barHeight, barWidth, barHeight);

            g.setColor(Color.BLACK);
            g.drawString(mapCodeToLabel(code), x, yBase + 15);  // x축 라벨
            x += barWidth + 30;
        }

        g.dispose();
        return Image.getInstance(chart, null);
    }


    private String mapCodeToLabel(Long code) {
        return switch (code.intValue()) {
            case 1 -> "출근";
            case 2 -> "지각";
            case 3 -> "결근";
            case 4 -> "외근";
            case 5 -> "연차";
            case 6 -> "질병";
            case 7 -> "반차";
            case 8 -> "상(喪)";
            default -> "기타";
        };
    }
}

