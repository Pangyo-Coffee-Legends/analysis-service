package com.nhnacademy.workanalysis.generator;

import com.nhnacademy.workanalysis.dto.report.AttendanceReportDto;
import com.nhnacademy.workanalysis.exception.PdfReportGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ResourceLoader;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link PdfReportGenerator} 클래스에 대한 단위 테스트입니다.
 */
class PdfReportGeneratorTest {

    private PdfReportGenerator generator;
    private ResourceLoader resourceLoader;
    @BeforeEach
    void setUp() {
        generator = new PdfReportGenerator(resourceLoader);
    }

    @Test
    @DisplayName("generateAttendancePdf() - 정상 PDF 생성 확인")
    void testGenerateAttendancePdf_success() {
        // given
        Map<Long, Long> map = new LinkedHashMap<>();
        map.put(1L, 5L); // 출근
        map.put(2L, 2L); // 지각
        map.put(3L, 1L); // 결근

        AttendanceReportDto dto = new AttendanceReportDto(map, "요약 마크다운", 2025, 6);

        // when
        byte[] pdfBytes = generator.generateAttendancePdf(dto, "홍길동", 2025, 6);

        // then
        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(1000); // PDF 정상 생성 여부 판단
    }

    @Test
    @DisplayName("generateAttendancePdf() - 빈 Map 입력 시 예외 발생")
    void testGenerateAttendancePdf_withEmptyData() {
        // given
        Map<Long, Long> emptyMap = new LinkedHashMap<>();
        AttendanceReportDto dto = new AttendanceReportDto(emptyMap, "비어있는 요약", 2025, 6);

        // when & then
        assertThrows(PdfReportGenerationException.class,
                () -> generator.generateAttendancePdf(dto, "홍길동", 2025, 6),
                "근태 데이터가 없어 바 차트를 생성할 수 없습니다.");
    }
    @Test
    @DisplayName("generateAttendancePdf() - null DTO 입력 시 예외 발생")
    void testGenerateAttendancePdf_withNullInput() {
        // when & then
        assertThrows(PdfReportGenerationException.class,
                () -> generator.generateAttendancePdf(null, "홍길동", 2025, 6));
    }
}
