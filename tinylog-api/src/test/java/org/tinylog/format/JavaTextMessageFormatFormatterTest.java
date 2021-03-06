/*
 * Copyright 2020 Martin Winandy
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.tinylog.format;

import java.text.ChoiceFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.junit.Rule;
import org.junit.Test;
import org.tinylog.Supplier;
import org.tinylog.rules.SystemStreamCollector;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link JavaTextMessageFormatFormatter}.
 */
public class JavaTextMessageFormatFormatterTest {

	/**
	 * Redirects and collects system output streams.
	 */
	@Rule
	public final SystemStreamCollector systemStream = new SystemStreamCollector(true);

	/**
	 * Verifies that a text message without any placeholders will be returned unchanged.
	 */
	@Test
	public void withoutPlaceholders() {
		assertThat(format("Hello World!")).isEqualTo("Hello World!");
	}

	/**
	 * Verifies that a placeholder without any context text will be replaced.
	 */
	@Test
	public void onlyPlaceholder() {
		assertThat(format("{0}", 42)).isEqualTo("42");
	}

	/**
	 * Verifies that a single placeholder will be replaced.
	 */
	@Test
	public void singlePlaceholder() {
		assertThat(format("Hello {0}!", "tinylog")).isEqualTo("Hello tinylog!");
	}

	/**
	 * Verifies that multiple placeholders will be replaced in the correct order.
	 */
	@Test
	public void multiplePlaceholders() {
		assertThat(format("{2} = {0} + {1}", 1, 2, 3)).isEqualTo("3 = 1 + 2");
	}

	/**
	 * Verifies that lazy argument suppliers can be evaluated.
	 *
	 * @see Supplier
	 */
	@Test
	public void lazyArgumentSupplier() {
		Supplier<Integer> supplier = () -> 42;
		assertThat(format("It is {0}", supplier)).isEqualTo("It is 42");
	}

	/**
	 * Verifies that {@link ChoiceFormat} compatible patterns are supported.
	 */
	@Test
	public void choiceFormat() {
		assertThat(format("{0,choice,0#zero|1#one|1<multiple}", 0)).isEqualTo("zero");
		assertThat(format("{0,choice,0#zero|1#one|1<multiple}", 1)).isEqualTo("one");
		assertThat(format("{0,choice,0#zero|1#one|1<multiple}", 2)).isEqualTo("multiple");
	}

	/**
	 * Verifies that {@link NumberFormat} compatible patterns are supported.
	 */
	@Test
	public void numberFormat() {
		assertThat(format(Locale.US, "{0,number,0.00}", 1)).isEqualTo("1.00");
		assertThat(format(Locale.GERMANY, "{0,number,0.00}", 1)).isEqualTo("1,00");
	}

	/**
	 * Verifies that {@link NumberFormat} compatible patterns can be used in {@link ChoiceFormat} patterns.
	 */
	@Test
	public void choiceAndNumberFormat() {
		assertThat(format("{0,choice,0#zero|1#one|1<{0,number,000}}", 0)).isEqualTo("zero");
		assertThat(format("{0,choice,0#zero|1#one|1<{0,number,000}}", 42)).isEqualTo("042");
	}

	/**
	 * Verifies that text messages with more arguments than placeholders can be handled.
	 */
	@Test
	public void tooManyArguments() {
		assertThat(format("Hello {0}!", "tinylog", "world")).isEqualTo("Hello tinylog!");
	}

	/**
	 * Verifies that text messages with less arguments than placeholders can be handled.
	 */
	@Test
	public void tooFewArguments() {
		assertThat(format("Hello {0}!")).isEqualTo("Hello {0}!");
	}

	/**
	 * Verifies that placeholders can be escaped.
	 */
	@Test
	public void ignoreEscapedPlaceholders() {
		assertThat(format("'{0}' {0}", "foo")).isEqualTo("{0} foo");
	}

	/**
	 * Verifies that a non-matching argument type will be reported.
	 */
	@Test
	public void illegalArgumentType() {
		assertThat(format("Test {0,number}!", "TEXT")).isEqualTo("Test {0,number}!");
		assertThat(systemStream.consumeErrorOutput()).containsOnlyOnce("WARN").containsOnlyOnce("Test {0,number}!");
	}

	/**
	 * Uses {@link JavaTextMessageFormatFormatter} for formatting a text message.
	 *
	 * @param message
	 *            Text message with placeholders
	 * @param arguments
	 *            Replacements for placeholders
	 * @return Formatted text message
	 */
	private static String format(final String message, final Object... arguments) {
		return format(Locale.ROOT, message, arguments);
	}

	/**
	 * Uses {@link JavaTextMessageFormatFormatter} for formatting a text message.
	 *
	 * @param locale
	 *            Locale for formatting numbers and dates
	 * @param message
	 *            Text message with placeholders
	 * @param arguments
	 *            Replacements for placeholders
	 * @return Formatted text message
	 */
	private static String format(final Locale locale, final String message, final Object... arguments) {
		return new JavaTextMessageFormatFormatter(locale).format(message, arguments);
	}

}
