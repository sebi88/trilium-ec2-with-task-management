package xxx.uk.tasks.fetching;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class DatePatternTest {

  @ParameterizedTest
  @ValueSource(
      strings = {"xxxx-xx-12", "xxXX-xx-12", "xxxx-01-xx", "xxxx-01/02-xx", "xxxx-01/02-12/01",
          "2021-01-01", "xxxx-xx-12 Some title", "xxXX-xx-12 Some title", "xxxx-01-xx Some title",
          "xxxx-01/02-xx Some title", "xxxx-01/02-12/01 Some title", "2021-01-01 Some title"})
  void should_be_pattern(String string) {
    assertThat(DatePattern.isPattern(string)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {"xxxx-xx", "xx-12", "Some title", "2021-Some title"})
  void should_not_be_pattern(String string) {
    assertThat(DatePattern.isPattern(string)).isFalse();
  }

  @ParameterizedTest
  @MethodSource("matchesTodayParams")
  void should_match_day(LocalDate today, String string, boolean shouldMatch) {
    assertThat(DatePattern.matchesToday(today, string)).isEqualTo(shouldMatch);
  }

  static Stream<Arguments> matchesTodayParams() {
    LocalDate today = LocalDate.parse("2024-05-17");
    return Stream.of(Arguments.of(today, "2024-05-17", true),
        Arguments.of(today, "2024-054-17", false), 
        Arguments.of(today, "2024-05-171", false),
        Arguments.of(today, "20242-05-17", false), 
        Arguments.of(today, "2025-05-17", false),
        Arguments.of(today, "2024-11-17", false), 
        Arguments.of(today, "2024-1-17", false),
        Arguments.of(today, "2024-11-1", false), 
        Arguments.of(today, "2024-05-18", false),
        Arguments.of(today, "2024-05-17 Title", true),
        Arguments.of(today, "2025-05-17 Title", false),

        Arguments.of(today, "20XX-05-17", true), 
        Arguments.of(today, "xxXX-05-17", true),
        Arguments.of(today, "20XX-5-17", true), 
        Arguments.of(today, "xxXX-xx-17", true),
        Arguments.of(today, "xxXX-05-xx", true), 
        Arguments.of(today, "20XX-05-17 Title", true),
        Arguments.of(today, "xxXX-05-17 Title", true),
        Arguments.of(today, "xxXX-xx-17 Title", true),
        Arguments.of(today, "xxXX-05-x7 Title", true),
        Arguments.of(today, "xxXX-05-xx Title", true),

        Arguments.of(today, "xxXX-02/05-17 Title", true),
        Arguments.of(today, "xxXX-02/05/07-17 Title", true),
        Arguments.of(today, "xxXX-02/07-17 Title", false));
  }
}
