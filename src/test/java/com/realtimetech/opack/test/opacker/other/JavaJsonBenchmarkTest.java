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
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.test.OpackAssert;
import com.realtimetech.opack.test.RandomUtil;
import com.realtimetech.opack.transformer.impl.time.annotation.TimeFormat;
import com.realtimetech.opack.value.OpackValue;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
            @TimeFormat("yyyy-MM-dd")
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
                for (long l : nums) {
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

    public static void fillClients(Clients clients, int size) {
        clients.setClients(new ArrayList<>());

        for (int i = 0; i < size; i++) {
            appendClient(clients);
        }
    }

    private static int appendClient(Clients uc) {
        int expectedSize = 2; // {}

        Clients.Client u = new Clients.Client();
        u.setId(Math.abs(RandomUtil.nextLong()));
        expectedSize += 9 + Long.toString(u.getId()).length(); // ,'id':''
        u.setIndex(RandomUtil.nextInt(0, Integer.MAX_VALUE));
        expectedSize += 11 + Integer.toString(u.getIndex()).length(); // ,'index':''
        u.setGuid(RandomUtil.nextUUID());
        expectedSize += 10 + 36; // ,'guid':''
        u.setIsActive(RandomUtil.nextInt(0, 2) == 1);
        expectedSize += 17 + (u.getIsActive() ? 4 : 5); // ,'isActive':''
        u.setBalance(RandomUtil.randomBigDecimal());
        expectedSize += 16 + u.getBalance().toPlainString().length(); // ,'balance':''
        u.setPicture(RandomUtil.randomAlphanumeric(100));
        expectedSize += 16 + u.getPicture().length(); // ,'picture':''
        u.setAge(RandomUtil.nextInt(0, 100));
        expectedSize += 9 + Integer.toString(u.getAge()).length(); // ,'age':''
        u.setEyeColor(Clients.EyeColor.fromNumber(RandomUtil.nextInt(3)));
        expectedSize += 17 + u.getEyeColor().name().length(); // ,'eyeColor':''
        u.setName(RandomUtil.randomAlphanumeric(20));
        expectedSize += 10 + u.getName().length(); // ,'name':''
        u.setGender(RandomUtil.randomAlphanumeric(20));
        expectedSize += 12 + u.getGender().length(); // ,'gender':''
        u.setCompany(RandomUtil.randomAlphanumeric(20));
        expectedSize += 13 + u.getCompany().length(); // ,'company':''
        u.setEmails(new String[]{
                RandomUtil.randomAlphabetic(RandomUtil.nextInt(100)),
                RandomUtil.randomAlphabetic(RandomUtil.nextInt(100)),
                RandomUtil.randomAlphabetic(RandomUtil.nextInt(100))
        });
        int calcSize = 0;
        for (String e : u.getEmails()) {
            calcSize += 3 + e.length();
        }
        expectedSize += 11 + calcSize; // ,'email':''
        u.setPhones(new long[]{
                RandomUtil.nextInt(10),
                RandomUtil.nextInt(10),
                RandomUtil.nextInt(10),
                RandomUtil.nextInt(10)
        });
        calcSize = 0;
        for (long p : u.getPhones()) {
            calcSize += 1 + Long.toString(p).length();
        }
        expectedSize += 11 + calcSize; // ,'phone':''
        u.setAddress(RandomUtil.randomAlphanumeric(20));
        expectedSize += 13 + u.getAddress().length(); // ,'address':''
        u.setAbout(RandomUtil.randomAlphanumeric(20));
        expectedSize += 11 + u.getAbout().length(); // ,'about':''
        u.setRegistered(LocalDate.of(1900 + RandomUtil.nextInt(110), 1 + RandomUtil.nextInt(12), 1 + RandomUtil.nextInt(28)));
        expectedSize += 16 + 10; // ,'registered':''
        u.setLatitude(RandomUtil.nextDouble(0, 90));
        expectedSize += 14 + Double.toString(u.getLatitude()).length(); // ,'latitude':''
        u.setLongitude(RandomUtil.nextDouble(0, 180));
        expectedSize += 15 + Double.toString(u.getLongitude()).length(); // ,'longitude':''

        u.setTags(new ArrayList<>());
        expectedSize += 10; // ,'tags':[]
        int nTags = RandomUtil.nextInt(0, 50);
        for (int i = 0; i < nTags; i++) {
            String t = RandomUtil.randomAlphanumeric(10);
            u.getTags().add(t);
            expectedSize += t.length(); // '',
        }

        int nPartners = RandomUtil.nextInt(0, 30);
        u.setPartners(new ArrayList<>());
        expectedSize += 13; // ,'partners':[]
        for (int i = 0; i < nPartners; i++) {
            long id = RandomUtil.nextLong();
            String name = RandomUtil.randomAlphabetic(30);
            OffsetDateTime at = OffsetDateTime.of(
                    1900 + RandomUtil.nextInt(110),
                    1 + RandomUtil.nextInt(12),
                    1 + RandomUtil.nextInt(28),
                    RandomUtil.nextInt(24),
                    RandomUtil.nextInt(60),
                    RandomUtil.nextInt(60),
                    RandomUtil.nextInt(1000000000),
                    ZoneOffset.UTC
            );
            u.getPartners().add(Clients.Partner.create(id, name, at));
            expectedSize += Long.toString(id).length() + name.length() + 50; // {'id':'','name':'','since':''},
        }

        uc.getClients().add(u);

        return expectedSize;
    }

    @Test
    public void test() throws SerializeException, DeserializeException, OpackAssert.AssertException {
        Clients clients = new Clients();
        fillClients(clients, 8);

        Opacker opacker = Opacker.Builder.create().build();

        OpackValue serialized = opacker.serialize(clients);
        assert serialized != null;
        Clients deserialized = opacker.deserialize(Clients.class, serialized);

        OpackAssert.assertEquals(clients, deserialized);
    }
}