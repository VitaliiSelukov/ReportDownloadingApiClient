package com.vs.report.model;

public class Report {
    private String name;
    private String content;

    public Report(final String name, final String content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }
}