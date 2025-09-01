package com.github.nikolayill.dto.simple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleProperties {
    private Map<String, String> properties = new HashMap<String, String>();
    private List<SimplePropertiesSection> sections = new ArrayList<>();

    public Map<String, String> getProperties() {return properties;}
    public void setProperties(Map<String, String> properties) {this.properties = properties;}

    public List<SimplePropertiesSection> getSections() {
        return sections;
    }

    public void setSections(List<SimplePropertiesSection> sections) {
        this.sections = sections;
    }

    public static class SimplePropertiesSection {
        private String name;
        private Map<String, String> properties = new HashMap<String, String>();
        public Map<String, String> getProperties() {return properties;}
        public void setProperties(Map<String, String> properties) {this.properties = properties;}

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
