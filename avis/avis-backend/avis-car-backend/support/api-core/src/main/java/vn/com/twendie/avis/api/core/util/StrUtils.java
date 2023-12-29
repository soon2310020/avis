package vn.com.twendie.avis.api.core.util;

import com.google.common.base.CaseFormat;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.google.common.base.CaseFormat.*;
import static vn.com.twendie.avis.api.core.util.StrUtils.NumberLevel.*;

@Component
public class StrUtils {

    enum NumberLevel {
        UNITS(""),
        DOZENS("mươi", "mười", "linh"),
        HUNDREDS("trăm"),
        THOUSANDS("nghìn"),
        MILLIONS("triệu"),
        BILLION("tỉ");

        private final String[] names;

        NumberLevel(String... names) {
            this.names = names;
        }

    }

    @Getter
    enum Digit {
        ZERO('0', "không", ""),
        ONE('1', "một", "mốt"),
        TWO('2', "hai"),
        THREE('3', "ba"),
        FOUR('4', "bốn"),
        FIVE('5', "năm", "lăm"),
        SIX('6', "sáu"),
        SEVEN('7', "bảy"),
        EIGHT('8', "tám"),
        NINE('9', "chín");

        private final char value;
        private final String[] names;

        Digit(char value, String... names) {
            this.value = value;
            this.names = names;
        }

        private static String nameOf(char value) {
            return Arrays.stream(values())
                    .filter(digit -> digit.value == value)
                    .map(Digit::getNames)
                    .map(names -> names[0])
                    .findFirst()
                    .orElse(null);
        }

        private static String specialNameOf(char value) {
            return Arrays.stream(values())
                    .filter(digit -> digit.value == value)
                    .map(Digit::getNames)
                    .map(names -> names[names.length - 1])
                    .findFirst()
                    .orElse(null);
        }
    }

    public String normalize(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFC);
    }

    public CaseFormat getCaseFormat(String s) {
        if (s.matches("[_a-z0-9]*")) {
            return LOWER_UNDERSCORE;
        } else if (s.matches("[_A-Z0-9]*")) {
            return UPPER_UNDERSCORE;
        } else if (s.matches("[-a-z0-9]*")) {
            return LOWER_HYPHEN;
        } else if (s.matches("[a-z0-9]+([A-Z][a-z0-9]*)*")) {
            return LOWER_CAMEL;
        } else if (s.matches("([A-Z][a-z0-9]*)*")) {
            return UPPER_CAMEL;
        }
        return null;
    }

    public String toCaseFormat(String s, CaseFormat caseFormat) {
        return getCaseFormat(s).to(caseFormat, s);
    }

    public String toCamelCase(String s) {
        return toCaseFormat(s, LOWER_CAMEL);
    }

    public String createHttpImageLink(String ftpLink) {
        try {
            URL url = new URL(ftpLink);
            return String.format("http://%s/images%s", url.getHost(), url.getPath());
        } catch (Exception e) {
            return null;
        }
    }

    public String getFileName(String path) {
        if (Objects.nonNull(path)) {
            String[] parts = path.split("/");
            return parts[parts.length - 1];
        } else {
            return null;
        }
    }

    public String cleanWordMarks(String input) {
        input = Normalizer.normalize(input, Normalizer.Form.NFKD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(input)
                .replaceAll("")
                .replace('đ', 'd')
                .replace('Đ', 'D');
    }

    public String convertNumberToWords(Long number) {
        return Objects.nonNull(number) ? convertNumberToWords(number.toString()) : null;
    }

    public String convertNumberToWords(String number) {
        if (Objects.isNull(number)) {
            return null;
        } else {
            if (number.length() > 9) {
                String part1 = number.substring(0, number.length() - 9);
                String part2 = number.substring(number.length() - 9);
                return String.format("%s %s %s",
                        convertNumberToWords(part1),
                        BILLION.names[0],
                        convertTripleStringToWord(splitToTripleString(part2), true));
            } else {
                return convertTripleStringToWord(splitToTripleString(number), false);
            }
        }
    }

    private String[] splitToTripleString(String s) {
        String[] triple = new String[3];
        triple[0] = s.length() > 6 ? s.substring(0, s.length() - 6) : "0";
        triple[1] = s.length() > 3 ? s.substring(Math.max(0, s.length() - 6), s.length() - 3) : "0";
        triple[2] = s.length() > 0 ? s.substring(Math.max(0, s.length() - 3)) : "0";
        return triple;
    }

    private String convertTripleStringToWord(String[] triple, boolean withPrefix) {

        final char SPACE = ' ';

        final StringBuilder builder = new StringBuilder();

        String first = StringUtils.defaultIfBlank(triple[0], "0");
        String second = StringUtils.defaultIfBlank(triple[1], "0");
        String third = StringUtils.defaultIfBlank(triple[2], "0");

        if (Integer.parseInt(first) != 0) {
            builder.append(convertTripleDigitToWord(first.toCharArray(), withPrefix))
                    .append(SPACE)
                    .append(MILLIONS.names[0])
                    .append(SPACE);
            withPrefix = true;
        }
        if (Integer.parseInt(second) != 0) {
            builder.append(convertTripleDigitToWord(second.toCharArray(), withPrefix))
                    .append(SPACE)
                    .append(THOUSANDS.names[0])
                    .append(SPACE);
            withPrefix = true;
        }
        if (Integer.parseInt(third) != 0) {
            builder.append(convertTripleDigitToWord(third.toCharArray(), withPrefix));
        }
        return builder.toString().trim();
    }

    private String convertTripleDigitToWord(char[] triple, boolean withPrefix) {

        final char SPACE = ' ';
        final String EMPTY = "";

        final StringBuilder builder = new StringBuilder();

        char first = triple.length > 2 ? triple[triple.length - 3] : '0';
        char second = triple.length > 1 ? triple[triple.length - 2] : '0';
        char third = triple.length > 0 ? triple[triple.length - 1] : '0';

        if (first != '0' || withPrefix) {
            builder.append(Digit.nameOf(first))
                    .append(SPACE)
                    .append(HUNDREDS.names[0])
                    .append(SPACE);
            withPrefix = true;
        }
        if (second != '0' || withPrefix) {
            switch (second) {
                case '0':
                    if (third != '0') {
                        builder.append(DOZENS.names[2])
                                .append(SPACE)
                                .append(Digit.nameOf(third));
                    } else if (first == '0') {
                        return EMPTY;
                    }
                    break;
                case '1':
                    if (third == '1') {
                        builder.append(DOZENS.names[1])
                                .append(SPACE)
                                .append(Digit.nameOf(third));
                    } else {
                        builder.append(DOZENS.names[1])
                                .append(SPACE)
                                .append(Digit.specialNameOf(third));
                    }
                    break;
                default:
                    builder.append(Digit.nameOf(second))
                            .append(SPACE)
                            .append(DOZENS.names[0])
                            .append(SPACE)
                            .append(Digit.specialNameOf(third));
            }
        } else {
            builder.append(Digit.nameOf(third));
        }
        return builder.toString().trim();
    }
}