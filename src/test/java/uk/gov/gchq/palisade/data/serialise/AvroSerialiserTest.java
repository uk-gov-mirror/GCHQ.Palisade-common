/*
 * Copyright 2020 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.gov.gchq.palisade.data.serialise;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.junit.Test;

import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.util.JsonAssert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class AvroSerialiserTest {

    public static final int INPUT_SIZE = 100;
    public static final Integer[] INPUT = IntStream.range(0, INPUT_SIZE).boxed().toArray((a) -> new Integer[INPUT_SIZE]);

    @Test
    public void shouldConsistentlyPass() throws IOException {
        for (int i = 0; i < 10000; i++) {
            testPrimitiveSerialiseAndDeserialise();
        }
    }

    @Test
    public void testPrimitiveSerialise() throws IOException {
        // Given
        final AvroSerialiser<Integer> serialiser = new AvroSerialiser<>(Integer.class);

        // When
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        serialiser.serialise(Stream.of(INPUT), outputStream);

        // Then
        final DatumReader<Integer> datumReader = new SpecificDatumReader<>(Integer.class);
        final DataFileStream<Integer> in = new DataFileStream<>(new ByteArrayInputStream(outputStream.toByteArray()), datumReader);
        final List<Integer> deserialised = new ArrayList<>();
        in.forEachRemaining(deserialised::add);
        in.close();
        assertEquals(INPUT_SIZE + " records should be serialised", Arrays.asList(INPUT), deserialised);
    }

    @Test
    public void testPrimitiveDeserialise() throws IOException {
        // Given
        final AvroSerialiser<Integer> serialiser = new AvroSerialiser<>(Integer.class);

        final Schema schema = SpecificData.get().getSchema(Integer.class);
        final DatumWriter<Integer> datumWriter = new SpecificDatumWriter<>(schema);
        final DataFileWriter<Integer> dataFileWriter = new DataFileWriter<>(datumWriter);

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        dataFileWriter.create(schema, outputStream);
        Stream.of(INPUT).forEach(item -> {
            try {
                dataFileWriter.append(item);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        });
        dataFileWriter.flush();
        dataFileWriter.close();
        final InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        // When
        final Stream<Integer> deserialised = serialiser.deserialise(inputStream);

        // Then
        assertEquals(INPUT_SIZE + " records should be serialised", Arrays.asList(INPUT), deserialised.collect(Collectors.toList()));
    }

    @Test
    public void testPrimitiveSerialiseAndDeserialise() throws IOException {
        // Given
        final AvroSerialiser<Integer> serialiser = new AvroSerialiser<>(Integer.class);

        // When
        Stream<Integer> stream = Stream.of(INPUT);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        serialiser.serialise(stream, outputStream);
        final Stream<Integer> deserialised = serialiser.deserialise(new ByteArrayInputStream(outputStream.toByteArray()));

        // Then
        assertEquals(INPUT_SIZE + " records should be serialised and deserialised", Arrays.asList(INPUT), deserialised.collect(Collectors.toList()));
    }

    @Test
    public void shouldSerialiseAndDeserialiseWithClass() throws IOException {
        // Given
        final AvroSerialiser<TestObj> serialiser = new AvroSerialiser<>(TestObj.class);

        final List<TestObj> input = Arrays.asList(
                new TestObj("str1A", 1, null),
                new TestObj("str1B", 2, "str2B"),
                TestObj.newBuilder()
                        .setFieldStr1("str1C")
                        .setFieldInt(null)
                        .setFieldStr2("str2C")
                        .build()
        );

        // When
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        serialiser.serialise(input.stream(), outputStream);
        final Stream<TestObj> deserialised = serialiser.deserialise(new ByteArrayInputStream(outputStream.toByteArray()));

        // Then
        assertEquals("The serialised and deserialised TestObj list should match the input list", input, deserialised.collect(Collectors.toList()));
    }

    @Test
    public void shouldJsonSerialiseAndDeserialise() {
        // Given
        final AvroSerialiser<Integer> serialiser = new AvroSerialiser<>(Integer.class);

        // When
        final byte[] json = JSONSerialiser.serialise(serialiser, true);
        final Serialiser deserialised = JSONSerialiser.deserialise(json, Serialiser.class);

        // Then
        JsonAssert.assertEquals(String.format("{%n" +
                "  \"domainClass\" : \"java.lang.Integer\",%n" +
                "  \"class\" : \"uk.gov.gchq.palisade.data.serialise.AvroSerialiser\"%n" +
                "}").getBytes(), json);

        assertEquals("The deserialised class should be AvroSerialiser", AvroSerialiser.class, deserialised.getClass());
        assertEquals("The serialiser domain class should equal the deserialised domain class", serialiser.getDomainClass(), ((AvroSerialiser) deserialised).getDomainClass());
    }
}