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

package com.realtimetech.opack.test.opacker.other;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.annotation.Type;
import com.realtimetech.opack.codec.json.Json;
import com.realtimetech.opack.exception.DecodeException;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.test.OpackAssert;
import com.realtimetech.opack.value.OpackValue;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

public class JavaJsonBenchmarkTest {
    public static class Clients {
        @Type(ArrayList.class)
        private List<Client> clients;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Clients)) {
                return false;
            }

            Clients that = (Clients) o;

            return Objects.equals(clients, that.clients);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(clients);
        }

        @Override
        public String toString() {
            return "Clients{" + "clients=" + clients + '}';
        }

        public List<Client> getClients() {
            return clients;
        }

        public void setClients(List<Client> clients) {
            this.clients = clients;
        }

        public static final class Client {
            private long id;
            private int index;
            private UUID guid;
            private boolean isActive;
            private BigDecimal balance;
            private String picture;
            private int age;
            private EyeColor eyeColor;
            private String name;
            private String gender;
            private String company;
            private String[] emails;
            private long[] phones;
            private String address;
            private String about;
            private LocalDate registered;
            private double latitude;
            private double longitude;
            @Type(ArrayList.class)
            private List<String> tags;
            @Type(ArrayList.class)
            private List<Partner> partners;

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (!(o instanceof Client)) {
                    return false;
                }

                Client client = (Client) o;

                return index == client.index &&
                        isActive == client.isActive &&
                        age == client.age &&
                        Math.abs(Double.doubleToLongBits(client.latitude) - Double.doubleToLongBits(latitude)) < 3 &&
                        Math.abs(Double.doubleToLongBits(client.longitude) - Double.doubleToLongBits(longitude)) < 3 &&
                        Objects.equals(id, client.id) &&
                        Objects.equals(guid, client.guid) &&
                        balance.compareTo(client.balance) == 0 &&
                        Objects.equals(picture, client.picture) &&
                        Objects.equals(eyeColor, client.eyeColor) &&
                        Objects.equals(name, client.name) &&
                        Objects.equals(gender, client.gender) &&
                        Objects.equals(company, client.company) &&
                        Arrays.equals(emails, client.emails) &&
                        Arrays.equals(phones, client.phones) &&
                        Objects.equals(address, client.address) &&
                        Objects.equals(about, client.about) &&
                        Objects.equals(registered, client.registered) &&
                        Objects.equals(tags, client.tags) &&
                        Objects.equals(partners, client.partners);
            }

            @Override
            public int hashCode() {
                return Objects.hash(id, index, guid, isActive, balance, picture, age, eyeColor, name, gender, company,
                        Arrays.hashCode(emails), Arrays.hashCode(phones), address, about, registered, tags, partners);
            }

            private String toStr(long[] nums) {
                StringBuilder sb = new StringBuilder();
                sb.append('[');
                boolean first = true;
                for(long l : nums) {
                    if (first) first = false;
                    else sb.append(',');
                    sb.append(l);
                }
                sb.append(']');
                return sb.toString();
            }

            @Override
            public String toString() {
                return "JsonDataObj{" + "id=" + id + ", index=" + index + ", guid=" + guid + ", isActive=" + isActive + ", balance=" + balance + ", picture=" + picture + ", age=" + age + ", eyeColor=" + eyeColor + ", name=" + name + ", gender=" + gender + ", company=" + company + ", emails=" + (emails != null ? Arrays.asList(emails) : null) + ", phones=" + toStr(phones) + ", address=" + address + ", about=" + about + ", registered=" + registered + ", latitude=" + latitude + ", longitude=" + longitude + ", tags=" + tags + ", partners=" + partners + '}';
            }

            public long getId() {
                return id;
            }

            public void setId(long id) {
                this.id = id;
            }

            public int getIndex() {
                return index;
            }

            public void setIndex(int index) {
                this.index = index;
            }

            public UUID getGuid() {
                return guid;
            }

            public void setGuid(UUID guid) {
                this.guid = guid;
            }

            public boolean getIsActive() {
                return isActive;
            }

            public void setIsActive(boolean isActive) {
                this.isActive = isActive;
            }

            public BigDecimal getBalance() {
                return balance;
            }

            public void setBalance(BigDecimal balance) {
                this.balance = balance;
            }

            public String getPicture() {
                return picture;
            }

            public void setPicture(String picture) {
                this.picture = picture;
            }

            public int getAge() {
                return age;
            }

            public void setAge(int age) {
                this.age = age;
            }

            public EyeColor getEyeColor() {
                return eyeColor;
            }

            public void setEyeColor(EyeColor eyeColor) {
                this.eyeColor = eyeColor;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getGender() {
                return gender;
            }

            public void setGender(String gender) {
                this.gender = gender;
            }

            public String getCompany() {
                return company;
            }

            public void setCompany(String company) {
                this.company = company;
            }

            public String[] getEmails() {
                return emails;
            }

            public void setEmails(String[] emails) {
                this.emails = emails;
            }

            public long[] getPhones() {
                return phones;
            }

            public void setPhones(long[] phones) {
                this.phones = phones;
            }

            public String getAddress() {
                return address;
            }

            public void setAddress(String address) {
                this.address = address;
            }

            public String getAbout() {
                return about;
            }

            public void setAbout(String about) {
                this.about = about;
            }

            public LocalDate getRegistered() {
                return registered;
            }

            public void setRegistered(LocalDate registered) {
                this.registered = registered;
            }

            public double getLatitude() {
                return latitude;
            }

            public void setLatitude(double latitude) {
                this.latitude = latitude;
            }

            public double getLongitude() {
                return longitude;
            }

            public void setLongitude(double longitude) {
                this.longitude = longitude;
            }

            public List<String> getTags() {
                return tags;
            }

            public void setTags(List<String> tags) {
                this.tags = tags;
            }

            public List<Partner> getPartners() {
                return partners;
            }

            public void setPartners(List<Partner> partners) {
                this.partners = partners;
            }
        }

        public enum EyeColor {
            BROWN,
            BLUE,
            GREEN;

            public static EyeColor fromNumber(int i) {
                if (i == 0) return BROWN;
                if (i == 1) return BLUE;
                return GREEN;
            }
        }

        public static final class Partner {
            private long id;
            private String name;
            private OffsetDateTime since;

            public Partner() {
            }

            public static Partner create(long id, String name, OffsetDateTime since) {
                Partner partner = new Partner();
                partner.id = id;
                partner.name = name;
                partner.since = since;
                return partner;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (!(o instanceof Partner)) {
                    return false;
                }

                Partner partner = (Partner) o;

                return id == partner.id &&
                        Objects.equals(since, partner.since) &&
                        Objects.equals(name, partner.name);
            }

            @Override
            public int hashCode() {
                return Objects.hash(id, since, name);
            }

            @Override
            public String toString() {
                return "Partner{" + "id=" + id + ", name=" + name + ", since=" + since + '}';
            }

            public long getId() {
                return id;
            }

            public void setId(long id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public OffsetDateTime getSince() {
                return since;
            }

            public void setSince(OffsetDateTime since) {
                this.since = since;
            }
        }
    }

    @Test
    public void test() throws SerializeException, DeserializeException, OpackAssert.AssertException, DecodeException {
        Opacker opacker = Opacker.Builder.create().build();
        OpackValue serialized = Json.decode("{\"clients\":[{\"id\":112673038484856810,\"index\":1344055720,\"guid\":\"60688132-8a21-b944-f888-f509a6b90256\",\"isActive\":false,\"balance\":0.04548297857634076,\"picture\":\"vAwEeFeE9EM0mrfkg2ujwjCf77kbBYALtM9hutQ0cKRGmJkxM1nInZfBYXLZpCJOwGuoyv1bg9ocf7K4A8VrzTN1z23ifDgRkY6s\",\"age\":60,\"eyeColor\":\"BLUE\",\"name\":\"tnJW03NKSqfdWFShb4Zx\",\"gender\":\"mkHYjEd9AdQ9qugcOVPr\",\"company\":\"QBC3vKiRZz9KzZUFL387\",\"emails\":[],\"phones\":[746929031,561717506,990959217,1768136872,500288955,1213970016,1796482738,28628332,2066852063],\"address\":\"m9qwxh8ck4ZqfcTEn8DC\",\"about\":\"GPsNVdldwX7l5sKShO21\",\"registered\":\"1926-05-06\",\"latitude\":33.206450907142155,\"longitude\":22.589461322126944,\"tags\":[\"sgIXdOUPvN\",\"EaSpiLhL3E\",\"KheRxPm0oS\",\"njVbMOrzMf\",\"tKnGw8SNUk\",\"W56syc0sUv\",\"tnyhobz3Nm\",\"JO7ymoPNhf\",\"8oDGnPQeIX\",\"EP2Vgxe29b\",\"TMCD1h0s8V\",\"BjsKUHxlb2\",\"Gb10kL3FHo\",\"Mkyc0gT5wf\",\"FqOWswKKbb\",\"EURPNlWQHx\",\"NJjxMOFU1K\",\"oHCD4oBfIh\",\"uHYI3is1O8\",\"1ufor6JqZy\",\"JU7sDQCSFA\",\"LUiAfaDpgP\",\"Gp0ppmYBhh\",\"QQ7971iGtk\",\"NS8yP3IfVE\",\"pctZBahLR2\",\"k2msx6HoFv\",\"mRG8LGeAhx\",\"Ctp5PqxJc9\",\"cEP1p05GVa\",\"PqRmLI7U28\",\"Eb7UVo4nlI\",\"trDbHQy2sp\",\"ysv0FegxwU\",\"mlyr8PucKO\",\"W9Wqp4A8aL\",\"J56dFrPCbb\",\"y1fsnL7OXA\",\"tdvMCRMNCR\",\"OXdmNUvM4I\"],\"partners\":[{\"id\":6942554547810915627,\"name\":\"DwuzOOeuPautxtTzMHOvIxVHIboVis\",\"since\":\"1909-05-23T23:29:37.098674093Z\"},{\"id\":-8518542767576114527,\"name\":\"bGZuDwWWosUOgPbJzCviDkFUbvGDIn\",\"since\":\"1938-05-04T13:50:04.017434661Z\"},{\"id\":601371629166935988,\"name\":\"PlnjVauJisRvtUMkKZMaWDlFefJCwL\",\"since\":\"1987-05-09T20:46:24.620521962Z\"},{\"id\":4375579985883232220,\"name\":\"xAwXgtrZEcUxnPQJIRqXAmKCncRDTY\",\"since\":\"1990-11-01T06:38:20.380789524Z\"},{\"id\":8317268803618076921,\"name\":\"QQdmSLNtYJXcpLMQINKSzWQxJVQxLF\",\"since\":\"1940-05-18T10:51:42.883428669Z\"},{\"id\":4285484400196566404,\"name\":\"lfpUWBYwPYbMvRTtRvdORyZyCDJzlZ\",\"since\":\"2003-10-13T22:27:11.929616204Z\"},{\"id\":2265074678478192283,\"name\":\"alBTLDrrFIAdLOkRhGAYjrkgSYUoee\",\"since\":\"1980-12-02T14:27:57.767475522Z\"},{\"id\":8350482858520968813,\"name\":\"gOnmVOFQLyDkQFKmKPSRSozRHhmYoI\",\"since\":\"2008-01-07T15:11:04.239970727Z\"},{\"id\":-6587118629753263393,\"name\":\"TuFtTDQNEQGscizafoDcSRwMhkdTSB\",\"since\":\"1964-02-13T23:30:54.254993783Z\"},{\"id\":-2402179798706660819,\"name\":\"sJHfDYWhXfbQyXkAaiIyPAedPuRqEg\",\"since\":\"1934-12-12T23:22:00.430680784Z\"}]}]}");
        Clients deserialized = opacker.deserialize(Clients.class, serialized);
    }
}
