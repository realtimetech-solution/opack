/*
 * Copyright (C) 2025 REALTIMETECH All Rights Reserved
 *
 * Licensed either under the Apache License, Version 2.0, or (at your option)
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation (subject to the "Classpath" exception),
 * either version 2, or any later version (collectively, the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     http://www.gnu.org/licenses/
 *     http://www.gnu.org/software/classpath/license.html
 *
 * or as provided in the LICENSE file that accompanied this code.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.realtimetech.opack.benchmark;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

public class BenchmarkTable {
    public enum ColumnType {
        STRING, NUMBER, BYTES, TIME
    }

    private static final @NotNull DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###,###.###");

    static @NotNull String getFormattedNumber(Number number) {
        return DECIMAL_FORMAT.format(number);
    }

    static @NotNull String getFormattedSize(long bytes) {
        final long limitKiloBytes = 1024;
        final long limitMegaBytes = limitKiloBytes * 1024;
        final long limitGigaBytes = limitMegaBytes * 1024;

        if (bytes < limitKiloBytes) {
            return getFormattedNumber(bytes) + "byte";
        } else if (bytes < limitMegaBytes) {
            return getFormattedNumber(bytes / limitKiloBytes) + "kb";
        } else if (bytes < limitGigaBytes) {
            return getFormattedNumber(bytes / limitMegaBytes) + "mb";
        }

        return getFormattedNumber(bytes / limitGigaBytes) + "gb";
    }


    private final @NotNull ColumnType @NotNull [] columnTypes;
    private final @NotNull String @NotNull [] titles;

    private final @NotNull List<String[]> rows;

    public BenchmarkTable(@NotNull ColumnType @NotNull ... columnTypes) {
        this.columnTypes = columnTypes;
        this.titles = new String[this.columnTypes.length];

        this.rows = new LinkedList<>();
    }

    public @NotNull BenchmarkTable setTitles(@NotNull String @NotNull ... titles) {
        if (columnTypes.length != titles.length) {
            throw new IllegalArgumentException("Mismatch columns size and values.");
        }

        System.arraycopy(titles, 0, this.titles, 0, this.columnTypes.length);

        return this;
    }

    public @NotNull BenchmarkTable addLineRow() {
        this.rows.add(null);

        return this;
    }

    public @NotNull BenchmarkTable addRow(@NotNull Object @NotNull ... values) {
        if (columnTypes.length != values.length) {
            throw new IllegalArgumentException("Mismatch columns size and values.");
        }

        String[] strings = new String[this.columnTypes.length];

        for (int column = 0; column < this.columnTypes.length; column++) {
            ColumnType columnType = this.columnTypes[column];

            switch (columnType) {
                case BYTES:
                    strings[column] = getFormattedSize((Long) values[column]);
                    break;
                case NUMBER:
                    strings[column] = getFormattedNumber((Number) values[column]);
                    break;
                case TIME:
                    strings[column] = getFormattedNumber((Number) values[column]) + "ms";
                    break;
                case STRING:
                default:
                    strings[column] = values[column].toString();
                    break;
            }
        }

        this.rows.add(strings);

        return this;
    }

    private void appendLine(@NotNull StringBuffer stringBuffer, int @NotNull [] maxLengths) {
        stringBuffer.append('+');
        for (int column = 0; column < this.columnTypes.length; column++) {
            stringBuffer.append("-".repeat(maxLengths[column]));
            stringBuffer.append('+');
        }
        stringBuffer.append(System.lineSeparator());
    }

    public @NotNull String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        int columnCount = this.columnTypes.length;
        int rowCount = this.rows.size();
        int[] maxLengths = new int[columnCount];

        for (int column = 0; column < columnCount; column++) {
            maxLengths[column] = this.titles[column].length();
        }

        for (String[] row : this.rows) {
            if (row != null) {
                for (int column = 0; column < columnCount; column++) {
                    maxLengths[column] = Math.max(maxLengths[column], row[column].length());
                }
            }
        }

        for (int column = 0; column < columnCount; column++) {
            maxLengths[column] += 2;

            if ((maxLengths[column] - this.titles[column].length()) % 2 != 0) {
                maxLengths[column]++;
            }
        }

        this.appendLine(stringBuffer, maxLengths);

        stringBuffer.append('|');
        for (int column = 0; column < columnCount; column++) {
            String value = this.titles[column];

            int spaceSize = maxLengths[column] - value.length();
            int halfSize = spaceSize / 2;

            stringBuffer.append(" ".repeat(spaceSize - halfSize));
            stringBuffer.append(value);
            stringBuffer.append(" ".repeat(halfSize));
            stringBuffer.append("|");
        }
        stringBuffer.append(System.lineSeparator());

        this.appendLine(stringBuffer, maxLengths);

        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            String[] row = this.rows.get(rowIndex);

            if (row != null) {
                stringBuffer.append('|');

                for (int column = 0; column < columnCount; column++) {
                    String value = row[column];
                    int spaceSize = maxLengths[column] - value.length();

                    stringBuffer.append(" ");
                    stringBuffer.append(value);
                    stringBuffer.append(" ".repeat(spaceSize - 1));
                    stringBuffer.append("|");
                }

                stringBuffer.append(System.lineSeparator());
            } else {
                if (rowIndex != 0 && rowIndex != rowCount - 1) {
                    this.appendLine(stringBuffer, maxLengths);
                }
            }
        }

        this.appendLine(stringBuffer, maxLengths);

        return stringBuffer.toString();
    }
}