/*
 * Copyright 2018-2021 Crown Copyright
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

package uk.gov.gchq.palisade;

import org.junit.Test;

import uk.gov.gchq.palisade.rule.Rules;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.gov.gchq.palisade.Util.applyRulesToItem;

public class UtilTest {

    @Test
    public void shouldReturnResourceIfNoRules() {
        //when
        final String actual1 = applyRulesToItem("String", null, null, null);
        final String actual2 = applyRulesToItem("String", null, null, new Rules<>());
        //then
        assertEquals("Only 'String' should be returned if there are no rules", "String", actual1);
        assertEquals("Only 'String' should be returned if there are no rules", "String", actual2);
    }

    @Test
    public void shouldUpdateRecord() {
        //given
        final Rules<String> rules = new Rules<String>().addRule("r1", (record, user, context) -> "fromRule");
        //when
        final String actual1 = applyRulesToItem("String", null, null, rules);
        assertEquals("'fromRule' should be returned as the record has been updated", "fromRule", actual1);
    }

    @Test
    public void shouldUpdateRecordFromAllRules() {
        //given
        final Rules<String> rules = new Rules<String>()
                .addRule("r1", (record, user, context) -> "fromRule")
                .addRule("r2", (record, user, context) -> record.concat("2ndRule"));
        //when
        final String actual1 = applyRulesToItem("String", null, null, rules);
        //then
        assertEquals("'fromRule2ndRule' should be returned as the record has been updated for all rules", "fromRule" + "2ndRule", actual1);
    }

    @Test
    public void shouldUpdateStreamOfRecordsFromAllRules() {
        //given
        final AtomicLong recordsProcessed = new AtomicLong(0);
        final AtomicLong recordsReturned = new AtomicLong(0);
        final Stream<String> stream = Arrays.asList("one", "two").stream();
        final Rules<String> rules = new Rules<String>()
                .addRule("r2", (record, user, context) -> record.concat("2ndRule"))
                .addRule("r3", (record, user, context) -> record.concat("3rdRule"));
        //when
        final List<String> result = Util.applyRulesToStream(stream, null, null, rules, recordsProcessed, recordsReturned).collect(Collectors.toList());
        //then
        assertTrue("The stream of records and rules should be updated and show one2ndRule3rdRule", result.stream().anyMatch(s -> s.equals("one" + "2ndRule" + "3rdRule")));
        assertTrue("The stream of records and rules should be updated and show two2ndRule3rdRule", result.stream().anyMatch(s -> s.equals("two" + "2ndRule" + "3rdRule")));
        assertEquals("2 records should be processed", 2, recordsProcessed.get());
        assertEquals("2 records should be returned", 2, recordsReturned.get());
    }
}
