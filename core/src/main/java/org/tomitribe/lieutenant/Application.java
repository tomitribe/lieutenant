/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tomitribe.lieutenant;

public class Application {

    private String image;
    private String build;

    public Application(String image, String build) {
        this.image = image;

        if (build == null) {
            throw new IllegalArgumentException("Build cannot be null");
        }

        this.build = build;
    }

    public String getImage() {
        return this.image;
    }

    public String getBuild() {
        return this.build;
    }

    public boolean isImageSet() {
        return this.image != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Application that = (Application) o;

        if (image != null ? !image.equals(that.image) : that.image != null) return false;
        return build != null ? build.equals(that.build) : that.build == null;

    }

    @Override
    public int hashCode() {
        int result = image != null ? image.hashCode() : 0;
        result = 31 * result + (build != null ? build.hashCode() : 0);
        return result;
    }
}
