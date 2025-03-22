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

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.codec.dense.DenseCodec;
import com.realtimetech.opack.codec.json.JsonCodec;
import com.realtimetech.opack.exception.EncodeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.test.performance.PerformanceClass;
import com.realtimetech.opack.value.OpackValue;

public class TotalBenchmarkTest {
    static final boolean ENABLE_SERIALIZE_TEST = false;
    static final int SERIALIZE_LOOP = 15872;
    static final int SERIALIZE_ITERATION = 8;

    static final boolean ENABLE_DESERIALIZE_TEST = false;
    static final int DESERIALIZE_LOOP = 15680;
    static final int DESERIALIZE_ITERATION = 8;

    static final boolean ENABLE_JSON_ENCODE_TEST = true;
    static final int JSON_ENCODE_LOOP = 192;
    static final int JSON_ENCODE_ITERATION = 8;

    static final boolean ENABLE_JSON_DECODE_TEST = true;
    static final int JSON_DECODE_LOOP = 128;
    static final int JSON_DECODE_ITERATION = 8;

    static final boolean ENABLE_DENSE_ENCODE_TEST = false;
    static final int DENSE_ENCODE_LOOP = 1280;
    static final int DENSE_ENCODE_ITERATION = 8;

    static final boolean ENABLE_DENSE_DECODE_TEST = false;
    static final int DENSE_DECODE_LOOP = 2048;
    static final int DENSE_DECODE_ITERATION = 8;

    public static void main(String[] args) throws EncodeException, SerializeException {
        // Benchmark
        BenchmarkTable benchmarkTable = new BenchmarkTable(BenchmarkTable.ColumnType.STRING, BenchmarkTable.ColumnType.TIME, BenchmarkTable.ColumnType.NUMBER);
        benchmarkTable.setTitles("Name", "Time", "Throughput");

        // Opack Contexts
        Opacker opacker = Opacker.Builder.create().build();
        JsonCodec jsonCodec = JsonCodec.Builder.create().build();
        DenseCodec denseCodec = DenseCodec.Builder.create().build();

        // Values
        PerformanceClass performanceClass = new PerformanceClass();
        OpackValue serializedValue = opacker.serialize(performanceClass);
        assert serializedValue != null;

        String jsonString = jsonCodec.encode(serializedValue);
        byte[] denseBytes = denseCodec.encode(serializedValue);

        PerformanceClass.ExceptionRunnable serializeRunnable = () -> opacker.serialize(performanceClass);
        PerformanceClass.ExceptionRunnable deserializeRunnable = () -> opacker.deserialize(PerformanceClass.class, serializedValue);
        PerformanceClass.ExceptionRunnable jsonEncodeRunnable = () -> jsonCodec.encode(serializedValue);
        PerformanceClass.ExceptionRunnable jsonDecodeRunnable = () -> jsonCodec.decode(jsonString);
        PerformanceClass.ExceptionRunnable denseEncodeRunnable = () -> denseCodec.encode(serializedValue);
        PerformanceClass.ExceptionRunnable denseDecodeRunnable = () -> denseCodec.decode(denseBytes);

        // Run

        if (ENABLE_SERIALIZE_TEST) {
            long serializeTime = 0;
            for (int index = 0; index < SERIALIZE_ITERATION; index++) {
                serializeTime += PerformanceClass.measureRunningTime(SERIALIZE_LOOP, serializeRunnable);
            }
            serializeTime /= SERIALIZE_ITERATION;

            benchmarkTable.addRow("Serialization", serializeTime, (double) SERIALIZE_LOOP / ((double) serializeTime / 1000d));
        }

        if (ENABLE_DESERIALIZE_TEST) {
            long deserializeTime = 0;
            for (int index = 0; index < DESERIALIZE_ITERATION; index++) {
                deserializeTime += PerformanceClass.measureRunningTime(DESERIALIZE_LOOP, deserializeRunnable);
            }
            deserializeTime /= DESERIALIZE_ITERATION;

            benchmarkTable.addRow("Deserialization", deserializeTime, (double) DESERIALIZE_LOOP / ((double) deserializeTime / 1000d));
        }

        if (ENABLE_JSON_ENCODE_TEST) {
            long jsonEncodeTime = 0;
            for (int index = 0; index < JSON_ENCODE_ITERATION; index++) {
                jsonEncodeTime += PerformanceClass.measureRunningTime(JSON_ENCODE_LOOP, jsonEncodeRunnable);
            }
            jsonEncodeTime /= JSON_ENCODE_ITERATION;

            benchmarkTable.addRow("JSON Encoding", jsonEncodeTime, (double) JSON_ENCODE_LOOP / ((double) jsonEncodeTime / 1000d));
        }

        if (ENABLE_JSON_DECODE_TEST) {
            long jsonDecodeTime = 0;
            for (int index = 0; index < JSON_DECODE_ITERATION; index++) {
                jsonDecodeTime += PerformanceClass.measureRunningTime(JSON_DECODE_LOOP, jsonDecodeRunnable);
            }
            jsonDecodeTime /= JSON_DECODE_ITERATION;

            benchmarkTable.addRow("JSON Decoding", jsonDecodeTime, (double) JSON_DECODE_LOOP / ((double) jsonDecodeTime / 1000d));
        }

        if (ENABLE_DENSE_ENCODE_TEST) {
            long denseEncodeTime = 0;
            for (int index = 0; index < DENSE_ENCODE_ITERATION; index++) {
                denseEncodeTime += PerformanceClass.measureRunningTime(DENSE_ENCODE_LOOP, denseEncodeRunnable);
            }
            denseEncodeTime /= DENSE_ENCODE_ITERATION;

            benchmarkTable.addRow("Dense Encoding", denseEncodeTime, (double) DENSE_ENCODE_LOOP / ((double) denseEncodeTime / 1000d));
        }

        if (ENABLE_DENSE_DECODE_TEST) {
            long denseDecodeTime = 0;
            for (int index = 0; index < DENSE_DECODE_ITERATION; index++) {
                denseDecodeTime += PerformanceClass.measureRunningTime(DENSE_DECODE_LOOP, denseDecodeRunnable);
            }
            denseDecodeTime /= DENSE_DECODE_ITERATION;

            benchmarkTable.addRow("Dense Decoding", denseDecodeTime, (double) DENSE_DECODE_LOOP / ((double) denseDecodeTime / 1000d));
        }

        System.out.println(benchmarkTable);
    }
}
