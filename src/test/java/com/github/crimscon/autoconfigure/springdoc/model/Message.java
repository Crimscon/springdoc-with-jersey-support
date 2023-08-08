package com.github.crimscon.autoconfigure.springdoc.model;

import java.util.Objects;

public class Message {
    private String id;
    private String text;
    private String author;

    public Message() {
    }

    public Message(String id, String text, String author) {
        this.id = id;
        this.text = text;
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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
        if (!(o instanceof Message message)) {
            return false;
        }
        return Objects.equals(getText(), message.getText())
                && Objects.equals(getAuthor(), message.getAuthor());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getText(), getAuthor());
    }

    @Override
    public String toString() {
        return "Message{"
                + " text='" + text + '\''
                + ", author='" + author + '\''
                + "}";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
