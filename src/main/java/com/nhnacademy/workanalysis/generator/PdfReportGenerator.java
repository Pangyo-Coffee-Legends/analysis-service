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
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 근태 리포트를 PDF 형식으로 생성하는 클래스입니다.
 *
 * <p>리포트 구성 요소:</p>
 * <ul>
 *     <li>제목</li>
 *     <li>근태 요약 테이블</li>
 *     <li>바 차트 (근태 상태별 출현 일수 시각화)</li>
 *     <li>도넛 차트 (근태 상태별 비율 시각화)</li>
 * </ul>
 */
@Slf4j
@Component
public class PdfReportGenerator {

    private static final String DEFAULT_FONT_PATH = "src/main/resources/font/NotoSansKR-Regular.ttf";

    /**
     * 근태 데이터를 기반으로 PDF 형식의 리포트를 생성합니다.
     *
     * @param reportDto  근태 상태별 일수 통계가 담긴 DTO
     * @param memberName 사원의 이름
     * @param year       리포트를 생성할 연도
     * @param month      리포트를 생성할 월
     * @return 생성된 PDF 문서의 바이트 배열
     * @throws PdfReportGenerationException PDF 생성 중 오류가 발생할 경우 발생
     */

    public byte[] generateAttendancePdf(AttendanceReportDto reportDto, String memberName, int year, int month) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 20, 20, 20, 20);
            PdfWriter.getInstance(document, out);
            document.open();

            BaseFont baseFont = BaseFont.createFont(DEFAULT_FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font titleFont = new Font(baseFont, 18, Font.BOLD);

            Paragraph title = new Paragraph(String.format("근태 리포트 %d년 %02d월 (%s 사원)", year, month, memberName), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15f);
            document.add(title);

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

            Image barChart = createBarChartImage(reportDto.getStatusCountMap(), baseFont);
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

    /**
     * PDF 테이블의 헤더 셀을 생성합니다.
     *
     * @param text     셀에 표시할 텍스트
     * @param baseFont 사용할 기본 폰트 (한글 지원)
     * @return 가운데 정렬된 회색 배경의 헤더 셀
     */

    private PdfPCell makeHeaderCell(String text, BaseFont baseFont) {
        Font font = new Font(baseFont, 13, Font.BOLD);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setPadding(10f);
        return cell;
    }

    /**
     * PDF 테이블의 바디(내용) 셀을 생성합니다.
     *
     * @param text     셀에 표시할 텍스트
     * @param baseFont 사용할 기본 폰트
     * @return 가운데 정렬된 일반 셀
     */

    private PdfPCell makeBodyCell(String text, BaseFont baseFont) {
        Font font = new Font(baseFont, 12);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8f);
        return cell;
    }

    /**
     * 근태 상태 코드(Long)를 한글 라벨로 변환합니다.
     *
     * @param code 근태 코드 (예: 1=출근, 2=지각, ...)
     * @return 근태 코드에 대응하는 한글 라벨 문자열
     */

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

    /**
     * 근태 상태 코드에 해당하는 색상을 반환합니다.
     *
     * @param code 근태 코드
     * @return 해당 코드에 대응하는 {@link Color} 객체
     */

    private Color getColorForCode(Long code) {
        return switch (code.intValue()) {
            case 1 -> new Color(0, 102, 204);    // 출근
            case 2 -> new Color(255, 153, 51);   // 지각
            case 3 -> new Color(204, 0, 0);      // 결근
            case 4 -> new Color(102, 204, 0);    // 외근
            case 5 -> new Color(0, 204, 204);    // 연차
            case 6 -> new Color(153, 0, 204);    // 질병
            case 7 -> new Color(255, 204, 0);    // 반차
            case 8 -> new Color(120, 120, 120);  // 상
            default -> Color.GRAY;
        };
    }

    /**
     * 근태 상태별 출현 일수를 바 차트로 생성하여 PDF에 삽입 가능한 이미지 객체로 반환합니다.
     * 항목 수에 따라 막대 너비를 자동 조정합니다.
     *
     * @param codeCountMap 근태 코드별 출현 일수 맵
     * @param baseFont     PDF 폰트 렌더링에 사용할 기본 폰트
     * @return 생성된 바 차트 이미지 객체
     * @throws Exception 이미지 생성 중 오류 발생 시
     */

    private Image createBarChartImage(Map<Long, Long> codeCountMap, BaseFont baseFont) throws Exception {
        int width = 720, height = 270;
        BufferedImage chart = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = chart.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setFont(new java.awt.Font("SansSerif", Font.BOLD, 16));
        String title = "근태 상태별 일수 분포";
        g.setColor(Color.BLACK);
        g.drawString(title, (width - g.getFontMetrics().stringWidth(title)) / 2, 25);

        g.setFont(new java.awt.Font(baseFont.getPostscriptFontName(), java.awt.Font.PLAIN, 14));
        int max = codeCountMap.values().stream().mapToInt(Long::intValue).max().orElse(1);
        int itemCount = codeCountMap.size();

        int yBase = height - 50;
        int maxHeight = 160;
        int totalBarArea = width - 100; // 좌우 여백 제외
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


    /**
     * 근태 상태별 비율을 도넛 차트로 시각화하여 PDF에 삽입 가능한 이미지로 반환합니다.
     * 도넛 중앙에는 총 근무일을 표시하고, 우측에 색상별 범례(항목 + 퍼센트)를 출력합니다.
     *
     * @param codeCountMap 근태 코드별 일수 맵
     * @return 생성된 도넛 차트 이미지 객체
     * @throws Exception 이미지 생성 중 오류 발생 시
     */

    private Image createDoughnutChartImage(Map<Long, Long> codeCountMap) throws Exception {
        int width = 720, height = 350;
        BufferedImage chart = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = chart.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 16));
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
        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 18));
        String text1 = "총 근무일";
        String text2 = total + "일";
        g.drawString(text1, centerX - g.getFontMetrics().stringWidth(text1) / 2, centerY - 5);
        g.drawString(text2, centerX - g.getFontMetrics().stringWidth(text2) / 2, centerY + 20);

        int legendX = 500;
        int legendY = 110;
        int boxSize = 18;
        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 16));

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
