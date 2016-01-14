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

import org.apache.commons.lang.text.StrSubstitutor;

import java.util.regex.Pattern;

public class LieutenantConfig {

    private boolean force;
    private String prefix;
    private String suffix;
    private boolean withBranches = true;
    private boolean withTags = true;
    private Pattern exclusionImages = null;

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        final String replaceSystemProperties = StrSubstitutor.replace(prefix, System.getProperties());
        this.prefix = StrSubstitutor.replace(replaceSystemProperties, System.getenv());
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        final String replaceSystemProperties = StrSubstitutor.replace(suffix, System.getProperties());
        this.suffix = StrSubstitutor.replace(replaceSystemProperties, System.getenv());
    }

    public boolean isWithBranches() {
        return withBranches;
    }

    public void setWithBranches(boolean withBranches) {
        this.withBranches = withBranches;
    }

    public boolean isWithTags() {
        return withTags;
    }

    public void setWithTags(boolean withTags) {
        this.withTags = withTags;
    }

    public void setExclusionImagesPattern(String pattern) {
        if (pattern != null) {
            this.exclusionImages = Pattern.compile(pattern);
        }
    }

    public Pattern getExclusionImagesPattern() {
        return this.exclusionImages;
    }

    public boolean isPatternDefinedForPushingImages() {
        return this.exclusionImages != null;
    }
}
