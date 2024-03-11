package xxx.uk.tasks.fetching;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

class DatePattern {

	static boolean isPattern(String string) {
		return Optional.of(string)
			.map(s -> s.contains(" ") 
					? s.split(" ")[0].toLowerCase()
					: s.toLowerCase())
			.map(s -> s.split("-"))
			.filter(s -> s.length == 3)
			.filter(s -> s[0].matches("[0-9x]{4}"))
			.filter(s -> s[1].matches("[x0-9]{2}(/[x0-9]{2})*"))
			.filter(s -> s[1].matches("[x0-9]{2}(/[x0-9]{2})*"))
			.isPresent();
	}

    static boolean matchesToday(LocalDate today, String title) {
      String pattern = title.contains(" ") ? title.split(" ")[0] : title;

      String[] splitted = pattern.split("-");
      return matches(4, splitted[0], today.getYear())
          && matches(2, splitted[1], today.getMonthValue())
          && matches(2, splitted[2], today.getDayOfMonth());
    }

    private static boolean matches(int length, String subpattern, int currentValue) {
      if (subpattern.contains("/")) {
        return Arrays.asList(subpattern.split("/")).stream()
            .anyMatch(p -> matches(length, p, currentValue));
      }

      if (subpattern.length() > length) {
        return false;
      }

      String subpatternString = StringUtils.leftPad(subpattern + "", length, '0').toLowerCase();
      String currentValueString = StringUtils.leftPad(currentValue + "", length, '0');
      for (int i = 0; i < length; i++) {
        if (subpatternString.charAt(i) != 'x'
            && subpatternString.charAt(i) != currentValueString.charAt(i)) {
          return false;
        }
      }

      return true;
    }
	
}
