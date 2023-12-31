/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.bff.data;

import java.util.Objects;

public class Data
{
    private Integer id;
    private String quote;
    private String author;
    private String addedBy;
    private String addedOn;

    public Integer getId() {
        return id;
    }

    public String getAddedOn() {
        return addedOn;
    }

    public void setAddedOn(String addedOn) {
        this.addedOn = addedOn;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
        Data quote1 = (Data) o;
        return  Objects.equals(id, quote1.getId()) &&
                Objects.equals(quote, quote1.getQuote()) &&
                Objects.equals(author, quote1.getAuthor()) &&
                Objects.equals(addedBy, quote1.getAddedBy()) &&
                Objects.equals(addedOn, quote1.getAddedOn());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, quote, author);
    }
}
