package com.nhnacademy.workanalysis.generator;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.nhnacademy.workanalysis.dto.report.AttendanceReportDto;
import com.nhnacademy.workanalysis.exception.PdfReportGenerationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PdfReportGenerator {

    private static final String DEFAULT_FONT_PATH = "classpath:font/NotoSansKR-Regular.ttf";
    private final ResourceLoader resourceLoader;
    private java.awt.Font awtKoreanFont; // AWT용 폰트 (차트용)

    public PdfReportGenerator(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public byte[] generateAttendancePdf(AttendanceReportDto reportDto, String memberName, int year, int month) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 20, 20, 20, 20);
            PdfWriter.getInstance(document, out);
            document.open();

            // 📌 폰트 로드 (BaseFont + AWT Font 등록)
            Resource fontResource = resourceLoader.getResource(DEFAULT_FONT_PATH);
            File tempFontFile = File.createTempFile("tempFont", ".ttf");
            try (InputStream is = fontResource.getInputStream();
                 OutputStream os = new FileOutputStream(tempFontFile)) {
                is.transferTo(os);
            }

            // iText용 BaseFont
            BaseFont baseFont = BaseFont.createFont(tempFontFile.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            // AWT용 java.awt.Font 등록
            awtKoreanFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, tempFontFile).deriveFont(14f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(awtKoreanFont);

            // 제목
            Font titleFont = new Font(baseFont, 18, Font.BOLD);
            Paragraph title = new Paragraph(String.format("근태 리포트 %d년 %02d월 (%s 사원)", year, month, memberName), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15f);
            document.add(title);

            // 요약 테이블
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidths(new int[]{2, 1});
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingAfter(20f);
            summaryTable.addCell(makeHeaderCell("근태 구분", baseFont));
            summaryTable.addCell(makeHeaderCell("일수", baseFont));

            for (Map.Entry<Long, Long> entry : reportDto.getStatusCountMap().entrySet()) {
                summaryTable.addCell(makeBodyCell(mapCodeToLabel(entry.getKey()), baseFont));
                summaryTable.addCell(makeBodyCell(entry.getValue() + "일", baseFont));
            }
            document.add(summaryTable);

            // 차트 추가
            Image barChart = createBarChartImage(reportDto.getStatusCountMap());
            barChart.scaleToFit(520f, 240f);
            document.add(barChart);

            Image doughnutChart = createDoughnutChartImage(reportDto.getStatusCountMap());
            doughnutChart.scaleToFit(480f, 250f);
            document.add(doughnutChart);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            log.error("❌ PDF 리포트 생성 실패: 사용자={}, 연도={}, 월={}, 원인={}", memberName, year, month, e.getMessage(), e);
            throw new PdfReportGenerationException("PDF 리포트 생성 중 오류 발생", e);
        }
    }

    private PdfPCell makeHeaderCell(String text, BaseFont baseFont) {
        Font font = new Font(baseFont, 13, Font.BOLD);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setPadding(10f);
        return cell;
    }

    private PdfPCell makeBodyCell(String text, BaseFont baseFont) {
        Font font = new Font(baseFont, 12);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8f);
        return cell;
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

    private Color getColorForCode(Long code) {
        return switch (code.intValue()) {
            case 2 -> new Color(255, 230, 153);  // 지각 (#ffe699)
            case 3 -> new Color(248, 215, 218);  // 결근 (#f8d7da)
            case 4 -> new Color(204, 229, 255);  // 외근 (#cce5ff)
            case 5 -> new Color(226, 213, 248);  // 연차 (#e2d5f8)
            case 6 -> new Color(212, 237, 218);  // 질병 (#d4edda)
            case 7 -> new Color(255, 229, 180);  // 반차 (#ffe5b4)
            case 8 -> new Color(252, 228, 236);  // 상 (#fce4ec)
            case 1 -> new Color(180, 200, 255);  // 출근 (없던 색, 추가 지정)
            default -> new Color(208, 234, 255); // 기타 (#d0eaff)
        };
    }


    private Image createBarChartImage(Map<Long, Long> codeCountMap) throws Exception {
        int width = 720, height = 270;
        BufferedImage chart = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = chart.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setFont(awtKoreanFont.deriveFont(java.awt.Font.BOLD, 16f));
        String title = "근태 상태별 일수 분포";
        g.setColor(Color.BLACK);
        g.drawString(title, (width - g.getFontMetrics().stringWidth(title)) / 2, 25);

        g.setFont(awtKoreanFont.deriveFont(java.awt.Font.PLAIN, 14f));

        long totalCount = codeCountMap.values().stream().mapToLong(Long::longValue).sum();
        if (totalCount == 0) throw new PdfReportGenerationException("차트를 생성할 데이터가 없습니다.");

        int max = codeCountMap.values().stream().mapToInt(Long::intValue).max().orElse(1);
        int yBase = height - 50, maxHeight = 160;
        int itemCount = codeCountMap.size();
        int totalBarArea = width - 100;
        int barSpacing = 20;
        int barWidth = (totalBarArea - (barSpacing * (itemCount - 1))) / itemCount;
        int startX = (width - (barWidth * itemCount + barSpacing * (itemCount - 1))) / 2;

        for (int i = 0; i <= max; i++) {
            int y = yBase - (int) ((i / (double) max) * maxHeight);
            g.setColor(Color.GRAY);
            g.drawLine(60, y, width - 20, y);
            g.drawString(i + "일", 20, y + 5);
        }

        int x = startX;
        for (Map.Entry<Long, Long> entry : codeCountMap.entrySet()) {
            Long code = entry.getKey();
            Long count = entry.getValue();
            int barHeight = (int) ((count / (double) max) * maxHeight);

            g.setColor(getColorForCode(code));
            g.fillRect(x, yBase - barHeight, barWidth, barHeight);

            g.setColor(Color.BLACK);
            String label = mapCodeToLabel(code);
            int labelWidth = g.getFontMetrics().stringWidth(label);
            g.drawString(label, x + (barWidth - labelWidth) / 2, yBase + 20);

            x += barWidth + barSpacing;
        }

        g.dispose();
        return Image.getInstance(chart, null);
    }

    private Image createDoughnutChartImage(Map<Long, Long> codeCountMap) throws Exception {
        int width = 720, height = 350;
        BufferedImage chart = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = chart.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setFont(awtKoreanFont.deriveFont(java.awt.Font.BOLD, 16f));
        String title = "근태 상태별 비율";
        g.setColor(Color.BLACK);
        g.drawString(title, (width - g.getFontMetrics().stringWidth(title)) / 2, 25);

        int centerX = 320, centerY = height / 2 + 10;
        int outerR = 110, innerR = 60;
        int total = codeCountMap.values().stream().mapToInt(Long::intValue).sum();

        List<Long> fixedOrder = List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L);
        Map<Long, Long> ordered = new LinkedHashMap<>();
        for (Long code : fixedOrder) {
            if (codeCountMap.containsKey(code)) {
                ordered.put(code, codeCountMap.get(code));
            }
        }

        int startAngle = 0;
        for (Map.Entry<Long, Long> entry : ordered.entrySet()) {
            Long code = entry.getKey();
            double ratio = entry.getValue() / (double) total;
            int angle = (int) Math.round(ratio * 360);
            g.setColor(getColorForCode(code));
            g.fillArc(centerX - outerR, centerY - outerR, outerR * 2, outerR * 2, startAngle, angle);
            startAngle += angle;
        }

        g.setColor(Color.WHITE);
        g.fillOval(centerX - innerR, centerY - innerR, innerR * 2, innerR * 2);

        g.setColor(Color.BLACK);
        g.setFont(awtKoreanFont.deriveFont(java.awt.Font.BOLD, 18f));
        String text1 = "총 근무일";
        String text2 = total + "일";
        g.drawString(text1, centerX - g.getFontMetrics().stringWidth(text1) / 2, centerY - 5);
        g.drawString(text2, centerX - g.getFontMetrics().stringWidth(text2) / 2, centerY + 20);

        int legendX = 500;
        int legendY = 110;
        int boxSize = 18;
        g.setFont(awtKoreanFont.deriveFont(java.awt.Font.PLAIN, 16f));

        for (Map.Entry<Long, Long> entry : ordered.entrySet()) {
            Long code = entry.getKey();
            double ratio = entry.getValue() / (double) total;
            double percent = Math.round(ratio * 1000) / 10.0;

            g.setColor(getColorForCode(code));
            g.fillRect(legendX, legendY, boxSize, boxSize);

            g.setColor(Color.BLACK);
            String label = String.format("%s (%.1f%%)", mapCodeToLabel(code), percent);
            g.drawString(label, legendX + boxSize + 10, legendY + boxSize - 3);

            legendY += 30;
        }

        g.dispose();
        return Image.getInstance(chart, null);
    }
}
